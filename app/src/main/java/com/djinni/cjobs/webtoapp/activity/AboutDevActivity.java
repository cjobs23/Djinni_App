package com.djinni.cjobs.webtoapp.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import com.djinni.cjobs.webtoapp.R;
import com.djinni.cjobs.webtoapp.utility.AdsUtilities;

public class AboutDevActivity extends BaseActivity {
    private Activity mActivity;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVar();
        initView();
    }

    private void initVar() {
        mActivity = AboutDevActivity.this;
        mContext = getApplicationContext();
    }

    private void initView() {
        setContentView(R.layout.activity_about_dev);

        initToolbar(true);
        setToolbarTitle(getString(R.string.about_dev));
        enableUpButton();

        // show full-screen ads
        AdsUtilities.getInstance(mContext).showFullScreenAd();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

