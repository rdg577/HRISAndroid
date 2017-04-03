package ph.gov.davaodelnorte.hris;

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

public class JustificationPerMonthActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private String TAG = JustificationPerMonthActivity.class.getSimpleName();
    private ListView lv;

    final String url = "WebService/Toolbox/JustificationPerMonth";

    // Session Manager Class
    SessionManager session;
    HashMap<String, String> user;

    private static String EIC, approvingEIC, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_justification_per_month);
        try {
            session = new SessionManager(getApplicationContext());
            session.checkLogin();
            user = session.getUserDetails();
            approvingEIC = user.get(SessionManager.KEY_EIC);

            lv = (ListView) findViewById(R.id.listJustificationPerMonth);
            lv.setOnItemClickListener(this);

            // get selected EIC
            EIC = getIntent().getExtras().getString("EIC");
            name = getIntent().getExtras().getString("name");
            getSupportActionBar().setTitle(name);
            Log.d(TAG, "onCreate name = " + name);

            fetchJustificationPerMonth(this.EIC, this.approvingEIC);

        } catch (Exception ex) {
            Log.e(TAG, "onCreate: " + ex.getMessage());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            // get the object being selected
            Object r = parent.getItemAtPosition(position);
            HashMap<String, String> item = (HashMap<String, String>) r;

            // forward the recNo to the next activity
            Intent i = new Intent(this, JustificationDetailActivity.class);
            i.putExtra("EIC", this.EIC);
            i.putExtra("month", Integer.valueOf(item.get("month")));
            i.putExtra("year", Integer.valueOf(item.get("year")));
            i.putExtra("month_year", item.get("month_year"));
            i.putExtra("period", Integer.valueOf(item.get("period")));
            startActivity(i);
        } catch(Exception ex) {
            Log.e(TAG, "Error: " + ex.getMessage());
        }
    }

    private void fetchJustificationPerMonth(String EIC, String approvingEIC) {

        try {
            Map<String, String> params = new HashMap<>();
            params.put("EIC", EIC);
            params.put("approvingEIC", approvingEIC);
            JSONObject parameters = new JSONObject(params);

            // appending to url
            String url = user.get(SessionManager.KEY_DOMAIN) + this.url;
//            Log.d(TAG, "fetchDetail Url: " + url);

            // Volley's json array request object
            JsonObjectRequest req = new JsonObjectRequest(url, parameters,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
//                            Log.d(TAG, "Response: " + response);
                            try {

                                // for justifications
                                ArrayList<HashMap<String, String>> _list = new ArrayList<>();

                                JSONArray items = response.getJSONArray("justifications");
                                for(int i = 0; i < items.length(); i++) {

                                    JSONObject j = items.getJSONObject(i);

                                    HashMap<String, String> entry = new HashMap<>();
                                    entry.put("total", String.valueOf(j.getInt("total")));
                                    entry.put("month_year", j.getString("month_year"));
                                    entry.put("month", j.getString("month"));
                                    entry.put("year", j.getString("year"));
                                    entry.put("period", String.valueOf(j.getInt("period")));
                                    entry.put("period_label", j.getInt("period") == 0 ? "Full Month" : j.getInt("period") == 1 ? "1st Half": "2nd Half" );

                                    _list.add(entry);

                                }

                                ListAdapter adapter = new SimpleAdapter(
                                        JustificationPerMonthActivity.this
                                        ,_list
                                        ,R.layout.list_item_justification_per_month
                                        ,new String[]{"total", "month_year", "month", "year", "period", "period_label"}
                                        ,new int[]{R.id.total, R.id.month_year, R.id.month, R.id.year, R.id.period, R.id.period_label}
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
            Log.e(TAG, "fetchJustificationPerMonth ERROR: " + ex.getMessage());
        }

    }
}
