package de.rwth.aachen.comsys.androidcv;

import java.util.List;
import java.util.ListIterator;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.FpsMeter;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.app.Activity;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class OpenCVActivity extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "OpenCVActivity";

//	private CameraBridgeViewBase mOpenCvCameraView;
	private ComsysCameraView mOpenCvCameraView;
	private DescriptorController mDescriptorController;

	private Mat mRgba;
	private Mat mGray;
	
	private MenuItem[] 			   mResolutionMenuItems;
    private SubMenu 			   mResolutionMenu;
    private List<Size>             mResolutionList;
	
//	private FpsMeterCustom fpsMeter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.open_cv_activity_layout);
		
//		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.open_cv_activity_view);
		mOpenCvCameraView = (ComsysCameraView) findViewById(R.id.open_cv_activity_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		
//		fpsMeter = new FpsMeterCustom();
		
		// Uncomment to lower the resolution and increase FPS
//		mOpenCvCameraView.setMaxFrameSize(400, 240);

	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");

				// Load native library after OpenCV initialization
				System.loadLibrary("COMSYSAndroidCV");

				// Call native library to load the descriptor
//				String internalStoragePath = getFilesDir().getAbsolutePath();
				String externalStoragePath = getExternalFilesDir(null).getAbsolutePath();
				
				// Load the descriptors files from external storage and
				// prepare the matcher with train descriptors
				mDescriptorController = new DescriptorController(externalStoragePath);
				mDescriptorController.loadDescriptor();
				mDescriptorController.trainMatcher(DescriptorController.Algorithm.ORB, 
						                           DescriptorController.Refine.BOTH);

				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	protected void onPause() {
		
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this,
				mLoaderCallback);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		
		Log.d(TAG, "onCreateOptionMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.open_cv, menu);
		
		mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        int idx = 0;
        while(resolutionItr.hasNext()) {
            Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
         }
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
		
		if (item.getGroupId() == 2) {
            Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);
            resolution = mOpenCvCameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        }
		return true;
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
			View rootView = inflater.inflate(R.layout.fragment_open_cv,
					container, false);
			return rootView;
		}
	}

	@Override
	public void onCameraViewStarted(int width, int height) {

		mGray = new Mat();
		mRgba = new Mat();

	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub

	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		
//		fpsMeter.measure();
		
//		mDescriptorController.FindMatch(mGray.getNativeObjAddr(), 
//                						mRgba.getNativeObjAddr(), 
//                						DescriptorController.Algorithm.ORB,
//                						DescriptorController.Refine.BOTH,
//                						DescriptorController.Matcher.BF_NORM_HAMMING2);
		
		mDescriptorController.FindMatch(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
		
		return mRgba;
	}

	@Override
	protected void onDestroy() {

		mDescriptorController.destroy();
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

}


