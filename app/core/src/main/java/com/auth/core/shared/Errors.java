package com.auth.core.shared;

public enum Errors {

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
