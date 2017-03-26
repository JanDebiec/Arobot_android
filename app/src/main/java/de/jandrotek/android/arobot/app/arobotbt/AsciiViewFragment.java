package de.jandrotek.android.arobot.app.arobotbt;

import java.text.DecimalFormat;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AsciiViewFragment extends Fragment {
    private static final String TAG = "AsciiDisplay";
	DecimalFormat d = SensorRx.d;//new DecimalFormat("#.##");
	private TextView mAzimuthView;
	private TextView mPitchView;
	private TextView mRollView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_ascii_data, parent, false);

		mAzimuthView = (TextView) v.findViewById(R.id.textAzimuth);
		mPitchView = (TextView) v.findViewById(R.id.textPitch);
		mRollView = (TextView) v.findViewById(R.id.textRoll);
		//mTilter = new TiltView(getActivity());

		//mTilter = (TiltView) v.findViewById(R.id.tiltView);

		return v;//mTilter;
	}

	public void updateDisplay(float[] data){
		if(mAzimuthView != null){
			mAzimuthView
				.setText(d.format(data[0] * 180 / Math.PI));
			mPitchView
				.setText(d.format(data[1] * 180 / Math.PI));
			mRollView
				.setText(d.format(data[2] * 180 / Math.PI));
		}
	}

}
