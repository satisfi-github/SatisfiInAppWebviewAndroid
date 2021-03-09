package com.satisfilabs.webclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainScreenActivity extends AppCompatActivity implements View.OnClickListener {
    //public static final String URL = "https://chat.satisfitest.us/?pageID=2434";//https://www.youtube.com/";//https://chat.satisfitest.us/?pageID=2434//"https://www.google.com/";//"https://upload.photobox.com/en/";//"https://www.tinley123park.org/";
    //public static final String cameraPicsWillStoreIn = "/storage/emulated/0/Android/data/com.satisfilabs.webclient/files/Pictures/WebView2021-02-22 12:39:03905581833.png";
    public static final String noInternetTittle = "No Internet Connection";
    public static final String noInternetPositiveButton = "Ok";
    public static final String exitDialogTittle = "Do you want to exit our app";
    public static final String exitDialogPositiveButton = "Yes";
    public static final String exitDialogNegativeButton = "No";
    public static final String exitURL = "Enter Url to continue";
    public static final String invalidURL = "In Valid URL";
    private SwitchCompat topBar,bottomBar;
    private EditText baseUrl;
    private EditText urlParams;
    private TextView baseUrlPlusUrlParams;
    private String baseURL;
    private String URLParams;
    private String totalUrl;

    // These are the buttons of webView and webChrome
    private Button webView,webChrome;
    private Dialog progressDialog;
    private Toast toast;

    // buttonClickedOnWebViewOrWebChrome if false webChrome , true webView
    private boolean buttonClickedOnWebViewOrWebChrome;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        init();
    }

    /**
     * this is for initializing
     */
    public void init(){
        topBar = findViewById(R.id.topBar);
        bottomBar = findViewById(R.id.bottomBar);
        webView = findViewById(R.id.webView);
        webChrome = findViewById(R.id.webChrome);
        baseUrl = findViewById(R.id.baseURL);
        urlParams = findViewById(R.id.urlParam);
        baseUrlPlusUrlParams = findViewById(R.id.baseURLParam);
        progressDialog = getProgressDialog(this);
        webChrome.setOnClickListener(this);
        webView.setOnClickListener(this);

        baseUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                baseURL = baseUrl.getText().toString();
                totalUrl = baseURL+URLParams;
                baseUrlPlusUrlParams.setText(totalUrl);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        urlParams.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                URLParams = urlParams.getText().toString();
                totalUrl = baseURL+URLParams;
                baseUrlPlusUrlParams.setText(totalUrl);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        if( view == webChrome ) {
            if (toast != null) {
                toast.cancel();
            }
            buttonClickedOnWebViewOrWebChrome = false;
            Execute();
        } else if( view == webView ) {
            if (toast != null) {
                toast.cancel();
            }
            buttonClickedOnWebViewOrWebChrome = true;
            Execute();
        }
    }

    /**
     * this is for validating url and opening webContainer Activity
     */
    public void Execute(){
        progressDialog.show();
        new Thread(() -> {
            String getError = ConstantsUtility.openWebView(baseUrl.getText().toString(),urlParams.getText().toString(),MainScreenActivity.this,buttonClickedOnWebViewOrWebChrome,topBar.isChecked(),bottomBar.isChecked());
            runOnUiThread(() -> {
                switch (getError) {
                    case ConstantsUtility.urlLength:
                        toast = disappearToastMessage(getApplicationContext(),exitURL);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        progressDialog.dismiss();
                        break;
                    case ConstantsUtility.noInternet:
                        progressDialog.dismiss();
                        AlertDialog.Builder exitApp = new AlertDialog.Builder(MainScreenActivity.this);
                        exitApp.setCancelable(false);
                        exitApp.setTitle(noInternetTittle);
                        exitApp.setIcon(R.mipmap.ic_launcher);
                        exitApp.setPositiveButton(noInternetPositiveButton, (dialogInterface, i) -> dialogInterface.dismiss());
                        exitApp.show();
                        break;
                    case ConstantsUtility.inValidURL:
                        toast = disappearToastMessage(getApplicationContext(),invalidURL);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        progressDialog.dismiss();
                        break;
                    default:
                        progressDialog.dismiss();
                        break;
                }
            });
        }).start();
    }

    /**
     * this is for displaying the progressbar for entire screen
     */
    public static Dialog getProgressDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(context,android.R.color.transparent));
        ProgressBar progressBar = new ProgressBar(context);
        dialog.addContentView(progressBar,new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        dialog.setCancelable(false);
        return dialog;
    }

    /**
     * this is for disappearing Toast Message when user clicks more number of times on a button
     */
    public static Toast disappearToastMessage(Context context,String msg){
        return Toast.makeText(context, msg, Toast.LENGTH_SHORT);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder exitApp = new AlertDialog.Builder(MainScreenActivity.this);
        exitApp.setCancelable(false);
        exitApp.setTitle(exitDialogTittle);
        exitApp.setIcon(R.mipmap.ic_launcher);
        exitApp.setNegativeButton(exitDialogNegativeButton,((dialogInterface, i) -> dialogInterface.dismiss()));
        exitApp.setPositiveButton(exitDialogPositiveButton, (dialogInterface, i) -> {
            finishAffinity();
            overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);
        });
        exitApp.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences getSavedUrlData = getSharedPreferences(ConstantsUtility.sharePreferenceName,MODE_PRIVATE);
        String getPreviousUrls = getSavedUrlData.getString(ConstantsUtility.savePreviousUrlUsedSharedPreferences, ConstantsUtility.stringDefaultValue);
        String getPreviousUrlsParams = getSavedUrlData.getString(ConstantsUtility.savePreviousUrlParamsUsedSharedPreferences, ConstantsUtility.stringDefaultValue);
        baseUrl.setText(getPreviousUrls);
        urlParams.setText(getPreviousUrlsParams);
        topBar.setChecked(getSavedUrlData.getBoolean(ConstantsUtility.intentPutExtraTopBar,ConstantsUtility.booleanDefaultValue));
        bottomBar.setChecked(getSavedUrlData.getBoolean(ConstantsUtility.intentPutExtraBottomBar,!ConstantsUtility.booleanDefaultValue));
        progressDialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressDialog.dismiss();
    }
}