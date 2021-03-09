package com.satisfilabs.webclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

@SuppressLint("SetJavaScriptEnabled")
public class WebContainerActivity extends AppCompatActivity implements View.OnClickListener {

    private WebView webView;
    private ProgressBar progressBar;
    private ImageView leftArrowImageView, refreshImageView, exitImageView;
    private ValueCallback<Uri[]> uploadFile;
    private String cameraPhotoPath;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != ConstantsUtility.requestCode || uploadFile == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        Uri[] results = null;
        if(resultCode == RESULT_OK) {
            //If user Opened Camera and take picture
            if (data == null) {
                if (cameraPhotoPath != null) {
                    results = new Uri[]{Uri.parse(cameraPhotoPath)};
                }
            } else {    // If User Opened Gallery and selecting picture
                ClipData clipData = data.getClipData();
                //If User Select More than One Picture
                if(clipData!=null){
                    results = new Uri[clipData.getItemCount()];
                    for(int i=0;i<clipData.getItemCount();i++){
                        Uri imageUri = clipData.getItemAt(i).getUri();
                        results[i] = imageUri;
                    }
                }else{  //If User Select Only One Picture
                    results = new Uri[]{data.getData()};
                }
            }
        }
        uploadFile.onReceiveValue(results);
        uploadFile = null;
    }

    /*
    * This Method is for,
    * If any user doesn't allow permission like Storage and Location then
    * you will be exit from this activty and opens previous activity.
    */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ConstantsUtility.requestCode) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED || grantResults[2] == PackageManager.PERMISSION_DENIED) {
                    ConstantsUtility.toastMessage(getApplicationContext(),ConstantsUtility.permission_denied);
                    goToMainScreenWindow();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_container);

        //This is for checking permission every time when user opens our APP
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, ConstantsUtility.requestCode);
        }

        init();
    }

    /**
     * this is for initializing
     */
    public void init(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(ConstantsUtility.stringDefaultValue);
        Toolbar bottomNavigationView = findViewById(R.id.bottom);

        progressBar = findViewById(R.id.progress_circular);
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        leftArrowImageView = findViewById(R.id.leftArrow);
        refreshImageView = findViewById(R.id.refresh);
        exitImageView = findViewById(R.id.exit);
        String URL = getIntent().getStringExtra(ConstantsUtility.intentPutExtraUrl);

        if(!getIntent().getBooleanExtra(ConstantsUtility.intentPutExtraTopBar, ConstantsUtility.booleanDefaultValue))
            toolbar.setVisibility(View.GONE);
        if(!getIntent().getBooleanExtra(ConstantsUtility.intentPutExtraBottomBar, ConstantsUtility.booleanDefaultValue))
            bottomNavigationView.setVisibility(View.GONE);

        webView.setWebViewClient(new WebViewClient(){
            //This method is for Visibility of the progressBar
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            /**
             * This is above API Level 24
             * This method is used to open the maps in google map when user
             * clicks on the "Navigate in app" button.
             * and To open pdf's in pdf viewer
             **/
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if(request.getUrl().toString().contains(ConstantsUtility.apiLevelAbove24NavigatingToGoogleMap)) {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString())));
                    return true;
                } else if(request.getUrl().toString().contains(".pdf")){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(request.getUrl().toString()), "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    return true;
                } else
                    return false;
            }

            /**
             * This is below API Level 24
             * This method is used to open the maps in google map when user
             * clicks on the "Navigate in app" button.
             * and To open pdf's in pdf viewer
             **/
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.contains(ConstantsUtility.apiLevelBelow24NavigatingToGoogleMap)) {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else if(url.contains(".pdf")){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(url), "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    return true;
                } else
                    return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){
            //This method is for showing the progress of current webView page on the progressBar
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if(newProgress == 100) {
                    new Handler().postDelayed(()-> progressBar.setVisibility(View.GONE), ConstantsUtility.circularProgressBarDelayMills);
                }
                super.onProgressChanged(view, newProgress);
            }

            //This method is for accessing location
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                LocationManager locationManager = (LocationManager) WebContainerActivity.this.getSystemService(Context.LOCATION_SERVICE);
                boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if(!gpsEnabled && !networkEnabled){
                    new AlertDialog.Builder(WebContainerActivity.this)
                            .setIcon(R.mipmap.ic_launcher)
                            .setCancelable(false)
                            .setTitle(ConstantsUtility.locationDialogBoxTittle)
                            .setMessage(ConstantsUtility.locationDialogBoxMessage)
                            .setPositiveButton(ConstantsUtility.locationDialogBoxPositiveButton, (dialogInterface, i) -> {
                                WebContainerActivity.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                dialogInterface.dismiss();
                            })
                            .setNegativeButton(ConstantsUtility.locationDialogBoxNegativeButton, (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                                if(webView.canGoBack())
                                    webView.goBack();
                                else
                                    goToMainScreenWindow();
                            }).create().show();
                }
                callback.invoke(origin, !ConstantsUtility.booleanDefaultValue, ConstantsUtility.booleanDefaultValue);
            }

            //This method is for uploading images into webView
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if(uploadFile!=null){
                    uploadFile.onReceiveValue(null);
                    uploadFile = null;
                }

                uploadFile = filePathCallback;
                //This Intent for CAMERA
                Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Creating path to store image in our device
                File photoFile = null;
                try {
                    String timeStamp = new SimpleDateFormat(ConstantsUtility.simpleDateAndFormat, Locale.getDefault()).format(new Date());
                    String imageFileName = ConstantsUtility.appName + timeStamp;
                    photoFile = File.createTempFile(imageFileName,ConstantsUtility.imageExtension,getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                // Continue If picture is successfully saved
                if (photoFile != null) {
                    cameraPhotoPath = ConstantsUtility.cameraIntentPhotoPath + photoFile.getAbsolutePath();
                    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                } else {
                    openCameraIntent = null;
                }

                //This Intent for GALLERY
                Intent galleryIntent = fileChooserParams.createIntent();
                galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                galleryIntent.setType(ConstantsUtility.galleryIntentSetType);
                galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, fileChooserParams.getAcceptTypes());

                /*
                 * This Intent is for ,
                 * if an user device does'nt allow to use Camera intent then
                 * Camera option should not be displayed.
                 */
                Intent[] cameraIntent;
                if (openCameraIntent != null)
                    cameraIntent = new Intent[]{openCameraIntent};
                else
                    cameraIntent = new Intent[0];

                //This Intent for Displaying Both Options Camera as well as Gallery
                Intent displayIntent = new Intent(Intent.ACTION_CHOOSER);
                displayIntent.putExtra(Intent.EXTRA_INTENT, galleryIntent);
                displayIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntent);
                displayIntent.putExtra(Intent.EXTRA_TITLE, ConstantsUtility.cameraOrGalleryIntentDisplayingTittle);
                startActivityForResult(displayIntent, ConstantsUtility.requestCode);

                return true;
            }
        });

        webView.loadUrl(URL);

        leftArrowImageView.setOnClickListener(this);
        refreshImageView.setOnClickListener(this);
        exitImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == leftArrowImageView){
            if (webView.canGoBack())
                webView.goBack();
            else
                goToMainScreenWindow();
        } else if(view == refreshImageView){
            webView.reload();
        } else if(view == exitImageView){
            goToMainScreenWindow();
        }
    }

    //This is for backButton
    @Override
    public void onBackPressed() {
        if(webView.canGoBack())
            webView.goBack();
        else {
            goToMainScreenWindow();
        }
    }

    /**
     * this is for exit the current screen and go back to main screen
     * Here you need to add the Your own Activity class
     */
    private void goToMainScreenWindow(){
        finish();
        // The below line should be removed when you don't use animation effect
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(webView.getWindowToken(), 0);
        }
    }
}