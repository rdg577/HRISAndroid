package ph.gov.davaodelnorte.hris;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
/**
 * Created by Reden Gallera on 09/03/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class SessionManager {
    // Shared Preferences
    private final SharedPreferences pref;

    // Editor for Shared preferences
    private final Editor editor;

    // Context
    private final Context _context;

    // Shared pref mode
    private final int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "HRISPreferences";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_EIC = "EIC";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "name";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    // Domain (make variable public to access from outside)
    public static final String KEY_DOMAIN = "domain";

    public static final String KEY_NOTIFICATION_COUNT = "notification_count";

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(String eic, String name, String domain){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing EIC in pref
        editor.putString(KEY_EIC, eic);

        // Storing name in pref
        editor.putString(KEY_NAME, name);

        // Storing email in pref
        // editor.putString(KEY_EMAIL, email);

        // Storing domain in pref
        editor.putString(KEY_DOMAIN, domain);

        // commit changes
        editor.commit();
    }

    public void setNotificationCount(int total_notification) {
        // Storing total notification
        editor.putInt(KEY_NOTIFICATION_COUNT, total_notification);
        // commit changes
        editor.commit();
    }

    public int getNotificationCount() {
        return pref.getInt(KEY_NOTIFICATION_COUNT, 0);
    }


    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<>();

        // user name
        user.put(KEY_EIC, pref.getString(KEY_EIC, null));

        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        // user email id
        // user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // domain
        user.put(KEY_DOMAIN, pref.getString(KEY_DOMAIN, null));

        // return user
        return user;
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}
