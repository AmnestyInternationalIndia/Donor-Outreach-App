package in.org.amnesty.outreach.receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import in.org.amnesty.outreach.services.DownloadService;

public class DownloadBroadcastReceiver extends WakefulBroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent downloadIntent = new Intent(context, DownloadService.class);
            downloadIntent.setAction(DownloadService.ACTION_DOWNLOAD);
            startWakefulService(context, downloadIntent);
        }
    }