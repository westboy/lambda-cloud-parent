package com.lambda.security.web.verify.service.captcha;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.math.Calculator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.google.common.collect.Maps;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.security.exception.VerifyCodeValidationException;
import com.lambda.security.web.verify.generator.MathGenerator;
import com.lambda.security.web.verify.service.VerifyCodeService;
import com.lambda.security.web.verify.service.captcha.store.CaptchaStore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import tools.jackson.databind.ObjectMapper;

/**
 * 图形验证码生成服务实现
 *
 * <p>设计目标：
 * <ul>
 *   <li>验证码生成：生成数学运算类型的图形验证码</li>
 *   <li>多种输出：支持Ajax JSON响应和直接图片输出</li>
 *   <li>安全存储：将验证码答案存储到指定的存储介质</li>
 *   <li>配置驱动：通过配置文件控制验证码的各种参数</li>
 * </ul>
 *
 * <p>主要功能：
 * <ul>
 *   <li>支持检测：根据配置和请求特征判断是否需要生成验证码</li>
 *   <li>验证码生成：使用Hutool的CircleCaptcha生成圆圈干扰的验证码</li>
 *   <li>数学运算：使用MathGenerator生成数学运算题目</li>
 *   <li>多格式输出：支持Base64编码的JSON响应和直接图片流输出</li>
 * </ul>
 *
 * <p>验证码特性：
 * <ul>
 *   <li>数学运算：生成加减乘运算题目，用户需要计算结果</li>
 *   <li>圆圈干扰：使用CircleCaptcha添加圆圈干扰，增加识别难度</li>
 *   <li>可配置性：验证码尺寸、字符数量、数字长度等均可配置</li>
 *   <li>开发模式：支持开发模式下在日志中输出验证码答案</li>
 * </ul>
 *
 * <p>输出格式：
 * <ul>
 *   <li>Ajax请求：返回JSON格式，包含token和Base64编码的图片</li>
 *   <li>普通请求：直接输出图片流到响应体（主要用于测试）</li>
 *   <li>缓存控制：设置适当的HTTP头防止验证码被缓存</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 配置验证码服务
 * @Bean
 * public VerifyCodeService captchaService(SecurityProperties properties,
 *                                         ObjectMapper mapper,
 *                                         CaptchaStore store) {
 *     return new CaptchaVerifyCodeGenerateImpl(properties, mapper, store);
 * }
 *
 * // Ajax请求获取验证码
 * GET /api/captcha
 * Accept: application/json
 *
 * // 响应格式：
 * {
 *   "__token": "uuid-string",
 *   "verifyImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
 * }
 * }</pre>
 *
 * <p>配置参数：
 * <ul>
 *   <li>captchaWidth：验证码图片宽度</li>
 *   <li>captchaHeight：验证码图片高度</li>
 *   <li>captchaCodeCount：验证码字符数量</li>
 *   <li>captchaNumberLength：数学运算中数字的长度</li>
 *   <li>duration：验证码有效期</li>
 *   <li>timeUnit：时间单位</li>
 *   <li>devMode：开发模式开关</li>
 * </ul>
 *
 * <p>安全特性：
 * <ul>
 *   <li>随机生成：每次请求生成不同的验证码</li>
 *   <li>时效控制：验证码有过期时间限制</li>
 *   <li>一次性使用：验证码验证后立即失效</li>
 *   <li>防缓存：设置HTTP头防止浏览器缓存</li>
 * </ul>
 *
 * @author jpjoo
 * @see VerifyCodeService
 * @see CaptchaStore
 * @see MathGenerator
 * @see CircleCaptcha
 */
@Slf4j
public class CaptchaVerifyCodeGenerateImpl implements VerifyCodeService {

    /**
     * Ant路径匹配器，用于匹配请求URL是否需要生成验证码
     *
     * <p>匹配特性：
     * <ul>
     *   <li>通配符支持：支持*、**、?等通配符</li>
     *   <li>路径匹配：支持复杂的路径模式匹配</li>
     *   <li>大小写敏感：默认区分大小写</li>
     * </ul>
     */
    private final AntPathMatcher matcher = new AntPathMatcher();

