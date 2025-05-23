package com.lambda.cloud.redis.model;

import java.net.URI;

public record ConnectionInfo(URI uri, boolean useSsl, String password) {

    public String getHostName() {
        return this.uri.getHost();
    }

    public int getPort() {
        return this.uri.getPort();
    }
}
