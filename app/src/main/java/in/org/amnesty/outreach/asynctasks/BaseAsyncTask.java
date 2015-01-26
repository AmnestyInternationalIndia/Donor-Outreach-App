package in.org.amnesty.outreach.asynctasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import java.io.IOException;

import in.org.amnesty.outreach.activity.HomeActivity;
import in.org.amnesty.outreach.helpers.Utils;

/**
 * Created by rachit on 22/1/15.
 */
public abstract class BaseAsyncTask extends AsyncTask<Void, Void, Boolean>{

    protected final HomeActivity mActivity;

    public BaseAsyncTask(Activity activity){
        mActivity = (HomeActivity) activity;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            doInBackground();
            return true;
        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            Utils.PhoneUtils.enabled(mActivity, Utils.PhoneUtils.PLAY_SERVICES);
        } catch (UserRecoverableAuthIOException userRecoverableException) {
            mActivity.startActivityForResult(
                        userRecoverableException.getIntent(), HomeActivity.REQUEST_AUTHORIZATION);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {

    }

    abstract protected void doInBackground() throws IOException;
}
