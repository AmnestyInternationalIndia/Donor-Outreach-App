package in.org.amnesty.outreach.receivers;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import in.org.amnesty.outreach.services.DownloadService;

public class DownloadBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (action.equalsIgnoreCase(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            Intent downloadIntent = new Intent(context, DownloadService.class);
            downloadIntent.setAction(DownloadService.ACTION_BROADCAST);
            downloadIntent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID,
                    intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
            context.startService(downloadIntent);
        } else {
            Intent downloadIntent = new Intent(context, DownloadService.class);
            downloadIntent.setAction(DownloadService.ACTION_DOWNLOAD);
            startWakefulService(context, downloadIntent);
        }
    }
}