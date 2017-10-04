package site.swaraj.jaikisan;

import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by rajag on 2017-08-10.
 */

public class SwarajApp extends Application {

    private static final String TAG = "JK:SwarajApp";
    static final String dbDir = "/Removable/MicroSD/Android/apps/site.swaraj.jaikisan/databases";
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM hh:mm:ss");
    private static Context ctx;
    private static SmsDbHlpr dbs;

    static final HashMap<String, String> ptrnStrs = new HashMap<>(24);
    static final HashMap<String, Pattern> ptrns = new HashMap<>(24);

    public void onCreate() {
        Log.d(TAG,"onCreate(): <<<");
        super.onCreate();
        // < KLUDGE to run on emulator main memory or on device sdcard(path hard-coded in DbCtx)>
        ctx = Environment.getExternalStorageDirectory().getAbsolutePath().equals("/storage/sdcard0")
                ? new DbCtx(getApplicationContext()) : getApplicationContext();
        // </KLUDGE>
        dbs = new SmsDbHlpr( ctx );

        final String[] mosArray = {"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
        final String mos = "JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC";
        final HashMap<String, String> mosHash = new HashMap<String, String>(12);
        for ( int i = 0; i < mosArray.length; i++ ) mosHash.put( mosArray[i], String.format("%02d", i+1) );
//        final HashMap<String, String> mosHash_1 = new HashMap<String, String>(12){{put("JAN","01");put("FEB","02");
//            put("MAR","03");put("APR","04");put("MAY","05");put("JUN","06");put("JUL","07");
//            put("AUG","08");put("SEP","09");put("OCT","10");put("NOV","11");put("DEC","12"); }};
//        for ( String str : mosArray ) mos += str + "|";   // set at declaration
//        mos = mos.substring( 0, mos.length() - 2 );       // set at declaration

        // TODO - generate parts of regex strings from the db - aliases, etc.
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

        Log.d(TAG,"onCreate(): >>>");
    }

    public static SmsDbHlpr getDbs() {
        return dbs;
    }
    public static Context getCtx() {
        return ctx;
    }
    public static Pattern getPtrn(String key) {
        return ptrns.get(key);
    }
//    public static String getDbDir() { return dbDir; }
    public static void deleteSMS( Context ctx, String cid, Long mts ) {
        Log.d(TAG, "deleteSMS(): <<<");
        try {
            Cursor c = ctx.getContentResolver().query(Uri.parse("content://sms/inbox"),
                    new String[]{"_id", "address", "body", "date", "date_sent"}, null, null, null);
            if (c != null && c.moveToFirst()) {
                do {
                    String id = c.getString(0);
                    String addr = c.getString(1);
                    String body = c.getString(2);
                    Long msg_ts = c.getLong(3);
                    Long snt_ts = c.getLong(4);
//                        Log.d(TAG, "delIf...(): [" + cid + "=" + addr + "] : [" + mts + "=" + snt_ts + "] :[" + msg + "=" + body + "]" );
//                        Log.d(TAG, "delIf...(): [" + cid.equals(addr) + "] : [" + mts.equals(snt_ts) + "] : [" + msg.equals(body) + "]" );
//                        Log.d(TAG, "delIf...(): " + mts + "=" + snt_ts + "] : [" + rts + "=" + msg_ts + "] : rid:"+ rid );
                    if ( cid.equals(addr) && mts.equals(snt_ts) ) {
                        Log.i( TAG, "deleteSMS(): Deleting " + id + ":" + cid + "@" + mts );
                        ctx.getContentResolver().delete(Uri.parse("content://sms/" + id), null, null);
                    }
                } while (c.moveToNext());
                c.close();
            }
        } catch (Exception e) {
            //            SmsDbHelper.log_error(e);
            Log.e(TAG, "deleteSMS():" + e);
        }
        Log.d(TAG, "deleteSMS(): >>>");
    }
}

class DbCtx extends ContextWrapper {
    private static final String TAG = "JK:DbCtx";

    public DbCtx(Context base) {
        super(base);
    }

    @Override public File getDatabasePath(String name) {
//        File sdcard = Environment.getExternalStorageDirectory();
//        String fqn = sdcard.getAbsolutePath() + File.separator + "apps"
//                + File.separator + "site.swaraj.jaikisan"
//                + File.separator+ "databases" + File.separator + name;
        String fqn = SwarajApp.dbDir + "/dbs/" + name;
        if ( ! fqn.endsWith(".db") ) fqn += ".db";
        File dbfile = new File( fqn );
        Boolean c = null;
        if ( ! dbfile.getParentFile().exists() ) c = dbfile.getParentFile().mkdirs();

        Log.i( TAG, "getDatabasePath(" + name + ") => " + dbfile.getAbsolutePath() + ":" + c + ":" + dbfile.getParentFile().exists()  );
        return dbfile;
    }
    @Override public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler ) {
        SQLiteDatabase db = openOrCreateDatabase( name, mode, factory);
        Log.i( TAG, "openOrCreateDatabase(" + name + ",mode,factory,error) => " + db.getPath() );
        return db;
    }
    @Override public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory ) {
        SQLiteDatabase db =  SQLiteDatabase.openOrCreateDatabase( getDatabasePath(name), null );
        Log.i( TAG, "openOrCreateDatabase(" + name + ",mode,factory) => " + db.getPath() );
        return db;
    }
}
