package site.swaraj.jaikisan;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

import static android.database.Cursor.FIELD_TYPE_BLOB;
import static android.database.Cursor.FIELD_TYPE_FLOAT;
import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.database.Cursor.FIELD_TYPE_STRING;

/**
 * Created by rajag on 2017-09-30.
 *
 * TODO - seperate inserts from creates creates, generate inserts from data, write proper onUpgrade() method
 */

abstract class AbstractDb extends SQLiteOpenHelper {
    private String TAG = "JK:";
    private int DB_VER;
    private String name;
    SQLiteDatabase db;
    private ArrayList<String> createSQLs;
    private ArrayList<String> dropSQLs;

    AbstractDb( Context ctx, String name, Integer ver, Boolean rw ) {
        super( ctx, "jk." + name + "." + ver + ".db", null, ver );
        Log.i( TAG, "AbstractDb(): <<<" );

        this.TAG += getClass().getName();
        this.DB_VER = ver;
        this.name = name;
        this.dropSQLs = _getSqls( name + ".drops.");
        this.createSQLs = _getSqls(name + ".creates.");

        db = rw ? getWritableDatabase() : getReadableDatabase();
        Log.i( TAG, "AbstractDb(): >>>" );
    }
    private ArrayList<String> _getSqls( String fn ) {
        Log.d(TAG, "_getSqls(): <<<");
        String fqn = SwarajApp.dbDir + "/sqls/jk." + fn + DB_VER + ".sql";
        Log.d(TAG, "_getSqls(): fqn:" + fqn );
        ArrayList<String> al = new ArrayList<>();
        String line = null;
        StringBuilder sql = null;
        try {
//            String line;
//            StringBuilder sql = null;
            boolean inStmt = false;
            BufferedReader br = new BufferedReader( new FileReader(new File(fqn)) );
            while( (line = br.readLine()) != null ) {
                if ( !inStmt && Pattern.matches("^-- START$", line) ) {
                    inStmt = true;
                    sql = new StringBuilder();
                } else if ( inStmt && Pattern.matches("^-- STOP$", line) ) {
                    if ( sql.length() > 1 ) {
                        sql.deleteCharAt(sql.length() - 1); //.append(';');
                        al.add(sql.toString());
                    }
                    inStmt = false;
                } else if ( Pattern.matches("^\\s*(?:--.*|\\.|\\s*)$", line) ) {
                    continue;
                } else if ( inStmt ) {
                    sql.append(line.trim()).append(" ");
                }
            }
            br.close();
        } catch ( IOException ioe ) {
//            log_error( ioe );
            Log.e( TAG, "cannot read file " + fqn );
        } catch ( Exception e ) {
//            log_error( ioe );
            Log.e( TAG, "error: " +":"+ line + fqn +":"+ sql );
        }
        Log.d(TAG, "_getSqls(): >>>");
        return al;
    }
    HashMap<String, Object> getRow( String tbl, String[] cols, String where, String[] args ) {
        Cursor c = db.query( tbl, cols, where, args, null, null, null );
        HashMap<String, Object> hm = null;
        if ( c.moveToFirst() ) {
            hm = new HashMap<>(cols.length);
            for (int i = 0; i < c.getColumnCount(); i++)
                switch (c.getType(i)) {
                    case FIELD_TYPE_INTEGER:
                        hm.put(c.getColumnName(i), c.getInt(i));
                        break;
                    case FIELD_TYPE_FLOAT:
                        hm.put(c.getColumnName(i), c.getFloat(i));
                        break;
                    case FIELD_TYPE_STRING:
                        hm.put(c.getColumnName(i), c.getString(i));
                        break;
                    case FIELD_TYPE_BLOB:
                        hm.put(c.getColumnName(i), c.getBlob(i));
                        break;
                    case FIELD_TYPE_NULL:
                        hm.put(c.getColumnName(i), null);
                        break;
                }
        }
        return hm;
    }
    @Override public void onOpen( SQLiteDatabase db ) {
        Log.i( TAG, "onOpen(): <<<" );
        Log.i(TAG, (db.isOpen() ? "Is" : "Is Not") + " Open" );
        Log.i(TAG, (db.isDatabaseIntegrityOk() ? "Is" : "Is Not") + " DatabaseIntegrityOk" );
        Log.i(TAG, (db.isDbLockedByCurrentThread() ? "Is" : "Is Not") + " DbLockedByCurrentThread" );
        Log.i(TAG, (db.isDbLockedByOtherThreads() ? "Is" : "Is Not") + " DbLockedByOtherThreads" );
        Log.i(TAG, (db.isReadOnly() ? "Is" : "Is Not") + " ReadOnly" );
        Log.i(TAG, (db.isWriteAheadLoggingEnabled() ? "Is" : "Is Not") + " WriteAheadLoggingEnabled" );
        Log.i( TAG, "onOpen(): >>>" );
    }
    @Override public void onCreate( SQLiteDatabase db ) {
        Log.i( TAG, "onCreate(): <<<" );
        for ( String sql : createSQLs ) {
            Log.d(TAG, sql);
            db.execSQL( sql );
        }
        Log.i( TAG, "onCreate(): >>>" );
    }
    @Override public void onConfigure( SQLiteDatabase db ) {
        Log.i( TAG, "onConfigure(): <<<" );
        db.enableWriteAheadLogging();
        db.setForeignKeyConstraintsEnabled( true );
//        db.execSQL("PRAGMA foreign_keys = ON;");
        Log.i( TAG, "onConfigure(): >>>" );
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        Log.i( TAG, "onUpgrade(): <<<" + oldVer +":"+ newVer );
        if ( oldVer == 0 && newVer == 1 ) for ( String sql : dropSQLs ) db.execSQL( sql );
        Log.i( TAG, "onUpgrade(): <<<" );
    }
    @Override public void onDowngrade(SQLiteDatabase db, int oldVer, int newVer) {
        Log.i( TAG, "onUpgrade(): <<<" );
        if ( oldVer == 0 && newVer == 1 ) for ( String sql : dropSQLs ) db.execSQL( sql );
        Log.i( TAG, "onUpgrade(): <<<" );
    }
}
class DataDb extends AbstractDb {
    private static final String TAG = "JK:DataDb";