    /**
     * 安全配置属性，包含验证码相关的所有配置参数
     *
     * <p>配置内容：
     * <ul>
     *   <li>验证码尺寸：宽度、高度</li>
     *   <li>验证码内容：字符数量、数字长度</li>
     *   <li>有效期设置：时间单位、持续时间</li>
     *   <li>功能开关：是否启用验证码、开发模式</li>
     * </ul>
     */
    private final SecurityProperties securityProperties;

    /**
     * JSON对象映射器，用于将验证码响应数据序列化为JSON格式
     *
     * <p>序列化功能：
     * <ul>
     *   <li>对象转JSON：将Map对象转换为JSON字符串</li>
     *   <li>编码处理：自动处理特殊字符的编码</li>
     *   <li>格式化：提供美观的JSON输出格式</li>
     * </ul>
     */
    private final ObjectMapper objectMapper;

    /**
     * 验证码存储接口，用于存储生成的验证码答案
     *
     * <p>存储功能：
     * <ul>
     *   <li>答案存储：存储验证码的正确答案</li>
     *   <li>时效管理：设置验证码的过期时间</li>
     *   <li>验证支持：提供验证码验证功能</li>
     * </ul>
     */
    private final CaptchaStore captchaStore;

    /**
     * 验证码令牌的键名，用于在响应中标识验证码的唯一ID
     *
     * <p>用途说明：
     * <ul>
     *   <li>唯一标识：每个验证码都有唯一的token</li>
     *   <li>关联验证：验证时通过token找到对应的答案</li>
     *   <li>前端使用：前端需要将此token与用户输入一起提交</li>
     * </ul>
     */
    public static final String TOKEN_KEY = "__token";

    /**
     * 验证码参数名，用于标识用户提交的验证码输入
     *
     * <p>用途说明：
     * <ul>
     *   <li>参数标识：前端提交验证码时使用的参数名</li>
     *   <li>验证匹配：验证时通过此参数名获取用户输入</li>
     *   <li>统一规范：整个系统使用统一的参数名</li>
     * </ul>
     */
    public static final String VERIFY_CODE_PARAMETER = "verifyCode";

    /**
     * 验证码图片的键名，用于在JSON响应中标识Base64编码的图片数据
     *
     * <p>用途说明：
     * <ul>
     *   <li>图片数据：包含完整的Base64编码图片</li>
     *   <li>前端显示：前端可直接使用此数据显示验证码</li>
     *   <li>数据格式：格式为"data:image/png;base64,xxx"</li>
     * </ul>
     */
    public static final String VERIFY_IMAGE = "verifyImage";

