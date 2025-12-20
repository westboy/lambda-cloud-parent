package com.lambda.cloud.mybatis.purview.support;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.*;
import static com.lambda.cloud.mybatis.utils.SQLUtils.toIn;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.lambda.autoconfig.PurviewProperties;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mybatis.purview.PurviewContext;
import com.lambda.cloud.mybatis.purview.annotation.Purview;
import com.lambda.cloud.mybatis.purview.config.PurviewPropertiesHolder;
import com.lambda.cloud.mybatis.utils.SQLUtils;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Alias;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.binding.MapperMethod.ParamMap;

/**
 * 获取用户下的权限标识集合
 *
 * @author Jin
 **/
@Slf4j
public final class PurviewSqlHelper {
    private static final String PERMISSIONS = "'lambda-permissions(\\|(\\d+)(,\\d+)*+)?(\\|([><])?=?-?\\d*)?'";
    private static final Pattern PATTERN = Pattern.compile(PERMISSIONS);
    private static final Pattern CLEAR_PATTERN =
            Pattern.compile("\\s*\\S*\\s*(?i)(IN)\\s*\\(\\s*" + PERMISSIONS + "\\s*\\)");
    /**
     * 匹配replace模式下，level的设置，可以匹配数字和带运算符的数字
     */
    private static final Pattern LEVEL_PATTERN = Pattern.compile("(>=|<=|>|<|=)?(\\d+)");

    private static final Integer ONE = 1;
    private static final Integer TWO = 2;

    private PurviewSqlHelper() {}

    /**
     * 判断当前用户是否是数据的拥有者
     *
     */
    public static boolean isOwner(LoginUser operator) {
        if (operator == null || StringUtils.isBlank(operator.getName())) {
            return false;
        }
        PurviewProperties config = PurviewPropertiesHolder.getInstance();
        List<String> superAdmins = config.getSuperAdminUsernames();
        if (CollectionUtils.isNotEmpty(superAdmins)) {
            return superAdmins.contains(operator.getName());
        }
        return false;
    }

    /**
     * 获取数据权限tid结合
     * tid可能是用户名、用户角色、用户组织机构ID
     * 如果当前用户拥有开发人员、后台管理员、租户管理员返回全量数据
     *
     * @return java.util.Set<java.lang.String>
     */
    public static Set<String> getPurviewIds(LoginUser operator) {
        if (isOwner(operator)) {
            return Collections.emptySet();
        }
        Set<String> ids = new HashSet<>();
        if (StringUtils.isNotBlank(operator.getName())) {
            ids.add(operator.getName());
        }
        String orgId = operator.getOrgId();
        if (StringUtils.isNotBlank(orgId)) {
            ids.add(orgId);
        }
        return ids;
    }