    private static SQLiteStatement mmbrUpdStmt;
//    int updMmbrPin( int rowid, String pin ) {
//        int c = 0;  //  UPDATE member SET pin = ?  WHERE _id = ?;
//        try {
//            rplyUpdStmt.clearBindings();
//            rplyUpdStmt.bindString( 1, reply.out );
//            rplyUpdStmt.bindLong( 2, reply.rid );
//            c = rplyUpdStmt.executeUpdateDelete();
//        } catch ( Exception e) {
//            log_error(e, reply.rid);
//        }
//        Log.d(TAG, "updMsg(): >>>");
//        return c;
//    }

    private static SQLiteStatement mmbrInsStmt;

    HashMap<String, Object> insMmbr(SmsService.Reply reply) {
        HashMap<String, Object> mmbr = null;
        try {   //  "INSERT INTO member ( cid, ref, flags, state, msgC, last_msg ) VALUES ( ?, ?, ?, ?, ?, ? );"
            mmbrInsStmt.clearBindings();;
            mmbrInsStmt.bindString( 1, reply.cid );
            mmbrInsStmt.bindString( 2, "+919748633792" );
            mmbrInsStmt.bindLong( 3, Member.CID );
            mmbrInsStmt.bindString( 4, "RECORDED" );
            mmbrInsStmt.bindLong( 5, 1 );
            mmbrInsStmt.bindString( 6, SwarajApp.sdf.format(reply.mts) );
            long rowid = mmbrInsStmt.executeInsert();
//            Log.d(TAG, "insMmbr():owid: " + rowid);
            mmbr = getRow( "member", Member.mmbr_ordr, "_id = ? ", new String[]{Long.toString(rowid)} );
        } catch ( Exception e) {
            LogDb.log_error( e, reply.rid);
        }
//        Log.d(TAG, "insMmbr(): " + (mmbr == null ? "<NULL>" : "<VALID>") );
        return mmbr;
    }

    DataDb(Context ctx) {
        super( ctx, "data", 1, true );
        Log.i( TAG, "DataDb(): <<<" );
        mmbrInsStmt = db.compileStatement( "INSERT INTO member ( cid, ref, flags, state, msgC, last_msg ) " +
                "VALUES ( ?, ?, ?, ?, ?, ? );" );
//        mmbrUpdStmt = db.compileStatement( "UPDATE member SET " +
//                "( cid, ref, last_msg ) VALUES ( ?, ?, ? );" );

        Log.i( TAG, "LogDb(): >>>" );
    }

//        Member member(Member mmbr) {}

}
class LogDb extends AbstractDb {
    private static String TAG = "JK:LogDb";

