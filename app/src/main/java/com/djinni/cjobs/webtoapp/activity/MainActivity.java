package com.djinni.cjobs.webtoapp.activity;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Bundle;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.google.android.gms.ads.AdView;
import com.djinni.cjobs.webtoapp.R;
import com.djinni.cjobs.webtoapp.data.constant.AppConstant;
import com.djinni.cjobs.webtoapp.data.sqlite.NotificationDbController;
import com.djinni.cjobs.webtoapp.listeners.WebListener;
import com.djinni.cjobs.webtoapp.models.notification.NotificationModel;
import com.djinni.cjobs.webtoapp.utility.ActivityUtilities;
import com.djinni.cjobs.webtoapp.utility.AdsUtilities;
import com.djinni.cjobs.webtoapp.utility.AppUtilities;
import com.djinni.cjobs.webtoapp.utility.DialogUtilities;
import com.djinni.cjobs.webtoapp.utility.FilePickerUtilities;
import com.djinni.cjobs.webtoapp.utility.PermissionUtilities;
import com.djinni.cjobs.webtoapp.utility.RateItDialogFragment;
import com.djinni.cjobs.webtoapp.webengine.WebEngine;

import java.util.ArrayList;


public class MainActivity extends BaseActivity implements DialogUtilities.OnCompleteListener {

    private Activity mActivity;
    private Context mContext;

    private RelativeLayout mNotificationView;
    private ImageButton mHomeButton;
    private ImageButton mRefreshButton;

    private WebView mWebView;
    private WebEngine mWebEngine;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NumberProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RateItDialogFragment.show(this, getSupportFragmentManager());

