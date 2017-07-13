package ph.gov.davaodelnorte.hris;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.MyApplication;
import helper.AlarmReceiver;
import helper.Badge;
import helper.Menu;
import helper.SwipeListAdapter;
import me.leolin.shortcutbadger.ShortcutBadgeException;
import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {
    private final String TAG = MainActivity.class.getSimpleName();
    private final String URL = "WebService/Toolbox/GetAllApplications?approvingEIC=";

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private SwipeListAdapter adapter;
    private List<Menu> menuList;

    // Session Manager Class
    private SessionManager session;
    private HashMap<String, String> user;

    private PendingIntent pendingIntent;
    private AlarmManager manager;

    private int total_applications;

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("DavNor HRIS")
                .setMessage("Do you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes, exit now!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getApplicationContext(), "HRIS works for you.....Thanks!", Toast.LENGTH_SHORT).show();
                        finishAffinity();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        swipeRefreshLayout.setOnRefreshListener(this);
        fetchMenus(user.get(SessionManager.KEY_EIC));

        if(session.isLoggedIn()) {
            // make a broadcast of the alarm intent
            Intent alarmIntent = new Intent(this, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

            startAlarm(getCurrentFocus());
        }
    }

    public void startAlarm(View view) {
        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        int num_of_hours = 1;
//        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5000, pendingIntent);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR * num_of_hours, pendingIntent);
    }

    public void cancelAlarm(View view) {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // Session class instance
            session = new SessionManager(getApplicationContext());
            session.checkLogin();

            user = session.getUserDetails();

            Log.d(TAG, "user=" + user.toString());

            listView = (ListView) findViewById(R.id.lvJustifications);
            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

            menuList = new ArrayList<>();
            adapter = new SwipeListAdapter(this, menuList);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);

        } catch (Exception ex) {
            Log.e(TAG, "ERROR: " + ex.getMessage());
        }

    }
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:

                try {

                    // clear badge
                    ShortcutBadger.setBadge(getApplicationContext(), 0);

                    /*Context context = getApplicationContext();
                    if (Badge.isBadgingSupported(context)) {
                        Badge badge = Badge.getBadge(context);
                        if (badge != null) {
                            Log.d(TAG, "Badge : " + badge.toString());
                            badge.mBadgeCount = 0;
                            badge.update(context);
                        } else {
                            // Nothing to do as this means you don't have a badge record with the BadgeProvider
                            // Thus you shouldn't even have a badge count on your icon
                        }
                    }*/
                } catch (ShortcutBadgeException e) {
                    e.printStackTrace();
                }

                stopService(getIntent());

                // set off the alarm
                cancelAlarm(getCurrentFocus());

                session.logoutUser();

                return true;
            case R.id.refresh:
                onRefresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onRefresh() {
        fetchMenus(user.get(SessionManager.KEY_EIC));
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            // get the object being selected
            Object r = parent.getItemAtPosition(position);
            Menu m = (Menu) r;
            if(m.getTotalApplications() > 0) {
                switch (m.getTitle()) {
                    case "PASS SLIP":
                        Intent intentPassSlip = new Intent(this, PassSlipActivity.class);
                        startActivity(intentPassSlip);
                        break;
                    case "PTLOS":
                        Intent intentPTLOS = new Intent(this, PTLOSActivity.class);
                        startActivity(intentPTLOS);
                        break;
                    case "JUSTIFICATION":
                        Intent intentJustification = new Intent(this, JustificationActivity.class);
                        startActivity(intentJustification);
                        break;
                    case "REVERT - JUSTIFICATION":
                        Intent intentJustificationRevert = new Intent(this, JustificationRevertActivity.class);
                        startActivity(intentJustificationRevert);
                        break;
                    case "RETURN - DTR":
                        Intent intentDTRReturn = new Intent(this, DTRReturnActivity.class);
                        startActivity(intentDTRReturn);
                        break;
                }
            } else {
                Toast.makeText(getApplicationContext(), "Zero application!", Toast.LENGTH_LONG).show();
            }
        } catch(Exception ex) {
            Log.e(TAG, "Error: " + ex.getMessage());
        }
    }
    /**
     * Fetching movies json by making http call
     */
    private void fetchMenus(String approvingEIC) {
        try {

            // showing refresh animation before making http call
            swipeRefreshLayout.setRefreshing(true);
            // appending to url
            String url = user.get(SessionManager.KEY_DOMAIN) + URL + approvingEIC;
            // Volley's json array request object
            JsonArrayRequest req = new JsonArrayRequest(url,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {

                            Log.d(TAG, "response=" + response);

                            if (response.length() > 0) {

                                // initial applications before request response
                                total_applications = 0;

                                // clear the list
                                menuList.clear();
                                // looping through json
                                for (int i = 0; i < response.length(); i++) {
                                    try {
                                        JSONObject menuObj = response.getJSONObject(i);

                                        int id = menuObj.getInt("Id");
                                        String title = menuObj.getString("Title");
                                        String iconUrl = menuObj.getString("IconUrl");
                                        int applications = menuObj.getInt("TotalApplications");

                                        // update total applications
                                        total_applications += applications;

                                        Menu m = new Menu(id, title, iconUrl, applications);

                                        menuList.add(0, m);
                                    } catch (JSONException e) {
                                        Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
                            // stopping swipe refresh
                            swipeRefreshLayout.setRefreshing(false);

                            // store up total applications
                            session.setNotificationCount(total_applications);

//                            startService(new Intent(getBaseContext(),HRISService.class));

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Server Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(), "Server connection failed. Kindly check your internet connection.", Toast.LENGTH_LONG).show();
                    // stopping swipe refresh
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
            );

            // Adding request to request queue
            MyApplication.getInstance().addToRequestQueue(req);
        } catch (Exception ex) {
            Log.e(TAG, "ERROR: " + ex.getMessage());
        } finally {

        }
    }
}
