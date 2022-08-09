package com.djinni.cjobs.webtoapp.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import com.djinni.cjobs.webtoapp.R;
import com.djinni.cjobs.webtoapp.utility.ActivityUtilities;
import com.djinni.cjobs.webtoapp.utility.AdsUtilities;


public class SettingsActivity extends BaseActivity {

    private Activity mActivity;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVar();
        initView();
    }

    private void initVar() {
        mActivity = SettingsActivity.this;
        mContext = getApplicationContext();
    }

    private void initView() {
        setContentView(R.layout.activity_settings);

        // replace linear layout by preference screen
        getFragmentManager().beginTransaction().replace(R.id.content, new MyPreferenceFragment()).commit();

        initToolbar(true);
        setToolbarTitle(getString(R.string.settings));
        enableUpButton();

        // show full-screen ads
        AdsUtilities.getInstance(mContext).showFullScreenAd();
    }


    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_preference);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActivityUtilities.getInstance().invokeNewActivity(mActivity, MainActivity.class, true);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityUtilities.getInstance().invokeNewActivity(mActivity, MainActivity.class, true);
    }
}