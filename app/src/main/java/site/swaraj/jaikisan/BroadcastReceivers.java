package site.swaraj.jaikisan;

// TODO - move sms_completed events to MsgLogActivity and deletre events there

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * receives broadcasts and calls appropriate intent
 *
 * Created by rajag on 2017-07-02.
 */
public class BroadcastReceivers extends BroadcastReceiver {
    static final String TAG = "JK:BroadcastReceiver";
    static final String SMS_RCVD = "android.provider.Telephony.SMS_RECEIVED";
    static final String BOOT = "android.intent.action.BOOT_COMPLETED";
//    static final String SMS_SENT = "site.swaraj.jaikisan.SmsService.SMS_SENT";
//    static final String SMS_DLVR = "site.swaraj.jaikisan.SmsService.SMS_DELIVERED";

    @Override
    public void onReceive(Context ctx, Intent in ) {
        final String ACTION = in.getAction();
        Log.i(TAG, "Received ACTION: " + ACTION );
        switch ( ACTION ) {
            case SMS_RCVD:
                Intent smsIntent = new Intent(ctx, SmsService.class);
                smsIntent.putExtras(in.getExtras());
                Log.i(TAG, "Starting smsService . . . @ " + SystemClock.elapsedRealtime());
                ctx.startService(smsIntent);
                Log.i(TAG, "Started smsService @ " + SystemClock.elapsedRealtime());
                break;
            case SmsService.SMS_SENT:
                Log.i(TAG, "SMS_SENT" + getExtras(in) );
                break;
            case SmsService.SMS_DELIVERED:
                Log.i(TAG, "SMS_DELIVERED" + getExtras(in) );
                delIfComplete( ctx, in );
                break;
            case BOOT:
                Log.i(TAG, "Boot broadcast received @ " + SystemClock.elapsedRealtime());
                break;
            default:
                Log.i(TAG, "Unknown ACTION " + ACTION + " @ " + SystemClock.elapsedRealtime());
                break;
        }
    }
    private String getExtras( Intent in ) {
        Long msg_ts = in.getLongExtra( "msg_ts", 0 );
        String cid = in.getStringExtra( "phoneNo" );
        Long part_no = in.getLongExtra( "part_no", 0 );
        Long parts = in.getLongExtra( "parts", 0 );
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.ENGLISH);
        return " for " + cid + " @ " + sdf.format(new Date(msg_ts)) + "; part " + part_no + " of " + parts;
    }
    private void delIfComplete( Context ctx, Intent in ) {
        Long msg_ts = in.getLongExtra("msg_ts", 0);
        String cid = in.getStringExtra("phoneNo");
        Long part_no = in.getLongExtra("part_no", 0);
        Long parts = in.getLongExtra("parts", 0);
        if (part_no == parts) {
            try {
                Cursor c = ctx.getContentResolver().query(Uri.parse("content://sms/inbox"),
                        new String[]{"_id", "address", "date", "date_sent"}, null, null, null);
                if (c != null && c.moveToFirst()) {
                    do {
                        String id = c.getString(0);
                        String addr = c.getString(1);
                        Long mts = c.getLong(4);
                        Long sts = c.getLong(5);
                        if (cid.equals(addr) && sts == msg_ts) {
                            Log.i(TAG, "deleteSMS():Deleting " + id + ":" + cid + ":" + mts + ":" + sts);
                            ctx.getContentResolver().delete(Uri.parse("content://sms/" + id), null, null);
                        }
                    } while (c.moveToNext());
                    c.close();
                }
            } catch (Exception e) {
                //            SmsDbHelper.log_error(e);
                Log.e(TAG, "deleteSMS():" + e);
            }
        }
    }
}
/*****************************
 if ( ACTION.equals( SMS_RCVD ) ) {
 Intent smsIntent = new Intent(ctx, SmsService.class);
 smsIntent.putExtras(in.getExtras());
 Log.i(TAG, "onReceive(): Starting smsService . . . @ " + SystemClock.elapsedRealtime());
 ctx.startService(smsIntent);
 Log.i(TAG, "onReceive(): Started smsService @ " + SystemClock.elapsedRealtime());
 } else if ( ACTION.equals( SmsService.SMS_SENT ) ) {
 Long msg_ts = in.getLongExtra( "msg_ts", 0 );
 String cid = in.getStringExtra( "phoneNo" );
 Integer part_no = in.getIntExtra( "part_no", 0 );
 Integer parts = in.getIntExtra( "parts", 0 );
 SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.ENGLISH);
 Log.i(TAG, "onReceive(): SMS_SENT for " + cid + " @ "
 + sdf.format(new Date(msg_ts)) + "; part " + part_no + " of " + parts );
 } else if ( ACTION.equals( SmsService.SMS_DELIVERED ) ) {
 Long msg_ts = in.getLongExtra( "msg_ts", 0 );
 String cid = in.getStringExtra( "phoneNo" );
 Integer part_no = in.getIntExtra( "part_no", 0 );
 Integer parts = in.getIntExtra( "parts", 0 );
 SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.ENGLISH);
 Log.i(TAG, "onReceive(): SMS_DELIVERED for " + cid + " @ "
 + sdf.format(new Date(msg_ts)) + "; part " + part_no + " of " + parts );
 if ( part_no == parts ) deleteSMS( ctx, cid, msg_ts );
 } else if ( ACTION.equals(BOOT) ) {
 Log.i(TAG, "onReceive(): Boot broadcast received @ " + SystemClock.elapsedRealtime());
 }
*************************/