    /**
     * 构造图形验证码生成服务
     *
     * <p>依赖注入：
     * <ul>
     *   <li>安全配置：提供验证码生成的各种参数配置</li>
     *   <li>JSON映射器：用于序列化验证码响应数据</li>
     *   <li>存储服务：用于存储和验证验证码答案</li>
     * </ul>
     *
     * <p>初始化过程：
     * <ul>
     *   <li>配置验证：检查必要的配置参数是否完整</li>
     *   <li>依赖设置：设置各个依赖组件的引用</li>
     *   <li>服务准备：准备验证码生成和存储服务</li>
     * </ul>
     *
     * <p>使用示例：
     * <pre>{@code
     * @Bean
     * public CaptchaVerifyCodeGenerateImpl captchaService(
     *         SecurityProperties securityProperties,
     *         ObjectMapper objectMapper,
     *         CaptchaStore captchaStore) {
     *     return new CaptchaVerifyCodeGenerateImpl(
     *         securityProperties, objectMapper, captchaStore);
     * }
     * }</pre>
     *
     * @param securityProperties 安全配置属性，包含验证码相关配置
     * @param objectMapper       JSON对象映射器，用于序列化响应数据
     * @param captchaStore       验证码存储接口，用于存储验证码答案
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "RedisHelper is thread safe")
    public CaptchaVerifyCodeGenerateImpl(
            SecurityProperties securityProperties, ObjectMapper objectMapper, CaptchaStore captchaStore) {
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
        this.captchaStore = captchaStore;
    }

    /**
     * 检查当前请求是否需要生成验证码
     *
     * <p>支持条件：
     * <ol>
     *   <li>验证码功能已启用：securityProperties.form.enableVerify = true</li>
     *   <li>请求方法为GET：只有GET请求才生成验证码</li>
     *   <li>URL路径匹配：请求路径匹配配置的验证码URL模式</li>
     * </ol>
     *
     * <p>匹配逻辑：
     * <ul>
     *   <li>功能开关：首先检查验证码功能是否启用</li>
     *   <li>方法限制：只处理GET请求，POST等其他方法用于验证</li>
     *   <li>路径匹配：使用Ant路径匹配器检查URL是否匹配</li>
     * </ul>
     *
     * <p>配置示例：
     * <pre>
     * lambda:
     *   security:
     *     form:
     *       enable-verify: true
     *     verify:
     *       url: "/api/captcha"
     * </pre>
     *
     * <p>匹配示例：
     * <ul>
     *   <li>GET /api/captcha → 匹配，生成验证码</li>
     *   <li>POST /api/captcha → 不匹配，不是GET请求</li>
     *   <li>GET /api/login → 不匹配，路径不符合</li>
     *   <li>GET /api/captcha?type=math → 匹配，查询参数不影响</li>
     * </ul>
     *
     * @param request HTTP请求对象，包含请求方法、路径等信息
     * @return true表示需要生成验证码，false表示跳过
     */
    @Override
    public boolean support(HttpServletRequest request) {
        final SecurityProperties.Verify verify = securityProperties.getVerify();
        boolean captchaEnabled = securityProperties.getForm().isEnableVerify()
                || securityProperties.getForm().getCaptchaTrigger().isEnabled()
                || securityProperties.getSms().isEnableVerify();
        return captchaEnabled && matcher.match(verify.getUrl(), request.getRequestURI());
    }

    /**
     * 执行验证码生成逻辑
     *
     * <p>执行流程：
     * <ol>
     *   <li>调用writeCaptcha方法生成验证码</li>
     *   <li>根据请求类型选择输出格式</li>
     *   <li>设置适当的HTTP响应头</li>
     *   <li>输出验证码数据到响应流</li>
     * </ol>
     *
     * <p>处理特点：
     * <ul>
     *   <li>直接输出：不继续过滤器链，直接返回验证码</li>
     *   <li>格式自适应：根据请求类型自动选择输出格式</li>
     *   <li>异常处理：捕获并处理验证码生成过程中的异常</li>
     * </ul>
     *
     * <p>注意事项：
     * <ul>
     *   <li>不调用chain.doFilter()：验证码生成是终端操作</li>
     *   <li>响应完整性：确保响应数据完整写入</li>
     *   <li>资源清理：及时关闭输出流等资源</li>
     * </ul>
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param chain    过滤器链（此方法中不会调用）
     * @throws IOException 写入响应数据时可能抛出的IO异常
     */
    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException {
        this.writeCaptcha(request, response);
    }

