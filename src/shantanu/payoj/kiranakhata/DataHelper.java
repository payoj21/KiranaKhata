package shantanu.payoj.kiranakhata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataHelper extends SQLiteOpenHelper{

		public static final String DATABASE_NAME = "devices.db";
	
	    private static final int DATABASE_VERSION = 2;
	    public static final String KNOWN_TABLE_NAME = "knownDevices";
	    private static final String KNOWN_TABLE_CREATE =
	                "CREATE TABLE " + KNOWN_TABLE_NAME + " (" +
	                "DEVICE_NAME" + " TEXT);";

	    DataHelper(Context context) {
	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	        db.execSQL(KNOWN_TABLE_CREATE);
	    }

	    @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("Database", "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + KNOWN_TABLE_NAME);
            onCreate(db);
        }
	}
