package com.banyan.missedcallalert.Global;

/**
 * Created by Sangavi on 20/6/2016.
 */
public class Config {
    //URL to our login.php file
    public static String LOGIN_URL = null;
    public static String MISSED_URL = null;
    public static String SMS_URL = null;

    //Keys for email and password as defined in our $_POST['key'] in login.php
    public static final String KEY_USER = "email";
    public static final String KEY_PASSWORD = "password";

    //If server response is equal to this that means login is successful
    public static final String LOGIN_SUCCESS = "success";

    //This would be the name of our shared preferences
    public static final String SHARED_PREF_NAME = "myloginapp";

    //This would be used to store the email of current logged in user
    public static final String EMAIL_SHARED_PREF = "email";

    public static String UserId = null;
    public static String UserName = null;
    public static String Pass = null;
    public static String Success = null;
    public static String imei = null;

    //We will use this to store the boolean in sharedpreference to track user is loggedin or not
    public static final String LOGGEDIN_SHARED_PREF = "loggedin";
    public static final String LOGGEDIN_SHARED_User_Name = "username";
    public static final String LOGGEDIN_SHARED_User_ID = "userid";
}
