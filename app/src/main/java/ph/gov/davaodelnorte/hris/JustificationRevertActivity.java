package ph.gov.davaodelnorte.hris;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.MyApplication;

public class JustificationRevertActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private final String TAG = JustificationRevertActivity.class.getSimpleName();

    private ProgressDialog progressDialog;
    private ListView lv;

    private final String url = "WebService/Toolbox/JustificationRevert";

    private ArrayList<HashMap<String, String>> _list;

    // Session Manager Class
    private SessionManager session;
    private HashMap<String, String> user;

    private static String approvingEIC;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_justification_revert);

        try {
            session = new SessionManager(getApplicationContext());
            session.checkLogin();
            user = session.getUserDetails();
            approvingEIC = user.get(SessionManager.KEY_EIC);

            lv = (ListView) findViewById(R.id.listJustificationRevert);
            lv.setOnItemClickListener(this);

            fetchJustificationRevert(approvingEIC, user.get(SessionManager.KEY_DOMAIN) + this.url);
        } catch (Exception ex) {
            Log.e(TAG, "onCreate Error: " + ex.getMessage());
        }

    }

    private void fetchJustificationRevert(String approvingEIC, String url) {

        try {
            Map<String, String> params = new HashMap<>();
            params.put("approvingEIC", approvingEIC);
            JSONObject parameters = new JSONObject(params);

            // Volley's json array request object
            JsonObjectRequest req = new JsonObjectRequest(url, parameters,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progressDialog.dismiss();

                            try {

                                ArrayList<HashMap<String, String>> _list = new ArrayList<>();

                                JSONArray items = response.getJSONArray("justifications");
                                for(int i = 0; i < items.length(); i++) {

                                    JSONObject j = items.getJSONObject(i);

                                    HashMap<String, String> entry = new HashMap<>();
                                    entry.put("name", j.getString("fullnameFirst"));
                                    entry.put("EIC", j.getString("EIC"));
                                    entry.put("total", "Total: " + String.valueOf(j.getInt("total")));

                                    _list.add(entry);

                                }

                                ListAdapter adapter = new SimpleAdapter(
                                        JustificationRevertActivity.this
                                        ,_list
                                        ,R.layout.list_item_justification
                                        ,new String[]{"name", "total", "EIC"}
                                        ,new int[]{R.id.name, R.id.total_justifications, R.id.EIC}
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

            progressDialog = new ProgressDialog(JustificationRevertActivity.this);
            progressDialog.setMessage("Requesting data from server ....");
            progressDialog.show();

        } catch (Exception ex) {
            Log.e(TAG, "fetchJustificationRevert Error: " + ex.getMessage());
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            // get the object being selected
            Object r = parent.getItemAtPosition(position);
            HashMap<String, String> item = (HashMap<String, String>) r;

            Intent i = new Intent(this, JustificationRevertPerMonthActivity.class);
            i.putExtra("EIC", item.get("EIC"));
            i.putExtra("name", item.get("name"));
            startActivity(i);

        } catch(Exception ex) {
            Log.e(TAG, "onItemClick Error: " + ex.getMessage());
        }
    }
}
