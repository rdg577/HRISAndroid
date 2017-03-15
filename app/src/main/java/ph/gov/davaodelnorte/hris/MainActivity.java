package ph.gov.davaodelnorte.hris;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.text.Html;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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
    private String TAG = MainActivity.class.getSimpleName();
    final String URL = "http://172.16.0.81/hris/Toolbox/GetAllApplications?approvingEIC=";

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private SwipeListAdapter adapter;
    private List<Menu> menuList;
    // Session Manager Class
    SessionManager session;
    HashMap<String, String> user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Session class instance
        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        user = session.getUserDetails();

        listView = (ListView) findViewById(R.id.listView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        menuList = new ArrayList<>();
        adapter = new SwipeListAdapter(this, menuList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        swipeRefreshLayout.setOnRefreshListener(this);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);

                                        fetchMenus(user.get(SessionManager.KEY_EIC));
                                    }
                                }
        );
        // displaying user data
        /*lblName.setText(name);
        lblEmail.setText(eic);*/
        /*
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.NOUGAT) {
            // displaying user data
            lblName.setText(Html.fromHtml("Name: <b>" + name + "</b>", Html.FROM_HTML_MODE_COMPACT));
            lblEmail.setText(Html.fromHtml("Email: <b>" + email + "</b>", Html.FROM_HTML_MODE_COMPACT));
        }
        */
        /**
         * Logout button click event
         * */
        /*btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Clear the session data
                // This will clear all session data and
                // redirect user to LoginActivity
                session.logoutUser();
            }
        });*/
    }

    @Override
    public void onRefresh() {
        fetchMenus(user.get(SessionManager.KEY_EIC));
    }

    /**
     * Fetching movies json by making http call
     */
    private void fetchMenus(String approvingEIC) {

        // showing refresh animation before making http call
        swipeRefreshLayout.setRefreshing(true);

        // appending offset to url
        String url = URL + approvingEIC;

        // Volley's json array request object
        JsonArrayRequest req = new JsonArrayRequest(url,
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Log.d(TAG, response.toString());

                    if (response.length() > 0) {
                        // clear the list
                        menuList.clear();

                        // looping through json and adding to movies list
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject menuObj = response.getJSONObject(i);

                                int rank = menuObj.getInt("Id");
                                String title = menuObj.getString("Title");
                                String iconUrl = menuObj.getString("IconUrl");
                                int totalApplications = menuObj.getInt("TotalApplications");

                                Menu m = new Menu(rank, title, iconUrl, totalApplications);

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
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            // get the object being selected
            Object r = parent.getItemAtPosition(position);
            Menu m = (Menu) r;
            switch (m.Title) {
                case "PASS SLIP":
                    Intent intent = new Intent(this, PassSlipActivity.class);
                    startActivity(intent);
                    break;
            }
            Log.d(TAG, "Menu Title: " + m.Title);
//            String menuTitle =
//            // forward the recNo to the next activity
//            Intent i = new Intent(this, PassSlipApplicationDetailActivity.class);
//            startActivity(i);
        } catch(Exception ex) {
            Log.e(TAG, "Error: " + ex.getMessage());
        }
    }
}
