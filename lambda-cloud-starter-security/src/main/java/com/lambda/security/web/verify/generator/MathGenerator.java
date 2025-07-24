package com.lambda.security.web.verify.generator;

import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.math.Calculator;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import java.io.Serial;

/**
 * 数学运算验证码生成器
 *
 * <p>设计目标：
 * <ul>
 *   <li>智能验证：生成数学运算题目，提高验证码的智能性</li>
 *   <li>防机器人：数学运算比图形识别更难被机器破解</li>
 *   <li>用户友好：简单的数学运算，用户容易计算</li>
 *   <li>可配置性：支持自定义数字长度和运算复杂度</li>
 * </ul>
 *
 * <p>主要功能：
 * <ul>
 *   <li>题目生成：生成加减乘运算题目</li>
 *   <li>答案验证：验证用户输入的计算结果</li>
 *   <li>格式化输出：生成格式化的运算表达式</li>
 *   <li>长度控制：控制参与运算的数字位数</li>
 * </ul>
 *
 * <p>运算规则：
 * <ul>
 *   <li>支持运算符：加法(+)、减法(-)、乘法(*)</li>
 *   <li>数字范围：根据numberLength参数确定</li>
 *   <li>结果保证：确保减法结果为正数</li>
 *   <li>格式统一：所有数字右对齐，长度一致</li>
 * </ul>
 *
 * <p>生成示例：
 * <pre>
 * numberLength=2时：
 * "12+34="  (12 + 34 = 46)
 * "56-23="  (56 - 23 = 33)
 * "7 *8 ="  (7 * 8 = 56)
 * </pre>
 *
 * <p>使用场景：
 * <ul>
 *   <li>登录验证：替代传统图形验证码</li>
 *   <li>表单提交：防止恶意提交</li>
 *   <li>API保护：保护敏感API接口</li>
 *   <li>注册验证：用户注册时的人机验证</li>
 * </ul>
 *
 * @param numberLength 参与计算数字的最大位数，决定数字的取值范围
 * @author 系统生成
 * @see CodeGenerator
 */
public record MathGenerator(int numberLength) implements CodeGenerator {
    @Serial
    private static final long serialVersionUID = -5514819971774091076L;

    /**
     * 支持的运算符常量
     *
     * <p>运算符说明：
     * <ul>
     *   <li>+：加法运算，结果为两数之和</li>
     *   <li>-：减法运算，确保结果为正数</li>
     *   <li>*：乘法运算，结果为两数之积</li>
     * </ul>
     *
     * <p>选择原则：
     * <ul>
     *   <li>简单易懂：避免除法可能产生的小数</li>
     *   <li>计算方便：用户可以心算完成</li>
     *   <li>结果合理：避免过大或过小的结果</li>
     * </ul>
     */
    private static final String OPERATORS = "+-*";

    /**
     * 默认构造函数
     *
     * <p>默认配置：
     * <ul>
     *   <li>数字长度：2位数</li>
     *   <li>数字范围：10-99</li>
     *   <li>运算难度：适中</li>
     *   <li>适用场景：一般的验证需求</li>
     * </ul>
     *
     * <p>使用示例：
     * <pre>{@code
     * MathGenerator generator = new MathGenerator();
     * String question = generator.generate(); // 如："23+45="
     * boolean isCorrect = generator.verify("23+45=", "68");
     * }</pre>
     */
    public MathGenerator() {
        this(2);
    }

    /**
     * 带参数的构造函数
     *
     * <p>参数说明：
     * <ul>
     *   <li>numberLength=1：数字范围1-9，适合儿童或简单验证</li>
     *   <li>numberLength=2：数字范围10-99，适合一般用户</li>
     *   <li>numberLength=3：数字范围100-999，适合高安全要求</li>
     * </ul>
     *
     * <p>使用建议：
     * <ul>
     *   <li>移动端：建议使用1-2位数，便于输入</li>
     *   <li>桌面端：可以使用2-3位数，提高安全性</li>
     *   <li>高安全场景：使用3位数或更多</li>
     * </ul>
     *
     * <p>配置示例：
     * <pre>{@code
     * // 简单验证（1位数）
     * MathGenerator simple = new MathGenerator(1);
     *
     * // 标准验证（2位数）
     * MathGenerator standard = new MathGenerator(2);
     *
     * // 复杂验证（3位数）
     * MathGenerator complex = new MathGenerator(3);
     * }</pre>
     *
     * @param numberLength 参与计算的数字最大位数，必须大于0
     */
    public MathGenerator {}

