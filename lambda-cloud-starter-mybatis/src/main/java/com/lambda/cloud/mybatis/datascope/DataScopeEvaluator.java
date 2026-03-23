package com.lambda.cloud.mybatis.datascope;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.lambda.autoconfig.datascope.DataScopeProperties;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mybatis.datascope.annotation.DataScope;
import com.lambda.cloud.mybatis.datascope.context.DataScopeContext;
import com.lambda.cloud.mybatis.utils.SqlConditionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.binding.MapperMethod.ParamMap;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.*;
import static com.lambda.cloud.mybatis.utils.SqlConditionUtils.toIn;

/**
 * 获取用户下的权限标识集合
 *
 * @author Jin
 **/
@Slf4j
public final class DataScopeEvaluator {
    private static final String PERMISSIONS = "'permissions(\\|(\\d+)(,\\d+)*+)?(\\|([><])?=?-?\\d*)?'";
    private static final Pattern PATTERN = Pattern.compile(PERMISSIONS);
    private static final Pattern CLEAR_PATTERN =
            Pattern.compile("\\s*\\S*\\s*(?i)(IN)\\s*\\(\\s*" + PERMISSIONS + "\\s*\\)");
    /**
     * 匹配replace模式下，level的设置，可以匹配数字和带运算符的数字
     */
    private static final Pattern LEVEL_PATTERN = Pattern.compile("(>=|<=|>|<|=)?(\\d+)");

    private static final Integer ONE = 1;
    private static final Integer TWO = 2;

    private DataScopeEvaluator() {
    }

    /**
     * 判断当前用户是否是数据的拥有者
     *
     */
    public static boolean isOwner(LoginUser operator) {
        if (operator == null || StringUtils.isBlank(operator.getName())) {
            return false;
        }
        DataScopeProperties config = DataScopePropertiesHolder.getInstance();
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
    public static Set<String> getDataScopeIds(LoginUser operator) {
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
     * @param context DataScopeContext
     * @return int
     */
    public static int getLevel(DataScopeContext context) {
        int level = context.getLevel();
        if (level == 0) {
            return Integer.MAX_VALUE;
        }
        return level;
    }

    /**
     * 获取SQL
     */
    public static String getSql(String source, String sql) {
        return PATTERN.matcher(source).replaceAll(sql);
    }

    /**
     * 根据replace字符串解析数据权限属性
     *
     */
    public static DataScopeContext parseReplaceDataScope(String sql) {
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
        DataScopeContext context = new DataScopeContext();
        context.setReplace(true);
        context.setType(new int[]{0});
        // 解析type
        if (tokens.length > ONE && StringUtils.isNotBlank(tokens[ONE])) {
            context.setType(Arrays.stream(tokens[1].split(","))
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
                context.setLevelExp(DataScope.Expression.parseExpression(operator));
                // 获取数字
                String number = levelMatcher.group(2);
                context.setLevel(Integer.parseInt(number));
            }
        }
        return context;
    }

    /**
     * 获取SQL
     *
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
     * 是否需要替换权限
     */
    public static boolean getReplace(String source) {
        return PATTERN.matcher(source).find();
    }

    /**
     * 为数据拥有者修改SQL将xxx in ('permissions') 替换为 1 = 1；
     *
     * @return java.lang.String
     */
    public static String modifySqlForOwner(String source) {
        Matcher matcher = CLEAR_PATTERN.matcher(source);
        return matcher.replaceAll(" 1 = 1 ");
    }

    /**
     * 构建带有高级策略过滤的SQL (支持组织架构模式、Condition、Checked状态等)
     *
     * @param context  数据权限上下文
     * @param operator 当前登录用户
     * @return java.lang.String
     */
    @Nonnull
    public static String buildStrategyScopeSql(@Nonnull DataScopeContext context, @Nonnull LoginUser operator) {
        DataScopeProperties properties = DataScopePropertiesHolder.getInstance();
        int[] types = context.getType();
        int level = getLevel(context);
        String condition = context.getCondition();
        StringBuilder builder = new StringBuilder();
        builder.append(properties.getDataScopeTableAlias())
                .append(DOT)
                .append(properties.getDataScopeTidColumn())
                .append(toIn(getDataScopeIds(operator)));
        builder.append(" AND ")
                .append(properties.getDataScopeTableAlias())
                .append(DOT)
                .append(properties.getDataScopeTypeColumn())
                .append(SqlConditionUtils.toIn(types));
        DataScope.Scheme scheme = context.getScheme();
        if (DataScope.Scheme.ORGANIZATION.equals(scheme)) {
            String orgId = operator.getOrgId();
            if (StringUtils.isBlank(orgId)) {
                // 如果用户没有组织ID，直接返回一个查不到数据的条件（或根据业务需要抛出异常）
                return "SELECT '' FROM DUAL WHERE 1=0";
            }
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
        } else {
            if (level > -1) {
                builder.append(" AND ")
                        .append(properties.getDataScopeTableAlias())
                        .append(DOT)
                        .append(properties.getDataScopeRankColumn())
                        .append(" ")
                        .append(context.getLevelExp().getComparison())
                        .append(StringPool.SPACE)
                        .append(level);
            }
            int checked = context.getChecked();
            if (checked > 0) {
                builder.append(" AND ")
                        .append(properties.getDataScopeTableAlias())
                        .append(DOT)
                        .append(properties.getDataScopeCheckedColumn())
                        .append(" = ")
                        .append(checked);
            }
            if (StringUtils.isNotBlank(condition)) {
                builder.append(" AND ")
                        .append(properties.getDataScopeTableAlias())
                        .append(DOT)
                        .append(condition);
            }
            return builder.insert(
                            0,
                            "SELECT DISTINCT " + properties.getDataScopeIdColumn() + " FROM "
                                    + properties.getDataScopeTableName() + SPACE + properties.getDataScopeTableAlias()
                                    + " WHERE ")
                    .toString();
        }
    }
}
