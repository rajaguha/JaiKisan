package site.swaraj.pricefinder;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.util.Log;

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
 * Created by rajag on 2017-07-29.
 */

class SmsDbHelper { // extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DB_VER = 1;
    private static final String TAG = "pF:SmsDbHelper";
    private static String DB_PATH;
    private static SmsDbHelper HELPER = null;
    private static long msg_rowid = 0;
    private static SQLiteDatabase log;
    private static SQLiteDatabase data;
    private static SQLiteDatabase meta;

    static SQLiteStatement errInsStmt;
    static SQLiteStatement msgInsStmt;
    static SQLiteStatement rplyUpdStmt;
    static SQLiteStatement sntUpdStmt;
    static SQLiteStatement dlvUpdStmt;
//    private static SQLiteStatement lstIdStmt;

    // for Android APIs 11 to 20, SQLite ver. is 3.7
    ////    vvvv    ////    vvvv    ////    vvvv    ////    vvvv    ////    vvvv    ////    vvvv    ////
    HashMap<String, Object> getMmbr( String phone ) throws SQLException {
        String[] cols = new String[]{ "phone", "pin", "lang", "dob", "name"
                , "aadhaar", "upi", "ref", "famiily", "acres", "own", "_insert_at"
                , "_id", "rowid" };
        String[] typs = new String[]{ "STRING", "pin", "lang", "dob", "name"
                , "aadhaar", "upi", "ref", "famiily", "acres", "own", "_insert_at"
                , "_id", "rowid" };
        HashMap<String, Object> mmbr = new HashMap<>( cols.length );
        Cursor c = data.query("member", cols,"phone = ?", new String[]{phone}, null, null, null );
        int count = c.getCount();
        if ( count == 0 )
            mmbr = null;
        else if ( count == 1 ) {
            for ( String col : cols ) {
                int i = c.getColumnIndex(col);
                switch ( c.getType(i) ) {
                    case android.database.Cursor.FIELD_TYPE_STRING:
                        mmbr.put( col, c.getString(i) );
                        break;
                    case FIELD_TYPE_INTEGER:
                        mmbr.put( col, c.getLong(i) );
                        break;
                    case FIELD_TYPE_FLOAT:
                        mmbr.put( col, c.getDouble(i) );
                        break;
                    case FIELD_TYPE_NULL:
                        mmbr.put( col, null );
                        break;
                    case android.database.Cursor.FIELD_TYPE_BLOB:
                        mmbr.put( col, c.getBlob(i) );
                        break;
                }
            }
        } else {
            throw new SQLException("Duplicate entries in member for phone "+ phone);
        }
        return mmbr;
    }

    String translate( String str, String lang ) {
        Cursor c = meta.query("translat",new String[]{lang},"en_En = ?",new String[]{str},null,null,null);
        String translation = str;
        if ( c.getCount() == 1 ) {
            c.moveToFirst();
            translation = c.getString(0);
        }
        c.close();
        return translation;
    }

    HashMap<String, Object> getStdName( String[] item, String unit ) {
        Cursor c = null;
        if ( item.length == 3 ) {
            c = meta.query(
                // "std_name"                                       // table_name
                true, "std_name"                                  // DISTINCT, table name
                , new String[]{"from_unit","to_unit","conv","std_main","std_sub","std_qual","sid_main","sid_sub","sid_qual","COUNT(*) AS count"} // fields
                , "src_unit LIKE ? AND alias_main LIKE ? AND alias_sub LIKE ? AND alias_qual LIKE ?" // WHERE clause
                , new String[]{unit + "%", item[0] + "%", item[1] + "%", item[2] + "%"}              // WHERE clause args
                , null // , "from_unit,to_unit,conv,std_main,std_sub,std_qual,sid_main,sid_sub,sid_qual"       // GROUP BY" +
                , null, null, null );
        } else if ( item.length == 2 ) {
            c = meta.query(
                // "std_name"                                       // table_name
                true, "std_name"                                  // DISTINCT, table name
                , new String[]{"from_unit", "to_unit", "conv", "std_main", "std_sub", "sid_main", "sid_sub", "COUNT(*) AS count"} // fields
                , "src_unit LIKE ? AND alias_main LIKE ? AND alias_sub LIKE ?" // WHERE clause
                , new String[]{unit + "%", item[0] + "%", item[1] + "%"}              // WHERE clause args
                , null // , "from_unit,to_unit,conv,std_main,std_sub,sid_main,sid_sub"       // GROUP BY" +
                , null, null, null
            );
        } else if ( item.length == 1 ) {
            c = meta.query(
                    // "std_name"                                       // table_name
                    true, "std_name"                                  // DISTINCT, table name
                    , new String[]{"from_unit", "to_unit", "conv", "std_main", "sid_main", "COUNT(*) AS count"} // fields
                    , "src_unit LIKE ? AND alias_main LIKE ?" // WHERE clause
                    , new String[]{unit + "%", item[0] + "%", item[1] + "%"}              // WHERE clause args
                    , null // , "from_unit,to_unit,conv,std_main,sid_main"       // GROUP BY" +
                    , null, null, null
            );
        }
        HashMap<String, Object> hm = getRow( c );
        c.close();
        return hm;
    }
    HashMap<String, Object> convert( String item, String unit ) throws SQLException {
        Cursor c = meta.rawQuery( "SELECT alias_main, src_unit, from_unit, to_unit, conv " +
                        " FROM std_name WHERE UPPER(alias_main) = ? AND UPPER(src_unit) = ?;"
                , new String[]{item.toUpperCase(), unit.toUpperCase()} );
        return getRow( c );
    }

////    ^^^^    ////    ^^^^    ////    ^^^^    ////    ^^^^    ////    ^^^^    ////    ^^^^    ////
    String getCell( SQLiteDatabase db, String sql, String[] args ) throws SQLException {
        return getRow( db, sql, args ).values().toArray()[0].toString();
    }
    String getCell( SQLiteDatabase db, String tbl, String fld, String where, String[] args )
            throws SQLException {
        return getRow( db, tbl, new String[]{fld}, where, args ).get( fld ).toString();
    }
    String getCell( Cursor c ) throws SQLException {
        return getRow( c ).values().toArray()[0].toString();
    }