    /**
     * 生成数学运算验证码
     *
     * <p>生成流程：
     * <ol>
     *   <li>根据numberLength计算数字的取值范围</li>
     *   <li>随机生成两个数字v1和v2</li>
     *   <li>为确保减法结果为正数，将较大数放在前面</li>
     *   <li>对数字进行格式化，保持长度一致</li>
     *   <li>随机选择运算符（+、-、*）</li>
     *   <li>拼接成完整的运算表达式</li>
     * </ol>
     *
     * <p>格式化规则：
     * <ul>
     *   <li>数字右对齐：使用空格填充到指定长度</li>
     *   <li>运算符居中：运算符前后各有一个字符位置</li>
     *   <li>等号结尾：表达式以等号结束</li>
     * </ul>
     *
     * <p>生成示例：
     * <pre>
     * numberLength=1: "5+3=", "9-2=", "4*7="
     * numberLength=2: "23+45=", "67-34=", "12*8="
     * numberLength=3: "123+456=", "789-234=", "45*67="
     * </pre>
     *
     * <p>特殊处理：
     * <ul>
     *   <li>减法保护：确保被减数大于减数，避免负数结果</li>
     *   <li>格式统一：所有数字都填充到相同长度</li>
     *   <li>随机性：运算符和数字都是随机选择</li>
     * </ul>
     *
     * @return 格式化的数学运算表达式，如"12+34="
     */
    @Override
    public String generate() {
        final int limit = getLimit();

        int v1 = RandomUtil.randomInt(limit);
        int v2 = RandomUtil.randomInt(limit);

        String number1 = Integer.toString(v1);
        String number2 = Integer.toString(v2);

        if (v1 > v2) {
            number1 = StrUtil.padAfter(number1, this.numberLength, CharUtil.SPACE);
            number2 = StrUtil.padAfter(number2, this.numberLength, CharUtil.SPACE);
        } else {
            number1 = StrUtil.padAfter(number2, this.numberLength, CharUtil.SPACE);
            number2 = StrUtil.padAfter(number1, this.numberLength, CharUtil.SPACE);
        }

        return StrUtil.builder()
                .append(number1)
                .append(RandomUtil.randomChar(OPERATORS))
                .append(number2)
                .append('=')
                .toString();
    }

    /**
     * 验证用户输入的答案是否正确
     *
     * <p>验证流程：
     * <ol>
     *   <li>解析用户输入的答案为整数</li>
     *   <li>使用Calculator计算题目的正确答案</li>
     *   <li>比较用户答案与正确答案</li>
     *   <li>返回验证结果</li>
     * </ol>
     *
     * <p>输入处理：
     * <ul>
     *   <li>数字解析：将字符串转换为整数</li>
     *   <li>异常处理：捕获非数字输入的异常</li>
     *   <li>空值处理：空输入视为验证失败</li>
     * </ul>
     *
     * <p>计算引擎：
     * <ul>
     *   <li>使用Hutool的Calculator进行表达式计算</li>
     *   <li>支持加减乘运算</li>
     *   <li>自动处理运算优先级</li>
     * </ul>
     *
     * <p>验证示例：
     * <pre>{@code
     * MathGenerator generator = new MathGenerator();
     * String question = "12+34=";
     *
     * boolean result1 = generator.verify(question, "46");   // true
     * boolean result2 = generator.verify(question, "45");   // false
     * boolean result3 = generator.verify(question, "abc");  // false
     * boolean result4 = generator.verify(question, "");     // false
     * }</pre>
     *
     * @param code 生成的数学运算题目，如"12+34="
     * @param userInputCode 用户输入的答案字符串
     * @return true表示答案正确，false表示答案错误或输入无效
     */
    @Override
    public boolean verify(String code, String userInputCode) {
        int result;
        try {
            result = Integer.parseInt(userInputCode);
        } catch (NumberFormatException e) {
            // 用户输入非数字
            return false;
        }

        final int calculateResult = (int) Calculator.conversion(code);
        return result == calculateResult;
    }

    /**
     * 获取验证码字符串的长度
     *
     * <p>长度计算公式：
     * <ul>
     *   <li>总长度 = numberLength * 2 + 2</li>
     *   <li>第一个数字：numberLength个字符</li>
     *   <li>运算符：1个字符</li>
     *   <li>第二个数字：numberLength个字符</li>
     *   <li>等号：1个字符</li>
     * </ul>
     *
     * <p>长度示例：
     * <ul>
     *   <li>numberLength=1：长度=4，如"5+3="</li>
     *   <li>numberLength=2：长度=6，如"12+34="</li>
     *   <li>numberLength=3：长度=8，如"123+456="</li>
     * </ul>
     *
     * <p>用途说明：
     * <ul>
     *   <li>UI布局：确定验证码显示区域的宽度</li>
     *   <li>输入验证：验证生成的验证码格式</li>
     *   <li>存储优化：预估存储空间需求</li>
     * </ul>
     *
     * @return 验证码字符串的总长度
     */
    public int getLength() {
        return this.numberLength * 2 + 2;
    }

    /**
     * 根据数字长度计算随机数的上限值
     *
     * <p>计算逻辑：
     * <ul>
     *   <li>numberLength=1：上限=10（生成0-9）</li>
     *   <li>numberLength=2：上限=100（生成0-99）</li>
     *   <li>numberLength=3：上限=1000（生成0-999）</li>
     * </ul>
     *
     * <p>实现方式：
     * <ul>
     *   <li>构造字符串"1" + "000..."（n个0）</li>
     *   <li>将字符串解析为整数</li>
     *   <li>作为RandomUtil.randomInt()的上限参数</li>
     * </ul>
     *
     * <p>数字范围：
     * <ul>
     *   <li>最小值：0（包含）</li>
     *   <li>最大值：10^numberLength - 1（包含）</li>
     *   <li>分布：均匀分布</li>
     * </ul>
     *
     * <p>注意事项：
     * <ul>
     *   <li>包含0：可能生成以0开头的数字</li>
     *   <li>格式化：通过padAfter方法统一长度</li>
     *   <li>性能：字符串拼接在初始化时执行一次</li>
     * </ul>
     *
     * @return 随机数生成的上限值（不包含）
     */
    private int getLimit() {
        return Integer.parseInt("1" + StrUtil.repeat('0', this.numberLength));
    }
}
