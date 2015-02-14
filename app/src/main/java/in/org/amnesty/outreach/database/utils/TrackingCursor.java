package in.org.amnesty.outreach.database.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteQuery;

import java.util.LinkedList;
import java.util.List;

public class TrackingCursor extends SQLiteCursor {

    private static List<Cursor> openCursors = new LinkedList<>();

    public TrackingCursor(SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
        super(driver, editTable, query);
        openCursors.add(this);
    }

    public static List<Cursor> getOpenCursors() {
        return openCursors;
    }

    @Override
    public void close() {
        super.close();
        openCursors.remove(this);
    }

}