package ro.pontes.pontesgamezone;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class MiscellaneousSettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_miscellaneous_settings);

		// Check or check the check boxes, depending of current boolean values:

		// For keeping screen awake:
		CheckBox cbtKeepScreenAwake = (CheckBox) findViewById(R.id.cbtKeepScreenAwake);
		cbtKeepScreenAwake.setChecked(MainActivity.isWakeLock);

		// For allowing vibrations:
		CheckBox cbtIsVibration = (CheckBox) findViewById(R.id.cbtIsVibration);
		cbtIsVibration.setChecked(MainActivity.isVibration);

	} // end onCreate method.

	// Let's see what happens when a check box is clicked in audio settings:
	public void onCheckboxClicked(View view) {
		// Is the view now checked?
		boolean checked = ((CheckBox) view).isChecked();

		Settings set = new Settings(this); // to save changes.

		// Check which check box was clicked
		switch (view.getId()) {
		case R.id.cbtKeepScreenAwake:
			if (checked) {
				MainActivity.isWakeLock = true;
			} else {
				MainActivity.isWakeLock = false;
			}
			set.saveBooleanSettings("isWakeLock", MainActivity.isWakeLock);
			break;
		case R.id.cbtIsVibration:
			if (checked) {
				MainActivity.isVibration = true;
			} else {
				MainActivity.isVibration = false;
			}
			set.saveBooleanSettings("isVibration", MainActivity.isVibration);
			break;

		} // end switch.
	} // end method for OnChecked settings.

} // end Miscellaneous settings class.