    /**
     * 获取数据权限注解级别
     *
     * @param purview
     * @return int
     */
    public static int getLevel(PurviewContext purview) {
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
    public static PurviewContext getDynamicPurview(Method method, String sql) {
        if (method != null) {
            Purview actual = method.getAnnotation(Purview.class);
            if (Objects.nonNull(actual)) {
                PurviewContext purview = new PurviewContext();
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
    public static PurviewContext getReplacePurview(String sql) {
        if (StringUtils.isBlank(sql)) {
            return null;
        }
        Matcher matcher = PATTERN.matcher(sql);
        if (!matcher.find()) {
            return null;
        }
        String group = matcher.group();
        if (StringUtils.isBlank(group)) {
            return null;
        }
        // 提取和组装数据权限对象
        String[] tokens = group.split("'")[1].split("\\|");
        PurviewContext purview = new PurviewContext();
        purview.setReplace(true);
        purview.setType(new int[] {0});
        // 解析type
        if (tokens.length > ONE && StringUtils.isNotBlank(tokens[ONE])) {
            purview.setType(Arrays.stream(tokens[1].split(","))
                    .mapToInt(Integer::parseInt)
                    .toArray());
        }
        // 解析level
        if (tokens.length > TWO && StringUtils.isNotBlank(tokens[TWO])) {
            Matcher levelMatcher = LEVEL_PATTERN.matcher(tokens[TWO]);
            if (levelMatcher.matches()) {
                // 获取运算符，如果没有运算符，则默认为 "="
                String operator =
                        (levelMatcher.group(1) != null && !levelMatcher.group(1).isEmpty())
                                ? levelMatcher.group(1)
                                : "=";
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
            Optional<Object> optional = ((ParamMap<Object>) parameter)
                    .values().stream().filter(LoginUser.class::isInstance).findFirst();
            if (optional.isPresent()) {
                return (LoginUser) optional.get();
            }
        }
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
     * @return java.lang.String
     */
    public static String modifySqlForOwner(String source) {
        Matcher matcher = CLEAR_PATTERN.matcher(source);
        return matcher.replaceAll(" 1 = 1 ");
    }

    @Nonnull
    public static String buildSQL01(@Nonnull PurviewContext purview, @Nonnull LoginUser operator) {
        PurviewProperties properties = PurviewPropertiesHolder.getInstance();
        int[] types = purview.getType();
        Set<String> ids = PurviewSqlHelper.getPurviewIds(operator);
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT " + properties.getPurviewIdColumn() + " FROM " + properties.getPurviewTableName());
        sql.append(SPACE)
                .append("WHERE ")
                .append(properties.getPurviewTidColumn())
                .append(toIn(ids));
        sql.append(SPACE)
                .append("AND ")
                .append(properties.getPurviewTypeColumn())
                .append(toIn(types));
        if (purview.getLevel() > -1) {
            sql.append(SPACE)
                    .append("AND ")
                    .append(properties.getPurviewRankColumn())
                    .append(" ")
                    .append(purview.getLevelExp().getComparison())
                    .append(StringPool.SPACE)
                    .append(getLevel(purview));
        }
        return sql.toString();
    }

    /**
     * @param purview
     * @param operator
     * @return java.lang.String
     */
    @Nonnull
    public static String buildSQL02(@Nonnull PurviewContext purview, @Nonnull LoginUser operator) {
        PurviewProperties properties = PurviewPropertiesHolder.getInstance();
        int[] types = purview.getType();
        int level = getLevel(purview);
        String condition = purview.getCondition();
        StringBuilder builder = new StringBuilder();
        builder.append(properties.getPurviewTableAlias())
                .append(DOT)
                .append(properties.getPurviewTidColumn())
                .append(toIn(getPurviewIds(operator)));
        builder.append(" AND ")
                .append(properties.getPurviewTableAlias())
                .append(DOT)
                .append(properties.getPurviewTypeColumn())
                .append(SQLUtils.toIn(types));
        Purview.Scheme scheme = purview.getScheme();
        if (Purview.Scheme.ORGAN.equals(scheme)) {
            String orgId = "";
            builder = new StringBuilder();
            builder.append("SELECT ")
                    .append(properties.getOrganizationIdColumn())
                    .append(" FROM ")
                    .append(properties.getOrganizationTableName())
                    .append(SPACE)
                    .append(properties.getOrganizationTableAlias())
                    .append(" WHERE ")
                    .append(properties.getOrganizationTableAlias())
                    .append(DOT)
                    .append(properties.getOrganizationIdColumn())
                    .append(" = '")
                    .append(orgId)
                    .append(SINGLE_QUOTE);
            builder.append(" OR ")
                    .append(properties.getOrganizationTableAlias())
                    .append(DOT)
                    .append(properties.getOrganizationParentKeysColumn())
                    .append(" LIKE '%")
                    .append(orgId)
                    .append("%'");
            return builder.toString();
        } else if (Purview.Scheme.CASCADE.equals(scheme)) {
            if (level > -1) {
                builder.append(" AND ")
                        .append(properties.getPurviewTableAlias())
                        .append(DOT)
                        .append(properties.getPurviewRankColumn())
                        .append(" ")
                        .append(purview.getLevelExp().getComparison())
                        .append(StringPool.SPACE)
                        .append(level);
            }
            int checked = purview.getChecked();
            if (checked > 0) {
                builder.append(" AND ")
                        .append(properties.getPurviewTableAlias())
                        .append(DOT)
                        .append(properties.getPurviewCheckedColumn())
                        .append(" = ")
                        .append(checked);
            }
            if (StringUtils.isNotBlank(condition)) {
                builder.append(" AND ")
                        .append(properties.getPurviewTableAlias())
                        .append(DOT)
                        .append(condition);
            }
            return builder.insert(
                            0,
                            "SELECT DISTINCT " + properties.getPurviewIdColumn() + " FROM "
                                    + properties.getPurviewTableName() + SPACE + properties.getPurviewTableAlias()
                                    + " WHERE ")
                    .toString();
        } else {
            if (StringUtils.isNotBlank(condition)) {
                condition = "AND VDV." + condition;
            }
            String type = purview.getType()[0] > 0 ? String.valueOf(purview.getType()[0]) : "";
            return "SELECT VDV." + properties.getDataViewSidColumn() + " FROM " + properties.getPurviewTableName()
                    + SPACE + properties.getPurviewTableAlias() + "," + properties.getDataViewTableNamePrefix() + type
                    + " VDV WHERE VDV." + properties.getDataViewIdColumn() + " LIKE" + " CONCAT("
                    + properties.getPurviewTableAlias() + DOT + properties.getPurviewIdColumn() + ", '%') " + condition
                    + " AND " + builder;
        }
    }
}
