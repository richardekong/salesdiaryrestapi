package com.daveace.salesdiaryrestapi.controller

class ControllerPath {
    companion object {

        const val API = "/api"

        private const val SALES_DIARY = "/sales-diary"
        private const val AUTH = "/auth"
        private const val PASSWORD_RESET_LINK ="/password-reset-link"
        private const val RESET_PASSWORD = "/reset-password"
        private const val SIGN_UP = "/signUp"
        private const val LOGIN = "/login"
        private const val USERS = "/users"
        private const val TRADERS = "/traders"
        private const val CUSTOMERS = "/customers"
        private const val PRODUCTS = "/products"
        private const val EVENTS = "/events"
        private const val WILDCARD = "/**"

        const val SALES_DIARY_AUTH_WILD_CARD = "$SALES_DIARY$AUTH$WILDCARD"
        const val SALES_DIARY_AUTH_SIGN_UP_USERS = "$SALES_DIARY$AUTH$SIGN_UP$USERS"
        const val SALES_DIARY_AUTH_LOGIN_USERS = "$SALES_DIARY$AUTH$LOGIN$USERS"
        const val SALES_DIARY_AUTH_PASSWORD_RESET_LINK = "$SALES_DIARY$AUTH$PASSWORD_RESET_LINK/" //append path variable
        const val SALES_DIARY_AUTH_RESET_PASSWORD = "$SALES_DIARY$AUTH$RESET_PASSWORD/" //append path variable
        const val SALES_DIARY_USERS = "$SALES_DIARY$USERS"
        const val SALES_DIARY_USER = "$SALES_DIARY$USERS/" //append path variable value
        const val BASE_URL = "http://localhost:8092"

        const val SALES_DIARY_TRADERS = "$SALES_DIARY$TRADERS"
        const val SALES_DIARY_TRADER = "$SALES_DIARY$TRADERS/" //append path variable e.g trader's email

        const val SALES_DIARY_CUSTOMERS = "$SALES_DIARY$CUSTOMERS"
        const val SALES_DIARY_CUSTOMER = "$SALES_DIARY$CUSTOMERS/" //append path variable e.g email

        const val SALES_DIARY_PRODUCTS = "$SALES_DIARY$PRODUCTS"
        const val SALES_DIARY_PRODUCT = "$SALES_DIARY$PRODUCTS/" // append desired path variable

        const val SALES_DIARY_SALES_EVENTS = "$SALES_DIARY$EVENTS"
        const val SALES_DIARY_SALES_EVENT = "$SALES_DIARY$EVENTS/" //append desired path variable

    }
}