    HashMap<String, Object> getRow( SQLiteDatabase db, String sql, String[] args )
            throws SQLException {
        ArrayList<HashMap<String, Object>> al = getTable( db, sql, args, 1 );
        switch ( al.size() ) {
            case 0: throw new RowNotFoundException();
            case 1: return al.get(0);
            default: throw new TooManyRowsException();
        }
    }
    HashMap<String, Object> getRow(
            SQLiteDatabase db, String tbl, String[] flds, String where, String[] args ) throws SQLException {
        ArrayList<HashMap<String, Object>> al = getTable( db, tbl, flds, where, args, 1 );
        switch ( al.size() ) {
            case 0: throw new RowNotFoundException();
            case 1: return al.get(0);
            default: throw new TooManyRowsException();
        }
    }
    HashMap<String, Object> getRow( Cursor c ) throws SQLException {
        ArrayList<HashMap<String, Object>> al = getTable( c );
        switch ( al.size() ) {
            case 0: throw new RowNotFoundException();
            case 1: return al.get(0);
            default: throw new TooManyRowsException();
        }
    }

    ArrayList<HashMap<String, Object>> getTable(
            SQLiteDatabase db, String sql, String[] args, int count ) throws SQLException {
        ArrayList<HashMap<String, Object>> tbl = new ArrayList<>(count);
        Cursor c = db.rawQuery( sql, args );
        String[] colNames = c.getColumnNames();
        while ( c.moveToNext() ) {
            HashMap<String, Object> hm = new HashMap<>(colNames.length);
            for ( int i = 0; i < colNames.length; i++ ) {
                int type = c.getType(i);
                if ( type == FIELD_TYPE_NULL )
                    hm.put( colNames[i], null );
                else if ( type == FIELD_TYPE_INTEGER )
                    hm.put( colNames[i], c.getInt(i) );
                else if ( type == FIELD_TYPE_FLOAT )
                    hm.put( colNames[i], c.getFloat(i) );
                else if ( type == FIELD_TYPE_STRING )
                    hm.put( colNames[i], c.getString(i) );
                else if ( type == FIELD_TYPE_BLOB )
                    hm.put( colNames[i], c.getBlob(i) );
            }
            tbl.add( hm );
        }
        c.close();
        return tbl;
    }
    ArrayList<HashMap<String, Object>> getTable(
            SQLiteDatabase db, String tbl, String[] flds, String where, String[] args, int count ) throws SQLException {
        ArrayList<HashMap<String, Object>> al = new ArrayList<>(count);
        Cursor c = db.query( tbl, flds, where, args, null, null, null );
        String[] colNames = c.getColumnNames();
        while ( c.moveToNext() ) {
            HashMap<String, Object> hm = new HashMap<>(colNames.length);
            for ( int i = 0; i < colNames.length; i++ ) {
                int type = c.getType(i);
                if ( type == FIELD_TYPE_NULL )
                    hm.put( colNames[i], null );
                else if ( type == FIELD_TYPE_INTEGER )
                    hm.put( colNames[i], c.getInt(i) );
                else if ( type == FIELD_TYPE_FLOAT )
                    hm.put( colNames[i], c.getFloat(i) );
                else if ( type == FIELD_TYPE_STRING )
                    hm.put( colNames[i], c.getString(i) );
                else if ( type == FIELD_TYPE_BLOB )
                    hm.put( colNames[i], c.getBlob(i) );
            }
            al.add( hm );
        }
        db.close();
        return al;
    }
    ArrayList<HashMap<String, Object>> getTable( Cursor c ) throws SQLException {
        ArrayList<HashMap<String, Object>> tbl = new ArrayList<>(c.getCount());
        String[] colNames = c.getColumnNames();
        while ( c.moveToNext() ) {
            HashMap<String, Object> hm = new HashMap<>(colNames.length);
            for ( int i = 0; i < colNames.length; i++ ) {
                int type = c.getType(i);
                if ( type == FIELD_TYPE_NULL )
                    hm.put( colNames[i], null );
                else if ( type == FIELD_TYPE_INTEGER )
                    hm.put( colNames[i], c.getInt(i) );
                else if ( type == FIELD_TYPE_FLOAT )
                    hm.put( colNames[i], c.getFloat(i) );
                else if ( type == FIELD_TYPE_STRING )
                    hm.put( colNames[i], c.getString(i) );
                else if ( type == FIELD_TYPE_BLOB )
                    hm.put( colNames[i], c.getBlob(i) );
            }
            tbl.add( hm );
        }
        c.close();
        return tbl;
    }

