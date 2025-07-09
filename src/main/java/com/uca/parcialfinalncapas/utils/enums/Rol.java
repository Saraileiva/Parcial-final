package com.uca.parcialfinalncapas.utils.enums;

import jdk.jshell.Snippet;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public enum Rol {
    USER("USER"),
    TECH("TECH");

    private final String value;

    Rol(String value) {
        this.value = value;
    }


    public UUID getId() {
    }
}
