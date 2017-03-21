package ph.gov.davaodelnorte.hris;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void viewPassSlip(View view)
    {
        Intent intent = new Intent(this, PassSlipActivity.class);
        startActivity(intent);
    }
}