    int updDlv() {
        int c = 0;
        long ts = new Date().getTime();
        try {
            dlvUpdStmt.clearBindings();
            dlvUpdStmt.bindLong( 1, ts );
            dlvUpdStmt.bindLong( 2, msg_rowid );
            c = dlvUpdStmt.executeUpdateDelete();
        } catch ( Exception e) {
            log_error(e);
        }
        msg_rowid = 0;
        return c;
    }
    int updSnt() {
        int c = 0;
        long ts = new Date().getTime();
        try {
            sntUpdStmt.clearBindings();
            sntUpdStmt.bindLong( 1, ts );
            sntUpdStmt.bindLong( 2, msg_rowid );
            c = sntUpdStmt.executeUpdateDelete();
        } catch ( Exception e) {
            log_error(e);
        }
        return c;
    }
    int updMsg( String msg ) {
        int c = 0;
        try {
            rplyUpdStmt.clearBindings();
            rplyUpdStmt.bindString( 1, msg );
            rplyUpdStmt.bindLong( 2, msg_rowid );
            c = rplyUpdStmt.executeUpdateDelete();
        } catch ( Exception e) {
            log_error(e);
        }
        return c;
    }
    long insMsg( String phoneNo, String in_msg, long sms_ts ) {
        long rowid = 0;
        long ts = new Date().getTime();
        try {
            msgInsStmt.clearBindings();
            msgInsStmt.bindString( 1, phoneNo );
            msgInsStmt.bindString( 2, in_msg );
            msgInsStmt.bindLong( 3, sms_ts );
            msgInsStmt.bindLong( 4, ts );
            rowid = msgInsStmt.executeInsert();
        } catch ( Exception e) {
            log_error(e);
        }
        msg_rowid = rowid;
        return rowid;
    }
    static void log_error(Exception e) {
        try {
            StackTraceElement[] stes = e.getStackTrace();
            StackTraceElement ste;
            int back = 0;
            do {
                ste = stes[back++];
            } while ( back < stes.length && ! Pattern.matches( "^site\\.swaraj\\.pricefinder", ste.getClassName() ) );
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            errInsStmt.clearBindings();
            errInsStmt.bindString( 1, ste.getClassName() ); // this.getClass().getName() );
            errInsStmt.bindString( 2, ste.getMethodName() + "()" );
            errInsStmt.bindString( 3, ste.getFileName() );
            errInsStmt.bindLong  ( 4, ste.getLineNumber() );
            errInsStmt.bindString( 5, sw.toString() );
            errInsStmt.bindString( 6, e.getClass().getName() );
            errInsStmt.bindString( 7, e.getMessage() == null ? "<NULL MESSAGE>" : e.getMessage() );
            errInsStmt.bindLong  ( 8, msg_rowid );
            Log.w( TAG, errInsStmt.toString() );
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
//    static SmsDbHelper getHelper(Context ctx) {
    static SmsDbHelper getHelper() {
        if ( HELPER != null ) return HELPER;
        DB_PATH = Environment.getExternalStorageDirectory()
                + "/site.swaraj.pricefinder/databases";
//        try {
            Context ctx = SwarajApp.getAppCtx();
            Log.d( TAG, "ctx: " + ctx.toString() );
            Log.d( TAG, "db path: "+DB_PATH+"/pF.log."+DB_VER+".db" );
            log = new SQLiteOpenHelper( ctx, DB_PATH+"/pF.log."+DB_VER+".db", null, DB_VER ) {
                @Override public void onCreate(SQLiteDatabase sqLiteDatabase) {}
                @Override public void onUpgrade(SQLiteDatabase sqLiteDatabase, int from, int to) {}
            }.getWritableDatabase();
            errInsStmt = log.compileStatement( "INSERT INTO err_log"
                    + "( class, method, file, line, stack, err_class, err_msg, msg_rowid ) "
                    + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ? );" );
            msgInsStmt = log.compileStatement( "INSERT INTO msg_log"
                    + "( phoneNo, in_msg, sms_ts, ins_ts ) "
                    + "VALUES( ?, ?, ?, ? );" );
            rplyUpdStmt = log.compileStatement( "UPDATE msg_log SET "
                    + "out_msg = ?  WHERE rowid = ?;" );
            sntUpdStmt = log.compileStatement( "UPDATE msg_log SET "
                    + "snt_ts = ?  WHERE rowid = ?;" );
            dlvUpdStmt = log.compileStatement( "UPDATE msg_log SET "
                    + "dlv_ts = ?  WHERE rowid = ?;" );
//            lstIdStmt = log.compileStatement( "SELECT last_insert_rowid();" );

            data = new SQLiteOpenHelper( ctx, DB_PATH+"/pF.data."+DB_VER+".db", null, DB_VER ) {
                @Override public void onCreate(SQLiteDatabase sqLiteDatabase) {}
                @Override public void onUpgrade(SQLiteDatabase sqLiteDatabase, int from, int to) {}
            }.getWritableDatabase();

            meta = new SQLiteOpenHelper( ctx, DB_PATH+"/pF.meta."+DB_VER+".db", null, DB_VER ) {
                @Override public void onCreate(SQLiteDatabase sqLiteDatabase) {}
                @Override public void onUpgrade(SQLiteDatabase sqLiteDatabase, int from, int to) {}
            }.getReadableDatabase();
//        } catch ( Exception e ) {
//            Log.e( TAG, e.getMessage() );
//            log_error(e);
//        }

        HELPER = new SmsDbHelper();
        return HELPER;
    }
    private SmsDbHelper() {}
    class RowNotFoundException extends SQLException {
        RowNotFoundException() { super(); }
        RowNotFoundException(String msg) { super(msg); }
        RowNotFoundException(String msg, Throwable cause) { super(msg, cause); }
    }
    class TooManyRowsException extends SQLException {
        TooManyRowsException() { super(); }
        TooManyRowsException(String msg) { super(msg); }
        TooManyRowsException(int i) { super( i+" rows found."); }
        TooManyRowsException(int i, String sql) { super( i+" rows found for sql <"+sql+">."); }
        TooManyRowsException(String msg, Throwable cause) { super(msg, cause); }
    }
}

/****************************************************************
 int updMsg( SQLiteStatement stmt, Object[] vals, long rowid ) {
 int c = 0;
 try {
 stmt.clearBindings();
 int i;
 for ( i = 0; i < vals.length; i++ ) {
 String type = vals[i].getClass().getName().substring(10);
 if ( type.equals("String") ) {
 stmt.bindString( i+1, (String)vals[i] );
 } else if ( type.equals("Long") ) {
 stmt.bindLong( i+1, (Long)vals[i] );
 } else if ( type.equals("Double") ) {
 stmt.bindDouble( i+1, (Double)vals[i] );
 }
 }
 stmt.bindLong( i+1, (Long)rowid );
 c = stmt.executeUpdateDelete();
 } catch ( Exception e) {
 log_error(e);
 }
 if ( stmt.equals(dlvUpdStmt) ) msg_rowid = 0;
 return c;
 }

 public void onCreate(SQLiteDatabase db) {
 try {
 for ( String SQL : CREATES ) db.execSQL(SQL);
 for ( int i = 0; i < INSERTS.length(); i++ ) {
 JSONObject ins = INSERTS.getJSONObject( i );
 String tbl = ins.getString( "table" );
 JSONArray flds = ins.getJSONArray( "fields" );
 JSONArray typs = ins.getJSONArray( "types" );
 if ( flds.length() <> typs.length() )
 throw new JSONException("Field/Type count mismatch in "+tbl+"("+j+")");
 JSONArray vals_array = ins.getJSONArray( "values" );
 for ( int j = 0; j < vals_array.length(); j++ ) {
 JSONArray vals = vals_array.getJSONArray( j );
 if ( flds.length() <> vals.length() )
 throw new JSONException("Field/Value count mismatch in "+tbl+"("+j+")");
 ContentValues values = new ContentValues( flds.length() );
 for ( int k = 0; k < flds.length(); k++ ) {
 if ( typs.getString(k).equals("TEXT") )
 values.put( flds.getString(k), vals.getString(k) );
 else if ( typs.getString(k).equals("INTEGER") )
 values.put( flds.getString(k), vals.getLong(k) );
 else if ( typs.getString(k).equals("REAL") )
 values.put( flds.getString(k), vals.getDouble(k) );
 else
 throw new Exception("FieldUnknown type "+typs.getString(k)+" in "+tbl+"("+j+")");
 }
 long rowId = db.insert(tbl, null, values);
 Log.i( TAG, "Inserted row id"+rowId+" into "+tbl );
 }
 }
 } catch ( SQLException e ) {
 Log.e( TAG, "SQLException",  e );
 } catch ( JSONException e ) {
 Log.e( TAG, "JSONException",  e );
 } catch ( Exception e ) {
 Log.e( TAG, "Exception",  e );
 }


 private static JSONArray INSERTS;
 INSERTS = (JSONArray) (new JSONTokener( inserts_json )).nextValue();
    private static final String inserts_json = "["                          +
            "{  table: unit, "                                              +
            "   fields: [ name, abbriv, conversion, standard, _id ], "      +
            "   types:  [ TEXT, TEXT, REAL, INTEGER, INTEGER ], "           +
            "   values: [   [ kilo,         kg, 1,      1,  1   ], "        +
            "               [ litre,        lt, 1,      2,  2   ], "        +
            "               [ bigha,        bg, 1,      3,  3   ], "        +
            "               [ meter,        mt, 1,      4,  4   ], "        +
            "               [ piece,        pc, 1,      5,  5   ], "        +
            "               [ quintal,      qt, 100,    1,  6   ], "        +
            "               [ Ton,          Mt, 1000,   1,  7   ], "        +
            "               [ gram,         gm, 0.001,  1,  8   ], "        +
            "               [ milliliter,   ml, 0.001,  2,  9   ], "        +
            "               [ acre,         ac, 3.025,  3, 10   ], "        +
            "               [ Hectare,      Ht, 7.4749, 3, 11   ], "        +
            "               [ millimeter,   mm, 0.001,  4, 12   ], "        +
            "               [ dozen,        dz, 12,     5, 13   ], ], },"   +
            "{  table: category, "                                          +
            "   fields: [ name, standard_unit, _id ], "                     +
            "   types:  [ TEXT, INTEGER, INTEGER ], "                       +
            "   values: [   [ AALU,    6, 1   ], ], },"                     +
            "{  table: sub_category, "                                      +
            "   fields: [ name, category_id ], "                            +
            "   types:  [ TEXT, INTEGER ], "                                +
            "   values: [   [ JYOT,    1   ],"                              +
            "               [ CHMU,    1   ], ], },"                        +
            "{  table: category_alias, "                                    +
            "   fields: [ alias, category_id ], "                           +
            "   types:  [ TEXT, INTEGER ], "                                +
            "   values: [   [ ALOO,    1   ], ], },"                        +
            "{  table: sub_category_alias, "                                +
            "   fields: [ alias, category_id, sub_category_id ], "          +
            "   types:  [ TEXT, INTEGER, INTEGER ], "                       +
            "   values: [   [ Jyoti,           1,  1    ],"                 +
            "               [ Chandramukhi,    1,  2    ], ], }, ]";
    private static final String[] CREATES = {
            "CREATE TABLE sms (" +
                    "   type CHAR(4) CHECK (type IN ('_OK_', 'NEW_', 'PART', 'BAD_')) NOT NULL, " +
                    "   timestamp DATE NOT NULL, " +
                    "	phone CHAR(10) UNIQUE ON CONFLICT ROLLBACK NOT NULL CHECK ( phone GLOB '[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]' )," +
                    "   message TEXT NOT NULL, " +
                    "	_insert_at INTEGER DEFAULT CURRENT_TIMESTAMP NOT NULL, " +
                    "	_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL );",
            "CREATE TABLE member (" +
                    "	phone CHAR(10) UNIQUE ON CONFLICT ROLLBACK NOT NULL CHECK ( phone GLOB '[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]' )," +
                    "	pin CHAR(6) CHECK ( pin GLOB '[0-9][0-9][0-9][0-9][0-9][0-9]' ) NOT NULL," +
                    "	name TEXT," +
                    "	dob DATE," +
                    "	aadhaar CHAR(12) UNIQUE ON CONFLICT ROLLBACK CHECK ( aadhaar GLOB '[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]' )," +
                    "	upi TEXT CHECK ( upi LIKE '%_@_%' ) UNIQUE ON CONFLICT ROLLBACK," +
                    "	_insert_at INTEGER DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                    "	_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL );",
            "CREATE TABLE farmer (" +
                    "	member_id INTEGER REFERENCES member(_id) ON DELETE CASCADE NOT NULL," +
                    "	type CHAR(5) CHECK ( type IN ('OWNER', 'LEASE', 'LABOR') ) NOT NULL," +
                    "	land REAL," +
                    "	family_size INTEGER," +
                    "	_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL );",
            "CREATE TABLE unit (" +
                    "	name TEXT UNIQUE NOT NULL," +
                    "	abbriv TEXT UNIQUE NOT NULL," +
                    "	conversion REAL NOT NULL," +
                    "	standard INTEGER REFERENCES unit(_id) DEFERRABLE INITIALLY DEFERRED NOT NULL," +
                    "	_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL );",
            "CREATE TABLE category (" +
                    "	name CHAR(4) CHECK ( name GLOB '[A-Z][A-Z][A-Z][A-Z]' ) UNIQUE ON CONFLICT ROLLBACK NOT NULL," +
                    "	desc TEXT," +
                    "	standard_unit INTEGER REFERENCES unit(_id) NOT NULL," +
                    "	_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL );",
            "CREATE TABLE category_alias (" +
                    "	alias TEXT UNIQUE ON CONFLICT ROLLBACK NOT NULL," +
                    "	category_id INTEGER REFERENCES category(_id) ON DELETE CASCADE NOT NULL," +
                    "	_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL );",
            // name and id(expanded to 4 chars) are entered as aliases
            "CREATE TRIGGER category_trg AFTER INSERT ON category FOR EACH ROW BEGIN" +
                    "	INSERT INTO category_alias ( alias, category_id )" +
                    "		VALUES ( NEW.name, NEW._id);" +
                    "	INSERT INTO category_alias ( alias, category_id )" +
                    "		VALUES ( substr('0000' || NEW._id, -4, 4), NEW._id);" +
                    "END;",
            "CREATE TABLE sub_category (" +
                    "	name CHAR(4) CHECK ( name GLOB '[A-Z][A-Z][A-Z][A-Z]' ) NOT NULL," +
                    "	desc TEXT," +
                    "	category_id INTEGER REFERENCES category(_id) ON DELETE CASCADE NOT NULL," +
                    "	_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "	CONSTRAINT sub_category_unq UNIQUE ( category_id, name ) ON CONFLICT ROLLBACK );",
            "CREATE TABLE sub_category_alias (" +
                    "    alias TEXT NOT NULL," +
                    "    category_id INTEGER REFERENCES category(_id) ON DELETE CASCADE NOT NULL," +
                    "    sub_category_id INTEGER REFERENCES sub_category(_id) ON DELETE CASCADE NOT NULL," +
                    "    _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "	CONSTRAINT sub_category_alias_unq UNIQUE (category_id, alias) ON CONFLICT ROLLBACK );",
            // name and id(expanded to 4 chars) are entered as aliases
            "CREATE TRIGGER sub_category_trg AFTER INSERT ON sub_category FOR EACH ROW BEGIN" +
                    "	INSERT INTO sub_category_alias ( alias, category_id, sub_category_id )" +
                    "		VALUES ( NEW.name, NEW.category_id, NEW._id);" +
                    "	INSERT INTO sub_category_alias ( alias, category_id, sub_category_id )" +
                    "		VALUES ( substr('0000' || NEW._id, -4, 4), NEW.category_id, NEW._id);" +
                    "END;",
//        "CREATE VIEW full_name AS " +
//                "	SELECT" +
//                "	    c.name || '.' || s.name AS full_name," +
//                "	    ca.alias AS cat," +
//                "	    sa.alias AS sub_cat" +
//                "	    s._id AS _id," +
//                "    FROM category c " +
//                "	    JOIN category_alias ca ON ca.category_id = c._id" +
//                "	    JOIN sub_category s ON s.category_id = c._id" +
//                "	    JOIN sub_category_alias sa ON sa.sub_category_id = s._id" +
//                "	 ORDER BY cat, _id;",
            "CREATE VIEW std_name_qty AS " +
                    "    SELECT " +
                    "       s._id AS _id " +
                    "       , c.name || '.' || s.name AS name " +
                    "       , ca.alias || '.' || sa.alias AS alias " +
                    "       , src.abbriv AS src_unit " +
                    "       , dest.abbriv AS dest_unit " +
                    "       , src.conversion/dest.conversion AS mult " +
                    "       FROM category c " +
                    "       JOIN unit dest ON dest._id = c.standard_unit " +
                    "       JOIN unit src ON src.standard = dest.standard " +
                    "       JOIN category_alias ca ON ca.category_id = c._id " +
                    "       JOIN sub_category s ON s.category_id = c._id " +
                    "       JOIN sub_category_alias sa ON sa.sub_category_id = s._id;",
            "CREATE TABLE offer (" +
                    "    member_id INTEGER REFERENCES member(_id) NOT NULL," +
                    "    type CHAR(3) CHECK ( type IN ('BUY', 'SEL') ) NOT NULL," +
                    "    sub_category_id INTEGER REFERENCES sub_category(_id) NOT NULL," +
                    "    delivery_date DATE NOT NULL," +
                    "    delivery_pin CHAR(6) CHECK (delivery_pin GLOB '[0-9][0-9][0-9][0-9][0-9][0-9]') NOT NULL," +
                    "    quantity REAL NOT NULL," +
                    "    price REAL NOT NULL," +
                    "    min_qty REAL NOT NULL," +
                    "    max_tail_qty REAL NOT NULL," +
                    "    booked_qty REAL DEFAULT 0.0 NOT NULL," +
                    "    _insert_at INTEGER DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                    "    _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL );",
            "CREATE TABLE deal (" +
                    "    buy_offer_id INTEGER REFERENCES offer(_id) NOT NULL," +
                    "    sel_offer_id INTEGER REFERENCES offer(_id) NOT NULL," +
                    "    quantity REAL NOT NULL," +
                    "    price REAL NOT NULL," +
                    "    distance REAL NOT NULL," +
                    "    _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL );"
    };
}
 * ****************************************************************
 SQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
 SQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler)

 SQLiteDatabase getReadableDatabase() Create and/or open a database.
 SQLiteDatabase getWritableDatabase() Create and/or open a database that will be used for reading and writing.

 abstract void onCreate(SQLiteDatabase db) Called when the database is created for the first time.
 abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) Called when the database needs to be upgraded.

 void onConfigure(SQLiteDatabase db) Called when the database connection is being configured, to enable features such as write-ahead logging or foreign key support.
 void onOpen(SQLiteDatabase db) Called when the database has been opened.
 void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) Called when the database needs to be downgraded.

 void setWriteAheadLoggingEnabled(boolean enabled) Enables or disables the use of write-ahead logging for the database.
 void close()    Close any open database object.
 String getDatabaseName() Return the name of the SQLite database being opened, as given to the constructor.

 private static final String inserts_json =
 "[" +
 "{" +
 "\"table\": \"unit\", " +
 "\"fields\": [ \"name\", \"abbriv\", \"standard\", \"conversion\" ], " +
 "\"values\": [" +
 "[ \"kilogram\",    \"kg\", 1, 1    ]," +
 "[ \"quintal\",     \"qt\", 1, 100  ]," +
 "[ \"metric ton\",  \"mt\", 1, 1000 ]," +
 "[ \"gram\",        \"gm\", 1, 0.001]," +
 "]," +
 "}," +
 "{" +
 "\"table\": \"category\", " +
 "\"fields\": [ \"name\", \"category_id\" ], " +
 "\"values\": [" +
 "[ \"AALU\",    1   ]" +
 "]" +
 "}," +
 "{" +
 "\"table\": \"sub_category\", " +
 "\"fields\": [ \"name\", \"category_id\" ], " +
 "\"values\": [" +
 "[ \"JYOT\",    1   ]," +
 "[ \"CHMU\",    1   ]" +
 "]" +
 "}," +
 "{" +
 "\"table\": \"category_alias\", " +
 "\"fields\": [ \"alias\", \"category_id\" ], " +
 "\"values\": [" +
 "[ \"ALOO\",    1   ]," +
 "]" +
 "}," +
 "{" +
 "\"table\": \"sub_category_alias\", " +
 "\"fields\": [ \"alias\", \"category_id\", \"sub_category_id\" ], " +
 "\"values\": [" +
 "[ \"Jyoti\",           1,  1    ]," +
 "[ \"Chandramukhi\",    1,  2    ]," +
 "]" +
 "}," +
 "]";

 +       "{"
 +          "\"table\": \"\", "
 +          "\"fields\": [ \"\", \"\", \"\", \"\" ], "
 +          "\"values\": ["
 +               "[ \"\",    \"\", 1, 1    ],"

 +           "]"
 +       "},"

 "INSERT INTO unit (name, abbriv, standard, conversion ) " +
 "	VALUES ( 'kilogram', 'kg', 1, 1 );",
 "INSERT INTO unit (name, abbriv, standard, conversion) " +
 "	VALUES ( 'quintal', 'qt', 1, 100 );",
 "INSERT INTO unit (name, abbriv, standard, conversion) " +
 "	VALUES ( 'metric ton', 'mt', 1, 1000 );",
 "INSERT INTO unit (name, abbriv, standard, conversion) " +
 "	VALUES ( 'gram', 'gm', 1, 0.001 );",
 "INSERT INTO category (name, standard_unit) VALUES ( 'AALU', 1);",
 "INSERT INTO sub_category (name, category_id) VALUES ( 'JYOT', 1);",
 "INSERT INTO sub_category (name, category_id) VALUES ( 'CHMU', 1);",
 "INSERT INTO category_alias (alias, category_id) VALUES ( 'ALOO', 1 );",
 "INSERT INTO sub_category_alias (alias, category_id, sub_category_id) " +
 "	VALUES ( 'Jyoti', 1, 1 );",
 "INSERT INTO sub_category_alias (alias, category_id, sub_category_id) " +
 "	VALUES ( 'Chandramukhi', 1, 2 );",

 public void onCreate(SQLiteDatabase db) {
 db.execSQL(SmsDb.SQL_CREATE_ENTRIES);
 db.execSQL("PRAGMA foreign_keys = ON;" );
 db.execSQL( "CREATE TABLE member (" +
 "	phone CHAR(10) UNIQUE ON CONFLICT ROLLBACK NOT NULL CHECK ( phone GLOB '[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]' )," +
 "	pin CHAR(6) CHECK ( pin GLOB '[0-9][0-9][0-9][0-9][0-9][0-9]' ) NOT NULL," +
 "	name TEXT," +
 "	dob DATE," +
 "	aadhaar CHAR(12) UNIQUE ON CONFLICT ROLLBACK CHECK ( aadhaar GLOB '[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]' )," +
 "	upi TEXT CHECK ( upi LIKE '%_@_%' ) UNIQUE ON CONFLICT ROLLBACK," +
 "	_insert_at INTEGER DEFAULT CURRENT_TIMESTAMP NOT NULL," +
 "	_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL );" );
 db.execSQL( "CREATE TABLE farmer (" +
 "	member_id INTEGER REFERENCES member(_id) ON DELETE CASCADE NOT NULL," +
 "	type CHAR(5) CHECK ( type IN ('OWNER', 'LEASE', 'LABOR') ) NOT NULL," +
 "	acres REAL," +
 "	family_size INTEGER," +
 "	_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL );" );
 db.execSQL( "CREATE TABLE unit (" +
 "	name TEXT NOT NULL," +
 "	abbriv TEXT NOT NULL," +
 "	standard INTEGER REFERENCES unit(_id) DEFERRABLE INITIALLY DEFERRED NOT NULL," +
 "	conversion REAL DEFAULT 1 NOT NULL," +
 "	_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL );" );
 db.execSQL( "INSERT INTO unit (name, abbriv, standard, conversion) " +
 "	VALUES ( 'kilogram', 'kg', 1, 1 );" );
 db.execSQL( "INSERT INTO unit (name, abbriv, standard, conversion) " +
 "	VALUES ( 'quintal', 'qt', 1, 100 );" );
 db.execSQL( "INSERT INTO unit (name, abbriv, standard, conversion) " +
 "	VALUES ( 'metric ton', 'mt', 1, 1000 );" );
 db.execSQL( "INSERT INTO unit (name, abbriv, standard, conversion) " +
 "	VALUES ( 'gram', 'gm', 1, 0.001 );" );
 db.execSQL( "CREATE TABLE category (" +
 "	name CHAR(4) CHECK ( name GLOB '[A-Z][A-Z][A-Z][A-Z]' ) UNIQUE ON CONFLICT ROLLBACK NOT NULL," +
 "	desc TEXT," +
 "	standard_unit INTEGER REFERENCES unit(_id) NOT NULL," +
 "	_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL );" );
 db.execSQL( "CREATE TABLE category_alias (" +
 "	alias TEXT UNIQUE ON CONFLICT ROLLBACK NOT NULL," +
 "	category_id INTEGER REFERENCES category(_id) ON DELETE CASCADE NOT NULL," +
 "	_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL );" );
 // name and id(expanded to 4 chars) are entered as aliases
 db.execSQL( "CREATE TRIGGER category_trg AFTER INSERT ON category FOR EACH ROW BEGIN" +
 "	INSERT INTO category_alias ( alias, category_id )" +
 "		VALUES ( NEW.name, NEW._id);" +
 "	INSERT INTO category_alias ( alias, category_id )" +
 "		VALUES ( substr('0000' || NEW._id, -4, 4), NEW._id);" +
 "END;" );
 db.execSQL( "CREATE TABLE sub_category (" +
 "	name CHAR(4) CHECK ( name GLOB '[A-Z][A-Z][A-Z][A-Z]' ) NOT NULL," +
 "	desc TEXT," +
 "	category_id INTEGER REFERENCES category(_id) ON DELETE CASCADE NOT NULL," +
 "	_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
 "	CONSTRAINT sub_category_unq UNIQUE ( category_id, name ) ON CONFLICT ROLLBACK );" );
 db.execSQL( "CREATE TABLE sub_category_alias (" +
 "    alias TEXT NOT NULL," +
 "    category_id INTEGER REFERENCES category(_id) ON DELETE CASCADE NOT NULL," +
 "    sub_category_id INTEGER REFERENCES sub_category(_id) ON DELETE CASCADE NOT NULL," +
 "    _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
 "	CONSTRAINT sub_category_alias_unq UNIQUE (category_id, alias) ON CONFLICT ROLLBACK );" );
 // name and id(expanded to 4 chars) are entered as aliases
 db.execSQL( "CREATE TRIGGER sub_category_trg AFTER INSERT ON sub_category FOR EACH ROW BEGIN" +
 "	INSERT INTO sub_category_alias ( alias, category_id, sub_category_id )" +
 "		VALUES ( NEW.name, NEW.category_id, NEW._id);" +
 "	INSERT INTO sub_category_alias ( alias, category_id, sub_category_id )" +
 "		VALUES ( substr('0000' || NEW._id, -4, 4), NEW.category_id, NEW._id);" +
 "END;" );
 db.execSQL( "INSERT INTO category (name, standard_unit) VALUES ( 'AALU', 1);" );
 db.execSQL( "INSERT INTO sub_category (name, category_id) VALUES ( 'JYOT', 1);" );
 db.execSQL( "INSERT INTO sub_category (name, category_id) VALUES ( 'CHMU', 1);" );
 db.execSQL( "INSERT INTO category_alias (alias, category_id) VALUES ( 'ALOO', 1 );" );
 db.execSQL( "INSERT INTO sub_category_alias (alias, category_id, sub_category_id) " +
 "	VALUES ( 'Jyoti', 1, 1 );" );
 db.execSQL( "INSERT INTO sub_category_alias (alias, category_id, sub_category_id) " +
 "	VALUES ( 'Chandramukhi', 1, 2 );" );
 db.execSQL( "CREATE VIEW full_name AS " +
 "	SELECT" +
 "	    c.name || '.' || s.name AS full_name," +
 "	    ca.alias AS cat," +
 "	    sa.alias AS sub_cat" +
 "	    s._id AS _id," +
 "    FROM category c " +
 "	    JOIN category_alias ca ON ca.category_id = c._id" +
 "	    JOIN sub_category s ON s.category_id = c._id" +
 "	    JOIN sub_category_alias sa ON sa.sub_category_id = s._id" +
 "	 ORDER BY cat, _id;" );
 db.execSQL( "CREATE TABLE offer (" +
 "    member_id INTEGER REFERENCES member(_id) NOT NULL," +
 "    type CHAR(3) CHECK ( type IN ('BUY', 'SEL') ) NOT NULL," +
 "    sub_category_id INTEGER REFERENCES sub_category(_id) NOT NULL," +
 "    delivery_date DATE NOT NULL," +
 "    delivery_pin CHAR(6) CHECK (delivery_pin GLOB '[0-9][0-9][0-9][0-9][0-9][0-9]') NOT NULL," +
 "    quantity REAL NOT NULL," +
 "    price REAL NOT NULL," +
 "    min_qty REAL NOT NULL," +
 "    max_tail_qty REAL NOT NULL," +
 "    booked_qty REAL DEFAULT 0.0 NOT NULL," +
 "    _insert_at INTEGER DEFAULT CURRENT_TIMESTAMP NOT NULL," +
 "    _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL );" );
 db.execSQL( "CREATE TABLE deal (" +
 "    buy_offer_id INTEGER REFERENCES offer(_id) NOT NULL," +
 "    sel_offer_id INTEGER REFERENCES offer(_id) NOT NULL," +
 "    quantity REAL NOT NULL," +
 "    price REAL NOT NULL," +
 "    distance REAL NOT NULL," +
 "    _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL );" );
 }


 final class SmsDb {
 // To prevent someone from accidentally instantiating the contract class,
 // make the constructor private.
 private SmsDb() {}
 static class Raw implements BaseColumns {
 static final String TABLE_NAME = "raw";
 static final String COLUMN_NAME_TS = "timestamp";
 static final String COLUMN_NAME_CALLER = "caller";
 static final String COLUMN_NAME_MSG = "message";
 }
 static class Members implements BaseColumns {
 static final String TABLE = "members";
 static final String INSERT_AT = "timestamp";
 static final String NAME = "caller";
 static final String COLUMN_NAME_MSG = "message";
 }

 static final String SQL_CREATE_ENTRIES =
 "CREATE TABLE " + Raw.TABLE_NAME + " (" +
 Raw._ID + " INTEGER PRIMARY KEY," +
 Raw.COLUMN_NAME_TS + " INT," +
 Raw.COLUMN_NAME_CALLER + " CHAR(13)," +
 Raw.COLUMN_NAME_MSG + " TEXT" +
 " );";
 static final String SQL_DELETE_ENTRIES =
 "DROP TABLE IF EXISTS " + Raw.TABLE_NAME;
 }

 ****************************************************************/
