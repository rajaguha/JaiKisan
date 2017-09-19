package site.swaraj.jaikisan;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MsgLogActivity extends AppCompatActivity {
    private static final String TAG = "JK:MsgLogActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,">>> onCreate()");
        setContentView(R.layout.activity_msg_log);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Context ctx = this;

//  TODO - improve to https://www.raywenderlich.com/124438/android-listview-tutorial
//        SwarajApp.setLstVu( lv );
//        class LVAdapter<T> extends ArrayAdapter<T> {
//            LVAdapter(List list) { super( ctx, R.layout.list_item, list ); }
//            @Override
//            public View getView(int position, View convertView, ViewGroup container) {
//                if (convertView == null)
//                    convertView = getLayoutInflater().inflate(R.layout.list_item, container, false);
//
//                ((TextView) convertView.findViewById(R.id))
//                    .setText(getItem(position));
//                return convertView;
//            }
//        }
//        final LVAdapter<String> lvAdapter = new LVAdapter<>(list);
//        lv.setAdapter(lvAdapter);

        final ListView lv = (ListView) findViewById(R.id.list_view);
        final int lstLen = 6;
        final List<String> list = new ArrayList<>( lstLen );
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, list);
        lv.setAdapter( adapter );

        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context ctx, Intent in) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        list.add( 0, "SENT: " + in.getStringExtra("msg") );
                        break;
                    default:
                        list.add( 0, "SERR: " + getResultCode() );
                        break;
                }
                if ( list.size() > (lstLen*2)) list.remove((lstLen*2)-1);
                adapter.notifyDataSetChanged();
                Log.d(TAG,"SENT: " + list.size() + " : " + adapter.getCount() );
            }
        }, new IntentFilter(SmsService.SMS_SENT));

        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context ctx, Intent in) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        list.add( 0, "DLVR:" + in.getStringExtra("msg") );
                        break;
                    default:
                        list.add( 0, "DERR: " + getResultCode() );
                        break;
                }
                if ( list.size() > lstLen) list.remove(lstLen-1);
                adapter.notifyDataSetChanged();
                Log.d(TAG,"DLVR: " + list.size() );
            }
        }, new IntentFilter(SmsService.SMS_DELIVERED));



        Log.d(TAG,"<<< onCreate()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_msg_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
