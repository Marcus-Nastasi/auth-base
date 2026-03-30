package com.auth.core.shared;

import java.io.Serializable;

public enum Errors implements Serializable {

    USER_ALREADY_ACTIVE("user already activated"),
    USER_ALREADY_EXISTS("user already exists"),
    COULD_NOT_SAVE_USER("Could not save user"),
    COULD_NOT_UPDATE_USER("Could not update user"),
    ROLE_NOT_FOUND("Role not found"),
    USER_NOT_FOUND("Could not find user"),
    USERS_NOT_FOUND("Could not find users");

    private final String msg;

    Errors(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
