
/************************

        BSNL sms costs
        --------------
        Rs.	count	days	Rs/sms	Rs/day	sms/day
        12    130	   7	 0.092	 1.714	 18.571
        21	  265	  15	 0.079	 1.400	 17.667
        ---------------------------------------------
        22	  500	  10	 0.044	 2.200	 50.000
        ---------------------------------------------
        32
        ---------------------------------------------
        34	  385	  30	 0.088	 1.133	 12.833
        ---------------------------------------------
        52	  860	  30	 0.060	 1.733	 28.667
        54
        147  3000	  60	 0.049	 2.450	 50.000

************************/

// TODO - make multi-threaded; convert to extension of Service

package site.swaraj.jaikisan;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import java.util.regex.Pattern;

import static android.os.SystemClock.sleep;

/**
 * loads SMS into db
 * called by BroadcastReceiver
 *
 * Created by rajag on 2017-07-02.
 */

public class SmsService extends IntentService {
    static final String TAG = "JK:SmsService:";
    static final String OUT = "স্বরাজ অভিযান জয় কিষান আন্দোলনের গ্রামীন পদার্থর মূল্য অনুমান যন্ত্রে আপনার স্বাগতম।\n"
            + "এই প্রকল্পের দৈনিক উন্নত করা হচ্ছে। নতুন বৈশিষ্ট্য দেখতে পুনরায় বার্তা পাঠান।";
    public static final String SMS_SENT = "site.swaraj.jaikisan.SmsService.SMS_SENT";
    public static final String SMS_DELIVERED = "site.swaraj.jaikisan.SmsService.SMS_DELIVERED";

