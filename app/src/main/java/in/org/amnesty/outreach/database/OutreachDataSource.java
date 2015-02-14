package in.org.amnesty.outreach.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;

import in.org.amnesty.outreach.helpers.Utils;

public class OutreachDataSource {

    private SQLiteDatabase mDatabase;
    private OutreachDatabaseHelper mDatabaseHelper;

    public OutreachDataSource(Context context) {
        mDatabaseHelper = new OutreachDatabaseHelper(context);
    }

    public void open() throws SQLException {
        mDatabase = mDatabaseHelper.getWritableDatabase();
    }

    public void close() {
        mDatabaseHelper.close();
    }

    public boolean bulkInsert(String path, ContentValues[] contentValuesArray) {
        boolean status = false;
        for (ContentValues contentValues : contentValuesArray) {
            long insertId = mDatabase.insert(path, null,
                    contentValues);
            status = insertId > 0;
        }

        return status;
    }

    public boolean update(String path, ContentValues contentValues) {
        long rows = mDatabase.update(path, contentValues, null,
                null);
        return rows > 0;
    }

    public boolean downloadIdExists(String path, long downloadId) {
        long count = 0;
        Cursor cursor = mDatabase.rawQuery("SELECT count(*) from " + path + "" +
                        " where " + OutreachDatabaseHelper.Tables.Downloads.DOWNLOAD_ID + " = '" + downloadId + "'",
                null);
        if (!Utils.isCursorEmpty(cursor)) {
            cursor.moveToFirst();
            count = Long.valueOf(cursor.getString(cursor.getColumnIndex("count(*)")));
        }

        return count > 0;
    }

    public long count(String path) {
        long count = 0;
        Cursor cursor = mDatabase.rawQuery("SELECT count(*) from " + OutreachDatabaseHelper.Tables.Downloads.PATH,
                null);
        if (!Utils.isCursorEmpty(cursor)) {
            cursor.moveToFirst();
            count = Long.valueOf(cursor.getString(cursor.getColumnIndex("COUNT(*)")));
        }

        return count;
    }
}
