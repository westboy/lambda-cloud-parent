package com.lambda.cloud.oss.model;

import com.amazonaws.services.s3.model.PartETag;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
public class UploadPartTag implements Serializable {

    private List<PartETag> partETags = new ArrayList<>();

    public void addPartETag(PartETag partETag) {
        partETags.add(partETag);
    }
}
