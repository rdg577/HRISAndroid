package ph.gov.davaodelnorte.hris;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.MyApplication;

public class PassSlipApplicationDetailActivity extends AppCompatActivity {
    private final String TAG = PassSlipApplicationDetailActivity.class.getSimpleName();
    private ProgressDialog pDialog;

    // URL to get JSON
    private final String urlPassSlipDetail = "WebService/Toolbox/PassSlipDetail";
    private final String urlPassSlipApproval = "WebService/Toolbox/PassSlipApproval";

    private PassSlip passSlip;

    // Session Manager Class
    private SessionManager session;
    private HashMap<String, String> user;

    private int recNo;

    // fields
    private TextView name;
    private TextView destination;
    private TextView purpose;
    private TextView time_out;
    private RadioButton isOfficial;
    private RadioButton isPersonal;
    private Button btnApprove;
    private Button btnDisapprove;
    private Button btnCancel;

    private ProgressDialog progressDialog;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, PassSlipActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_slip_application_detail);

        // Session class instance
        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        user = session.getUserDetails();

        passSlip = new PassSlip();

        try {
            // fields
            name = (TextView) findViewById(R.id.name);
            destination=(TextView) findViewById(R.id.total_application);
            purpose = (TextView) findViewById(R.id.reason);
            time_out = (TextView) findViewById(R.id.time_out);
            isOfficial = (RadioButton) findViewById(R.id.isOfficial);
            isPersonal = (RadioButton) findViewById(R.id.isPersonal);

            btnApprove = (Button) findViewById(R.id.btnRevert);
            btnApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int status = 1;
                    passSlip.set_isOfficial(isOfficial.isChecked()? 1: 2);
                    getPassSlipApproval(passSlip.get_recNo(), status, passSlip.get_isOfficial());
                }
            });
            btnDisapprove = (Button) findViewById(R.id.btnDisapprove);
            btnDisapprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int status = 2;
                    passSlip.set_isOfficial(isOfficial.isChecked()? 1: 2);
                    getPassSlipApproval(passSlip.get_recNo(), status, passSlip.get_isOfficial());
                }
            });
            btnCancel = (Button) findViewById(R.id.btnCancel);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int status = 3;
                    passSlip.set_isOfficial(isOfficial.isChecked()? 1: 2);
                    getPassSlipApproval(passSlip.get_recNo(), status, passSlip.get_isOfficial());
                }
            });

            recNo = (int) getIntent().getExtras().getInt("recNo");
            Log.d(TAG, "recNo = " + recNo);

            new GetPassSlipDetail(user.get(SessionManager.KEY_DOMAIN) + urlPassSlipDetail + "?id=" + recNo).execute();

        } catch (Exception ex) {
            Log.e(TAG, "onCreate Error: " + ex.getMessage());
        }

    }
    private void getPassSlipApproval(int id, int statusId, int isOfficial) {

        try {
            // appending to url
            String url = user.get(SessionManager.KEY_DOMAIN) + urlPassSlipApproval;
            Log.d(TAG, "url = " + url);

            Map<String, String> params = new HashMap<>();
            params.put("id", String.valueOf(id));
            params.put("statusId", String.valueOf(statusId));
            params.put("isOfficial", String.valueOf(isOfficial));
            JSONObject parameters = new JSONObject(params);

            // Volley's json array request object
            JsonObjectRequest req = new JsonObjectRequest(url, parameters,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progressDialog.dismiss();
                            //Log.d(TAG, "Response: " + response);
                            try {
                                JSONObject j = response.getJSONObject("pass_slip_approval");

                                Intent i = new Intent(getApplicationContext(), PassSlipActivity.class);
                                startActivity(i);

                                if(Integer.parseInt(j.getString("statusId")) == 1) {
                                    Toast.makeText(getApplicationContext(),
                                            "Pass Slip Approved!",
                                            Toast.LENGTH_LONG)
                                            .show();
                                }else if(Integer.parseInt(j.getString("statusId")) == 2) {
                                    Toast.makeText(getApplicationContext(),
                                            "Pass Slip Disapproved!",
                                            Toast.LENGTH_LONG)
                                            .show();
                                }else if(Integer.parseInt(j.getString("statusId")) == 3) {
                                    Toast.makeText(getApplicationContext(),
                                            "Pass Slip Cancelled!",
                                            Toast.LENGTH_LONG)
                                            .show();
                                }
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

            progressDialog = new ProgressDialog(PassSlipApplicationDetailActivity.this);
            progressDialog.setMessage("Requesting data from server ....");
            progressDialog.show();
        } catch (Exception ex) {
            Log.e(TAG, "getPassSlipApproval ERROR: " + ex.getMessage());
        }

    }
    private class GetPassSlipDetail extends AsyncTask<Void, Void, Void> {

        private final String _url;

        public GetPassSlipDetail(String _url) {
            this._url = _url;
        }
        @Override
        protected void onPreExecute() {
            try{
                super.onPreExecute();
                // Showing progress dialog
                pDialog = new ProgressDialog(PassSlipApplicationDetailActivity.this);
                pDialog.setMessage("Requesting data from server ....");
                pDialog.setCancelable(false);
                pDialog.show();
            }catch (Exception ex) {
                Log.e(TAG, "Error: " + ex.getMessage());
            }
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(this._url);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("pass_slip_detail");

                    // looping through all items
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject c = items.getJSONObject(i);

                        passSlip.set_recNo(c.getInt("recNo"));
                        passSlip.set_fullname(c.getString("fullnameFirst"));
                        passSlip.set_destination(c.getString("destination"));
                        passSlip.set_purpose(c.getString("purpose"));
                        passSlip.set_isOfficial(c.getInt("isOfficial"));
                        passSlip.set_timeOut(c.getString("timeOut"));

                    }
                } catch (final JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            try
            {
                name.setText(passSlip.get_fullname());
                destination.setText(passSlip.get_destination());
                purpose.setText(passSlip.get_purpose());
                time_out.setText(passSlip.get_timeOut());
                if(passSlip.get_isOfficial()==2) {
                    isOfficial.setChecked(true);
                } else if(passSlip.get_isOfficial()==1) {
                    isPersonal.setChecked(true);
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error: " + ex.getMessage());
            }
        }
    }
}
