package in.org.amnesty.outreach.helpers;

/**
 * Created by rachit on 3/1/15.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.org.amnesty.outreach.activity.HomeActivity;

public class Utils {

    public static class Constants {
        public static final String DEFAULT_APP_FOLDER  = "outreach";
        public static final String DEFAULT_APP_FOLDER_WITH_SLASH  = "/outreach/";
        public static final String DEFAULT_APP_THUMBNAIL_FOLDER  = "outreach/thumbnails";
    }

    /**
     * Hide keyboard.
     *
     * @param activity
     */
    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isCursorEmpty(Cursor cursor) {
        if(cursor == null || cursor.getCount() <= 0) {
            return true;
        }

        return false;
    }

	/**************************************
	 ***************** User ****************
	 **************************************/

//    public static User getCurrentUser(Context context) {
//        User user = new User();
//        user.mId = Utils.getLongPrefs(context, CURRENT_USER_ID);
//        user.mName = Utils.getStringPrefs(context, CURRENT_USER_NAME);
//        user.mEmail = Utils.getStringPrefs(context, CURRENT_USER_EMAIL);
//
//        return user;
//    }

    public static class LogUtils {

        public static final boolean LOGGING = false;
        /**
         * Log debug message.
         *
         * @param message
         */
        public static void d(String message) {
            if(LOGGING) {
                Log.d("@amnesty", message);
            }
        }

        /**
         * Log warning message.
         *
         * @param message
         */
        public static void w(String message) {
            if(LOGGING) {
                Log.w("@amnesty", message);
            }
        }

        /**
         * Log informative message.
         *
         * @param message
         */
        public static void i(String message) {
            if(LOGGING) {
                Log.i("@amnesty", message);
            }
        }
    }



    public static class PhoneUtils {
        public static final int GPS = 1;

        public static final int INTERNET = 2;

        public static final int PLAY_SERVICES = 3;

        public static final int EXTERNAL_STORAGE = 4;

        /**
         *
         * Check phone feature enabled
         *
         * @param context
         * @param featureType
         * @return
         */
        public static boolean enabled(Context context, int featureType) {
            boolean data = false;
            switch (featureType) {
                case GPS:
                    data = isGpsEnabled(context);
                    break;
                case INTERNET:
                    data = isInternetEnabled(context);
                    break;
                case PLAY_SERVICES:
                    data = isPlayServiceInstalled((Activity)context);
                    break;
                case EXTERNAL_STORAGE:
                    data = isExternalStorageEnabled(context);
                    break;
                default:
                    data = false;
                    break;

            }
            return data;
        }

        /**
         *
         * Is GPS enabled
         *
         * @param context
         *
         * @return status
         */
        private static boolean isGpsEnabled(Context context) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                return true;
            return false;
        }

        private static boolean isPlayServiceInstalled(final Activity activity) {
            final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
            if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Dialog dialog =
                                GooglePlayServicesUtil.getErrorDialog(connectionStatusCode, activity,
                                        HomeActivity.REQUEST_GOOGLE_PLAY_SERVICES);
                        dialog.show();
                    }
                });
                return false;
            }
            return true;
        }

        private static boolean isExternalStorageEnabled(Context context) {
            boolean mExternalStorageAvailable = false;
            boolean mExternalStorageWriteable = false;
            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mExternalStorageAvailable = mExternalStorageWriteable = true;
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                mExternalStorageAvailable = true;
                mExternalStorageWriteable = false;
            } else {
                mExternalStorageAvailable = mExternalStorageWriteable = false;
            }
            return mExternalStorageAvailable && mExternalStorageWriteable;
        }

        private static boolean isInternetEnabled(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            }
            return false;
        }
    }

    public static class PreferenceUtils {

        public static final String ACCOUNT_USER_ID = "userId";

        public static final String ACCOUNT_USER_NAME = "userName";

        public static final String ACCOUNT_USER_IMAGE_URL = "userImage";

        public static final String ACCOUNT_REGISTRATION_DATE = "userRegistrationDate";

        public static final String FIRST_LOGIN = "firstLogin";

        public static final String SPLASH_STATUS = "splash";

        public static final String APP_INITIALIZATION_STATUS = "googleConnected";

        public static final String IS_DOWNLOADING = "isDownloading";

        public static final String GOOGLE_CONNECT_STATUS = "appInitializationStatus";

        public static final String SELECTED_CITY_NAME = "selectedCityName";

        public static final String SELECTED_CITY_FOLDER_ID = "selectedCityFolderId";

        public static final String ACCOUNT_TOKEN =  "accountToken";

        public static Boolean getBooleanPrefs(Context ctx, String key) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(key, false);
        }

        public static void setBooleanPrefs(Context ctx, String key, Boolean value) {
            PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean(key, value).commit();
        }

        public static String getStringPrefs(Context ctx, String key) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getString(key, "");
        }

        public static void setStringPrefs(Context ctx, String key, String value) {
            PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(key, value).commit();
        }

        public static int getIntPrefs(Context ctx, String key) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getInt(key, 0);
        }

        public static void setIntPrefs(Context ctx, String key, int value) {
            PreferenceManager.getDefaultSharedPreferences(ctx).edit().putInt(key, value).commit();
        }

        public static long getLongPrefs(Context ctx, String key) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getLong(key, 0);
        }

        public static void setLongPrefs(Context ctx, String key, long value) {
            PreferenceManager.getDefaultSharedPreferences(ctx).edit().putLong(key, value).commit();
        }

        public static float getFloatPrefs(Context ctx, String key) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getFloat(key, 0);
        }

        public static void setFloatPrefs(Context ctx, String key, float value) {
            PreferenceManager.getDefaultSharedPreferences(ctx).edit().putFloat(key, value).commit();
        }

        public static void clearPrefs(Context ctx) {
            PreferenceManager.getDefaultSharedPreferences(ctx).edit().clear().commit();
        }
    }

    public static final String APP_PATH = "/exports";

    public static File createFile(int type, String path, String name, Context ctx) throws IOException,
            ExternalStorageNotFoundException {
        if (!PhoneUtils.enabled(ctx, PhoneUtils.EXTERNAL_STORAGE)) {
            throw ExternalStorageNotFoundException.newInstance();
        }

        File storageLocation = new File(Environment.getExternalStorageDirectory(), path);

        if (!storageLocation.exists()) {
            storageLocation.mkdirs();
        }

        String extension = ".txt";

//        switch (type) {
//            case ExportFragment.EXPORT_EXCEL:
//                extension = ".csv";
//                break;
//            case ExportFragment.EXPORT_PDF:
//                extension = ".pdf";
//                break;
//            default:
//                break;
//        }
        File file = new File(storageLocation, name + extension);
        file.createNewFile();
        return file;
    }

    /**************************************
     ************* Validations ************
     **************************************/

    /**
     * Validate hex with regular expression
     *
     * @param email
     *            email for validation
     * @return true valid hex, false invalid hex
     */
    public static boolean validateEmail(final String email) {
        Pattern pattern;
        Matcher matcher;

        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static String getFormattedDate(long milis, String format) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(milis);
        return new SimpleDateFormat(format, Locale.getDefault()).format(calendar.getTime());
    }

    public static String capitalize(String sentence){
        if (TextUtils.isEmpty(sentence)) {
            return sentence;
        }

        String[] words = sentence.split("-");
        StringBuilder sentenceBuilder = new StringBuilder();
        for (String word : words) {
            sentenceBuilder.append(word.substring(0, 1).toUpperCase()).append(word.substring(1)).append(" ");
        }

        return sentenceBuilder.toString().trim();
    }

	public static class ExternalStorageNotFoundException extends IOException {

		private static final long serialVersionUID = -6203316041370436518L;

		public static ExternalStorageNotFoundException newInstance() {
			return new ExternalStorageNotFoundException();
		}

		public ExternalStorageNotFoundException() {
			super("ExternalStorageNotFound");
		}

		public ExternalStorageNotFoundException(String detailMessage) {
			super("External Storage is not available on the device");
		}

	}
}