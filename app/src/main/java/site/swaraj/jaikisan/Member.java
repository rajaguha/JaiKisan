package site.swaraj.jaikisan;

import android.util.Log;
import java.util.HashMap;

/**
 * Created by rajag on 2017-10-02.
 */

// TODO - record service center address for member to identify sending mobile provider
// TODO - update last_msg, last_msg_ts, etc. for returning members

class Member {
    static final String TAG = "JK:Member:";

    static final short CID = 0x0001;
    static final short PIN = 0x0002;
    static final short LANG = 0x0004;
    static final short TYPE = 0x0008;
    static final short NAME = 0x0010;
    static final short DOB = 0x0020;
    static final short ANO = 0x0040;
    static final short UPI = 0x0080;
    static final short REF = 0x0100;
    static final short P_TYPE = 0x0200;
    static final short P_ACRES = 0x0400;
    static final short P_HH_SIZE = 0x0800;
    static final short B_TYPE = 0x1000;
    static final short B_QTY = 0x2000;
    static final short A = 0x4000;

    class Field {
        String col; String disp; int type; boolean updbl; Object val;
        // 0 - initial / automatic
        // 1 - required for enrollment( collected from first offer/query )
        // 2 - required for functioning( inferred from other info )
        // 3 - asked for in sequence
        Field(String col, String disp, int type, boolean updbl, Object val) {
            this.col = col; this.disp = disp; this.type = type; this.updbl = updbl; this.val = val;
        }
    }
    static final String[] mmbr_ordr = {
              "cid", "pin", "lang", "type", "name", "dob", "ano", "upi"
            , "p_type", "p_acres", "p_hh_size", "b_type", "b_qty"
            , "ref", "flags", "state", "msgC", "last_msg", "_insert_at", "_id"
    };
    HashMap<String, Field> mmbr = new HashMap<String, Field>(12) {{
        put( "cid",  new Field( "cid",  "Phone No.", 0, false, null));     // 0 - initial / automatic
        put( "pin",  new Field( "pin",  "PIN Code", 1, true, null));      // 1 - required for enrollment( collected from first offer/query )
        put( "lang", new Field( "lang", "Language", 2, true, null));     // 2 - required for functioning( inferred from other info )
        put( "type", new Field( "type", "Buyer/Seller", 2, true, null)); // 2 - required for functioning( inferred from other info )
        put( "name", new Field( "name", "Full Name", 3, true, null));    // 3 - asked for in sequence
        put( "dob",  new Field( "dob",  "Date of Birth", 3, true, null)); // 3 - asked for in sequence
        put( "ano",  new Field( "ano",  "Aadhaar No.", 3, false, null));   // 3 - asked for in sequence
        put( "upi",  new Field( "upi",  "BHIM Id.", 2, true, null));      // 2 - asked for in sequence

        put( "p_type", new Field( "p_type", "Producer Type", 3, true, null));
        put( "p_acres", new Field( "p_acres", "Producer Acres", 3, true, null));
        put( "p_hh_size", new Field( "p_hh_size", "Producer Household Size", 3, true, null));
        put( "b_type", new Field( "b_type", "Buyer Type", 3, true, null));
        put( "b_qty", new Field( "b_qty", "Buyer Quantity", 3, true, null));

        put( "ref", new Field( "ref", "Referer Phone No.", 0, false, null)); // 0 - initial / automatic
        put( "flags", new Field( "flags", "flag for inferred/confirmed", 0, false, null)); // 0 - initial / automatic
        put( "state", new Field( "state", "state", 0, false, null));           // 0 - initial / automatic
        put( "msgC", new Field( "msgC", "msg count", 0, false, null));   // 0 - initial / automatic
        put( "last_msg", new Field( "last_msg", "Last msg date", 0, false, null));// 0 - initial / automatic
        put( "_insert_at", new Field( "_insert_at", "Join Date", 0, false, null));  // 0 - initial / automatic
        put( "_id", new Field( "_id", "Member Id.", 0, false, null));
    }};
    Member(SmsService.Reply reply) {
        HashMap<String, Object> mmbr = SwarajApp.getDbs().getMmbrHM( reply );
        for ( String key : mmbr_ordr ) this.mmbr.get(key).val = mmbr.get(key);
//        Log.d(TAG, "Member() : " + mmbr == null ? "<NULL>" : "<VALID>" );
    }
    @Override public String toString() {
        StringBuilder out = new StringBuilder();
        for ( String fld : mmbr_ordr )
            out.append(fld).append(" => ")
                    .append(mmbr.get(fld).val == null ? "<NULL>" : mmbr.get(fld).val )
                    .append("\n");
        return  out.toString();
    }
}
