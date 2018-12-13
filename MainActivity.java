package org.celltools.cellguard;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import org.celltools.cellguard.fragments.CellInfo;
import org.celltools.cellguard.service.CellGuardService;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private boolean mBound;
    private CellGuardService cellGuardService;
    private long mLastPress = 0;    // Back press to exit timer

    final int REQ_COARSE_LOCATION = 10001;
    final int REQ_FINE_LOCATION = 10002;
    final int REQ_ACCESS_NETWORK_STATE = 10003;
    final int CHG_ACCESS_NETWORK_STATE = 10004;
    final int READ_PHONE_STATE = 10005;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        //check permissions
        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,REQ_COARSE_LOCATION);
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,REQ_FINE_LOCATION);
        checkPermission(Manifest.permission.ACCESS_NETWORK_STATE,REQ_ACCESS_NETWORK_STATE);
        checkPermission(Manifest.permission.CHANGE_NETWORK_STATE,CHG_ACCESS_NETWORK_STATE);
        checkPermission(Manifest.permission.READ_PHONE_STATE, READ_PHONE_STATE);
    }

    @Override
    public void onResume() {
        super.onResume();

        //TODO: startService
        startService();
    }

    @Override
    public void onPause() {
        super.onPause();

        //TODO: end service
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        /*final String PERSIST_SERVICE = getString(R.string.pref_persistservice_key);
        boolean persistService = prefs.getBoolean(PERSIST_SERVICE, false);
        if (!persistService) {
            stopService(new Intent(this, AimsicdService.class));
        }*/
    }

    /**
     * Exit application if Back button is pressed twice
     */
    @Override
    public void onBackPressed() {
        Toast onBackPressedToast = Toast
                .makeText(this, R.string.press_once_again_to_exit, Toast.LENGTH_SHORT);
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastPress > 5000) {
            onBackPressedToast.show();
            mLastPress = currentTime;
        } else {
            onBackPressedToast.cancel();
            super.onBackPressed();
            try {
                /*if (cellGuardService.isSmsTracking()) {
                    cellGuardService.stopSmsTracking();
                }*/
            } catch (Exception ee) {
                Log.e("Stopping SMS detection:", ee.getMessage());
            }
            finish();
        }
    }

    /**
     * Service Connection to bind the activity to the service
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            cellGuardService = ((CellGuardService.CellGuardBinder) service).getService();
            mBound = true;

            // Check if tracking cell details check location services are still enabled
            /*if (cellGuardService.isTrackingCell()) {
                cellGuardService.checkLocationServices();
            }*/

            /*if (!cellGuardService.isSmsTracking() && prefs.getBoolean(getString(R.string.adv_user_root_pref_key), false)) {

                //SmsDetection();
            }*/
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w("Warning","Service disconnected");
            mBound = false;
        }
    };

    private void startService() {
        if (!mBound) {
            // Bind to LocalService
            Intent intent = new Intent(MainActivity.this, CellGuardService.class);
            //Start Service before binding to keep it resident when activity is destroyed
            startService(intent);
            //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


        }
    }

    private void checkPermission(String permission, int permissionId){
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(permission, permissionId);
        }
    }

    private void requestPermission(String permission, int permissionId){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                permission)) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
        } else {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    permissionId);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment fragment = null;
            switch (position){
                case 0:
                    fragment=new CellInfo();
                    break;
            }
            //return PlaceholderFragment.newInstance(position + 1);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Cell Info";
            }
            return null;
        }


    }
}
