package in.org.amnesty.outreach.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.plus.PlusScopes;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import in.org.amnesty.outreach.database.OutreachDataSource;
import in.org.amnesty.outreach.database.OutreachDatabaseHelper;
import in.org.amnesty.outreach.helpers.Utils;
import in.org.amnesty.outreach.receivers.DownloadBroadcastReceiver;

import static in.org.amnesty.outreach.helpers.Utils.Preferences.ACCOUNT_TOKEN;

public class DownloadService extends IntentService {

    // public static final String TAG = DownloadService.class.getCanonicalName();

    public static final String ACTION_DOWNLOAD = "in.org.amnesty.outreach.intent.action.DOWNLOAD";
    public static final String ACTION_BROADCAST = "in.org.amnesty.outreach.intent.action.DOWNLOAD_BROADCAST";
    public static final String ACTION_DOWNLOAD_START = "in.org.amnesty.outreach.intent.action.DOWNLOAD_START";
    public static final String ACTION_DOWNLOAD_PROGRESS = "in.org.amnesty.outreach.intent.action.DOWNLOAD_PROGRESS";
    public static final String ACTION_DOWNLOAD_COMPLETE = "iin.org.amnesty.outreach.intent.action.DOWNLOAD_COMPLETE";

    private final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    private final AndroidJsonFactory jsonFactory = AndroidJsonFactory.getDefaultInstance();

    private Drive mDriveService;
    private DownloadManager mDownloadManager;
    private GoogleAccountCredential mGoogleCredential;
    private OutreachDataSource mOutreachDataSource;