    //    static SmsDbHelper dbHelper;
    static final SmsManager smsMgr = SmsManager.getDefault();
    private static final String[] mosArray = {"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
    private static String mos = "JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC";

    static final HashMap<String, String> ptrnStrs = new HashMap<>(24);
    static final HashMap<String, Pattern> ptrns = new HashMap<>(24);
    public SmsService() {   // sets up ptrnsStrs and ptrns
        super("SmsService");
        Log.d(TAG, ">>> SmsService()");
//        SMS_SENT = getPackageName() + "." + getClass().getName() + ".SMS_SENT";
//        SMS_DELIVERED = getPackageName() + "." + getClass().getName() + ".SMS_DELIVERED";
////        dbHelper = SmsDbHelper.getHelper(getApplicationContext());
////        dbHelper = SmsDbHelper.getHelper(SwarajApp.getAppCtx());
////        dbHelper = SmsDbHelper.getHelper();
////        for ( String str : mosArray ) mos += str + "|";   // set at declaration
////        mos = mos.substring( 0, mos.length() - 2 );       // set at declaration
////        smsMgr = SmsManager.getDefault();                 // set at declaration
//        // TODO - generate parts of regex strings from the db - aliases, etc.
//        ptrnStrs.put( "pin", "\\b (\\d{3}) (?:[-.]|\\s{1,3})? (\\d{3}) \\b");
//        ptrnStrs.put( "aadhaar", "\\b (\\d{4}) (?: (?:[-.]|\\s{1,3})? (\\d{4}) ){2} \\b" );
//        ptrnStrs.put( "upi", "\\b ( [-._A-Z0-9\\u0900-\\u09FF]+ @ [-._A-Z0-9\\u0900-\\u09FF]+ ) \\b" );
//        ptrnStrs.put( "phone", "\\b (\\d{5}) (?:[-.]|\\s{1,3})? (\\d{5}) \\b" );
//        ptrnStrs.put( "name", "\\b ( ([A-Z\\u0900-\\u09FF]+) (?:\\s{1,3}([A-Z\\u0900-\\u09FF]+)){1,3} ) \\b" );
//        ptrnStrs.put( "family", "\\b (\\d{1,2}) \\b" );
//        ptrnStrs.put( "lang", "\\b ( " +
//                "বাংলা | bn_bn | BANGLA | bn_EN | " +
//                "हिन्दी | hi_hi | HINDI | hi_EN | " +
//                "English | en_En ) \\b" );
//        ptrnStrs.put( "trade", "\\b ( " +
//                "BUY | \\# | \\+ | " +                        // 'B' is for bechna/becha i.e. SELL
//                "ক্রয় | ক্র | কেনা | কে | ক | KENA | KE | K |" +
//                "लेना | ले | ल | LENA | LE | L " +
//                "SELL | SEL | S | \\* | \\- | " +
//                "বিক্রয় | বিক্রি | বি | বেচা | বে | ব | BECHA | BE | B | " +
//                "बेचना | बे | ब | BECHNA ) \\b" );
//        ptrnStrs.put( "buy", "\\b (?: BUY | \\# | \\+ | " +   // 'B' is for bechna/becha i.e. SELL
//                "ক্রয় | ক্র | কেনা | কে | ক | KENA | KE | K | " +
//                "लेना | ले | ल | LENA | LE | L ) \\b" );
//        ptrnStrs.put( "sell", "\\b (?:  SELL | SEL | S | \\* | \\- | " +
//                "বিক্রয় | বিক্রি | বি | বেচা | বে | ব | BECHA | BE | B | " +
//                "बेचना | बे | ब | BECHNA ) \\b" );
//        ptrnStrs.put( "item", "\\b " +
//                "(?<ITEM>[_A-Z0-9\\u0900-\\u09FF]+) # " +          // item
//                "(?:(?<SUB>[_A-Z0-9\\u0900-\\u09FF]+) #)? " +     // sub-item
//                "(?<QUAL>[_A-Z0-9\\u0900-\\u09FF]+)? \\b" );       // quality
//        ptrnStrs.put( "price", "\\b (?:Rs\\. | Rs | R | ₹ | र | रु | র | রু | ট | টা | # ) \\s{0,2} (\\d+(?:[.*]\\d{2})?) \\b" );
//        // qty - a decimal w/o unit or '*' as unit is interpreted as Quantity in Standard Units of Item
//        ptrnStrs.put( "qty", "\\b (?<VAL> \\d+(?:[.*]\\d{1,3})? ) \\s{0,2} " +
//                "(?<UNIT> \\* | Q | QT | QUINTAL | क्विंटल | क्वि | क्व | কুইন্টাল | কু | " +
//                "KILO | KILOGRAM | K | KG | किलो | कि | क | কিলো | কি | ক | " +
//                "METRIC \\s{1,3} TON | M | MT | METRIC | TON | " +
//                "মেট্রিক \\s{1,3} টন | মেট্রিক | টন | মে | ম | ট | মট | " +
//                "म | मे | मीट्रिक \\s{1,3} टन | मीट्रिक | टन | ट | मट )? S? \\b" );
////            ptrnStrs.put( "quintal", "\\b (?: Q | QT | QUINTAL | क्विंटल | क्वि | क्व | কুইন্টাল | কু ) S? \\b" );
////            ptrnStrs.put( "kilo", "\\b (?: KILO | KILOGRAM | K | KG | किलो | कि | क | কিলো | কি | ক  ) S? \\b" );
////            ptrnStrs.put( "ton", "\\b (?: METRIC \\s{1,3} TON | M | MT | METRIC | TON | " +
////                    "মেট্রিক \\s{1,3} টন | মেট্রিক | টন | মে | ম | ট | মট | " +
////                    "म | मे | मीट्रिक \\s{1,3} टन | मीट्रिक | टन | ट | मट )? S? \\b" );
//
//        ptrnStrs.put( "oor", "\\b ( OWN | O | নিজের | নিজ | নি | ন | अप्ना | अ | " +
//                "LEASE | L | RENT | R | ভাড়া | ভা | ভ | भारा | भा | भ ) \\b" );
//        ptrnStrs.put( "own", "\\b (?: OWN | O | নিজের | নিজ | নি | ন | अप्ना | अ ) \\b" );
//        ptrnStrs.put( "rent", "\\b (?: LEASE | L | RENT | R | ভাড়া | ভা | ভ | भारा | भा | भ ) \\b" );
//
//        ptrnStrs.put( "acres", "\\b (?<VAL> \\d+(?:[.*]\\d{1,3})? ) \\s{0,2} " +
//                "(?<UNIT> A | AC | ACRE | B | BIGHA | H | HECTARE | " +
//                " एकर | ए | बीघा | बी | ब | हेक्टेयर  | हे | ह | " +
//                " একর | এ | বিঘা | বি | ব | হেক্টার  | হে | হ ) \\b" );
////            ptrnStrs.put( "acre", "\\b (?: A | AC | ACRE |  एकर | ए | একর | এ ) S? \\b" );
////            ptrnStrs.put( "bigha", "\\b (?: B | BG | BIGHA | बीघा | बी | ब | বিঘা | বি | ব ) S? \\b" );
////            ptrnStrs.put( "hectare", "\\b (?: H | HT | HECTARE | हेक्टेयर  | हे | ह | হেক্টার  | হে | হ ) S? \\b" );
//
//        ptrnStrs.put( "date", "\\b " +
//                "(?<YR>\\d{4}) [-/]? (?<MO>\\d{2}) [-/]? (?<DT>\\d{2}) | " +	            // 2017-09-04, 20170904
//                "(?<DT>\\d{1,2}) [-/] (?<MO>\\d{1,2}) [-/] (?<YR>\\d{4}|\\d{2}) | " +		// 04-09-2017, 4/9/17
//                "(?<MO>\\d{2}) [-/]? (?<DT>\\d{2}) | " +							        // 09-04, 0904
//                "(?<MO>\\d{1,2}) [-/] (?<DT>\\d{1,2}) | " +						            // 9-4, 9/4
//                "(?<DT>\\d{1,2}) [-/ ]? (?<MOS>" + mos + ") [-/ ]? (?<YR>\\d{2,4}) | " +	// 04-SEP-2017, 4 SEP 17
//                "(?<MOS>"+mos+") \\s{1,3} (?<DT>\\d{1,2}) , \\s{1,3} (?<YR>\\d{2,4}) | " +	// SEP 04, 2017;SEP 4, 17
//                "(?<DT>\\d{1,2}) [-/ ]? (?<MOS>" + mos + ") | " +					        // 04-SEP, 4 SEP
//                "(?<MOS>" + mos + ") [-/ ]? (?<DT>\\d{1,2})  \\b" );				        // SEP-04, SEP 4
//
//        for ( String key : ptrnStrs.keySet() ) {
//            Log.d(TAG, "SmsService() - processing regex:" + key);
//            ptrns.put(key, Pattern.compile(ptrnStrs.get(key), Pattern.COMMENTS
//                    | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS));
//        }
        Log.d(TAG, "<<< SmsService()");
    }

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
    @Override
    protected void onHandleIntent(@Nullable Intent in) {
        Log.d(TAG, ">>> onHandleIntent(): " );
        final Bundle b = in.getExtras();
        try {
            if (b != null) {
                long rts = new Date().getTime();
                Object[] pdus = (Object[]) b.get("pdus");
                Log.i(TAG, "_fromIntent(): Received " + pdus.length + " messages");
                int outC = 0;
                for (Object pdu : pdus) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                    Long mts = sms.getTimestampMillis();
                    String cid = sms.getOriginatingAddress();
                    String msg = sms.getMessageBody();
                    Log.i(TAG, "onHandleIntent():"+mts+":"+cid+":"+msg);

                    Reply reply = processSMS( mts, rts, cid, msg );
                    if ( reply == null )
                        Log.i(TAG, "onHandleIntent():No reply for msg from " + cid);
                    else
                        sendSMS( reply );
// TODO - deletions
//                    Log.d(TAG, "onHandleIntent():Deleting msg from " + cid);
//                    getContentResolver().delete(Uri.parse("content://sms/" + id), null, null);
                    outC += 1;
                }
                Log.i(TAG, "onHandleIntent():" + outC + " msgs processed");
            }
        } catch (Exception e) {
//            SmsDbHelper.log_error(e);
            Log.e(TAG, "_fromIntent():" + e );
            e.printStackTrace();
        }
        Log.d(TAG, "<<< onHandleIntent():" );
    }
    class Reply {
        Long msg_ts;
        Long rcv_ts;
        String phoneNo;
        String msg;
        Reply( Long msg_ts, Long rcv_ts, String phoneNo, String msg) {
            this.msg_ts = msg_ts;
            this.rcv_ts = rcv_ts;
            this.phoneNo = phoneNo;
            this.msg = msg;
        }
    }
    private Reply processSMS( Long msg_ts, Long rcv_ts, String cid, String msg ) {
        Log.d(TAG, "processSMS(): >>>" );
//        long rowid = dbHelper.insMsg( cid, msg, Long.valueOf(ts) );

//        HashMap<String, Object> mmbr = dbHelper.getMmbr( cid );
//        HashMap<String, HashMap<String, Bit>> bits = parseSMS( new StringBuilder(msg) );
//        Reply reply = getReply( mmbr, bits );

        Reply reply = null;
        if ( Pattern.matches( "^\\+91\\d{10}$", cid) || Pattern.matches( "^1555521\\d{4}$", cid) ) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            String out;
            if ( Pattern.matches("^<TEST> .+$", msg) ) {
                out = "Rcvd: <" + msg.substring(7) + ">";
                if (out.length() > 95) out = out.substring(0, 90) + " ...";
            } else out = OUT;
            out += "\nReply to msg from " + cid + " received at " + sdf.format(new Date(msg_ts));
            Log.i(TAG, "\tprocessSMS(): out " + out  );
            reply = new Reply( msg_ts, rcv_ts, cid, out );
            Log.i(TAG, "\tprocessSMS(): Replying to msg from " + cid  );
        } else if ( Pattern.matches( "^BA-PORTAL$", cid) ) {
            String out = "Payment @" + msg_ts + ":" + msg;
            if (out.length() > 160) out = out.substring(0, 153) + " . . .";
            reply = new Reply( msg_ts, rcv_ts, "+919748633792", out );
            Log.i(TAG, "\tprocessSMS(): Forwarding msg from " + cid  );
        } else {
            Log.w(TAG, "\tprocessSMS(): No reply to msg from " + cid  );
        }
        Log.d(TAG, "processSMS(): <<<" );
        return reply;
    }

    private void sendSMS(final Reply reply) {
        // see <https://mobiforge.com/design-development/sms-messaging-android>
        Log.d( TAG, "sendSMS(): >>>" );
        ArrayList<String> parts = smsMgr.divideMessage(reply.msg);
        Log.d( TAG, "sendSMS(): msg: length " + reply.msg.length() + "; parts " + parts.size() );
        ArrayList<PendingIntent> sendPIs = new ArrayList<>(parts.size());
        ArrayList<PendingIntent> dlvrPIs = new ArrayList<>(parts.size());
        for ( int i = 0; i < parts.size(); i++ ) {
            Intent intnt = new Intent();
            intnt.putExtra( "part_no", new Long(i + 1) );
            intnt.putExtra( "parts", new Long(parts.size()) );
            intnt.putExtra("msg", reply.msg );
            intnt.putExtra( "msg_ts", reply.msg_ts );
            intnt.putExtra( "phoneNo", reply.phoneNo );
            sendPIs.add(PendingIntent.getBroadcast(this, 0, intnt.setAction(SMS_SENT), PendingIntent.FLAG_UPDATE_CURRENT));
            dlvrPIs.add(PendingIntent.getBroadcast(this, 0, intnt.setAction(SMS_DELIVERED), PendingIntent.FLAG_UPDATE_CURRENT));
            Log.d( TAG, "sendSMS(): sending parts " + intnt.getLongExtra("part_no",0) + " of " + intnt.getLongExtra("parts",0) );
        }

//        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, intnt.setAction(SMS_SENT), 0 );
//        PendingIntent dlvrPI = PendingIntent.getBroadcast(this, 0, intnt.setAction(SMS_DELIVERED), 0 );

//        smsMgr.sendTextMessage( reply.phoneNo, null, reply.msg, sentPI, dlvrPI );   //returns void
        smsMgr.sendMultipartTextMessage(reply.phoneNo, null, parts, sendPIs, dlvrPIs );

//        int c = dbHelper.updMsg( reply.msg );
        Log.i(TAG, "sendSMS():queued reply to " + reply.phoneNo );
        Log.d(TAG, "sendSMS(): >>>" );
    }
}
