package com.auth.core.shared;

import java.io.Serializable;

public enum Errors implements Serializable {

    USER_ALREADY_ACTIVE("user already activated"),
    USER_ALREADY_EXISTS("user already exists"),
    COULD_NOT_SAVE_USER("Could not save user"),
    ROLE_NOT_FOUND("Role not found");;

    private final String msg;

    Errors(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
