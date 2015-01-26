package in.org.amnesty.outreach.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import in.org.amnesty.outreach.helpers.Utils;
import in.org.amnesty.outreach.receivers.DownloadBroadcastReceiver;

import static in.org.amnesty.outreach.helpers.Utils.PreferenceUtils.ACCOUNT_TOKEN;

public class DownloadService extends IntentService {

    public static final String ACTION_DOWNLOAD = "in.org.amnesty.outreach.download";
    public static final String ACTION_SYNC = "in.org.amnesty.outreach.sync";

    public static final String ACTION_DOWNLOAD_START = "in.org.amnesty.outreach.download.start";
    public static final String ACTION_DOWNLOAD_PROGRESS = "in.org.amnesty.outreach.download.progress";
    public static final String ACTION_DOWNLOAD_COMPLETE = "in.org.amnesty.outreach.download.complete";

    public static final String KEY_CURRENT_ITEM ="currentItemName";
    public static final String KEY_TOTAL_ITEM ="totalItems";

    private final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    private final AndroidJsonFactory jsonFactory = AndroidJsonFactory.getDefaultInstance();

    private Drive mDriveService;
    private DownloadManager mDownloadManager;
    private GoogleAccountCredential mGoogleCredential;

    public DownloadService () {
		super(DownloadService.class.getName ());
	}


    @Override
    public void onCreate() {
        super.onCreate();


        mGoogleCredential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(PlusScopes.PLUS_ME, DriveScopes.DRIVE));
        mGoogleCredential.setSelectedAccountName(Utils.PreferenceUtils.getStringPrefs(this, Utils.PreferenceUtils.ACCOUNT_USER_ID));

        mDriveService =
                new Drive.Builder(httpTransport, jsonFactory, mGoogleCredential)
                        .setApplicationName("OutReach/1.0").build();

        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
    }

	@Override protected void onHandleIntent (Intent intent) {
        String action = intent.getAction();
        if(action.equalsIgnoreCase(ACTION_DOWNLOAD)) {
            Utils.PreferenceUtils.setBooleanPrefs(this, Utils.PreferenceUtils.IS_DOWNLOADING, true);
            startDownload();
            Utils.PreferenceUtils.setBooleanPrefs(this, Utils.PreferenceUtils.IS_DOWNLOADING, false);
            DownloadBroadcastReceiver.completeWakefulIntent(intent);
        } else if(action.equalsIgnoreCase(ACTION_SYNC)) {
            startSync();
        }
	}

    private void startSync() {
    }

    private void startDownload() {

        try {
            Utils.PreferenceUtils.setStringPrefs(this,
                    ACCOUNT_TOKEN, mGoogleCredential.getToken());
        } catch (IOException | GoogleAuthException e) {
            e.printStackTrace();
        }

        List<File> result = new ArrayList<>();
        String selectedFolderId = Utils.PreferenceUtils.getStringPrefs(this, Utils.PreferenceUtils
                .SELECTED_CITY_FOLDER_ID);
        Drive.Files.List request = null;

        sendBroadcast("");
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

        for(File file : result) {
            sendBroadcast(file.getTitle());
            downloadFile(file);
        }

        sendBroadcast(new Intent(ACTION_DOWNLOAD_COMPLETE));
    }

    private void sendBroadcast(String currentItem) {
        Intent progressIntent = new Intent(ACTION_DOWNLOAD_PROGRESS);
        progressIntent.putExtra(KEY_CURRENT_ITEM, currentItem);
        sendBroadcast(progressIntent);
    }

    private void downloadFile(File downloadFile) {
        String downloadUrl = downloadFile.getDownloadUrl();
        //String thumbnailUrl = downloadFile.getThumbnailLink();

        if(TextUtils.isEmpty(downloadUrl)) {
            return;
        }

        Uri fileDownloadUri = Uri.parse(downloadUrl);
        //Uri fileThumbnailDownloadUri = Uri.parse(thumbnailUrl);

        DownloadManager.Request mDownloadFileRequest = new DownloadManager.Request(fileDownloadUri);
        //DownloadManager.Request mDownloadFileThumbnailRequest = new DownloadManager.Request(fileThumbnailDownloadUri);

        mDownloadFileRequest.setTitle(downloadFile.getTitle());
        mDownloadFileRequest.setVisibleInDownloadsUi(false);
        mDownloadFileRequest.setMimeType(downloadFile.getMimeType());
        mDownloadFileRequest.addRequestHeader("Authorization", "Bearer " + Utils.PreferenceUtils.getStringPrefs(this,
                Utils.PreferenceUtils.ACCOUNT_TOKEN));
        mDownloadFileRequest. setDestinationInExternalFilesDir(this,
                Environment.getExternalStorageDirectory().getAbsolutePath(),
                Utils.Constants.DEFAULT_APP_FOLDER);

//        mDownloadFileThumbnailRequest.setTitle(downloadFile.getTitle());
//        mDownloadFileThumbnailRequest.setVisibleInDownloadsUi(false);
//        mDownloadFileThumbnailRequest.setMimeType(downloadFile.getThumbnail().getMimeType());
//        mDownloadFileThumbnailRequest.addRequestHeader("Authorization", "Bearer " + Utils.PreferenceUtils.getStringPrefs(this,
//                Utils.PreferenceUtils.ACCOUNT_TOKEN));
//        mDownloadFileThumbnailRequest. setDestinationInExternalFilesDir(this,
//                Environment.getExternalStorageDirectory().getAbsolutePath(),
//                Utils.Constants.DEFAULT_APP_THUMBNAIL_FOLDER);

        mDownloadManager.enqueue(mDownloadFileRequest);
        //mDownloadManager.enqueue(mDownloadFileThumbnailRequest);
    }
}
