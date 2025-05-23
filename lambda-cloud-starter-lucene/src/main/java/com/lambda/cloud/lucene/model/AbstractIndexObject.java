package com.lambda.cloud.lucene.model;

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
    public int compareTo(AbstractIndexObject other) {
        return Float.compare(this.score, other.score);
    }
}
