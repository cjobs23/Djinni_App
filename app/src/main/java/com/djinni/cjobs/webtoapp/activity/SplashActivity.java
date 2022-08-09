package com.djinni.cjobs.webtoapp.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.RelativeLayout;


import com.djinni.cjobs.webtoapp.R;
import com.djinni.cjobs.webtoapp.utility.ActivityUtilities;
import com.djinni.cjobs.webtoapp.utility.AppUtilities;

import androidx.appcompat.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {

    private Context mContext;
    private Activity mActivity;
    private RelativeLayout mRootLayout;

    // Constants
    private static final int SPLASH_DURATION = 1500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initVar();
        initView();

    }

    private void initVar() {
        mContext = getApplicationContext();
        mActivity = SplashActivity.this;
    }

    private void initView() {
        setContentView(R.layout.activity_splash);
        mRootLayout = (RelativeLayout) findViewById(R.id.splashBody);

    }

    private void initFunctionality() {
        if (AppUtilities.isNetworkAvailable(mContext)) {
            mRootLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ActivityUtilities.getInstance().invokeNewActivity(mActivity, MainActivity.class, true);
                }
            }, SPLASH_DURATION);
        } else {
            AppUtilities.noInternetWarning(mRootLayout, mContext);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initFunctionality();
    }
}
