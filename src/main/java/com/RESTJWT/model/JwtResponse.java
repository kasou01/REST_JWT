package com.RESTJWT.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class JwtResponse implements Serializable {
    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwttoken;
    private String expiredTimestamp;

    public String getExpiredTimestamp() {
        return expiredTimestamp;
    }

    public void setExpiredTimestamp(String expiredTimestamp) {
        expiredTimestamp = expiredTimestamp;
    }

    public JwtResponse(String jwttoken,String expiredTimestamp) {
        this.jwttoken = jwttoken;
        this.expiredTimestamp = expiredTimestamp;
    }
    public String getToken() {
        return this.jwttoken;
    }
}