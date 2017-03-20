package ph.gov.davaodelnorte.hris;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import app.MyApplication;

public class PTLOSApplicationDetailActivity extends AppCompatActivity {

    private String TAG = PTLOSApplicationDetailActivity.class.getSimpleName();
    private ProgressDialog pDialog;

    // URL to get JSON
    final String urlPTLOSDetail = "hris/Toolbox/PTLOSDetail?id=";
    final String urlPTLOSApproval = "hris/Toolbox/PTLOSApproval?";

    // Session Manager Class
    SessionManager session;
    HashMap<String, String> user;

    // PTLOS record no
    int recNo;

    // fields
    TextView name, destination, purpose, date_applied, departure, arrival, official_return;
    Button btnApprove, btnDisapprove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ptlosapplication_detail);

        try{
            // Session class instance
            session = new SessionManager(getApplicationContext());
            session.checkLogin();
            // get user data from session
            user = session.getUserDetails();
            // fields
            name = (TextView) findViewById(R.id.name);
            destination = (TextView) findViewById(R.id.destination);
            purpose = (TextView) findViewById(R.id.purpose);
            date_applied = (TextView) findViewById(R.id.date_applied);
            departure = (TextView) findViewById(R.id.departure);
            arrival = (TextView) findViewById(R.id.arrival);
            official_return = (TextView) findViewById(R.id.official_return);
            btnApprove = (Button) findViewById(R.id.btnApprove);
            btnApprove.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PTLOSApproval(recNo, 5);    // 5 - approve
                        }
                    }
            );
            btnDisapprove = (Button) findViewById(R.id.btnDisapprove);
            btnDisapprove.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PTLOSApproval(recNo, 4);    // 4 - approve
                        }
                    }
            );

            recNo = (int) getIntent().getExtras().getInt("recNo");
            Log.d(TAG, "onCreate recNo = " + recNo);

            fetchPTLOSDetail(recNo);

        } catch (Exception ex) {
            Log.e(TAG, "onCreate: " + ex.getMessage());
        }
    }

    private void fetchPTLOSDetail(int id) {

        try {
            // appending to url
            String url = user.get(SessionManager.KEY_DOMAIN) + this.urlPTLOSDetail + id;
            Log.d(TAG, "fetchPTLOSDetail Url: " + url);

            // Volley's json array request object
            JsonObjectRequest req = new JsonObjectRequest(url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "Response: " + response);
                            try {
                                JSONArray items = response.getJSONArray("ptlos_detail");
                                JSONObject j = items.getJSONObject(0);      // zero(0) - only one item
                                Log.d(TAG, j.toString());
                                Log.d(TAG, "recNo = " + j.get("recNo"));
                                // display to fields
                                date_applied.setText(j.getString("date_applied"));
                                name.setText(j.getString("name"));
                                destination.setText(j.getString("destination"));
                                purpose.setText(j.getString("purpose"));
                                departure.setText(j.getString("departure"));
                                arrival.setText(j.getString("arrival"));
                                official_return.setText(j.getString("official_return"));
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
            Log.e(TAG, "fetchPTLOSDetail ERROR: " + ex.getMessage());
        }

    }

    private void PTLOSApproval(int id, int tag) {

        try {
            // appending to url
            String url = user.get(SessionManager.KEY_DOMAIN) + this.urlPTLOSApproval + "id=" + id + "&tag=" + tag;
            Log.d(TAG, "fetchPTLOSApproval Url: " + url);

            // Volley's json array request object
            JsonObjectRequest req = new JsonObjectRequest(url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "Response: " + response);
                            try {
                                JSONObject j = response.getJSONObject("ptlos_approval");
                                if(j.getInt("tag") == 4) {
                                    Toast.makeText(getApplicationContext(), "PTLOS Disapproved!",Toast.LENGTH_LONG).show();
                                }else if(j.getInt("tag") == 5) {
                                    Toast.makeText(getApplicationContext(), "PTLOS Approved!",Toast.LENGTH_LONG).show();
                                }
                                Intent i = new Intent(getApplicationContext(), PTLOSActivity.class);
                                startActivity(i);
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
            Log.e(TAG, "fetchPTLOSDetail ERROR: " + ex.getMessage());
        }

    }
}
