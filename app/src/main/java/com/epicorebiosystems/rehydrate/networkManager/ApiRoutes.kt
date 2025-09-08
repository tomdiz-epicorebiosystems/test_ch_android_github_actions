package com.epicorebiosystems.rehydrate.networkManager

object ApiRoutes {

    // Defaults to Production Server Info
    const val PROD_BASE_URL:String = "https://ch.epicorebiosystems.com"
    const val PROD_CH_PHONE_API_JWT_SECRET:String = "utbc_23p98Zb"
    const val PROD_CH_PHONE_API_KEY:String = "q3m7rvCPykvr3_4"
    const val PROD_CLIENT_ID:String = "&client_id=aiGuzIjPCu6Mxm7M34hrkXYERJfhepRT"
    const val PROD_AUTH0_URL:String = "auth.ch.epicorebiosystems.com"

    // Defaults to Staging Server Info
    const val STAGING_BASE_URL:String = "https://epicore.dev"
    const val STAGING_CH_PHONE_API_JWT_SECRET:String = "So0e5En79B3T"
    const val STAGING_CH_PHONE_API_KEY:String = "%{UF)43sVG(#ks3"
    const val STAGING_CLIENT_ID:String = "&client_id=aHekjFeRi5qHapVK5XX0d6lr5FyrFeB7"
    const val STAGING_AUTH0_URL:String = "auth.epicore.dev"

    var LOGIN_CONTEXT_GET = "/api/external/onboarding/login-context"
    var SEND_CODE_PUT = "/api/external/onboarding/send-code"
    var AUTHENTICATE_WITH_CODE_POST = "/api/external/onboarding/authenticate-with-code"
    var USER_HISTORY_STATS_GET = "/api/external/user-stats"
    var AVG_SWEAT_CONCENTRATION_GET = "/api/external/avg-sweat-volume-sodium-concentration"
    var UPDATE_USER_PUT = "/api/external/onboarding/update-user"
    var USER_PRIVACY = "/api/external/user-info"
    var ENTERPRISE_NAME_GET = "/api/external/onboarding/sites"
}