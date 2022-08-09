package com.djinni.cjobs.webtoapp.data.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.djinni.cjobs.webtoapp.R;
import com.djinni.cjobs.webtoapp.data.constant.AppConstant;

public class AppPreference {

    private static Context mContext;

    private static AppPreference mAppPreference = null;
    private SharedPreferences mSharedPreferences, mSettingsPreferences;
    private SharedPreferences.Editor mEditor;

    public static AppPreference getInstance(Context context) {
        if (mAppPreference == null) {
            mContext = context;
            mAppPreference = new AppPreference();
        }
        return mAppPreference;
    }

    private AppPreference() {
        mSharedPreferences = mContext.getSharedPreferences(PrefKey.APP_PREF_NAME, Context.MODE_PRIVATE);
        mSettingsPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mEditor = mSharedPreferences.edit();
    }


    public String getString(String key) {
        return mSharedPreferences.getString(key, null);
    }


    public boolean isNotificationOn() {
        return mSettingsPreferences.getBoolean(AppConstant.PREF_NOTIFICATION, true);
    }

    public boolean isCookieEnabled() {
        return mSettingsPreferences.getBoolean(AppConstant.PREF_COOKIE, true);
    }

    public boolean isZoomEnabled() {
        return mSettingsPreferences.getBoolean(AppConstant.PREF_ZOOM, false);
    }

    public String getTextSize() {
        return mSettingsPreferences.getString(AppConstant.PREF_FONT_SIZE, mContext.getResources().getString(R.string.default_text));
    }


}
