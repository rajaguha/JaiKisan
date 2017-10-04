

// TODO - make multi-threaded; convert to extension of Service, not IntentService
// DONE - use external drive - sdcard

package site.swaraj.jaikisan;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.SystemClock.sleep;

/**
 * loads SMS into db
 * called by BroadcastReceiver
 *
 * Created by rajag on 2017-07-02.
 */

public class SmsService extends IntentService {
    static final String TAG = "JK:SmsService";
    public static final String SMS_SENT = "site.swaraj.jaikisan.SmsService.SMS_SENT";
    public static final String SMS_DLVR = "site.swaraj.jaikisan.SmsService.SMS_DELIVERED";

    static final SmsManager smsMgr = SmsManager.getDefault();
    static final SmsDbHlpr dbs = SwarajApp.getDbs();
    static final LogDb log = dbs.logDb;
    static final DataDb data = dbs.dataDb;
    static final MetaDb meta = dbs.metaDb;

    class Reply {
        String cid;
        String msg;
        Long mts;
        Long rts = new Date().getTime();
        Long rid;
        String out;
        Boolean send;
        Reply( String cid, String msg, Long mts ) {
            this.cid = cid;
            this.msg = msg;
            this.mts = mts;
        }
        @Override public String toString() {
            StringBuffer str = new StringBuffer("Reply::");
            str.append( "cid:").append( cid == null ? "<NULL>" : cid );
            str.append( "\nmsg:").append( msg == null ? "<NULL>" : msg );
            str.append( "\nmts:").append( mts == null ? "<NULL>" : mts );
            str.append( "\nrts:").append( rts == null ? "<NULL>" : rts );
            str.append( "\nrid:").append( rid == null ? "<NULL>" : rid );
            str.append( "\nout:").append( out == null ? "<NULL>" : out );
            str.append( "\nerr:").append( send == null ? "<NULL>" : send );
            return new String(str);
        }
    }

    public SmsService() {
        super("SmsService");
        Log.d(TAG, "SmsService(): <<<");

        Log.d(TAG, "SmsService(): >>>");
    }

