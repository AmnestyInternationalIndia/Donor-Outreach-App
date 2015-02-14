package in.org.amnesty.outreach.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
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

    public static final File DEFAULT_APP_PATH = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
            Utils.Constants.DEFAULT_APP_FOLDER_WITH_SLASH);

    public static boolean isCursorEmpty(Cursor cursor) {
        return cursor == null || cursor.getCount() <= 0;
    }

    public static class Constants {
        public static final String DEFAULT_APP_FOLDER = "outreach";
        public static final String DEFAULT_APP_FOLDER_WITH_SLASH = "/outreach/";
        public static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
        public static final String CURRENT_FRAGMENT_TYPE = "current_fragment_type";
        public static final String DEFAULT_DATE_FORMAT = "dd-MMMM-yyyy";
    }

    public static class Logs {

        public static final boolean LOGGING = false;

        /**
         * Log debug message.
         *
         * @param message Message to log
         */
        public static void d(String message) {
            if (LOGGING) {
                android.util.Log.d("@amnesty", message);
            }
        }

        /**
         * Log warning message.
         *
         * @param message Message to log
         */
        public static void w(String message) {
            if (LOGGING) {
                android.util.Log.w("@amnesty", message);
            }
        }

        /**
         * Log informative message.
         *
         * @param message Message to log
         */
        public static void i(String message) {
            if (LOGGING) {
                android.util.Log.i("@amnesty", message);
            }
        }
    }


    public static class Device {
        public static final int GPS = 1;

        public static final int INTERNET = 2;

        public static final int PLAY_SERVICES = 3;

        public static final int EXTERNAL_STORAGE = 4;

        /**
         * Check phone feature enabled
         *
         * @param context     Context
         * @param featureType Feature
         * @return boolean
         */
        public static boolean enabled(Context context, int featureType) {
            boolean data;
            switch (featureType) {
                case GPS:
                    data = isGpsEnabled(context);
                    break;
                case INTERNET:
                    data = isInternetEnabled(context);
                    break;
                case PLAY_SERVICES:
                    data = isPlayServiceInstalled((Activity) context);
                    break;
                case EXTERNAL_STORAGE:
                    data = isExternalStorageEnabled();
                    break;
                default:
                    data = false;
                    break;

            }
            return data;
        }

        /**
         * Is GPS enabled
         *
         * @param context Context
         * @return status
         */
        private static boolean isGpsEnabled(Context context) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
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

        private static boolean isExternalStorageEnabled() {
            boolean mExternalStorageAvailable;
            boolean mExternalStorageWritable;
            String state = Environment.getExternalStorageState();

            switch (state) {
                case Environment.MEDIA_MOUNTED:
                    mExternalStorageAvailable = mExternalStorageWritable = true;
                    break;
                case Environment.MEDIA_MOUNTED_READ_ONLY:
                    mExternalStorageAvailable = true;
                    mExternalStorageWritable = false;
                    break;
                default:
                    mExternalStorageAvailable = mExternalStorageWritable = false;
                    break;
            }
            return mExternalStorageAvailable && mExternalStorageWritable;
        }

        private static boolean isInternetEnabled(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }

        public static void hideKeyboard(Activity activity) {
            try {
                InputMethodManager inputManager = (InputMethodManager) activity
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class Preferences {

        public static final String ACCOUNT_USER_ID = "userId";

        public static final String ACCOUNT_USER_NAME = "userName";

        public static final String ACCOUNT_USER_IMAGE_URL = "userImage";

        public static final String ACCOUNT_REGISTRATION_DATE = "userRegistrationDate";

        public static final String FIRST_LOGIN = "firstLogin";

        public static final String SPLASH_STATUS = "splash";

        public static final String APP_INITIALIZATION_STATUS = "googleConnected";

        public static final String IS_DOWNLOADING = "isDownloading";

        public static final String HAS_DOWNLOADED = "hasDownloaded";

        public static final String GOOGLE_CONNECT_STATUS = "appInitializationStatus";

        public static final String SELECTED_CITY_NAME = "selectedCityName";

        public static final String SELECTED_CITY_FOLDER_ID = "selectedCityFolderId";

        public static final String ACCOUNT_TOKEN = "accountToken";

        public static final String TOTAL_ITEMS = "totalItems";

        public static final String TOTAL_DOWNLOADED_ITEMS = "totalDownloadedItems";

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

        public static int getIntPrefs(Context ctx, String key, int defaultValue) {
            return PreferenceManager.getDefaultSharedPreferences(ctx).getInt(key, 1);
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

    public static class Storage {

        public static boolean existsFolder(Context context, String path) throws IOException,
                ExternalStorageNotFoundException {
            if (!Device.enabled(context, Device.EXTERNAL_STORAGE)) {
                throw ExternalStorageNotFoundException.newInstance();
            }

            File storageLocation = new File(Environment.getExternalStorageDirectory(), path);

            return storageLocation.exists();

        }

        public static File createFile(int type, String path, String name, Context ctx) throws IOException,
                ExternalStorageNotFoundException {
            if (!Device.enabled(ctx, Device.EXTERNAL_STORAGE)) {
                throw ExternalStorageNotFoundException.newInstance();
            }

            File storageLocation = new File(Environment.getExternalStorageDirectory(), path);

            if (!storageLocation.exists()) {
                storageLocation.mkdirs();
            }

            String extension = ".txt";

            switch (type) {
//            case EXPORT_EXCEL:
//                extension = ".csv";
//                break;
//            case EXPORT_PDF:
//                extension = ".pdf";
//                break;
                default:
                    break;
            }
            File file = new java.io.File(storageLocation, name + extension);
            file.createNewFile();
            return file;
        }

        public static boolean existsFile(Context context, String name) throws IOException,
                ExternalStorageNotFoundException {
            if (!Device.enabled(context, Device.EXTERNAL_STORAGE)) {
                throw ExternalStorageNotFoundException.newInstance();
            }

            File storageLocation = new File(Environment.getExternalStorageDirectory(), Constants.DEFAULT_APP_FOLDER);

            if (!storageLocation.exists()) {
                storageLocation.mkdirs();
                return false;
            }

            File file = new File(Environment.getExternalStorageDirectory(), Constants.DEFAULT_APP_FOLDER + "/" + name);

            return file.exists();

        }

        public static String removeExtension(String s) {

            String separator = System.getProperty("file.separator");
            String filename;

            // Remove the path up to the filename.
            int lastSeparatorIndex = s.lastIndexOf(separator);
            if (lastSeparatorIndex == -1) {
                filename = s;
            } else {
                filename = s.substring(lastSeparatorIndex + 1);
            }

            // Remove the extension.
            int extensionIndex = filename.lastIndexOf(".");
            if (extensionIndex == -1) { return filename; }

            return filename.substring(0, extensionIndex);
        }

        public static void deleteDefaultAppDirectory(Context context) throws ExternalStorageNotFoundException {

            if (!Device.enabled(context, Device.EXTERNAL_STORAGE)) {
                throw ExternalStorageNotFoundException.newInstance();
            }

            File dir = DEFAULT_APP_PATH;
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (String aChildren : children) {
                    new File(dir, aChildren).delete();
                }
            }
        }

        public static void clearMediaStorage(Context context) {
            String selection = MediaStore.MediaColumns.DATA + " like ?";
            String[] selectionArgs = new String[]{"%" + Utils.Constants.DEFAULT_APP_FOLDER + "%"};
            context.getContentResolver().delete( MediaStore.Images.Media
                    .EXTERNAL_CONTENT_URI, selection,selectionArgs );

            context.getContentResolver().delete( MediaStore.Video.Media
                    .EXTERNAL_CONTENT_URI, selection, selectionArgs );
        }

    }

    public static class Text {
        /**
         * Validate hex with regular expression
         *
         * @param email email for validation
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

        public static String formatDate(long milliSeconds, String format) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(milliSeconds);
            return new SimpleDateFormat(format, Locale.getDefault()).format(calendar.getTime());
        }

        public static String capitalize(String sentence) {
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

        public static String getStringFromArray(String[] strings, String delimiter) {
            StringBuilder sb = new StringBuilder();
            for (String string : strings) {
                sb.append(string).append(delimiter);
            }
            if (strings.length != 0) { sb.deleteCharAt(sb.length() - 1); }
            return sb.toString();
        }
    }

    public static class ExternalStorageNotFoundException extends IOException {

        private static final long serialVersionUID = -6203316041370436518L;

        public ExternalStorageNotFoundException() {
            super("ExternalStorageNotFound");
        }

        public ExternalStorageNotFoundException(String detailMessage) {
            super("External Storage is not available on the device");
        }

        public static ExternalStorageNotFoundException newInstance() {
            return new ExternalStorageNotFoundException();
        }

    }
}