package com.lambda.cloud.oss.policy;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lambda.cloud.oss.enums.PolicyType;

import java.util.ArrayList;
import java.util.List;

/**
 * MinIO 策略构建器
 * 用于构建符合 AWS S3 标准的存储桶策略 JSON
 *
 * @author jpjoo
 */
public class MinIOPolicyBuilder {

    private static final String VERSION = "2012-10-17";
    private static final String EFFECT_ALLOW = "Allow";
    private static final String EFFECT_DENY = "Deny";
    private static final String PRINCIPAL_ALL = "*";

    private final String bucketName;
    private final PolicyType policyType;

    /**
     * 构造函数
     *
     * @param bucketName 存储桶名称
     * @param policyType 策略类型
     */
    public MinIOPolicyBuilder(String bucketName, PolicyType policyType) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new IllegalArgumentException("存储桶名称不能为空");
        }
        if (policyType == null) {
            throw new IllegalArgumentException("策略类型不能为空");
        }
        this.bucketName = bucketName;
        this.policyType = policyType;
    }

    /**
     * 构建策略 JSON 字符串
     *
     * @return 策略 JSON 字符串
     */
    public String build() {
        JSONObject policy = new JSONObject();
        policy.set("Version", VERSION);
        policy.set("Statement", buildStatements());
        return JSONUtil.toJsonPrettyStr(policy);
    }

    /**
     * 构建策略声明列表
     *
     * @return 策略声明列表
     */
    private JSONArray buildStatements() {
        JSONArray statements = new JSONArray();

        // 添加存储桶级别的声明
        statements.add(buildBucketStatement());

        // 对于只读策略，添加拒绝 ListBucket 的声明
        if (policyType == PolicyType.READ) {
            statements.add(buildDenyListBucketStatement());
        }

        // 添加对象级别的声明
        statements.add(buildObjectStatement());

        return statements;
    }

    /**
     * 构建存储桶级别的策略声明
     *
     * @return 存储桶级别的策略声明
     */
    private JSONObject buildBucketStatement() {
        JSONObject statement = new JSONObject();
        statement.set("Effect", EFFECT_ALLOW);
        statement.set("Principal", PRINCIPAL_ALL);
        statement.set("Action", buildBucketActions());
        statement.set("Resource", buildBucketResource());
        return statement;
    }

    /**
     * 构建拒绝 ListBucket 的策略声明（仅用于只读策略）
     *
     * @return 拒绝 ListBucket 的策略声明
     */
    private JSONObject buildDenyListBucketStatement() {
        JSONObject statement = new JSONObject();
        statement.set("Effect", EFFECT_DENY);
        statement.set("Principal", PRINCIPAL_ALL);

        JSONArray actions = new JSONArray();
        actions.add("s3:ListBucket");
        statement.set("Action", actions);

        statement.set("Resource", buildBucketResource());
        return statement;
    }

    /**
     * 构建对象级别的策略声明
     *
     * @return 对象级别的策略声明
     */
    private JSONObject buildObjectStatement() {
        JSONObject statement = new JSONObject();
        statement.set("Effect", EFFECT_ALLOW);
        statement.set("Principal", PRINCIPAL_ALL);
        statement.set("Action", buildObjectActions());
        statement.set("Resource", buildObjectResource());
        return statement;
    }

    /**
     * 构建存储桶级别的操作列表
     *
     * @return 操作列表
     */
    private JSONArray buildBucketActions() {
        JSONArray actions = JSONUtil.createArray();
        actions.add("s3:GetBucketLocation");

        switch (policyType) {
            case WRITE:
                actions.add("s3:ListBucketMultipartUploads");
                break;
            case READ_WRITE:
                actions.add("s3:ListBucket");
                actions.add("s3:ListBucketMultipartUploads");
                break;
            case READ:
            default:
                // 只读策略只需要 GetBucketLocation
                break;
        }
        return actions;
    }

    /**
     * 构建对象级别的操作列表
     *
     * @return 操作列表
     */
    private JSONArray buildObjectActions() {
        JSONArray actions = JSONUtil.createArray();

        switch (policyType) {
            case WRITE:
                actions.add("s3:AbortMultipartUpload");
                actions.add("s3:DeleteObject");
                actions.add("s3:ListMultipartUploadParts");
                actions.add("s3:PutObject");
                break;
            case READ_WRITE:
                actions.add("s3:AbortMultipartUpload");
                actions.add("s3:DeleteObject");
                actions.add("s3:GetObject");
                actions.add("s3:ListMultipartUploadParts");
                actions.add("s3:PutObject");
                break;
            case READ:
            default:
                actions.add("s3:GetObject");
                break;
        }
        return actions;
    }

    /**
     * 构建存储桶资源 ARN
     *
     * @return 存储桶资源 ARN
     */
    private String buildBucketResource() {
        return "arn:aws:s3:::" + bucketName;
    }

    /**
     * 构建对象资源 ARN（包含通配符）
     *
     * @return 对象资源 ARN
     */
    private String buildObjectResource() {
        return "arn:aws:s3:::" + bucketName + "/*";
    }

    /**
     * 静态工厂方法：构建策略 JSON 字符串
     *
     * @param bucketName 存储桶名称
     * @param policyType 策略类型
     * @return 策略 JSON 字符串
     */
    public static String buildPolicy(String bucketName, PolicyType policyType) {
        return new MinIOPolicyBuilder(bucketName, policyType).build();
    }
}
