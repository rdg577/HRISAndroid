package ph.gov.davaodelnorte.hris;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class PassSlipActivity extends AppCompatActivity {

    private String TAG = PassSlipActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;

    // URL to get JSON
    private static String url = "http://172.16.120.220/hris/Toolbox/PassSlipsPending?approvingEIC=";

    ArrayList<HashMap<String, String>> _list;

    // Session Manager Class
    SessionManager session;

    public PassSlipActivity() {

    }

    private static String approvingEIC;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Try destroying.", Toast.LENGTH_LONG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_slip);

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

        // name
        String name = user.get(SessionManager.KEY_NAME);
        TextView tvApprovingOfficer = (TextView) findViewById(R.id.tvApprovingOfficer);
        tvApprovingOfficer.setText(Html.fromHtml("Approving Officer: <b>" + name.toString() + "</b>"));

        // approvingEIC
        approvingEIC = user.get(SessionManager.KEY_EIC);

        _list = new ArrayList<>();
        lv = (ListView) findViewById(R.id.listPassSlips);

        new GetPassSlipApplications().execute();

    }

    // ****************************************************************
    //  Downloading and Parsing the JSON
    // ****************************************************************
    /**
     * Async task class to get json by making HTTP call
     */
    private class GetPassSlipApplications extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(PassSlipActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String UrlWithEIC = url + approvingEIC;
            String jsonStr = sh.makeServiceCall(UrlWithEIC);

            /*Log.e(TAG, "URL: " + UrlWithEIC);
            Log.e(TAG, "Response from url: " + jsonStr);*/

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("pass_slips");

                    // looping through all items
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject c = items.getJSONObject(i);

                        String eic = c.getString("EIC");
                        String name = c.getString("fullnameFirst");
                        String time_out = c.getString("timeOut");
                        // convert timeOut to type Date
                        /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date dateTimeOut = sdf.parse(strTimeOut);*/

                        // tmp hash map for single entry
                        HashMap<String, String> entry = new HashMap<>();

                        // adding each child node to HashMap key => value
                        entry.put("name", name);
                        entry.put("eic", eic);
                        entry.put("time_out", time_out);

                        // adding entry to entry list
                        _list.add(entry);
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
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    PassSlipActivity.this
                    ,_list
                    ,R.layout.list_item_pass_slips
                    ,new String[]{"name", "eic", "time_out"}
                    ,new int[]{R.id.name, R.id.eic, R.id.time_out}
            );

            lv.setAdapter(adapter);
        }

    }

}
