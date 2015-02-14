package in.org.amnesty.outreach.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import in.org.amnesty.outreach.database.utils.TrackingCursorFactory;

public class OutreachDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = OutreachDatabaseHelper.class.getCanonicalName();

    private static final String DB_NAME = "outreach";

    private static final int DB_VERSION = 1;

    public OutreachDatabaseHelper(Context context) {
        super(context, DB_NAME, TrackingCursorFactory.newInstance(), DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Tables.Downloads.CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database. Existing contents will be lost. [" + oldVersion + "]->[" + newVersion + "]");
        db.execSQL(Tables.Downloads.DROP);
        onCreate(db);
    }

    public static class Tables {

        public static class Downloads implements BaseColumns {
            public static final String PATH = "download";

            public static final String DOWNLOAD_ID = "download_id";

            public static final String DOWNLOADING = "downloading";

            public static final String CREATE =
                    "CREATE TABLE " + PATH + " (" +
                            DOWNLOAD_ID + " INTEGER, " +
                            DOWNLOADING + " BOOLEAN);";

            public static final String DROP =
                    "DROP TABLE " + PATH + ";";

//            public String[] DEFAULT_PROJECTION = { Tables.Downloads._ID,
//                    Tables.Downloads.DOWNLOADING};
        }
    }
}
