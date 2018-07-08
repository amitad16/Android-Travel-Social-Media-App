package com.example.android.travel.Share;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.travel.R;
import com.example.android.travel.Utils.BottomNavigationViewHelper;
import com.example.android.travel.Utils.Permissions;
import com.example.android.travel.Utils.SectionPagerAdapter;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.MessageFormat;

public class ShareActivity extends AppCompatActivity {
    private static final String TAG = "ShareActivity";
    private Context context;

    // Constants
    private static final int ACTIVITY_NUM = 2;
    public static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG, "onCreate: Share Activity Started");

        context = ShareActivity.this;

        if (checkPermissionsArray(Permissions.PERMISSIONS)) {
            setupViewPager();
        } else {
            verifyPermissions(Permissions.PERMISSIONS);
        }

//        setupBottomNavigationView();
    }

    /**
     * Return the current tab number
     * 0 = Gallery Fragment
     * 1 = Camera Fragmetn
     * @return
     */
    public int getCurrentTabNumber() {
        return viewPager.getCurrentItem();
    }

    /**
     * Setup ViewPager for manager tabs
     */
    private void setupViewPager() {
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new CameraFragment());
        viewPager = findViewById(R.id.container);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs_bottom);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText(getString(R.string.gallery));
        tabLayout.getTabAt(1).setText(getString(R.string.photo));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabSelected: selected tab is: " + tab.getPosition());
                if (tab.getPosition() == 0) {
                    Log.d(TAG, "onTabSelected: Gallery tab is selected");
                }
                if (tab.getPosition() == 1) {
                    Log.d(TAG, "onTabSelected: Camera tab is selected");

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabSelected: unselected tab is: " + tab);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabSelected: reselected tab is: " + tab);
            }
        });
    }

    public int getTask() {
        Log.d(TAG, MessageFormat.format("getTask: TASK: {0}", getIntent().getFlags()));
        return getIntent().getFlags();
    }

    /**
     * Verify all the permissions passed to the array
     * @param permissions
     */
    private void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions");

        ActivityCompat.requestPermissions(ShareActivity.this, permissions, VERIFY_PERMISSIONS_REQUEST);

    }

    /**
     * Check an array of permissions
     * @param permissions
     * @return
     */
    private boolean checkPermissionsArray(String[] permissions) {
        Log.d(TAG, "checkPermissionsArray: Checking permissions array");

        for (String check : permissions) {
            if (!checkPermissions(check)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check a single permission has been verified
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission) {
        Log.d(TAG, "checkPermissions: Checking Permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: \n Permission was not granted for: " + permission);
            return false;
        } else {
            Log.d(TAG, "checkPermissions: \n Permission was granted for: " + permission);
            return true;
        }
    }

    /**
     * Bottom navigation setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting up Bottom Navigation View");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottom_nav_view_bar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(context, this, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
