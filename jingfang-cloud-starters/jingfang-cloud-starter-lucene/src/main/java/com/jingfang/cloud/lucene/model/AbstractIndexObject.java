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
public abstract class AbstractIndexObject implements Comparable<AbstractIndexObject> {

    /**
     * id
     * @return String
     */
    @JsonIgnore
    public abstract String id();

    private float score;

    @Override
    public int compareTo(AbstractIndexObject o) {
        if (this.score < o.getScore()) {
            return 1;
        } else if (this.score > o.getScore()) {
            return -1;
        }
        return 0;
    }
}