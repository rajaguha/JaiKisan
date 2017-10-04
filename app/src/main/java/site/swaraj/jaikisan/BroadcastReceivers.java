package site.swaraj.jaikisan;

// TODO - move sms_completed events to MsgLogActivity and deletre events there

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static java.lang.Math.abs;

/**
 * receives broadcasts and calls appropriate intent
 *
 * Created by rajag on 2017-07-02.
 */
public class BroadcastReceivers extends BroadcastReceiver {
    static final String TAG = "JK:BroadcastReceiver";
    static final String SMS_RCVD = "android.provider.Telephony.SMS_RECEIVED";
    static final String SMS_SENT = SmsService.SMS_SENT;
    static final String SMS_DLVR = SmsService.SMS_DLVR;
    static final String BOOT = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context ctx, Intent in ) {

        final String ACTION = in.getAction();
//        Log.i(TAG, "rcvd ACTION: " + ACTION );
        switch ( ACTION ) {
            case SMS_RCVD:
                Intent smsIntent = new Intent(ctx, SmsService.class);
                smsIntent.putExtras(in.getExtras());
                ctx.startService(smsIntent);
//                Log.i(TAG, "rcvd SMS_RCVD @ " + SystemClock.elapsedRealtime());
                break;
            case SMS_SENT:
                SwarajApp.getDbs().logDb.updSnt(in.getLongExtra("rid", 0));
//                Log.i(TAG, "rcvd SMS_SENT" + getMsgExtras(in) );
                break;
            case SMS_DLVR:
                SwarajApp.getDbs().logDb.updDlv(in.getLongExtra("rid", 0));
//                Log.i(TAG, "rcvd SMS_DLVR" + getMsgExtras(in) );
                int part_no = in.getIntExtra("part_no", 0);
                int parts = in.getIntExtra("parts", 0);
                String cid = in.getStringExtra("cid");
                Long mts = in.getLongExtra("mts",0);
                if ( part_no == parts )
                    SwarajApp.deleteSMS( ctx, cid, mts );
                break;
            case BOOT:
                Log.i(TAG, "rcvd BOOT @ " + SystemClock.elapsedRealtime());
                // TODO - clear existing msgs on boot
                break;
            default:
                Log.wtf(TAG, "rcvd unk ACTION " + ACTION + " @ " + SystemClock.elapsedRealtime());
                break;
        }
    }
    private String getMsgExtras( Intent in ) {
        Long mts = in.getLongExtra( "mts", 0 );
        String cid = in.getStringExtra( "cid" );
        int part_no = in.getIntExtra( "part_no", 0 );
        int parts = in.getIntExtra( "parts", 0 );
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.ENGLISH);
        return " for " + cid + " @ " + sdf.format(new Date(mts)) + "; part " + part_no + " of " + parts;
    }
}
