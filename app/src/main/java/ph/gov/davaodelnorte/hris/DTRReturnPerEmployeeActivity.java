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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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

public class DTRReturnPerEmployeeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private final String TAG = DTRReturnPerEmployeeActivity.class.getSimpleName();
    private ListView lv;

    private final String url = "WebService/Toolbox/DTRReturnRequestPerEmployee";
    private final String urlApproval = "WebService/Toolbox/DTRAction";

    // Session Manager Class
    private SessionManager session;
    private HashMap<String, String> user;

    private static String EIC, approvingEIC, name, DtrId, strPeriod;
    private int intPeriod;

    private ProgressDialog progressDialog;
    private EditText input;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, DTRReturnActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dtrreturn_per_employee);

        try {
            session = new SessionManager(getApplicationContext());
            session.checkLogin();
            user = session.getUserDetails();
            approvingEIC = user.get(SessionManager.KEY_EIC);

            lv = (ListView) findViewById(R.id.listReturnDTR);
            lv.setOnItemClickListener(this);

            // get selected EIC
            EIC = getIntent().getExtras().getString("EIC");
            name = getIntent().getExtras().getString("name");
            getSupportActionBar().setTitle(name);
            Log.d(TAG, "onCreate name = " + name);

            fetchDTRRequestForReturnPerEmployee(EIC, approvingEIC, user.get(SessionManager.KEY_DOMAIN) + this.url);

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

            DtrId = item.get("DtrId");
            intPeriod = Integer.parseInt(item.get("period"));
            strPeriod = item.get("year_month") + " " + item.get("period_label");

            input = new EditText(getApplicationContext());
            input.setTextColor(Color.parseColor("#ff0000aa"));

            AlertDialog.Builder builder = new AlertDialog.Builder(DTRReturnPerEmployeeActivity.this);
            builder.setTitle("Confirm Action - DTR Return")
                //.setMessage("Kindly enter the reason of returning this")
                .setCancelable(true)
                .setPositiveButton("Please return DTR!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String remarks = "As requested by the owner.";
                        int action = 0; // 0 - returned
                        returnDTR(DtrId, strPeriod, intPeriod, action, approvingEIC, remarks, user.get(SessionManager.KEY_DOMAIN) + urlApproval);

                        /*
                        AlertDialog.Builder alertGetRemarkBuilder = new AlertDialog.Builder(DTRReturnPerEmployeeActivity.this);
                        alertGetRemarkBuilder.setTitle("Remark")
                                .setMessage("Kindly enter the reason of returning this DTR.")
                                .setView(input)
                                .setCancelable(false)
                                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String remarks = input.getText().toString();
                                        int action = 0; // 0 - returned
                                        returnDTR(DtrId, strPeriod, intPeriod, action, approvingEIC, remarks, user.get(SessionManager.KEY_DOMAIN) + urlApproval);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(getApplicationContext(), "Operation has been cancelled.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        alertGetRemarkBuilder.create().show();
                        */
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getApplicationContext(), "Cancelled.", Toast.LENGTH_SHORT).show();
                    }
                });

            AlertDialog alert = builder.create();
            alert.show();

        } catch(Exception ex) {
            Log.e(TAG, "onItemClick Error: " + ex.getMessage());
        }
    }

    private void returnDTR(String DtrId, String strPeriod, int intPeriod, final int action, String approvingEIC, String remarks, String url) {

        try {
            Map<String, String> params = new HashMap<>();
            params.put("DtrId", DtrId);
            params.put("strPeriod", strPeriod);
            params.put("intPeriod", String.valueOf(intPeriod));
            params.put("action", String.valueOf(action));
            params.put("approvingEIC", approvingEIC);
            params.put("remarks", remarks);

            JSONObject parameters = new JSONObject(params);

            // Volley's json array request object
            JsonObjectRequest req = new JsonObjectRequest(url, parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();

                        try {
                            JSONObject j = response.getJSONObject("dtr_action");
                            if(!j.getBoolean("has_error")) {
                                if(action == 0)
                                    Toast.makeText(getApplicationContext(), "DTR has been returned!",Toast.LENGTH_LONG).show();
                                else if(action == 1)
                                    Toast.makeText(getApplicationContext(), "DTR has been approved!",Toast.LENGTH_LONG).show();
                                else if(action == 2)
                                    Toast.makeText(getApplicationContext(), "DTR has been posted!",Toast.LENGTH_LONG).show();
                                else if(action == 3)
                                    Toast.makeText(getApplicationContext(), "DTR has been reverted!",Toast.LENGTH_LONG).show();
                            }
                            Intent i = new Intent(getApplicationContext(), DTRReturnPerEmployeeActivity.class);
                            i.putExtra("EIC", EIC);
                            i.putExtra("name", name);
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

            progressDialog = new ProgressDialog(DTRReturnPerEmployeeActivity.this);
            progressDialog.setMessage("Requesting data from server ....");
            progressDialog.show();
        } catch (Exception ex) {
            Log.e(TAG, "fetchJustificationApproval ERROR: " + ex.getMessage());
        }

    }

    private void fetchDTRRequestForReturnPerEmployee(String EIC, String approvingEIC, String url) {

        try {
            Map<String, String> params = new HashMap<>();
            params.put("EIC", EIC);
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

                                JSONArray items = response.getJSONArray("dtrs");
                                for(int i = 0; i < items.length(); i++) {
                                    JSONObject j = items.getJSONObject(i);
                                    HashMap<String, String> entry = new HashMap<>();

                                    String year_month = "";
                                    switch (j.getInt("Month")) {
                                        case 1:
                                            year_month += "January";
                                            break;
                                        case 2:
                                            year_month += "February";
                                            break;
                                        case 3:
                                            year_month += "March";
                                            break;
                                        case 4:
                                            year_month += "April";
                                            break;
                                        case 5:
                                            year_month += "May";
                                            break;
                                        case 6:
                                            year_month += "June";
                                            break;
                                        case 7:
                                            year_month += "July";
                                            break;
                                        case 8:
                                            year_month += "August";
                                            break;
                                        case 9:
                                            year_month += "September";
                                            break;
                                        case 10:
                                            year_month += "October";
                                            break;
                                        case 11:
                                            year_month += "November";
                                            break;
                                        case 12:
                                            year_month += "December";
                                            break;
                                    }

                                    year_month += " " + String.valueOf(j.getInt("Year"));

                                    String period_label = "";
                                    switch (j.getInt("Period")) {
                                        case 0:
                                            period_label = "[ Full Month ]";
                                            break;
                                        case 1:
                                            period_label = "[ 1st Half ]";
                                            break;
                                        case 2:
                                            period_label = "[ 2nd Half ]";
                                            break;
                                    }

                                    entry.put("DtrId", j.getString("DtrID"));
                                    entry.put("year_month", year_month);
                                    entry.put("period_label", period_label);
                                    entry.put("period", String.valueOf(j.getInt("Period")));

                                    _list.add(entry);

                                }

                                ListAdapter adapter = new SimpleAdapter(
                                        DTRReturnPerEmployeeActivity.this
                                        ,_list
                                        ,R.layout.list_item_dtr
                                        ,new String[]{"DtrId", "year_month", "period", "period_label"}
                                        ,new int[]{R.id.dtr_id, R.id.year_month, R.id.period, R.id.period_label}
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

            progressDialog = new ProgressDialog(DTRReturnPerEmployeeActivity.this);
            progressDialog.setMessage("Requesting data from server ....");
            progressDialog.show();
        } catch (Exception ex) {
            Log.e(TAG, "fetchDTRRequestForReturnPerEmployee Error: " + ex.getMessage());
        }

    }
}
