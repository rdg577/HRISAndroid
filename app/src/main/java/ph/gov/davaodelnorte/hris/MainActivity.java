package ph.gov.davaodelnorte.hris;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import java.util.HashMap;

import android.text.Html;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    // Session Manager Class
    SessionManager session;

    // Button Logout
    Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Session class instance
        session = new SessionManager(getApplicationContext());

        TextView lblName = (TextView) findViewById(R.id.lblName);
        TextView lblEmail = (TextView) findViewById(R.id.lblEmail);

        // Button logout
        btnLogout = (Button) findViewById(R.id.btnLogout);

        // Toast.makeText(getApplicationContext(), "User Login Status: " + (session.isLoggedIn()? "IN":"OUT"), Toast.LENGTH_LONG).show();


        /**
         * Call this function whenever you want to check user login
         * This will redirect user to LoginActivity is he is not
         * logged in
         * */
        session.checkLogin();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        // eic
        String eic = user.get(SessionManager.KEY_EIC);

        // name
        String name = user.get(SessionManager.KEY_NAME);

        /*
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.NOUGAT) {
            // displaying user data
            lblName.setText(Html.fromHtml("Name: <b>" + name + "</b>", Html.FROM_HTML_MODE_COMPACT));
            lblEmail.setText(Html.fromHtml("Email: <b>" + email + "</b>", Html.FROM_HTML_MODE_COMPACT));
        }
        */
        // displaying user data
        lblName.setText(Html.fromHtml("Approving Officer: <b>" + name + "</b>"));
        lblEmail.setText(Html.fromHtml("EIC: <b>" + eic + "</b>"));


        /**
         * Logout button click event
         * */
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Clear the session data
                // This will clear all session data and
                // redirect user to LoginActivity
                session.logoutUser();
            }
        });
    }

    public void showPassSlipActivity(View view)
    {
        Intent intent = new Intent(this, PassSlipActivity.class);
        startActivity(intent);
    }
}
