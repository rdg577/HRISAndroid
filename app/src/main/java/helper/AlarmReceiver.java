package helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import ph.gov.davaodelnorte.hris.HRISService;

/**
 * Created by Reden D. Gallera on 16/05/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // start service
        context.startService(new Intent(context, HRISService.class));
    }
}
