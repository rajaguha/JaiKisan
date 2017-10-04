/****************************
 Get PINCODE distances, locations
     http://alldistancebetween.com/?calc=true
     https://www.mapdevelopers.com/

 Copy dbs to device
     C:\tools\Android\sdk\platform-tools\adb -s emulator-5554 push local remote
     C:\Users\rajag\Devel\sqlite\pF>C:\tools\Android\sdk\platform-tools\adb -s Medfield7682D10F push dbs/xxx.db /Removable/MicroSD/Android/data/site.swaraj.pricefinder/databases/xxx.db

 Manipulate dbs on device
     C:\tools\Android\sdk\platform-tools\adb -s [Medfield7682D10F | emulator-5554] shell
     root@android:/ # sqlite3 /data/data/site.swaraj.pricefinder/databases/SmsServer.db
 Paths
     Emulator sdcard: /mnt/sdcard/site.swaraj.jaikisan/databases/
     Emulator memory: /data/data/site.swaraj.jaikisan/databases/
     ASUS sdcard: /Removable/MicroSD/Android/data/site.swaraj.jaikisan/databases/
 ******************************/

// TODO - make multithreaded
// TODO - split qual tables into 3s for each level - avoid ref errors problem

package site.swaraj.jaikisan;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by rajag on 2017-09-20.
 */

class SmsDbHlpr {
    private static String TAG = "JK:SmsDbHlpr";

    LogDb logDb;
    DataDb dataDb;
    MetaDb metaDb;

    SmsDbHlpr(Context context) {
        Log.i( TAG, "SmsDbHlpr(): <<<" );

        logDb = new LogDb( SwarajApp.getCtx() );
        dataDb = new DataDb( SwarajApp.getCtx() );
        metaDb = new MetaDb( SwarajApp.getCtx() );

        Log.i( TAG, "SmsDbHlpr(): >>>" );
    }
    HashMap<String, Object> getMmbrHM(SmsService.Reply reply) {
        HashMap<String, Object> mmbr = dataDb.getRow(
                "member", Member.mmbr_ordr, "cid = ? ", new String[]{reply.cid} );
//        Log.d(TAG, "getMmbrHM(): " + (mmbr == null ? "<NULL>" : "<VALID>") );
        if ( mmbr == null ) mmbr = dataDb.insMmbr( reply );
        return mmbr;
    }
}