    static SQLiteStatement errInsStmt;
    private SQLiteStatement msgInsStmt;
    private SQLiteStatement rplyUpdStmt;
    private SQLiteStatement enqUpdStmt;
    private SQLiteStatement sntUpdStmt;
    private SQLiteStatement dlvUpdStmt;
    private SQLiteStatement badNumStmt;
    private SQLiteStatement bsnlInsStmt;
    private SQLiteStatement bsnlUpdStmt;

    int updBsnl( String refId ) {
        Log.d(TAG, "updBsnl(): <<<");
        int c = 0;
        try {   //  "UPDATE bsnl_sms_recharge SET cmpltd = ?  WHERE ref_id = ?;"
            bsnlUpdStmt.clearBindings();
            bsnlUpdStmt.bindString( 1, SwarajApp.sdf.format(new Date()) );
            bsnlUpdStmt.bindString( 2, refId );
            c = rplyUpdStmt.executeUpdateDelete();
        } catch ( Exception e) {
            log_error(e, 0);
        }
        Log.d(TAG, "updBsnl(): >>>");
        return c;
    }
    void insBsnl( float amt, String at, String refId ) {
        Log.d(TAG, "updBsnl(): <<<");
        try {    // "INSERT INTO bsnl_sms_recharge( amt, crdtd_at, ref_id ) VALUES( ?, ?, ? );"
            bsnlInsStmt.clearBindings();
            bsnlInsStmt.bindDouble( 1, amt );
            bsnlInsStmt.bindString( 2, at );
            bsnlInsStmt.bindString( 3, refId );
            msgInsStmt.executeInsert();
        } catch ( Exception e) {
            log_error(e, 0);
//            Log.e(TAG, "insMsg():" + e );
//            e.printStackTrace();
        }
//            msg_rowid = rowid;
        Log.d(TAG, "updBsnl(): >>>");
    }

    int updDlv(long rid) {
        int c = 0;  //  UPDATE msg_log SET dlv_ts = ?  WHERE rowid = ?;
        try {
            dlvUpdStmt.clearBindings();
            dlvUpdStmt.bindLong( 1, new Date().getTime() );
            dlvUpdStmt.bindLong( 2, rid );
            c = dlvUpdStmt.executeUpdateDelete();
        } catch ( Exception e) {
            log_error(e, rid);
        }
//            msg_rowid = 0;
        return c;
    }
    int updSnt(long rid) {
        int c = 0;  //  UPDATE msg_log SET snt_ts = ?  WHERE rowid = ?;
        try {
            sntUpdStmt.clearBindings();
            sntUpdStmt.bindLong( 1, new Date().getTime() );
            sntUpdStmt.bindLong( 2, rid );
            c = sntUpdStmt.executeUpdateDelete();
        } catch ( Exception e) {
            log_error(e, rid);
        }
        return c;
    }
    int updEnq(long rid) {
        int c = 0;  //  UPDATE msg_log SET enq_ts = ?  WHERE rowid = ?;
        try {
            enqUpdStmt.clearBindings();
            enqUpdStmt.bindLong( 1, new Date().getTime() );
            enqUpdStmt.bindLong( 2, rid );
            c = enqUpdStmt.executeUpdateDelete();
        } catch ( Exception e) {
            log_error(e, rid);
        }
        return c;
    }
    int updMsg(SmsService.Reply reply) {
        Log.d(TAG, "updMsg(): <<<");
        int c = 0;
        try {   //  "UPDATE msg_log SET out_msg = ?, send = ?  WHERE rowid = ?;"
            rplyUpdStmt.clearBindings();
            rplyUpdStmt.bindString( 1, reply.out );
            rplyUpdStmt.bindLong( 2, reply.send ? 1 : 0 );
            rplyUpdStmt.bindLong( 3, reply.rid );
            c = rplyUpdStmt.executeUpdateDelete();
        } catch ( Exception e) {
            log_error(e, reply.rid);
        }
        Log.d(TAG, "updMsg(): >>>");
        return c;
    }
    long insMsg( SmsService.Reply reply ) {
        long rowid = 0;
        try {   //  "INSERT INTO msg_log( cid, in_msg, msg_ts, rcv_ts ) VALUES( ?, ?, ?, ? );"
            msgInsStmt.clearBindings();
            msgInsStmt.bindString( 1, reply.cid );
            msgInsStmt.bindString( 2, reply.msg );
            msgInsStmt.bindLong( 3, reply.mts );
            msgInsStmt.bindLong( 4, reply.rts );
            rowid = msgInsStmt.executeInsert();
        } catch ( Exception e) {
            log_error(e, rowid);
//            Log.e(TAG, "insMsg():" + e );
//            e.printStackTrace();
        }
//            msg_rowid = rowid;
        return rowid;
    }
    boolean badNum( SmsMessage sms ) {
        int c = 0;  // "UPDATE bad_number SET last_msg = ?, last_date = ? WHERE cid = ?;"
        try {
            badNumStmt.clearBindings();
            badNumStmt.bindString( 1, sms.getMessageBody() );
            badNumStmt.bindString( 2, SwarajApp.sdf.format(new Date(sms.getTimestampMillis())) );
            badNumStmt.bindString( 3, sms.getOriginatingAddress() );
            c = badNumStmt.executeUpdateDelete();
        } catch ( Exception e) {
            log_error(e, 0);
        }
        return c == 1 ? true : false;
    }
    // TODO - move log_error to SmsDbHlpr
    static void log_error(Exception e, long rid) {
        try {
            StackTraceElement[] stes = e.getStackTrace();
            StackTraceElement ste;
            int back = 0;
            do {
                ste = stes[back++];
            } while ( back < stes.length && ! Pattern.matches( "^site\\.swaraj\\.jaikisan(?:\\.\\w+)+$", ste.getClassName() ) );
            errInsStmt.clearBindings();
            errInsStmt.bindString( 1, ste.getClassName() );
            errInsStmt.bindString( 2, ste.getMethodName() + "()" );
            errInsStmt.bindString( 3, ste.getFileName() );
            errInsStmt.bindLong  ( 4, ste.getLineNumber() );
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            errInsStmt.bindString( 5, sw.toString() );          // stack trace
            errInsStmt.bindString( 6, e.getClass().getName() ); // error
            errInsStmt.bindString( 7, e.getMessage() == null ? "<NULL MESSAGE>" : e.getMessage() );
            errInsStmt.bindLong  ( 8, rid );
            Log.w( TAG, "log_error(): "+e.getMessage()+":"+ste.getFileName()+":"+ste.getMethodName()+":"+ste.getLineNumber() );
            errInsStmt.executeInsert();
        } catch ( Exception e2 ) {
            throw new RuntimeException(
                    "\n---------------------------------------------"
                            + "\n  Error in source     : " + e.getMessage()
                            + "\n  Error in log_error(): " + e2.getMessage()
                            + "\n---------------------------------------------\n"
            );
        }
    }

