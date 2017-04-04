package ph.gov.davaodelnorte.hris;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class JustificationActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private String TAG = JustificationActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;

    final String url = "WebService/Toolbox/JustificationPending?approvingEIC=";

    ArrayList<HashMap<String, String>> _list;

    // Session Manager Class
    SessionManager session;
    HashMap<String, String> user;

    private static String approvingEIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_justification);

        // Session class instance
        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        user = session.getUserDetails();

        // name
        String name = user.get(SessionManager.KEY_NAME);

        // approvingEIC
        approvingEIC = user.get(SessionManager.KEY_EIC);

        _list = new ArrayList<>();
        lv = (ListView) findViewById(R.id.listJustification);
        lv.setOnItemClickListener(this);

        // domain
        new GetJustificationApplications(user.get(SessionManager.KEY_DOMAIN) + url + approvingEIC).execute();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            // get the object being selected
            Object r = parent.getItemAtPosition(position);
            HashMap<String, String> item = (HashMap<String, String>) r;

            Intent i = new Intent(this, JustificationPerMonthActivity.class);
            i.putExtra("EIC", item.get("EIC"));
            i.putExtra("name", item.get("name"));
            startActivity(i);
        } catch(Exception ex) {
            Log.e(TAG, "Error: " + ex.getMessage());
        }
    }

    private class GetJustificationApplications extends AsyncTask<Void, Void, Void> {

        private String _url;
        private int _count;

        public int get_count() {
            return _count;
        }

        public void set_count(int _count) {
            this._count = _count;
        }

        public GetJustificationApplications(String u) {
            this._url = u;
            this.set_count(0);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(JustificationActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler httpHandler = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = httpHandler.makeServiceCall(this._url);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("justifications");

                    // counted number of items
                    this.set_count(items.length());

                    // looping through all items
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject c = items.getJSONObject(i);

                        Log.d(TAG, "c.length=" + c.length());
                        Log.d(TAG, "c.getString(\"Key\")=" + c.getString("Key"));

                        String EIC = ((JSONObject) c.getJSONObject("Key")).getString("EIC");
                        String name = ((JSONObject) c.getJSONObject("Key")).getString("fullnameFirst");
                        int total_justifications = ((JSONObject) c.getJSONObject("Key")).getInt("total");

                        // tmp hash map for single entry
                        HashMap<String, String> entry = new HashMap<>();

                        // adding each child node to HashMap key => value
                        entry.put("name", name);
                        entry.put("EIC", EIC);
                        entry.put("total_justifications", "Total: " + String.valueOf(total_justifications));


                        // adding entry to entry list
                        _list.add(entry);
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
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    JustificationActivity.this
                    ,_list
                    ,R.layout.list_item_justification
                    ,new String[]{"name", "total_justifications", "EIC"}
                    ,new int[]{R.id.name, R.id.total_justifications, R.id.EIC}
            );

            lv.setAdapter(adapter);
        }

    }

}