    @Override protected void onHandleIntent(@Nullable Intent in) {
        Log.d(TAG, ">>> onHandleIntent(): " );
        // TODO -
        final Bundle b = in.getExtras();
        if (b != null) {
            Reply reply = null;
            try {
                Object[] pdus = (Object[]) b.get("pdus");
                Log.i(TAG, "onHandleIntent(): Received " + pdus.length + " messages");
                int outC = 0;
                for (Object pdu : pdus) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                    if ( log.badNum( sms ) ) {
                        SwarajApp.deleteSMS( SwarajApp.getCtx(), sms.getOriginatingAddress(), sms.getTimestampMillis() );
                    } else {
                        reply = processSMS(sms);
                        if (reply.send) sendSMS(reply);
                        else Log.i(TAG, "onHandleIntent():NO REPLY:" + reply.cid + ":" + reply.out);
                    }
                    outC += 1;
                }
                Log.i(TAG, "onHandleIntent():" + outC + " msgs processed");
            } catch (Exception e) {
//                LogDb.log_error(e, reply.rid != null ? reply.rid : 0 );
                            Log.e(TAG, "onHandleIntent():" + e );
                            e.printStackTrace();
            }
        }
        Log.d(TAG, "<<< onHandleIntent():" );
    }
    private Reply processSMS( SmsMessage sms ) {
        Log.d(TAG, "processSMS(): >>>" );
        Reply reply = new Reply(
                  sms.getOriginatingAddress()
                , sms.getMessageBody()
                , sms.getTimestampMillis()
        );
        reply.rid = log.insMsg( reply );
        Member mmbr = new Member( reply );
//        Log.d(TAG, "MEMBER: " + mmbr.toString() );
//        HashMap<String, HashMap<String, Bit>> bits = parseSMS( new StringBuilder(reply.msg) );

//      String OUT = "স্বরাজ অভিযান জয় কিষান আন্দোলনের গ্রামীন পদার্থর মূল্য অনুমান যন্ত্রে আপনার স্বাগতম।\n"
//            + "এই প্রকল্পের দৈনিক উন্নত করা হচ্ছে। নতুন বৈশিষ্ট্য দেখতে পুনরায় বার্তা পাঠান।";
        String OUT = "Welcome 12345 67890, to the Kisan Swaraj Project.  " +
                "It is presently in development.  Your ID No. is " + mmbr.mmbr.get("_id") +
                ".\n\nSend CANCEL to quit."
                ;
        if ( Pattern.matches("^\\+91\\d{10}$", reply.cid) ) {           // real deal
            reply.send = true;
            reply.out = OUT.replace( "12345", reply.cid.substring(3,8) )
                    .replace( "67890", reply.cid.substring(8) );
        } else if ( Pattern.matches( "^1555521\\d{4}$", reply.cid) ) {  // emulator
            reply.send = true;
            reply.out = OUT.replace( "12345", reply.cid.substring(1,6) )
                    .replace( "67890", reply.cid.substring(6) );
        } else if ( Pattern.matches( "^BA-PORTAL$", reply.cid) ) {      // recharge confirmation
            // cid = BA-PORTAL \n in_msg = Your payment of Rs.34.00 is successful on BSNL Portal. Ref Id:PGSM250917806615
            Matcher m = Pattern.compile("$Your payment of Rs\\.(\\d{1,3}.\\d{2}) is successful on BSNL Portal\\. Ref Id:(PGSM\\d+)$" ).matcher(reply.msg);
            if ( m.find() ) log.updBsnl( m.group(3) );
            reply.send = true;
            reply.cid = "+919748633792";    // notify me
            int len = reply.msg.length();
            reply.out = "BA-PORTAL:<" + reply.msg.substring(0, (len > 123 ? 122 : len))
                    + (len > 123 ? " ...>" : ">") + "\n@ " + SwarajApp.sdf.format(new Date(reply.mts));
        } else if ( Pattern.matches( "^BSNL$", reply.cid) ) {           // recharge
            // cid = BSNL \n in_msg = Ur a/c is credited by Rs.34 on 25/09/2017 at 03:35:34 AM  Ref No.PGSM250917806615
            Matcher m = Pattern.compile("$Ur a/c is credited by Rs\\.(\\d{1,3}) on (\\d{2}/\\d{2}/\\d{4}) at (\\d{2}:\\d{2}:\\d{2} [AP]M)  Ref No.(PGSM\\d+)$" ).matcher(reply.msg);
            if ( m.find() ) log.insBsnl( Float.valueOf(m.group(1)+".00"), m.group(2) +" " + m.group(3), m.group(4) );
            reply.send = false;
            reply.out = "Logged credit for Rs." + m.group(1) + " at " + m.group(2) + " " + m.group(3) + ". Ref. Id. " + m.group(4);
        } else {
            reply.send = false;
            reply.out = "ERROR:no cid pattern for " + reply.cid;
        }
        Log.i(TAG, "\tprocessSMS(): " + reply.out);
        log.updMsg( reply );
        Log.d(TAG, "processSMS(): <<<" );
        return reply;
    }
    private void sendSMS(final Reply reply) {
        // see <https://mobiforge.com/design-development/sms-messaging-android>
        Log.d( TAG, "sendSMS(): >>>" );
        ArrayList<String> parts = smsMgr.divideMessage(reply.out);
        Log.d( TAG, "sendSMS(): msg: length " + reply.out.length() + "; parts " + parts.size() );
        ArrayList<PendingIntent> sendPIs = new ArrayList<>(parts.size());
        ArrayList<PendingIntent> dlvrPIs = new ArrayList<>(parts.size());
        for ( int i = 0; i < parts.size(); i++ ) {
            Intent intnt = new Intent();
            intnt.putExtra( "cid", reply.cid );
            intnt.putExtra( "msg", reply.msg );
            intnt.putExtra( "out", reply.out );
            intnt.putExtra( "mts", reply.mts );
            intnt.putExtra( "rts", reply.rts );
            intnt.putExtra( "rid", reply.rid );
            intnt.putExtra( "part_no", i + 1 );
            intnt.putExtra( "parts", parts.size() );
            sendPIs.add(PendingIntent.getBroadcast(this, 0, intnt.setAction(SMS_SENT), PendingIntent.FLAG_UPDATE_CURRENT));
            dlvrPIs.add(PendingIntent.getBroadcast(this, 0, intnt.setAction(SMS_DLVR), PendingIntent.FLAG_UPDATE_CURRENT));
            Log.d( TAG, "sendSMS(): sending parts " + intnt.getIntExtra("part_no",0) + " of " + intnt.getIntExtra("parts",0) );
        }

//        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, intnt.setAction(SMS_SENT), 0 );
//        PendingIntent dlvrPI = PendingIntent.getBroadcast(this, 0, intnt.setAction(SMS_DELIVERED), 0 );
//        smsMgr.sendTextMessage( reply.phoneNo, null, reply.msg, sentPI, dlvrPI );   //returns void

        smsMgr.sendMultipartTextMessage(reply.cid, null, parts, sendPIs, dlvrPIs );
        log.updEnq( reply.rid );
//        Log.i(TAG, "\tsendSMS():queued reply to " + reply.cid );
        Log.d(TAG, "sendSMS(): >>>" );
    }
}
/*********************************
 private void _dumpSMStoLogV( SmsMessage sms ) {
 Log.d(TAG, ">>> _dumpSMStoLogV(): " );
 HashMap<String, Object> thm = new HashMap<>(25);
 thm.put( "DisplayMessageBody", sms.getDisplayMessageBody() );
 thm.put( "DisplayOriginatingAddress", sms.getDisplayOriginatingAddress() );

 thm.put( "MessageBody", sms.getMessageBody() );
 thm.put( "OriginatingAddress", sms.getOriginatingAddress() );
 thm.put( "TimestampMillis", sms.getTimestampMillis() );

 thm.put( "EmailBody", sms.getEmailBody() );
 thm.put( "EmailFrom", sms.getEmailFrom() );
 thm.put( "IndexOnIcc", sms.getIndexOnIcc() );
 thm.put( "MessageClass", sms.getMessageClass() );
 thm.put( "Pdu", sms.getPdu() );
 thm.put( "ProtocolIdentifier", sms.getProtocolIdentifier() );
 thm.put( "PseudoSubject", sms.getPseudoSubject() );
 thm.put( "ServiceCenterAddress", sms.getServiceCenterAddress() );
 thm.put( "Status", sms.getStatus() );
 thm.put( "StatusOnIcc", sms.getStatusOnIcc() );
 thm.put( "TPLayerLengthForPDU", sms.getTPLayerLengthForPDU( new String(sms.getPdu()) ) );
 thm.put( "UserData", sms.getUserData() );
 thm.put( "CphsMwiMessage", sms. isCphsMwiMessage() );
 thm.put( "isEmail", sms. isEmail() );
 thm.put( "isMWIClearMessage", sms. isMWIClearMessage() );
 thm.put( "isMWISetMessage", sms. isMWISetMessage() );
 thm.put( "isMwiDontStore", sms. isMwiDontStore() );
 thm.put( "isReplace", sms. isReplace() );
 thm.put( "isReplyPathPresent", sms. isReplyPathPresent() );
 thm.put( "isStatusReportMessage", sms. isStatusReportMessage() );
 for ( String key : thm.keySet() ) {
 try {
 Log.v(TAG, "processSMS(): " + key + " => " + thm.get(key).toString()
 + " isa " + thm.get(key).getClass().getName());
 } catch ( Exception e ) {
 Log.e( TAG, "processSMS(): " + key + " => " + e.getMessage()
 + " isa " + thm.get(key).getClass().getName() );
 }
 }
 Log.d(TAG, "<<< _dumpSMStoLogV(): " );
 }
 @TargetApi(16) @SuppressLint("NewApi") private void _fromIntent( Intent  in ) {
 Log.d(TAG, ">>> _fromIntent(): " );
 final Bundle b = in.getExtras();
 try {
 if (b != null) {
 Object[] pdus = (Object[]) b.get("pdus");
 Log.i(TAG, "_fromIntent(): Received " + pdus.length + " messages");
 int outC = 0;
 for (Object pdu : pdus) {
 SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
 _dumpSMStoLogV(sms);
 outC += 1;
 }
 Log.i(TAG, "_fromIntent():" + outC + " msgs");
 }
 } catch (Exception e) {
 //            SmsDbHelper.log_error(e);
 Log.e(TAG, "_fromIntent():" + e );
 e.printStackTrace();
 }
 Log.d(TAG, "<<< _fromIntent(): " );
 }
 private void _nullsCursor() {
 try {
 Cursor c1 = getContentResolver().query(Uri.parse("content://sms/inbox")
 , null, null, null, null);
 if (c1 != null && c1.moveToFirst()) {
 do {
 int cc = c1.getColumnCount();
 for (int i = 0; i < cc; i++)
 Log.i(TAG, "\tNULLS: " + c1.getColumnName(i) + ":" + c1.getString(i));
 Log.i(TAG, "\tURL: ----------------------------------------------");
 } while (c1.moveToNext());
 c1.close();
 }
 } catch (Exception e) {
 //            SmsDbHelper.log_error(e);
 Log.e(TAG, "_nullsCursor():" + e);
 }
 }
 private void _fromUrl() {
 try {
 Cursor c = getContentResolver().query(Uri.parse("content://sms/inbox"),
 new String[]{"_id", "thread_id", "address", "person", "date"
 , "date_sent", "body", "service_center"}, null, null, null);
 if (c != null && c.moveToFirst()) {
 do {
 long rts = new Date().getTime();
 int cc = c.getColumnCount();
 //                    for (int i = 0; i < cc; i++)
 //                        Log.i(TAG, "\tURL: " + c.getColumnName(i) + ":" + c.getString(i));
 String id = c.getString(0);
 String tid = c.getString(1);
 String cid = c.getString(2);
 String per = c.getString(3);
 Long mts = c.getLong(4);
 Long sts = c.getLong(5);
 String msg = c.getString(6);
 String sc = c.getString(7);
 Log.i(TAG, "onHandleIntent():"+id+":"+tid+":"+cid+":"+per+":"+mts+":"+sts+":"+msg);

 Reply reply = processSMS( mts, rts, cid, msg );
 if ( reply == null )
 Log.i(TAG, "onHandleIntent():No reply for msg " + id);
 else
 sendSMS( reply );
 //TODO - think about timing problems - deletion before actual send.  Solution: clone data from incoming
 Log.d(TAG, "onHandleIntent():Deleting SMS with id: " + id);
 getContentResolver().delete(Uri.parse("content://sms/" + id), null, null);
 } while (c.moveToNext());
 c.close();
 }
 } catch (Exception e) {
 //            SmsDbHelper.log_error(e);
 Log.e(TAG, "onHandleIntent():" + e);
 }
 }
*********************************/
