/**
 * 
 */
package de.rwth.aachen.comsys.androidcv;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentMain extends Fragment {

	public FragmentMain() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_layout_main, container,	false);
		return view;
	}
	
	public void startCamera(View startCameraBtn) {

		Intent intent = new Intent(getActivity(), OpenCVActivity.class);
		startActivity(intent);
	}
	
	
	

}
