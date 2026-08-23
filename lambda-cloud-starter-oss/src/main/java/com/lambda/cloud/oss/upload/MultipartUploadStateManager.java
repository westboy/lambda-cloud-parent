package com.lambda.cloud.oss.upload;

import com.lambda.cloud.oss.model.PartTag;
import java.util.List;

/**
 * 分片上传状态管理接口
 * 用于存储和管理分片上传的中间状态
 *
 * @author jpjoo
 */
public interface MultipartUploadStateManager {

    /**
     * 保存上传 ID
     *
     * @param stateKey 状态键
     * @param uploadId 上传 ID
     */
    void saveUploadId(String stateKey, String uploadId);

    /**
     * 获取上传 ID
     *
     * @param stateKey 状态键
     * @return 上传 ID，如果不存在则返回 null
     */
    String getUploadId(String stateKey);

    /**
     * 保存已上传的分片标签
     *
     * @param stateKey 状态键
     * @param partTags 分片标签列表
     */
    void savePartETags(String stateKey, List<PartTag> partTags);

    /**
     * 获取已上传的分片标签
     *
     * @param stateKey 状态键
     * @return 分片标签列表，如果不存在则返回 null
     */
    List<PartTag> getPartETags(String stateKey);

    /**
     * 删除上传状态
     *
     * @param stateKey 状态键
     */
    void deleteState(String stateKey);

    /**
     * 检查状态是否存在
     *
     * @param stateKey 状态键
     * @return 是否存在
     */
    boolean exists(String stateKey);
}
