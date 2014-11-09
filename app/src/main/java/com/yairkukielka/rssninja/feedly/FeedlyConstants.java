package com.yairkukielka.rssninja.feedly;

/**
 * Constants attached to Feedly
 */
public class FeedlyConstants {

    public static final String CLIENT_ID = "***";// "sandbox";
    public static final String CLIENT_SECRET = "***"; // sandbox-key every two months
    public static final String ROOT_URL = "https://cloud.feedly.com";
    public static final String AUTH_URL = "/v3/auth/auth?response_type=code&redirect_uri=" +
            "http://localhost&client_id="+CLIENT_ID+"&scope=https://cloud.feedly.com/subscriptions";
    public static final String MARKERS_PATH = "/v3/markers";
    public static final String TAGS_PATH = "/v3/tags";
    public static final String USERS_PREFIX_PATH = "user/";
    public static final String GLOBAL_ALL_SUFFIX = "/category/global.all";
    public static final String GLOBAL_UNCATEGORIZED_SUFFIX = "/category/global.uncategorized";
    public static final String TAG_URL_PART = "/tag/";
    public static final String GLOBAL_SAVED = "global.saved";
    public static final String UTF_8 = "utf-8";
    public static final String FEEDLY_CATEGORIES = "Feedly Categories";

}
