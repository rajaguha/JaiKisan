package site.swaraj.jaikisan;

import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by rajag on 2017-08-10.
 */

// TODO - fix ctx in SwarajApp - apparently this is a no-no
public class SwarajApp extends Application {
    private static final String TAG = "JK:SwarajApp";
//    private static Context ctx;
//    private static ListView lv;

    public void onCreate() {
        Log.d(TAG,">>> onCreate()");
        super.onCreate();
//        SwarajApp.ctx = getApplicationContext();
        Log.d(TAG,"<<< onCreate()");
    }

//    public static Context getAppCtx() { return SwarajApp.ctx; }
//    public static void setLstVu(ListView lv) { SwarajApp.lv = lv; }
//    public static ListView getLstVu() { return SwarajApp.lv; }
}