        initVar();
        initView();
        initFunctionality();
        initListener();

    }

    @Override
    protected void onPause() {
        super.onPause();

        //unregister broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newNotificationReceiver);

        mWebEngine.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //register broadcast receiver
        IntentFilter intentFilter = new IntentFilter(AppConstant.NEW_NOTI);
        LocalBroadcastManager.getInstance(this).registerReceiver(newNotificationReceiver, intentFilter);

        mWebEngine.onResume();

        initNotification();

        // load full screen ad
        AdsUtilities.getInstance(mContext).loadFullScreenAd(mActivity);
    }

    // received new broadcast
    private BroadcastReceiver newNotificationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            initNotification();
        }
    };


    @Override
    public void onBackPressed() {
        if (mWebEngine != null && mWebEngine.hasHistory()) {
            mWebEngine.loadPreviousPage();
        } else {
            AppUtilities.tapPromptToExit(mActivity);
        }
    }

    private void initVar() {
        mActivity = MainActivity.this;
        mContext = getApplicationContext();
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        mNotificationView = (RelativeLayout) findViewById(R.id.notificationView);
        mHomeButton = (ImageButton) findViewById(R.id.btn_home);
        mRefreshButton = (ImageButton) findViewById(R.id.btn_refresh);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mProgressBar = (NumberProgressBar) findViewById(R.id.progressBar);
        initWebEngine();

        initToolbar(false);
        initLoader();
    }

    public void initWebEngine() {

        mWebView = (WebView) findViewById(R.id.web_view);

        mWebEngine = new WebEngine(mWebView, mActivity);
        mWebEngine.initWebView();


        mWebEngine.initListeners(new WebListener() {
            @Override
            public void onStart() {
                showLoader();
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoaded() {
                hideLoader();
                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onProgress(int progress) {
                mProgressBar.setProgress(progress);
            }

            @Override
            public void onNetworkError() {
                showEmptyView();
            }

            @Override
            public void onPageTitle(String title) {
            }
        });
    }

    private void initFunctionality() {
        mWebEngine.loadPage(AppConstant.BASE_URL);

        // show banner ads
        AdsUtilities.getInstance(mContext).showBannerAd((AdView) findViewById(R.id.adsView));
    }

    private void initListener() {
        //notification view click listener
        mNotificationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtilities.getInstance().invokeNewActivity(mActivity, NotificationListActivity.class, false);
            }
        });

        //home button click listener
        mHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show full-screen ads
                AdsUtilities.getInstance(mContext).showFullScreenAd();
                // load full screen ad
                AdsUtilities.getInstance(mContext).loadFullScreenAd(mActivity);

                mWebEngine.loadPage(AppConstant.BASE_URL);
            }
        });

        //refresh button click listener
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show full-screen ads
                AdsUtilities.getInstance(mContext).showFullScreenAd();
                // load full screen ad
                AdsUtilities.getInstance(mContext).loadFullScreenAd(mActivity);

                reloadPage();
            }
        });

        //swipe refresh layout listener
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadPage();
            }
        });
    }

    public void reloadPage() {
        mWebEngine.reloadPage();
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }


    public void initNotification() {
        NotificationDbController notificationDbController = new NotificationDbController(mContext);
        TextView notificationCount = (TextView) findViewById(R.id.notificationCount);
        notificationCount.setVisibility(View.INVISIBLE);

        ArrayList<NotificationModel> notiArrayList = notificationDbController.getUnreadData();

        if (notiArrayList != null && !notiArrayList.isEmpty()) {
            int totalUnread = notiArrayList.size();
            if (totalUnread > 0) {
                notificationCount.setVisibility(View.VISIBLE);
                notificationCount.setText(String.valueOf(totalUnread));
            } else {
                notificationCount.setVisibility(View.INVISIBLE);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // main menus
        if (id == R.id.action_settings) {
            ActivityUtilities.getInstance().invokeNewActivity(mActivity, SettingsActivity.class, true);
        } else if (id == R.id.action_about_dev) {
            ActivityUtilities.getInstance().invokeNewActivity(mActivity, AboutDevActivity.class, false);
        }

        // social
        else if (id == R.id.action_youtube) {
            AppUtilities.youtubeLink(mActivity);
        } else if (id == R.id.action_facebook) {
            AppUtilities.faceBookLink(mActivity);
        } else if (id == R.id.action_twitter) {
            AppUtilities.twitterLink(mActivity);
        } else if (id == R.id.action_instagram) {
            AppUtilities.instagramLink(mActivity);
        }

        // others
        else if (id == R.id.action_share) {
            AppUtilities.shareApp(mActivity);
        } else if (id == R.id.action_rate_app) {
            AppUtilities.rateThisApp(mActivity); // this feature will only work after publish the app
        } else if (id == R.id.action_more_app) {
            AppUtilities.moreApps(mActivity);
        } else if (id == R.id.terms_conditions) {
            ActivityUtilities.getInstance().invokeCustomUrlActivity(mActivity, CustomUrlActivity.class, getResources().getString(R.string.terms), getResources().getString(R.string.terms_url), false);
        } else if (id == R.id.privacy_policy) {
            ActivityUtilities.getInstance().invokeCustomUrlActivity(mActivity, CustomUrlActivity.class, getResources().getString(R.string.privacy), getResources().getString(R.string.privacy_url), false);
        } else if (id == R.id.faq) {
            ActivityUtilities.getInstance().invokeCustomUrlActivity(mActivity, CustomUrlActivity.class, getResources().getString(R.string.faq), getResources().getString(R.string.faq_url), false);
        } else if (id == R.id.action_exit) {
            FragmentManager manager = getSupportFragmentManager();
            DialogUtilities dialog = DialogUtilities.newInstance(getString(R.string.exit), getString(R.string.close_prompt), getString(R.string.yes), getString(R.string.no), AppConstant.BUNDLE_KEY_EXIT_OPTION);
            dialog.show(manager, AppConstant.BUNDLE_KEY_DIALOG_FRAGMENT);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (PermissionUtilities.isPermissionResultGranted(grantResults)) {
            if (requestCode == PermissionUtilities.REQUEST_WRITE_STORAGE_UPLOAD) {
                if (mWebEngine != null) {
                    mWebEngine.invokeImagePickerActivity();
                }
            } else if (requestCode == PermissionUtilities.REQUEST_WRITE_STORAGE_DOWNLOAD) {
                if (mWebEngine != null) {
                    mWebEngine.downloadFile();
                }
            }
        } else {
            AppUtilities.showToast(mActivity, getString(R.string.permission_not_granted));
        }

    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (reqCode == WebEngine.KEY_FILE_PICKER) {
                String picturePath = FilePickerUtilities.getPickedFilePath(this, data);
                if (mWebEngine != null) {
                    mWebEngine.uploadFile(data, picturePath);
                } else {
                    AppUtilities.showToast(mContext, getString(R.string.failed));
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (mWebEngine != null) {
                mWebEngine.cancelUpload();
            }
        }
    }

    @Override
    public void onComplete(Boolean isOkPressed, String viewIdText) {
        if (isOkPressed) {
            if (viewIdText.equals(AppConstant.BUNDLE_KEY_EXIT_OPTION)) {
                mActivity.finishAffinity();
            }
        }
    }
}
