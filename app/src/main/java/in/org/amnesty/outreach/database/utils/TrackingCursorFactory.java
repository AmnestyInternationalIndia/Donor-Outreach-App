package in.org.amnesty.outreach.database.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQuery;

public class TrackingCursorFactory implements CursorFactory {

    public static TrackingCursorFactory newInstance() {
        return new TrackingCursorFactory();
    }

    @Override
    public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable,
                            SQLiteQuery query) {
        return new TrackingCursor(masterQuery, editTable, query);
    }
}