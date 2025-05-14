package com.lambda.cloud.oss.model;

import com.amazonaws.services.s3.model.PartETag;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class UploadPartTag implements Serializable {

    List<PartETag> partETags = new ArrayList<PartETag>();

    public void addPartETag(PartETag partETag) {
        partETags.add(partETag);
    }
}
