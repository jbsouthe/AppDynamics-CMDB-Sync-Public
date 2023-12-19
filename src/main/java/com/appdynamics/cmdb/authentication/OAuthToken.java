package com.appdynamics.cmdb.authentication;

public class OAuthToken {
    public String access_token, token_type, expires_in;
    public transient long generatedAt = System.currentTimeMillis();

    public String getAccessToken() {
        return isExpired() ? null : access_token;
    }

    public boolean isExpired() {
        long expiresInMilliSeconds = Long.parseLong(expires_in) * 1000;
        return System.currentTimeMillis() - generatedAt + expiresInMilliSeconds > 0;
    }

}
