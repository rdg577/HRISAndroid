package ph.gov.davaodelnorte.hris;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

/**
 * Created by Reden Gallera on 09/03/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class LoginActivity extends AppCompatActivity {

    private final String TAG = LoginActivity.class.getSimpleName();
//    private final String URL = "http://www.davaodelnorte.gov.ph:333/";
    private final String URL = "http://222.127.105.70:333/";

    private boolean login_flag = false;

    // Email, password edittext
    private EditText txtUsername;
    private EditText txtPassword;

    // login button
    private Button btnLogin;

    // Alert Dialog Manager
    private final AlertDialogManager alert = new AlertDialogManager();

    // Session Manager Class
    private SessionManager session;

    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Session Manager
        session = new SessionManager(getApplicationContext());

        // Email, Password input text
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);

        Toast.makeText(getApplicationContext(), "User Login Status: " + (session.isLoggedIn()? "IN":"OUT"), Toast.LENGTH_LONG).show();

        // Login button
        btnLogin = (Button) findViewById(R.id.btnLogin);

        // Login button click event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Get username, password from EditText
                String username = txtUsername.getText().toString();
                String password = txtPassword.getText().toString();

                // Check if username, password is filled
                if(username.trim().length() > 0 && password.trim().length() > 0){
                    try {
                        // appending to url
                        String url = URL + "WebService/Account/Login";

                        Map<String, String> params = new HashMap<>();
                        params.put("username", username);
                        params.put("password", password);
                        JSONObject parameters = new JSONObject(params);

                        // Volley's json array request object
                        JsonObjectRequest req = new JsonObjectRequest(url, parameters,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        progressDialog.dismiss();

                                        //Log.d(TAG, "response="+response.toString());

                                        String dataType = "";
                                        try {
                                            dataType = response.get("data").getClass().getSimpleName();

                                            if (dataType.matches("Integer")) {
                                                alert.showAlertDialog(LoginActivity.this, "Login failed..", "Username/Password is incorrect", false);
                                            } else if (dataType.matches("JSONArray")) {
                                                JSONArray items = response.getJSONArray("data");
                                                for (int i = 0; i < items.length(); i++) {
                                                    JSONObject j = items.getJSONObject(i);
                                                    // Creating user login session
                                                    session.createLoginSession(j.getString("EIC"), j.getString("fullnameLast"), URL);

//                                                    session.createLoginSession("HP158780225294D64318", j.getString("fullnameLast"), URL);
//                                                    session.createLoginSession("MS1229370656BF505D6E", j.getString("fullnameLast"), "http://172.16.130.65/");
                                                    /**
                                                     * MS1229370656BF505D6E,
                                                     * EB13329278333FAC0E72,
                                                     * SA168298389632EB9AD5,
                                                     * GG509743801BE28F3B0E,
                                                     * SG11024211748EF3FCD4,
                                                     * HP158780225294D64318
                                                      */
                                                }

                                                Toast.makeText(getApplicationContext(), "User Login Status: " + (session.isLoggedIn()? "IN":"OUT"), Toast.LENGTH_SHORT).show();

                                                // Starting MainActivity
                                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                                startActivity(i);
                                                finish();

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

                        progressDialog = new ProgressDialog(LoginActivity.this);
                        progressDialog.setMessage("Verifying user credentials from server ....");
                        progressDialog.show();
                    } catch (Exception ex) {
                        Log.e(TAG, "ERROR: " + ex.getMessage());
                    }
                }else{
                    // user didn't entered username or password
                    // Show alert asking him to enter the details
                    alert.showAlertDialog(LoginActivity.this, "Login failed..", "Please enter username and password", false);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        /*super.onBackPressed();*/
    }
}
