package de.jandrotek.android.arobot.app.arobotbt;

import java.util.List;

import android.preference.PreferenceActivity;

public class PreferencesFragment extends PreferenceActivity {

  @Override
  public void onBuildHeaders(List<Header> target) {
    loadHeadersFromResource(R.xml.preference_headers, target);
  }
}
