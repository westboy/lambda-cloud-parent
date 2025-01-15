package com.jingfang.cloud.mybatis.purview.utils;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.cloud.mybatis.purview.annotation.Purview;
import com.jingfang.cloud.mybatis.purview.support.DynamicPurview;
import com.jingfang.cloud.mybatis.utils.SQLUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Alias;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.binding.MapperMethod.ParamMap;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.*;
import static com.jingfang.cloud.mybatis.utils.SQLUtils.toIn;

/**
 * 获取用户下的权限标识集合
 *
 * @author Jin
 **/
@Slf4j
public final class PurviewUtils {
    private static final String JF_PERMISSIONS = "'jf-permissions(\\|(\\d+)(,\\d+)*+)?(\\|([><])?=?-?\\d*)?'";
    private static final Pattern PATTERN = Pattern.compile(JF_PERMISSIONS);
    private static final Pattern CLEAR_PATTERN = Pattern.compile("\\s*\\S*\\s*(?i)(IN)\\s*\\(\\s*" + JF_PERMISSIONS + "\\s*\\)");
    /**
     * 匹配replace模式下，level的设置，可以匹配数字和带运算符的数字
     */
    private static final Pattern LEVEL_PATTERN = Pattern.compile("(>=|<=|>|<|=)?(\\d+)");

    private static final Integer ONE = 1;
    private static final Integer TWO = 2;

    private PurviewUtils() {
    }

    /**
     * 判断当前用户是否是数据的拥有者
     *
     * @param operator
     * @return
     */
    public static boolean isOwner(LoginUser operator) {
        return false;
    }

    /**
     * 获取数据权限tid结合
     * tid可能是用户名、用户角色、用户组织机构ID
     * 如果当前用户拥有开发人员、后台管理员、租户管理员返回全量数据
     *
     * @param operator
     * @return java.util.Set<java.lang.String>
     */
    public static Set<String> getPurviewIds(LoginUser operator) {
        if (isOwner(operator)) {
            return Collections.emptySet();
        }
        Set<String> ids = new HashSet<>();
        ids.add("LoginUserid");
        String orgid = "";
        if (StringUtils.isNotBlank(orgid)) {
            ids.add(orgid);
        }
        return ids;
    }

    /**
     * 获取数据权限注解级别
     *
     * @param purview
     * @return int
     */
    public static int getLevel(DynamicPurview purview) {
        int level = purview.getLevel();
        if (level == 0) {
            return Integer.MAX_VALUE;
        }
        return level;
    }

    /**
     * 获取SQL
     *
     * @param source
     * @param permissions
     */
    public static String getSql(String source, Set<String> permissions) {
        if (CollectionUtils.isNotEmpty(permissions)) {
            StringJoiner joiner = new StringJoiner(SINGLE_QUOTE + COMMA + SINGLE_QUOTE, SINGLE_QUOTE, SINGLE_QUOTE);
            for (String item : permissions) {
                joiner.add(item);
            }
            return getSql(source, joiner.toString());
        }
        return source;
    }

    /**
     * 获取SQL
     *
     * @param source
     * @param sql
     * @return
     */
    public static String getSql(String source, String sql) {
        return PATTERN.matcher(source).replaceAll(sql);
    }


    /**
     * 获取数据权限注解信息
     *
     * @param method 当前方法
     * @param sql
     */
    public static DynamicPurview getDynamicPurview(Method method, String sql) {
        if (method != null) {
            Purview actual = method.getAnnotation(Purview.class);
            if (Objects.nonNull(actual)) {
                DynamicPurview purview = new DynamicPurview();
                purview.setKey(actual.key());
                purview.setLevel(actual.level());
                purview.setLevelExp(actual.levelExp());
                purview.setType(actual.type());
                purview.setMode(actual.mode());
                purview.setScheme(actual.scheme());
                purview.setCondition(actual.condition());
                purview.setPretreatment(actual.pretreatment());
                purview.setChecked(actual.checked());
                return purview;
            }
        }
        return null;
    }

    /**
     * 根据replace字符串解析数据权限属性
     *
     * @param sql
     * @return
     */
    @SuppressWarnings("AlibabaJavaConstants.UndefineMagicConstant")
    public static DynamicPurview getReplacePurview(String sql) {
        if (StringUtils.isBlank(sql)) {
            return null;
        }
        Matcher matcher = PATTERN.matcher(sql);
        if (!matcher.find()) {
            return null;
        }
        String purveiewStr = matcher.group();
        if (StringUtils.isBlank(purveiewStr)) {
            return null;
        }
        // 提取和组装数据权限对象
        String[] strs = purveiewStr.split("'")[1].split("\\|");
        DynamicPurview purview = new DynamicPurview();
        purview.setReplace(true);
        purview.setType(new int[]{0});
        // 解析type
        if (strs.length > ONE && StringUtils.isNotBlank(strs[ONE])) {
            purview.setType(Arrays.stream(strs[1].split(",")).mapToInt(Integer::parseInt).toArray());
        }
        // 解析level
        if (strs.length > TWO && StringUtils.isNotBlank(strs[TWO])) {
            Matcher levelMatcher = LEVEL_PATTERN.matcher(strs[TWO]);
            if (levelMatcher.matches()) {
                // 获取运算符，如果没有运算符，则默认为 "="
                String operator = (levelMatcher.group(1) != null && !levelMatcher.group(1).isEmpty()) ? levelMatcher.group(1) : "=";
                purview.setLevelExp(Purview.Expression.parseExpression(operator));
                // 获取数字
                String number = levelMatcher.group(2);
                purview.setLevel(Integer.parseInt(number));
            }
        }
        return purview;
    }

