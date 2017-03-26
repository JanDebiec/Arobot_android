package de.jandrotek.android.arobot.app.arobotbt;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TiltViewFragment extends Fragment {

	private TiltView mTilter;

	public TiltView getTilter() {
		return mTilter;
	}

	public void setTilter(TiltView tilter) {
		mTilter = tilter;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState){
		// here we get exception, but why?
		// TODO: the reason for exception
		//View v = inflater.inflate(R.layout.fragment_tilt, parent, false);

		mTilter = new TiltView(getActivity());

		//mTilter = (TiltView) v.findViewById(R.id.tiltView);

		return mTilter;
	}

//	//@Override
	public void setTilt(double azimuth, double pitch) {
		if(mTilter != null){
			mTilter.setTilt(azimuth, pitch);
		}
	}

}
