package ph.gov.davaodelnorte.hris;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.MyApplication;
import helper.Menu;

public class PTLOSActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private String TAG = PTLOSActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;

    // URL to get JSON
    final String url = "hris/Toolbox/GetPTLOSApplications?approvingEIC=";

    ArrayList<HashMap<String, String>> _list;

    // Session Manager Class
    SessionManager session;
    HashMap<String, String> user;

    private static String approvingEIC;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ptlos);
        // Session class instance
        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        // get user data from session
        user = session.getUserDetails();
        // approvingEIC
        approvingEIC = user.get(SessionManager.KEY_EIC);

        _list = new ArrayList<>();
        lv = (ListView) findViewById(R.id.listPTLOS);
        lv.setOnItemClickListener(this);

        fetchPTLOSApplications(approvingEIC);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            // get the object being selected
            Object r = parent.getItemAtPosition(position);
            HashMap<String, String> item = (HashMap<String, String>) r;
            // extract the record number of the pass slip application
            int recNo = Integer.parseInt(item.get("recNo"));
            // forward the recNo to the next activity
            Intent i = new Intent(this, PTLOSApplicationDetailActivity.class);
            i.putExtra("recNo", recNo);
            startActivity(i);
        } catch(Exception ex) {
            Log.e(TAG, "Error: " + ex.getMessage());
        }
    }

    private void fetchPTLOSApplications(String approvingEIC) {
        try {
            // appending to url
            String url = user.get(SessionManager.KEY_DOMAIN) + this.url + approvingEIC;
            // Volley's json array request object
            JsonObjectRequest req = new JsonObjectRequest(url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray items = response.getJSONArray("ptlos_applications");
                                for (int i = 0; i < items.length(); i++) {
                                    JSONObject j = items.getJSONObject(i);

                                    HashMap<String, String> entry = new HashMap<>();
                                    entry.put("name", j.getString("name"));
                                    entry.put("destination",j.getString("destination"));
                                    entry.put("date_applied", "Applied: " + j.getString("date_applied"));
                                    entry.put("recNo", j.getString("recNo"));

                                    _list.add(entry);
                                }

                                ListAdapter adapter = new SimpleAdapter(
                                        PTLOSActivity.this
                                        ,_list
                                        ,R.layout.list_item_ptlos
                                        ,new String[]{"name", "destination", "date_applied", "recNo"}
                                        ,new int[]{R.id.name, R.id.destination, R.id.date_applied, R.id.recNo}
                                );

                                lv.setAdapter(adapter);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Response error: " + error.getMessage());
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
