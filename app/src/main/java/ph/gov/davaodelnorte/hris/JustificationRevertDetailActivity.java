package ph.gov.davaodelnorte.hris;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

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

public class JustificationRevertDetailActivity extends AppCompatActivity {

    private final String TAG = JustificationRevertDetailActivity.class.getSimpleName();
    private ProgressDialog pDialog;

    // URL to get JSON
    private final String urlDetail = "WebService/Toolbox/JustificationRevertDetail";
    private String urlApproval = "WebService/Toolbox/JustificationApproval";

    // Session Manager Class
    private SessionManager session;
    private HashMap<String, String> user;

    private String EIC;
    private String approvingEIC;
    private String month_year;
    private String remarks = null;
    private int month;
    private int year;
    private int period;
    private int statusID;

    // fields
    private TextView name;
    private Button btnRevert;
    private ListView lvJustifications;
    private EditText input;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_justification_revert_detail);

        try{
            // Session class instance
            session = new SessionManager(getApplicationContext());
            session.checkLogin();
            // get user data from session
            user = session.getUserDetails();
            approvingEIC = user.get(SessionManager.KEY_EIC);

            this.urlApproval = user.get(SessionManager.KEY_DOMAIN) + this.urlApproval;

            // fields
            name = (TextView) findViewById(R.id.name);
            lvJustifications = (ListView) findViewById(R.id.lvJustifications);

            btnRevert = (Button) findViewById(R.id.btnRevert);
            btnRevert.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            revertJustification();
                        }
                    }
            );

            EIC = getIntent().getExtras().getString("EIC");
            month = getIntent().getExtras().getInt("month");
            year = getIntent().getExtras().getInt("year");
            month_year = getIntent().getExtras().getString("month_year");
            period = getIntent().getExtras().getInt("period");

            fetchJustificationDetail(EIC, approvingEIC, month, year, period, user.get(SessionManager.KEY_DOMAIN) + this.urlDetail);

        } catch (Exception ex) {
            Log.e(TAG, "onCreate Error: " + ex.getMessage());
        }
    }

    private void revertJustification() {
        input = new EditText(getApplicationContext());
        input.setTextColor(Color.parseColor("#ff0000aa"));
        AlertDialog.Builder builder = new AlertDialog.Builder(JustificationRevertDetailActivity.this);
        builder.setTitle("Revert Justification")
                .setMessage("Please enter remark.")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        remarks = input.getText().toString();
                        statusID = 0; // 0 - disapprove
                        JustificationApproval(EIC, month, year, month_year, approvingEIC, statusID, remarks, period, urlApproval);
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

    private void JustificationApproval(String EIC, int month, int year, String month_year, String approvingEIC, final int statusID, String remarks, int period, String url) {

        try {
            Map<String, String> params = new HashMap<>();
            params.put("EIC", EIC);
            params.put("month", String.valueOf(month));
            params.put("year", String.valueOf(year));
            params.put("month_year", month_year);
            params.put("approveEIC", approvingEIC);
            params.put("statusID", String.valueOf(statusID));
            params.put("remarks", remarks);
            params.put("period", String.valueOf(period));
            JSONObject parameters = new JSONObject(params);
            Log.d(TAG, "parameters = " + parameters.toString());

            // appending to url
            //String url = user.get(SessionManager.KEY_DOMAIN) + this.urlApproval;

            // Volley's json array request object
            JsonObjectRequest req = new JsonObjectRequest(url, parameters,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progressDialog.dismiss();

                            try {
                                JSONObject j = response.getJSONObject("justification_approval");
                                if(!j.getBoolean("has_error")) {
                                    if(statusID==1)
                                        Toast.makeText(getApplicationContext(), "Justification Approved!",Toast.LENGTH_LONG).show();
                                    else
                                        Toast.makeText(getApplicationContext(), "Justification has been reverted!",Toast.LENGTH_LONG).show();
                                }
                                Intent i = new Intent(getApplicationContext(), JustificationRevertActivity.class);
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

            progressDialog = new ProgressDialog(JustificationRevertDetailActivity.this);
            progressDialog.setMessage("Requesting data from server ....");
            progressDialog.show();
        } catch (Exception ex) {
            Log.e(TAG, "fetchJustificationApproval Error: " + ex.getMessage());
        }

    }

    private void fetchJustificationDetail(String EIC, String approvingEIC, int month, int year, int period, String url) {

        try {
            Map<String, String> params = new HashMap<>();
            params.put("EIC", EIC);
            params.put("approvingEIC", approvingEIC);
            params.put("month", String.valueOf(month));
            params.put("year", String.valueOf(year));
            params.put("period", String.valueOf(period));
            JSONObject parameters = new JSONObject(params);

            // Volley's json array request object
            JsonObjectRequest req = new JsonObjectRequest(url, parameters,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progressDialog.dismiss();

                            try {

                                // for justifications
                                ArrayList<HashMap<String, String>> _list = new ArrayList<>();

                                JSONArray items = response.getJSONArray("justifications");
                                for(int i = 0; i < items.length(); i++) {

                                    JSONObject j = items.getJSONObject(i);

                                    if(i==0) name.setText(j.getString("fullnameFirst"));

                                    HashMap<String, String> entry = new HashMap<>();
                                    entry.put("log", j.getInt("logType") == 8 ? "ABSENT" : j.getString("logTitle") + " " + j.getString("time"));
                                    entry.put("date", j.getString("date"));
                                    entry.put("reason", j.getString("reason"));

                                    _list.add(entry);

                                }

                                ListAdapter adapter = new SimpleAdapter(
                                        JustificationRevertDetailActivity.this
                                        ,_list
                                        ,R.layout.list_item_justification_detail
                                        ,new String[]{"date", "log", "reason"}
                                        ,new int[]{R.id.date, R.id.log, R.id.reason}
                                );

                                lvJustifications.setAdapter(adapter);
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

            progressDialog = new ProgressDialog(JustificationRevertDetailActivity.this);
            progressDialog.setMessage("Requesting data from server ....");
            progressDialog.show();
        } catch (Exception ex) {
            Log.e(TAG, "fetchJustificationDetail Error: " + ex.getMessage());
        }

    }

}