    LogDb(Context ctx) {
        super( ctx, "log", 1, true );
        Log.i( TAG, "LogDb(): <<<" );
        errInsStmt = db.compileStatement( "INSERT INTO err_log"
                + "( class, method, file, line_no, stack, err_class, err_msg, msg_rowid ) "
                + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ? );" );
        msgInsStmt = db.compileStatement(
                "INSERT INTO msg_log( cid, in_msg, msg_ts, rcv_ts ) VALUES( ?, ?, ?, ? );" );
        rplyUpdStmt = db.compileStatement( "UPDATE msg_log SET out_msg = ?, send = ?  WHERE rowid = ?;" );
        enqUpdStmt = db.compileStatement( "UPDATE msg_log SET enq_ts = ?  WHERE rowid = ?;" );
        sntUpdStmt = db.compileStatement( "UPDATE msg_log SET snt_ts = ?  WHERE rowid = ?;" );
        dlvUpdStmt = db.compileStatement( "UPDATE msg_log SET dlv_ts = ?  WHERE rowid = ?;" );
        badNumStmt = db.compileStatement( "UPDATE bad_number SET last_msg = ?, last_date = ? WHERE cid = ?;" );
        bsnlInsStmt = db.compileStatement(
                "INSERT INTO bsnl_sms_recharge( amt, crdtd_at, ref_id ) VALUES( ?, ?, ? );" );
        bsnlUpdStmt = db.compileStatement( "UPDATE bsnl_sms_recharge SET cmpltd_at = ?  WHERE ref_id = ?;" );
        Log.i( TAG, "LogDb(): >>>" );
    }
}
class MetaDb extends AbstractDb {
    private static String TAG = "JK:MetaDb";


    MetaDb(Context ctx) {
        super( ctx, "meta", 1, false );
        Log.i(TAG, "MetaDb(): <<<");

        Log.i(TAG, "MetaDb(): >>>");
    }

 }
