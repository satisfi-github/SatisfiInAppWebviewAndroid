package com.satisfilabs.webclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.Gravity;
import android.widget.Toast;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class ConstantsUtility {
    public static final int requestCode = 1637;
    public static final String appName = "WebView";
    public static final String cameraIntentPhotoPath = "file:";
    public static final String galleryIntentSetType = "image/*";
    public static final String imageExtension = ".jpg";
    public static final int circularProgressBarDelayMills = 500;
    public static final String sharePreferenceName = "Previous URL";
    public static final String isHttpPresent = "http://";
    public static final String isHttpsPresent = "https://";
    public static final String validURL = "validUrl";
    public static final String inValidURL = "invalidUrl";
    public static final String savePreviousUrlUsedSharedPreferences = "URL";
    public static final String savePreviousUrlParamsUsedSharedPreferences = "URLParams";
    public static final String stringDefaultValue = "";
    public static final boolean booleanDefaultValue = false;
    public static final String intentPutExtraUrl = "URL";
    public static final String intentPutExtraTopBar = "TopBar";
    public static final String intentPutExtraBottomBar = "BottomBar";
    public static final String forHttpUrlReplacingToHttps = "s";
    public static final int indexForReplacingHttpToHttps = 4;
    public static final String urlLength = "zero";
    public static final String noInternet = "noInternetConnection";
    public static final String permission_denied = "Without Your Permission We Cannot Access it!!!";
    public static final String cameraOrGalleryIntentDisplayingTittle = "Choose Image or Take Pic";
    public static final String apiLevelAbove24NavigatingToGoogleMap = "maps.app.goo.gl";
    public static final String apiLevelBelow24NavigatingToGoogleMap = "navlite.app.goo.gl";
    public static final String locationDialogBoxTittle = "Location Permission";
    public static final String locationDialogBoxMessage = "Please On Location To Continue";
    public static final String locationDialogBoxPositiveButton = "GoToSettings";
    public static final String locationDialogBoxNegativeButton = "Cancel";
    public static final String simpleDateAndFormat = "yyyy-MM-dd HH:mm:ss";
    public static String URL;

    /**
     *
     * @param baseUrl - simple url
     * @param UrlParams - rel parameters
     * @param context - Activity context
     * @param openInWebView - if true opens webView otherwise chrome browser
     * @param topBar - if true it shows top bar otherwise it is hidden
     * @param bottomBar - if true it shows bottom bar otherwise it is hidden
     * @return - returns if error found in url
     */
    public static String openWebView(String baseUrl,String UrlParams,Context context,boolean openInWebView,boolean topBar,boolean bottomBar){
        URL = baseUrl+UrlParams;
        if(URL.length() == 0){
            return urlLength;
        }

        /*
            checking internet connectivity
         */
        if(!isNetworkAvailable(context)) {
            return noInternet;
        }

        if(URL.contains(ConstantsUtility.isHttpPresent) || URL.contains(ConstantsUtility.isHttpsPresent)) {
            if(URL.contains(ConstantsUtility.isHttpPresent)){
                StringBuilder newString = new StringBuilder(URL);
                newString.insert(ConstantsUtility.indexForReplacingHttpToHttps,ConstantsUtility.forHttpUrlReplacingToHttps);
                URL = newString.toString();
            }
        }
        else{
            URL = ConstantsUtility.isHttpsPresent+URL;
        }

        /*
         * checking the URL validity
         */
        if(!isValidUrl(URL).equals(validURL)) {
            return inValidURL;
        }

        /*
            You Can Remove this code if you don't want to save the BaseURL,UrlParams,TopBar and Bottombar
         */
        SharedPreferences savePreviousUrlUsed = context.getSharedPreferences(ConstantsUtility.sharePreferenceName,MODE_PRIVATE);
        SharedPreferences.Editor editor = savePreviousUrlUsed.edit();
        editor.putString(ConstantsUtility.savePreviousUrlUsedSharedPreferences, baseUrl);
        editor.putString(ConstantsUtility.savePreviousUrlParamsUsedSharedPreferences, UrlParams);
        editor.putBoolean(ConstantsUtility.intentPutExtraTopBar,topBar);
        editor.putBoolean(ConstantsUtility.intentPutExtraBottomBar,bottomBar);
        editor.apply();
        /*
            Code Ends
         */
        if (!openInWebView) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
        } else {
            Intent intent = new Intent(context, WebContainerActivity.class);
            intent.putExtra(ConstantsUtility.intentPutExtraUrl, URL);
            intent.putExtra(ConstantsUtility.intentPutExtraTopBar, topBar);
            intent.putExtra(ConstantsUtility.intentPutExtraBottomBar, bottomBar);
            context.startActivity(intent);
        }
        // The below line should be removed when you don't use animation effect
        ((Activity) context).overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);

        return validURL;
    }

    /**
     * This is for checking Network Connection
     * @param context - Activity context
     * @return - returns true when internet is there otherwise false
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * this is for Toast messages
     * @param co - Activity context
     * @param data - data to be displayed on toast
     */
    public static void toastMessage(Context co,String data){
        Toast toast = Toast.makeText(co, data, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * this is for validating URL
     * @param siteUrl - baeUrl+UrlParams will be passed
     * @return it return true if valid otherwise false
     */
    public static String isValidUrl(String siteUrl){
        try {
            URL url = new URL(siteUrl);
            ConstantsUtility.HttpsTrustManager.allowAllSSL();
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            if(urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                urlConnection.disconnect();
                return ConstantsUtility.validURL;
            }
            else
                return ConstantsUtility.inValidURL;
        } catch (IOException e) {
            e.printStackTrace();
            return ConstantsUtility.inValidURL;
        }
    }

    /**
     * this is for Trusting Http SSL Certificate
     */
    public static class HttpsTrustManager implements X509TrustManager {

        private static TrustManager[] trustManagers;
        private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[]{};

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] x509Certificates, String s) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] x509Certificates, String s) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return _AcceptedIssuers;
        }

        public static void allowAllSSL() {
            HttpsURLConnection.setDefaultHostnameVerifier((arg0, arg1) -> true);

            SSLContext context = null;
            if (trustManagers == null) {
                trustManagers = new TrustManager[]{new HttpsTrustManager()};
            }

            try {
                context = SSLContext.getInstance("TLS");
                context.init(null, trustManagers, new SecureRandom());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }

            if (context != null) {
                HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            }
        }
    }
}
