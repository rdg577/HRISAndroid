package ph.gov.davaodelnorte.hris;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.MyApplication;
import helper.Badge;
import helper.Menu;

/**
 * Created by PADAC on 27/04/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class HRISService extends Service {

    private final String URL = "WebService/Toolbox/GetAllApplications?approvingEIC=";

    // Session Manager Class
    private SessionManager session;
    private HashMap<String, String> user;

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Session class instance
        session = new SessionManager(getApplicationContext());

        if(session.isLoggedIn()) {
            user = session.getUserDetails();

            //Creating new thread for my service
            //Always write your long running tasks in a separate thread, to avoid ANR
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String url = user.get(SessionManager.KEY_DOMAIN) + URL + user.get(SessionManager.KEY_EIC);
                    Log.d(getClass().getName(), url);

                    // Volley's json array request object
                    JsonArrayRequest req = new JsonArrayRequest(url,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {

                                    // initial applications before request response
                                    int total_applications = 0;

                                    if (response.length() > 0) {

                                        // looping through json
                                        for (int i = 0; i < response.length(); i++) {

                                            try {
                                                JSONObject menuObj = response.getJSONObject(i);

                                                // update total applications
                                                total_applications += menuObj.getInt("TotalApplications");

                                            } catch (Exception e) {
                                                Log.e(getClass().getName(), "JSON Parsing error: " + e.getMessage());
                                            }

                                        }
                                    }

                                    // store up total applications
                                    session.setNotificationCount(total_applications);

                                    if(session.getNotificationCount() > 0) {
                                        // display badge notification
                                        showBadge();
                                    } else {
                                        // clear badge
                                        clearBadge();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(getClass().getName(), "Server Error: " + error.getMessage());
                                }
                            }
                    );
                    // Adding request to request queue
                    MyApplication.getInstance().addToRequestQueue(req);
                }
            }).start();


        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showBadge() {

        Context context = getApplicationContext();
        if (Badge.isBadgingSupported(context)) {
            Badge badge = new Badge();
            badge.mPackage = context.getPackageName();
            badge.mClass = context.getPackageName().concat(".MainActivity"); // This should point to Activity declared as android.intent.action.MAIN
            badge.mBadgeCount = session.getNotificationCount();
            badge.save(context);
        }

    }

    private void clearBadge() {

        Context context = getApplicationContext();
        if (Badge.isBadgingSupported(context)) {
            Badge badge = Badge.getBadge(context);
            if (badge != null) {
                badge.mBadgeCount = 0;
                badge.update(context);
            } else {
                // Nothing to do as this means you don't have a badge record with the BadgeProvider
                // Thus you shouldn't even have a badge count on your icon
            }
        }

    }
}
