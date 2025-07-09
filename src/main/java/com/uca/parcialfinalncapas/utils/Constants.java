package com.uca.parcialfinalncapas.utils;

public class Constants {
    //General endpoints
    public static final String API = "/api";
    public static final String CREATE = "/create";
    public static final String UPDATE = "/update";
    public static final String DELETE = "/delete";
    public static final String GET_ONE = "/get";
    public static final String GET_ALL = "/getAll";
    //Endpoints for user
    public static final String USER = "/user";
    public static final String UPDATE_PASSWORD =  "/updatePass";
    public static final String BY_ID_USER = "/{idUser}";
    public static final String BY_EMAIL_USER = "/{email}";
    //Auth
    public static final String AUTH = "/auth";
    public static final String LOGIN = "/login";

    //Entities
    public static final String ENTITY_USER =  "User";

    //Exceptions Message
    public static final String NOT_FOUND =  " Not Found";
    public static final String JSON_MAPPER = "Cannot map request";
    public static final String NOT_PASSWORD_EQUALS = "Passwords don't match";
    public static final String HAVE_REVIEW = " already has a review";

    //Response Message
    public static final String CREATED =  " Created Successfully";
    public static final String UPDATED_PASS =  "Password Updated Successfully";
    public static final String UPDATED =  " Updated Successfully";
    public static final String DELETED =  " Deleted Successfully";
    //Validations message
    public static final String EMPTY_USER_NAME = "Username cannot be empty";
    public static final String EMPTY_EMAIL = "Email address cannot be empty";
    public static final String INVALID_EMAIL = "Invalid email format";
    public static final String EMPTY_PASSWORD = "Password cannot be empty";
    public static final String NOT_LONG_PASSWORD = "Password must have 6 characters";
    public static final String NULL_USER_ROL = "Role cannot be null";

}

