package de.jandrotek.android.arobot.app.arobotbt;

import java.text.DecimalFormat;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LeftRightViewFragment extends Fragment {
    private static final String TAG = "LeftRightDisplay";
	DecimalFormat d = SensorRx.d;//new DecimalFormat("#0.00");
	private TextView mPitchView;
	private TextView mRollView;
	private TextView mLeftView;
	private TextView mRightView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_lr_data, parent, false);

		mPitchView = (TextView) v.findViewById(R.id.textPitch);
		mRollView = (TextView) v.findViewById(R.id.textRoll);
		mLeftView = (TextView) v.findViewById(R.id.textLeft);
		mRightView = (TextView) v.findViewById(R.id.textRight);
		//mTilter = new TiltView(getActivity());

		//mTilter = (TiltView) v.findViewById(R.id.tiltView);

		return v;//mTilter;
	}

	public void updateDisplay(float[] data){
		if(mLeftView != null){
			mPitchView.setText(d.format(data[5]));
			mRollView.setText(d.format(data[6]));
			mLeftView.setText(d.format(data[3]));
			mRightView.setText(d.format(data[4]));
		}
	}

}
