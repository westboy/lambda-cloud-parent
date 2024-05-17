package com.jingfang.cloud.lucene.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

/**
 * IndexObject
 *
 * @author Jin
 */
@Getter
@Setter
public abstract class IndexObject implements Comparable<IndexObject> {

    @JsonIgnore
    public abstract String getId();

    private float score;

    @Override
    public int compareTo(IndexObject o) {
        if (this.score < o.getScore()) {
            return 1;
        } else if (this.score > o.getScore()) {
            return -1;
        }
        return 0;
    }
}