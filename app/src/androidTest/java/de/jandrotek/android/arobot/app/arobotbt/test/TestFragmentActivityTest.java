package de.jandrotek.android.arobot.app.arobotbt.test;

import android.app.Fragment;
import android.app.FragmentTransaction;

import de.jandrotek.android.arobot.app.arobotbt.AsciiViewFragment;
import de.jandrotek.android.arobot.app.arobotbt.TestFragmentActivity;

public class TestFragmentActivityTest extends
		android.test.ActivityInstrumentationTestCase2<TestFragmentActivity> {
	private TestFragmentActivity myFragmentActivity;
	AsciiViewFragment myFragment;

	public TestFragmentActivityTest() {
		super(TestFragmentActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		myFragmentActivity = (TestFragmentActivity) getActivity();
	}

	public void testPreConditions() {
		assertNotNull(myFragmentActivity);
	}

	private Fragment startFragment(Fragment fragment) {
		FragmentTransaction transaction = myFragmentActivity
				.getFragmentManager().beginTransaction();

//		transaction.add(R.id.fragment_ascii_data, fragment,
				transaction.add(R.id.test_fragment_tablelayout, fragment,
				"AsciiDisplay");
		transaction.commit();
		getInstrumentation().waitForIdleSync();
		Fragment frag = myFragmentActivity.getFragmentManager()
				.findFragmentByTag("AsciiDisplay");
		return frag;
	}

	public void testFragment() {
		AsciiViewFragment fragment = new AsciiViewFragment() {
			// Override methods and add assertations here.
		};

		Fragment frag = startFragment(fragment);
	}

}
