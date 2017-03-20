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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class PassSlipApplicationDetailActivity extends AppCompatActivity {
    private String TAG = PassSlipApplicationDetailActivity.class.getSimpleName();
    private ProgressDialog pDialog;

    // URL to get JSON
    final String urlPassSlipDetail = "hris/Toolbox/PassSlipDetail?id=";
    private static String urlPassSlipApproval = "hris/Toolbox/PassSlipApproval?";

    /*ArrayList<HashMap<String, String>> _list;*/

    PassSlip passSlip;

    // Session Manager Class
    SessionManager session;

    int recNo;

    // fields
    TextView name, destination, purpose, time_out;
    RadioButton isOfficial, isPersonal;
    Button btnApprove, btnDisapprove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_slip_application_detail);

        // Session class instance
        session = new SessionManager(getApplicationContext());

        /**
         * Call this function whenever you want to check user login
         * This will redirect user to LoginActivity is he is not
         * logged in
         * */
        session.checkLogin();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        // domain
        urlPassSlipApproval = user.get(SessionManager.KEY_DOMAIN) + urlPassSlipApproval;
        // urlPassSlipDetail = user.get(SessionManager.KEY_DOMAIN) + urlPassSlipDetail;

        passSlip = new PassSlip();

        try {
            // fields
            name = (TextView) findViewById(R.id.name);
            destination=(TextView) findViewById(R.id.destination);
            purpose = (TextView) findViewById(R.id.purpose);
            time_out = (TextView) findViewById(R.id.date_applied);
            isOfficial = (RadioButton) findViewById(R.id.isOfficial);
            isPersonal = (RadioButton) findViewById(R.id.isPersonal);

            btnApprove = (Button) findViewById(R.id.buttonApprove);
            btnApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int approve = 1;
                    passSlip.set_isOfficial(isOfficial.isChecked()? 1: 2);
                    String u = urlPassSlipApproval + "id=" + passSlip.get_recNo() + "&statusId=" + approve + "&isOfficial=" + passSlip.get_isOfficial();
                    new GetPassSlipApproval(u).execute();
                }
            });
            btnDisapprove = (Button) findViewById(R.id.buttonDisapprove);
            btnDisapprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int approve = 2;
                    passSlip.set_isOfficial(isOfficial.isChecked()? 1: 2);
                    String u = urlPassSlipApproval + "id=" + passSlip.get_recNo() + "&statusId=" + approve + "&isOfficial=" + passSlip.get_isOfficial();
                    new GetPassSlipApproval(u).execute();
                }
            });

            recNo = (int) getIntent().getExtras().getInt("recNo");
            Log.d(TAG, "recNo = " + recNo);

            new GetPassSlipDetail(user.get(SessionManager.KEY_DOMAIN) + urlPassSlipDetail).execute();

        } catch (Exception ex) {
            Log.e(TAG, "onCreate Error: " + ex.getMessage());
        }

    }

    // ****************************************************************
    //  Downloading and Parsing the JSON
    // ****************************************************************
    /**
     * Async task class to get json by making HTTP call
     */
    private class GetPassSlipDetail extends AsyncTask<Void, Void, Void> {

        private String _url;

        public GetPassSlipDetail(String _url) {
            this._url = _url;
        }

        @Override
        protected void onPreExecute() {
            try{
                super.onPreExecute();
                // Showing progress dialog
                pDialog = new ProgressDialog(PassSlipApplicationDetailActivity.this);
                pDialog.setMessage("Please wait...");
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
            this._url = this._url + recNo;
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
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
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
                Log.e(TAG, "Couldn't get json from server.");
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
            /**
             * Updating parsed JSON data
             * */
            try
            {
                name.setText(passSlip.get_fullname());
                destination.setText(passSlip.get_destination());
                purpose.setText(passSlip.get_purpose());
                time_out.setText(passSlip.get_timeOut().toString());
                if(passSlip.get_isOfficial()==1) {
                    isOfficial.setChecked(true);
                } else {
                    isPersonal.setChecked(true);
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error: " + ex.getMessage());
            }
        }

    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetPassSlipApproval extends AsyncTask<Void, Void, Void> {

        private String _url;
        private int approvalResult;

        public GetPassSlipApproval(String _url) {
            this._url = _url;
        }

        @Override
        protected void onPreExecute() {
            try{
                super.onPreExecute();
                // Showing progress dialog
                pDialog = new ProgressDialog(PassSlipApplicationDetailActivity.this);
                pDialog.setMessage("Please wait...");
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
                    JSONObject items = jsonObj.getJSONObject("pass_slip_approval");

                    this.approvalResult = Integer.parseInt(items.getString("statusId"));

                    Log.d(TAG, "Approval result: " + items.toString());

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
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
                Log.e(TAG, "Couldn't get json from server.");
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
                Intent i = new Intent(getApplicationContext(), PassSlipActivity.class);
                startActivity(i);

                if(this.approvalResult==1) {
                    Toast.makeText(getApplicationContext(),
                            "Pass Slip Approved!",
                            Toast.LENGTH_LONG)
                            .show();
                }

                if(this.approvalResult==2) {
                    Toast.makeText(getApplicationContext(),
                            "Pass Slip Disapproved!",
                            Toast.LENGTH_LONG)
                            .show();
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error: " + ex.getMessage());
            }
        }

    }
}
