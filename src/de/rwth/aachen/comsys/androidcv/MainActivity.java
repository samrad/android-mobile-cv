package de.rwth.aachen.comsys.androidcv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private CustomDrawerAdapter mAdapter;
	private List<DrawerItem> mDrawerItems;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity_layout);
		

		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		// DrawerItem list
		mDrawerItems = new ArrayList<DrawerItem>();
		mDrawerItems.add(new DrawerItem("Camera", R.drawable.ic_action_video));
		mDrawerItems.add(new DrawerItem("Bluetooth", R.drawable.ic_action_bluetooth));
		mDrawerItems.add(new DrawerItem("WiFi", R.drawable.ic_action_network_wifi));
		
		// Setting the drawer adapter
		mAdapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item, mDrawerItems);

		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(mAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(
				this, 					/* host Activity */
				mDrawerLayout, 			/* DrawerLayout object */
				R.drawable.ic_drawer, 	/* nav drawer image to replace 'Up' caret */
				R.string.drawer_open, 	/* "open drawer" description for accessibility */
				R.string.drawer_close 	/* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); 	// creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); 	// creates call to
											// onPrepareOptionsMenu()
			}
		};
		
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		// final Button startCameraBtn = (Button)
		// findViewById(R.id.main_activity_startCamera_btn);
		CopyDescriptor cDescriptor = new CopyDescriptor(getApplicationContext());

		try {
			cDescriptor.copy();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (savedInstanceState == null) {
			
			selectFragment(0);
//			getFragmentManager().beginTransaction()
//					.add(R.id.main_activity_layout, new PlaceholderFragment())
//					.commit();
		}

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		
		// Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	public void startCamera(View startCameraBtn) {

		Intent intent = new Intent(this, OpenCVActivity.class);
		startActivity(intent);
	}

	/* The click listener for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectFragment(position);
		}
	}

	/* Swaps fragments in the main content view */
	private void selectFragment(int position) {
		
		Fragment fragment = null;
		Bundle args = new Bundle();
		switch (position) {
		case 0:
			fragment = new FragmentMain();
			break;
		case 1:
			fragment = new FragmentBluetooth();
			break;
		case 2:
			fragment = new FragmentWifi();
			break;
//		case 3:
//			fragment = new FragmentMain();
//			break;
		default:
			break;
		}
		
		fragment.setArguments(args);
        FragmentManager frgManager = getFragmentManager();
        frgManager.beginTransaction().replace(R.id.main_activity_layout, fragment).commit();

        if (position != 0) {
			mDrawerList.setItemChecked(position, true);
//			setTitle(mDrawerItems.get(position).getItemName());
		}
		mDrawerLayout.closeDrawer(mDrawerList);
	}
}
