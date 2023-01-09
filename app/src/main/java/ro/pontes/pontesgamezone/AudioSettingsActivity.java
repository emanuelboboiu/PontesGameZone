package ro.pontes.pontesgamezone;

/*
 * Class started 22 September 2014, 01:00 by Manu.
 */

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

public class AudioSettingsActivity extends Activity {

	// Two arrays for background and music volumes:
	private Byte[] aBackgroundVolumes = new Byte[] { 0, 15, 30, 50, 75, 100 };
	private Byte[] aMusicVolumes = new Byte[] { 0, 10, 20, 40, 70, 100 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_audio_settings);

		// Check or check the check boxes, depending of current boolean values:

		// For sounds in game:
		CheckBox cbtSoundsSetting = (CheckBox) findViewById(R.id.cbtSoundsSetting);
		cbtSoundsSetting.setChecked(MainActivity.isSound);

		// For background sounds in game:
		CheckBox cbtSoundsBackgroundSetting = (CheckBox) findViewById(R.id.cbtSoundsLoopedSetting);
		cbtSoundsBackgroundSetting.setChecked(MainActivity.isSoundLooped);

		// For background music in games:
		CheckBox cbtSoundsMusicSetting = (CheckBox) findViewById(R.id.cbtSoundsMusicSetting);
		cbtSoundsMusicSetting.setChecked(MainActivity.isSoundMusic);

		// For dice sound, announce as number by voice:
		CheckBox cbtSoundDiceSetting = (CheckBox) findViewById(R.id.cbtDiceSoundSetting);
		cbtSoundDiceSetting.setChecked(MainActivity.isSoundDice);

		// For speech settings:
		CheckBox cbtSpeechSetting = (CheckBox) findViewById(R.id.cbtSpeechSetting);
		cbtSpeechSetting.setChecked(MainActivity.isSpeech);

		// For radio buttons, background and music volume percentage:

		// Check the radio button of the background volume percentage depending
		// of percentage chosen:
		// Search in aBackgroundVolumes which position is the current value:

		int whichRadio = 0;
		for (whichRadio = 0; whichRadio < aBackgroundVolumes.length; whichRadio++) {
			if (MainActivity.soundBackgroundPercentage == aBackgroundVolumes[whichRadio]) {
				break;
			}
		} // end for search in aBackgroundVolumes.
			// Just a check:
		if (whichRadio < 1 && whichRadio > 5) {
			whichRadio = 2; // the default value, 30%.
		}
		int resID = getResources().getIdentifier("rbRadioB" + whichRadio, "id",
				getPackageName());
		RadioButton radioButton = (RadioButton) findViewById(resID);
		radioButton.setChecked(true);

		// Check the radio button of the music volume percentage depending of
		// percentage chosen:
		// Search in aMusicVolumes which position is the current value:

		whichRadio = 0;
		for (whichRadio = 0; whichRadio < aMusicVolumes.length; whichRadio++) {
			if (MainActivity.soundMusicPercentage == aMusicVolumes[whichRadio]) {
				break;
			}
		} // end for search in aMusicVolumes.
			// Just a check:
		if (whichRadio < 1 && whichRadio > 5) {
			whichRadio = 2; // the default value, 20%.
		}
		resID = getResources().getIdentifier("rbRadioM" + whichRadio, "id",
				getPackageName());
		radioButton = (RadioButton) findViewById(resID);
		radioButton.setChecked(true);

		// end check the corresponding radio buttons.

	} // end onCreate settings activity.

	// Let's see what happens when a check box is clicked in audio settings:
	public void onCheckboxClicked(View view) {
		// Is the view now checked?
		boolean checked = ((CheckBox) view).isChecked();

		Settings set = new Settings(this); // to save changes.

		// Check which check box was clicked
		switch (view.getId()) {
		case R.id.cbtSoundsSetting:
			if (checked) {
				MainActivity.isSound = true;
			} else {
				MainActivity.isSound = false;
			}
			set.saveBooleanSettings("isSound", MainActivity.isSound);
			break;
		case R.id.cbtSoundsLoopedSetting:
			if (checked) {
				MainActivity.isSoundLooped = true;
			} else {
				MainActivity.isSoundLooped = false;
			}
			set.saveBooleanSettings("isSoundLooped", MainActivity.isSoundLooped);
			break;
		case R.id.cbtSoundsMusicSetting:
			if (checked) {
				MainActivity.isSoundMusic = true;
			} else {
				MainActivity.isSoundMusic = false;
			}
			set.saveBooleanSettings("isSoundMusic", MainActivity.isSoundMusic);
			break;
		case R.id.cbtDiceSoundSetting:
			if (checked) {
				MainActivity.isSoundDice = true;
			} else {
				MainActivity.isSoundDice = false;
			}
			set.saveBooleanSettings("isSoundDice", MainActivity.isSoundDice);
			break;
		case R.id.cbtSpeechSetting:
			if (checked) {
				MainActivity.isSpeech = true;
			} else {
				MainActivity.isSpeech = false;
			}
			set.saveBooleanSettings("isSpeech", MainActivity.isSpeech);
			break;
		} // end switch.
	} // end save settings after clicked a check box.

	// Now for background volume percentage choosing method, radio buttons:
	public void onRadioBButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked:
		switch (view.getId()) {
		case R.id.rbRadioB1:
			if (checked) {
				MainActivity.soundBackgroundPercentage = aBackgroundVolumes[1];
			}
			break;
		case R.id.rbRadioB2:
			if (checked) {
				MainActivity.soundBackgroundPercentage = aBackgroundVolumes[2];
			}
			break;
		case R.id.rbRadioB3:
			if (checked) {
				MainActivity.soundBackgroundPercentage = aBackgroundVolumes[3];
			}
			break;
		case R.id.rbRadioB4:
			if (checked) {
				MainActivity.soundBackgroundPercentage = aBackgroundVolumes[4];
			}
			break;
		case R.id.rbRadioB5:
			if (checked) {
				MainActivity.soundBackgroundPercentage = aBackgroundVolumes[5];
			}
			break;
		} // } // end switch.

		// Save now the setting:
		Settings set = new Settings(getApplicationContext()); // we need it for
																// saving with
																// SharedPreferences.
		set.saveIntSettings("soundBackgroundPercentage",
				MainActivity.soundBackgroundPercentage);
	} // end onRadioButtonClicked.

	// Now for music volume percentage choosing method, radio buttons:
	public void onRadioMButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked:
		switch (view.getId()) {
		case R.id.rbRadioM1:
			if (checked) {
				MainActivity.soundMusicPercentage = aMusicVolumes[1];
			}
			break;
		case R.id.rbRadioM2:
			if (checked) {
				MainActivity.soundMusicPercentage = aMusicVolumes[2];
			}
			break;
		case R.id.rbRadioM3:
			if (checked) {
				MainActivity.soundMusicPercentage = aMusicVolumes[3];
			}
			break;
		case R.id.rbRadioM4:
			if (checked) {
				MainActivity.soundMusicPercentage = aMusicVolumes[4];
			}
			break;
		case R.id.rbRadioM5:
			if (checked) {
				MainActivity.soundMusicPercentage = aMusicVolumes[5];
			}
			break;
		} // } // end switch.

		// Save now the setting:
		Settings set = new Settings(getApplicationContext()); // we need it for
																// saving with
																// SharedPreferences.
		set.saveIntSettings("soundMusicPercentage",
				MainActivity.soundMusicPercentage);
	} // end onRadioButtonClicked.

} // end audio settings activity.
