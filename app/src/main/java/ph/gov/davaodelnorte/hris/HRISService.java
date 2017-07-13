package ph.gov.davaodelnorte.hris;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
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
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by PADAC on 27/04/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class HRISService extends Service {

    private final String URL = "WebService/Toolbox/GetAllApplications?approvingEIC=";

    // Session Manager Class
    private SessionManager session;
    private HashMap<String, String> user;
    int smallIcon, largeIcon;


    @Override
    public void onCreate() {
        smallIcon = R.mipmap.ic_launcher_round;
        largeIcon = R.mipmap.ic_launcher;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            // monitor if there is a network connection before contacting the server
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
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
//                        Log.d(getClass().getName(), url);

                            // Volley's json array request object
                            JsonArrayRequest req = new JsonArrayRequest(url,
                                    new Response.Listener<JSONArray>() {
                                        @Override
                                        public void onResponse(JSONArray response) {
//                                        Log.d(getClass().getName(), response.toString());

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

                                                // display notification
                                                displayNotification(session.getNotificationCount());
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
            }

        } catch (Exception e) {
            Log.e(getClass().getName(), "Error : " + e.getMessage());
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
        try {
            ShortcutBadger.setBadge(getApplicationContext(), session.getNotificationCount());
        } catch (Exception e) {
            Log.e(getClass().getName(), "Error: " + e.getMessage());
        }

    }

    private void clearBadge() {
        try {
            ShortcutBadger.setBadge(getApplicationContext(), 0);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Error: " + e.getMessage());
        }

    }

    private void displayNotification(int totalApplications) {
        try {
            String msg = "You have " + String.valueOf(totalApplications) + " total applications to approve.\n\nHRIS works for you!";

            Log.d(getClass().getName(), "displayNotification: " + msg);

            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
            bigText.bigText(msg);
            bigText.setBigContentTitle("DavNor HRIS");
            bigText.setSummaryText("Tap to view applications");

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
            builder.setSmallIcon(smallIcon)
                    .setColor(Color.argb(255,76,175,80))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), largeIcon))
                    .setStyle(bigText)
                    .setContentTitle("DavNor HRIS")
                    .setContentText(msg)
                    .setSubText("Tap to view applications")
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_MAX);

            Notification notification = builder.build();
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_VIBRATE;

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(001, notification);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Error: " + e.getMessage());
        }
    }
}
