package ro.pontes.pontesgamezone;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

public class ShakeSettingsActivity extends Activity {

	public static Float[] aMagnitudes = new Float[] { 0.0F, 1.5F, 1.9F, 2.2F,
			2.6F, 3.0F };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shake_settings);

		// For shake detector:
		CheckBox cbtOnshakeToggle = (CheckBox) findViewById(R.id.cbtOnshakeToggle);
		cbtOnshakeToggle.setChecked(MainActivity.isShake);

		// Check the radio button depending of magnitude chosen:
		// Search in aMagnitudes which position is the current value:

		int whichRadio = 0;
		for (whichRadio = 0; whichRadio < aMagnitudes.length; whichRadio++) {
			if (MainActivity.onshakeMagnitude == aMagnitudes[whichRadio]) {
				break;
			}
		} // end for search in aMagnitudes.
			// Just a check:
		if (whichRadio < 1 && whichRadio > 5) {
			whichRadio = 3; // the default value, moderate.
		}

		int resID = getResources().getIdentifier("rbRadio" + whichRadio, "id",
				getPackageName());
		RadioButton radioButton = (RadioButton) findViewById(resID);
		radioButton.setChecked(true);

	} // end onCreate method.

	// Let's see what happens when a check box is clicked in on shake settings:
	public void onCheckboxClicked(View view) {
		// Is the view now checked?
		boolean checked = ((CheckBox) view).isChecked();

		Settings set = new Settings(getApplicationContext()); // to save
																// changes.

		// Check which check box was clicked
		switch (view.getId()) {
		case R.id.cbtOnshakeToggle:
			if (checked) {
				MainActivity.isShake = true;
			} else {
				MainActivity.isShake = false;
			}
			set.saveBooleanSettings("isShake", MainActivity.isShake);
			break;
		} // end switch.
	} // end the function called when the check box was clicked.

	// Now for magnitude choosing method, radio buttons:
	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked:
		switch (view.getId()) {
		case R.id.rbRadio1:
			if (checked) {
				MainActivity.onshakeMagnitude = aMagnitudes[1];
			}
			break;
		case R.id.rbRadio2:
			if (checked) {
				MainActivity.onshakeMagnitude = aMagnitudes[2];
			}
			break;
		case R.id.rbRadio3:
			if (checked) {
				MainActivity.onshakeMagnitude = aMagnitudes[3];
			}
			break;
		case R.id.rbRadio4:
			if (checked) {
				MainActivity.onshakeMagnitude = aMagnitudes[4];
			}
			break;
		case R.id.rbRadio5:
			if (checked) {
				MainActivity.onshakeMagnitude = aMagnitudes[5];
			}
			break;
		} // } // end switch.

		// Save now the setting:
		Settings set = new Settings(getApplicationContext()); // we need it for
																// saving with
																// SharedPreferences.
		set.saveFloatSettings("onshakeMagnitude", MainActivity.onshakeMagnitude);
	} // end onRadioButtonClicked.

} // end on shake settings class.
