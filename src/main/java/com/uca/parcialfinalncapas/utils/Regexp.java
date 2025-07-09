package com.uca.parcialfinalncapas.utils;


public class Regexp {
    public static final String REGEXP_PASSWORD = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]*$";
    public static final String REGEXP_IMAGE = "^.*\\.(jpg|jpeg|png|gif|webp)$";
    public static final String REGEXP_PHONE = "^\\d{4}-\\d{4}$";
}