    /**
     * 生成并输出验证码到HTTP响应
     *
     * <p>生成流程：
     * <ol>
     *   <li>创建CircleCaptcha验证码对象，配置尺寸和干扰参数</li>
     *   <li>设置MathGenerator数学运算生成器</li>
     *   <li>生成唯一的验证码ID（UUID）</li>
     *   <li>计算数学运算的答案并存储到CaptchaStore</li>
     *   <li>根据请求类型选择输出格式（JSON或图片流）</li>
     * </ol>
     *
     * <p>验证码特性：
     * <ul>
     *   <li>圆圈干扰：使用CircleCaptcha添加3个圆圈干扰</li>
     *   <li>数学运算：生成加减乘运算题目</li>
     *   <li>可配置尺寸：根据配置设置图片宽度和高度</li>
     *   <li>答案计算：使用Calculator计算运算结果作为正确答案</li>
     * </ul>
     *
     * <p>输出格式：
     * <ul>
     *   <li>Ajax请求：返回JSON格式，包含token和Base64图片数据</li>
     *   <li>普通请求：直接输出PNG图片流（主要用于测试）</li>
     * </ul>
     *
     * <p>JSON响应格式：
     * <pre>{@code
     * {
     *   "__token": "550e8400-e29b-41d4-a716-446655440000",
     *   "verifyImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
     * }
     * }</pre>
     *
     * <p>HTTP头设置：
     * <ul>
     *   <li>Expires: 0 - 立即过期</li>
     *   <li>Pragma: No-cache - 禁用缓存</li>
     *   <li>Cache-Control: no-cache - 不缓存响应</li>
     *   <li>Content-Type: application/json - JSON格式</li>
     * </ul>
     *
     * <p>开发模式：
     * <ul>
     *   <li>日志输出：在开发模式下输出验证码ID和答案到日志</li>
     *   <li>调试支持：便于开发和测试时查看验证码答案</li>
     *   <li>生产禁用：生产环境应关闭开发模式</li>
     * </ul>
     *
     * <p>存储机制：
     * <ul>
     *   <li>答案存储：将计算结果存储到CaptchaStore</li>
     *   <li>时效控制：根据配置设置验证码有效期</li>
     *   <li>唯一标识：使用UUID作为验证码的唯一标识</li>
     * </ul>
     *
     * <p>异常处理：
     * <ul>
     *   <li>JSON序列化异常：转换为VerifyCodeValidationException</li>
     *   <li>IO异常：向上抛出IOException</li>
     *   <li>资源清理：使用try-with-resources确保流的正确关闭</li>
     * </ul>
     *
     * <p>使用示例：
     * <pre>{@code
     * // Ajax请求
     * GET /api/captcha
     * Accept: application/json
     *
     * // 响应
     * HTTP/1.1 200 OK
     * Content-Type: application/json
     * Cache-Control: no-cache
     *
     * {
     *   "__token": "abc123",
     *   "verifyImage": "data:image/png;base64,..."
     * }
     * }</pre>
     *
     * @param request  HTTP请求对象，用于判断请求类型
     * @param response HTTP响应对象，用于输出验证码数据
     * @throws IOException                   写入响应数据时可能抛出的IO异常
     * @throws VerifyCodeValidationException JSON序列化失败时抛出
     */
    public void writeCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(
                securityProperties.getVerify().getCaptchaWidth(),
                securityProperties.getVerify().getCaptchaHeight(),
                securityProperties.getVerify().getCaptchaCodeCount(),
                3);
        MathGenerator mathGenerator =
                new MathGenerator(securityProperties.getVerify().getCaptchaNumberLength());
        captcha.setGenerator(mathGenerator);
        String captchaId = IdUtil.fastUUID();
        Integer captchaCode = (int) Calculator.conversion(captcha.getCode());
        if (securityProperties.getVerify().isDevMode()) {
            log.info("验证码[ {}:{}, {}:{} ]", TOKEN_KEY, captchaId, VERIFY_CODE_PARAMETER, captchaCode);
        }
        captchaStore.store(
                captchaId,
                captchaCode.toString(),
                securityProperties.getVerify().getTimeUnit(),
                securityProperties.getVerify().getDuration());
        if (WebHttpUtils.isAjaxRequest(request) || JakartaServletUtil.isPostMethod(request)) {
            try (PrintWriter writer = response.getWriter()) {
                response.setHeader("Expires", "0");
                response.setHeader("Pragma", "No-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                Map<String, String> result = Maps.newHashMapWithExpectedSize(2);
                result.put(TOKEN_KEY, captchaId);
                result.put(VERIFY_IMAGE, captcha.getImageBase64Data());
                objectMapper.writeValue(writer, result);
            } catch (Exception e) {
                throw new VerifyCodeValidationException(e.getMessage());
            }
        } else {
            // 这个只作为测试使用
            try (ServletOutputStream output = response.getOutputStream()) {
                captcha.write(output);
            }
        }
    }
}
