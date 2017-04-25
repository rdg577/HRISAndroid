package ph.gov.davaodelnorte.hris;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.MyApplication;
import helper.Menu;

/**
 * Created by Reden Gallera on 09/03/2017.
 */

public class LoginActivity extends AppCompatActivity {

    private final String TAG = LoginActivity.class.getSimpleName();
    // private final String URL = "http://www.davaodelnorte.gov.ph:333/";
    private final String URL = "http://222.127.105.70:333/";

    private boolean login_flag = false;

    // Email, password edittext
    EditText txtUsername, txtPassword;

    // login button
    Button btnLogin;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    // Session Manager Class
    SessionManager session;

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
                                        Log.d(TAG, "response="+response.toString());

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
                                                }

                                                Toast.makeText(getApplicationContext(), "User Login Status: " + (session.isLoggedIn()? "IN":"OUT"), Toast.LENGTH_LONG).show();
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
}