    /**
     * 获取LoginUser参数
     *
     * @param parameter 参数信息
     */
    @SuppressWarnings("unchecked")
    public static LoginUser getOperator(Object parameter) {
        if (parameter instanceof ParamMap) {
            Optional<Object> optional = ((ParamMap<Object>) parameter).values().stream()
                    .filter(LoginUser.class::isInstance).findFirst();
            if (optional.isPresent()) {
                return (LoginUser) optional.get();
            }
        }
        //todo 从shiro 取用户
        return null;
    }

    /**
     * @param alias
     * @param key
     * @return void
     */
    public static String getPurviewKey(Alias alias, @Nonnull String key) {
        if (key.contains(LEFT_BRACKET) || key.contains(DOT)) {
            return key;
        }
        if (alias != null) {
            return alias.getName() + DOT + key;
        }
        return key;
    }

    /**
     * 是否需要替换权限
     *
     * @param source
     * @return boolean
     */
    public static boolean getReplace(String source) {
        return PATTERN.matcher(source).find();
    }

    /**
     * 为数据拥有者修改SQL将xxx in ('PURVermissions') 替换为 1 = 1；
     *
     * @param source
     * @return java.lang.String
     */
    public static String modifySqlForOwner(String source) {
        Matcher matcher = CLEAR_PATTERN.matcher(source);
        return matcher.replaceAll(" 1 = 1 ");
    }


    @Nonnull
    @SuppressWarnings({"AlibabaLowerCamelCaseVariableNaming", "PMD"})
    public static String buildSQL01(@Nonnull DynamicPurview purview, @Nonnull LoginUser operator) {
        int[] types = purview.getType();
        Set<String> ids = PurviewUtils.getPurviewIds(operator);
        StringBuilder sql = new StringBuilder("SELECT DISTINCT id FROM PURVIEWS");
        sql.append(SPACE).append("WHERE TID").append(toIn(ids));
        sql.append(SPACE).append("AND type2").append(toIn(types));
        if (purview.getLevel() > -1) {
            sql.append(SPACE).append("AND rank2 ").append(purview.getLevelExp().getComparison()).append(StringPool.SPACE).append(getLevel(purview));
        }
        return sql.toString();
    }


    /**
     * @param purview
     * @param operator
     * @return java.lang.String
     */
    @Nonnull
    @SuppressWarnings({"AlibabaLowerCamelCaseVariableNaming", "PMD"})
    public static String buildSQL02(@Nonnull DynamicPurview purview, @Nonnull LoginUser operator) {
        int[] types = purview.getType();
        int level = getLevel(purview);
        String condition = purview.getCondition();
        StringBuilder builder = new StringBuilder();
        builder.append("PURV.TID").append(toIn(getPurviewIds(operator)));
        builder.append(" AND PURV.type2").append(SQLUtils.toIn(types));
        Purview.Scheme scheme = purview.getScheme();
        if (Purview.Scheme.ORGAN.equals(scheme)) {
            String orgId = "";
            builder = new StringBuilder();
            builder.append("SELECT id FROM ORGANIZATION ORGA WHERE ORGA.id = '").append(orgId).append(SINGLE_QUOTE);
            builder.append(" OR ORGA.parentkeys LIKE '%").append(orgId).append("%'");
            return builder.toString();
        } else if (Purview.Scheme.CASCADE.equals(scheme)) {
            if (level > -1) {
                builder.append(" AND PURV.rank2 ").append(purview.getLevelExp().getComparison()).append(StringPool.SPACE).append(level);
            }
            int checked = purview.getChecked();
            if (checked > 0) {
                builder.append(" AND PURV.checked = ").append(checked);
            }
            if (StringUtils.isNotBlank(condition)) {
                builder.append(" AND PURV.").append(condition);
            }
            return builder.insert(0, "SELECT DISTINCT id FROM PURVIEWS PURV WHERE ").toString();
        } else {
            if (StringUtils.isNotBlank(condition)) {
                condition = "AND VDV." + condition;
            }
            String type = purview.getType()[0] > 0 ? String.valueOf(purview.getType()[0]) : "";
            return "SELECT VDV.sid FROM PURVIEWS PURV,V_DATAVIEW" + type + " VDV WHERE VDV.ID LIKE" +
                    " CONCAT(PURV.ID, '%') " + condition + " AND " + builder;
        }
    }
}