    public DownloadService() {
        super(DownloadService.class.getName());
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleCredential =
                GoogleAccountCredential.usingOAuth2(this, Arrays.asList(PlusScopes.PLUS_ME, DriveScopes.DRIVE));
        mGoogleCredential.setSelectedAccountName(
                Utils.Preferences.getStringPrefs(this, Utils.Preferences.ACCOUNT_USER_ID));

        mDriveService =
                new Drive.Builder(httpTransport, jsonFactory, mGoogleCredential)
                        .setApplicationName("OutReach/1.0").build();

        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        mOutreachDataSource = new OutreachDataSource(this);

        try {
            mOutreachDataSource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOutreachDataSource.close();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();

        if (!Utils.Device.enabled(this, Utils.Device.EXTERNAL_STORAGE)) {
            Toast.makeText(this, "External Storage not found!", Toast.LENGTH_LONG).show();
            DownloadBroadcastReceiver.completeWakefulIntent(intent);
        }

        if (action.equalsIgnoreCase(ACTION_DOWNLOAD)) {
            Utils.Preferences.setBooleanPrefs(this, Utils.Preferences.IS_DOWNLOADING, true);
            Utils.Preferences.setIntPrefs(this, Utils.Preferences.TOTAL_DOWNLOADED_ITEMS, 0);
            startDownload();
        } else if (action.equalsIgnoreCase(ACTION_BROADCAST)) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            boolean downloadIdExists =
                    mOutreachDataSource.downloadIdExists(OutreachDatabaseHelper.Tables.Downloads
                            .PATH, downloadId);

            int totalItems = Utils.Preferences.getIntPrefs(this, Utils.Preferences.TOTAL_ITEMS);
            int totalDownloadedItems = Utils.Preferences.getIntPrefs(this, Utils.Preferences.TOTAL_DOWNLOADED_ITEMS);

            if (downloadIdExists) {
                if ((totalItems - 1) == totalDownloadedItems) {
                    Utils.Preferences.setBooleanPrefs(this, Utils.Preferences.IS_DOWNLOADING, false);
                    Utils.Preferences.setBooleanPrefs(this, Utils.Preferences.HAS_DOWNLOADED, true);
                    Utils.Preferences.setBooleanPrefs(this, Utils.Preferences.APP_INITIALIZATION_STATUS, true);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_DOWNLOAD_COMPLETE));
                } else {
                    Utils.Preferences.setIntPrefs(this, Utils.Preferences.TOTAL_DOWNLOADED_ITEMS,
                            totalDownloadedItems + 1);
                    sendBroadcast();
                }
            }
        }

        DownloadBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void startDownload() {

        try {
            Utils.Preferences.setStringPrefs(this,
                    ACCOUNT_TOKEN, mGoogleCredential.getToken());
        } catch (IOException | GoogleAuthException e) {
            e.printStackTrace();
        }

        List<File> result = new ArrayList<>();
        String selectedFolderId = Utils.Preferences.getStringPrefs(this, Utils.Preferences
                .SELECTED_CITY_FOLDER_ID);
        Drive.Files.List request = null;

        try {
            request = mDriveService.files().list()
                                   .setQ("'" + selectedFolderId + "' in parents and trashed = false");
        } catch (IOException e) {
            e.printStackTrace();
        }

        do {
            try {
                FileList files = request.execute();
                result.addAll(files.getItems());
                request.setPageToken(files.getNextPageToken());
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);

        int totalItems = result.size();
        Utils.Preferences.setIntPrefs(this, Utils.Preferences.TOTAL_ITEMS, totalItems);
        ArrayList<ContentValues> contentValuesArrayList = new ArrayList<>();

        for (File file : result) {
            String fileName = file.getTitle();
            try {
                if (Utils.Storage.existsFile(this, fileName)) {
                    totalItems = totalItems - 1;
                    if (totalItems == 0) {
                        Utils.Preferences.setBooleanPrefs(this, Utils.Preferences.IS_DOWNLOADING, false);
                        Utils.Preferences.setBooleanPrefs(this, Utils.Preferences.HAS_DOWNLOADED, true);
                        Utils.Preferences.setBooleanPrefs(this, Utils.Preferences.APP_INITIALIZATION_STATUS, true);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_DOWNLOAD_COMPLETE));
                    }
                    Utils.Preferences.setIntPrefs(this, Utils.Preferences.TOTAL_ITEMS, totalItems);
                    continue;
                }
            } catch (IOException e) {
                continue;
            }

            ContentValues contentValues = new ContentValues();
            long downloadId = downloadFile(file);
            contentValues.put(OutreachDatabaseHelper.Tables.Downloads.DOWNLOAD_ID, downloadId);
            contentValues.put(OutreachDatabaseHelper.Tables.Downloads.DOWNLOADING, true);
            contentValuesArrayList.add(contentValues);

            sendBroadcast();
        }

        mOutreachDataSource.bulkInsert(OutreachDatabaseHelper.Tables.Downloads.PATH,
                contentValuesArrayList.toArray(new ContentValues[contentValuesArrayList.size()]));
    }

    private void sendBroadcast() {
        Intent progressIntent = new Intent(ACTION_DOWNLOAD_PROGRESS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(progressIntent);
    }

    private long downloadFile(File downloadFile) {
        String downloadUrl = downloadFile.getDownloadUrl();

        if (TextUtils.isEmpty(downloadUrl)) {
            return -1;
        }

        Uri fileDownloadUri = Uri.parse(downloadUrl);
        DownloadManager.Request mDownloadFileRequest = new DownloadManager.Request(fileDownloadUri);
        mDownloadFileRequest.setTitle(downloadFile.getTitle());
        mDownloadFileRequest.setVisibleInDownloadsUi(false);
        mDownloadFileRequest.setMimeType(downloadFile.getMimeType());
        mDownloadFileRequest.addRequestHeader("Authorization", "Bearer " + Utils.Preferences.getStringPrefs(this,
                Utils.Preferences.ACCOUNT_TOKEN));
        mDownloadFileRequest
                .setDestinationInExternalPublicDir(Utils.Constants.DEFAULT_APP_FOLDER, downloadFile.getTitle());

        return mDownloadManager.enqueue(mDownloadFileRequest);
    }
}
