package ph.gov.davaodelnorte.hris;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
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
import helper.Menu;
import helper.SwipeListAdapter;

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

    @Override
    protected void onStart() {
        super.onStart();
        swipeRefreshLayout.setOnRefreshListener(this);
        fetchMenus(user.get(SessionManager.KEY_EIC));
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
                session.logoutUser();
                // start service
                stopService(new Intent(getBaseContext(),HRISService.class));

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
                                // clear the list
                                menuList.clear();
                                // looping through json and adding to movies list
                                for (int i = 0; i < response.length(); i++) {
                                    try {
                                        JSONObject menuObj = response.getJSONObject(i);

                                        int id = menuObj.getInt("Id");
                                        String title = menuObj.getString("Title");
                                        String iconUrl = menuObj.getString("IconUrl");
                                        int totalApplications = menuObj.getInt("TotalApplications");

                                        Menu m = new Menu(id, title, iconUrl, totalApplications);

                                        menuList.add(0, m);
                                    } catch (JSONException e) {
                                        Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
                            // stopping swipe refresh
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Server Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    // stopping swipe refresh
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
            );
            // Adding request to request queue
            MyApplication.getInstance().addToRequestQueue(req);
        } catch (Exception ex) {
            Log.e(TAG, "ERROR: " + ex.getMessage());
        }
    }
}
