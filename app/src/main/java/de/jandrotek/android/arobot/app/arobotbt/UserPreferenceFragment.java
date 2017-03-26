package de.jandrotek.android.arobot.app.arobotbt;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public  class UserPreferenceFragment extends PreferenceFragment {

	  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.userpreferences);
  }

//	  @Override
//	  public void onBuildHeaders(List<Header> target) {
//
//		  loadHeadersFromResource(R.xml.preference_headers, target);
//	  }
}
