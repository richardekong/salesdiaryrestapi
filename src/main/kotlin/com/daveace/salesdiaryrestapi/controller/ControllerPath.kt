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
        private const val CREDITS = "/credits"
        private const val EXPENDITURES = "/expenditures"
        private const val DAILY = "/daily"
        private const val WEEKLY = "/weekly"
        private const val MONTHLY = "/monthly"
        private const val QUARTERLY = "/quarterly"
        private const val SEMESTER ="/semester"
        private const val YEARLY = "/yearly"
        private const val METRICS = "/metrics"
        private const val REPORT = "/report"
        private const val EXTENDED_REPORT = "/extended-report"
        private const val WILDCARD = "/**"
        private const val DOT_ASTERISKS = ".*"

        const val SALES_DIARY_AUTH_WILD_CARD = "$SALES_DIARY$AUTH$WILDCARD"
        const val SALES_DIARY_AUTH_SIGN_UP_USERS = "$SALES_DIARY$AUTH$SIGN_UP$USERS"
        const val SALES_DIARY_AUTH_LOGIN_USERS = "$SALES_DIARY$AUTH$LOGIN$USERS"
        const val SALES_DIARY_AUTH_PASSWORD_RESET_LINK = "$SALES_DIARY$AUTH$PASSWORD_RESET_LINK/" //append path variable
        const val SALES_DIARY_AUTH_RESET_PASSWORD = "$SALES_DIARY$AUTH$RESET_PASSWORD/" //append path variable
        const val SALES_DIARY_USERS = "$SALES_DIARY$USERS"
        const val SALES_DIARY_USER = "$SALES_DIARY$USERS/" //append path variable value

        const val TWO_FACTOR_AUTH = "/2FA"
        const val TWO_FACTOR_AUTH_CHANNEL = "/2FA/channel"
        const val DEACTIVATED_2FA = "/deactivated-2FA"

        const val BASE_URL = "http://localhost:8092"

        const val SALES_DIARY_TRADERS = "$SALES_DIARY$TRADERS"
        const val SALES_DIARY_TRADER = "$SALES_DIARY$TRADERS/" //append path variable e.g trader's email

        const val SALES_DIARY_CUSTOMERS = "$SALES_DIARY$CUSTOMERS"
        const val SALES_DIARY_CUSTOMER = "$SALES_DIARY$CUSTOMERS/" //append path variable e.g email

        const val SALES_DIARY_PRODUCTS = "$SALES_DIARY$PRODUCTS"
        const val SALES_DIARY_PRODUCT = "$SALES_DIARY$PRODUCTS/" // append desired path variable

        const val SALES_DIARY_SALES_EVENT = "$SALES_DIARY$EVENTS/" //append desired path variable
        const val SALES_DIARY_SALES_EVENTS = "$SALES_DIARY$EVENTS"
        const val SALES_DIARY_SALES_EVENTS_REPORT = "$SALES_DIARY_SALES_EVENTS$REPORT$DOT_ASTERISKS"
        const val SALES_DIARY_SALES_EVENTS_EXTENDED_REPORT = "$SALES_DIARY_SALES_EVENTS$EXTENDED_REPORT$DOT_ASTERISKS"

        const val SALES_DIARY_DAILY_SALES_EVENTS = "$SALES_DIARY_SALES_EVENTS$DAILY"
        const val SALES_DIARY_DAILY_SALES_EVENTS_REPORT = "$SALES_DIARY_DAILY_SALES_EVENTS$REPORT$DOT_ASTERISKS"
        const val SALES_DIARY_DAILY_SALES_EVENTS_EXTENDED_REPORT = "$SALES_DIARY_DAILY_SALES_EVENTS$EXTENDED_REPORT$DOT_ASTERISKS"

        const val SALES_DIARY_WEEKLY_SALES_EVENTS = "$SALES_DIARY_SALES_EVENTS$WEEKLY"
        const val SALES_DIARY_WEEKLY_SALES_EVENTS_REPORT = "$SALES_DIARY_WEEKLY_SALES_EVENTS$REPORT$DOT_ASTERISKS"
        const val SALES_DIARY_WEEKLY_SALES_EVENTS_EXTENDED_REPORT = "$SALES_DIARY_WEEKLY_SALES_EVENTS$EXTENDED_REPORT$DOT_ASTERISKS"

        const val SALES_DIARY_MONTHLY_SALES_EVENTS = "$SALES_DIARY_SALES_EVENTS$MONTHLY"
        const val SALES_DIARY_MONTHLY_SALES_EVENTS_REPORT = "$SALES_DIARY_SALES_EVENTS$MONTHLY$REPORT$DOT_ASTERISKS"
        const val SALES_DIARY_MONTHLY_SALES_EVENTS_EXTENDED_REPORT = "$SALES_DIARY_MONTHLY_SALES_EVENTS$EXTENDED_REPORT$DOT_ASTERISKS"

        const val SALES_DIARY_QUARTERLY_SALES_EVENTS = "$SALES_DIARY_SALES_EVENTS$QUARTERLY"
        const val SALES_DIARY_QUARTERLY_SALES_EVENTS_REPORT = "$SALES_DIARY_QUARTERLY_SALES_EVENTS$REPORT$DOT_ASTERISKS"
        const val SALES_DIARY_QUARTERLY_SALES_EVENTS_EXTENDED_REPORT = "$SALES_DIARY_QUARTERLY_SALES_EVENTS$EXTENDED_REPORT$DOT_ASTERISKS"

        const val SALES_DIARY_SEMESTER_SALES_EVENTS = "$SALES_DIARY_SALES_EVENTS$SEMESTER"
        const val SALES_DIARY_SEMESTER_SALES_EVENTS_REPORT = "$SALES_DIARY_SEMESTER_SALES_EVENTS$REPORT$DOT_ASTERISKS"
        const val SALES_DIARY_SEMESTER_SALES_EVENTS_EXTENDED_REPORT = "$SALES_DIARY_SEMESTER_SALES_EVENTS$EXTENDED_REPORT$DOT_ASTERISKS"

        const val SALES_DIARY_YEARLY_SALES_EVENTS = "$SALES_DIARY_SALES_EVENTS$YEARLY"
        const val SALES_DIARY_YEARLY_SALES_EVENTS_REPORT = "$SALES_DIARY_YEARLY_SALES_EVENTS$REPORT"
        const val SALES_DIARY_YEARLY_SALES_EVENTS_EXTENDED_REPORT = "$SALES_DIARY_YEARLY_SALES_EVENTS$EXTENDED_REPORT$DOT_ASTERISKS"

        const val SALES_DIARY_SALES_EVENTS_METRICS = "$SALES_DIARY_SALES_EVENTS$METRICS"
        const val SALES_DIARY_DAILY_SALES_EVENTS_METRICS = "$SALES_DIARY_DAILY_SALES_EVENTS$METRICS"
        const val SALES_DIARY_WEEKLY_SALES_EVENTS_METRICS = "$SALES_DIARY_WEEKLY_SALES_EVENTS$METRICS"
        const val SALES_DIARY_MONTHLY_SALES_EVENTS_METRICS = "$SALES_DIARY_MONTHLY_SALES_EVENTS$METRICS"
        const val SALES_DIARY_QUARTERLY_SALES_EVENTS_METRICS = "$SALES_DIARY_QUARTERLY_SALES_EVENTS$METRICS"
        const val SALES_DIARY_SEMESTER_SALES_EVENTS_METRICS = "$SALES_DIARY_SEMESTER_SALES_EVENTS$METRICS"
        const val SALES_DIARY_YEARLY_SALES_EVENTS_METRICS = "$SALES_DIARY_YEARLY_SALES_EVENTS$METRICS"

        const val SALES_DIARY_CREDITS = "$SALES_DIARY$CREDITS"
        const val SALES_DIARY_CREDITS_FROM_EVENT = "$SALES_DIARY_CREDITS-from-events"

        const val SALES_DAIRY_EXPS = "$SALES_DIARY$EXPENDITURES"

    }
}