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
    private Byte[] aBackgroundVolumes = new Byte[]{0, 15, 30, 50, 75, 100};
    private Byte[] aMusicVolumes = new Byte[]{0, 10, 20, 40, 70, 100};

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
        boolean checked = ((CheckBox) view).isChecked();

        Settings set = new Settings(this); // to save changes.

        int viewId = view.getId();

        if (viewId == R.id.cbtSoundsSetting) {
            MainActivity.isSound = checked;
            set.saveBooleanSettings("isSound", MainActivity.isSound);

        } else if (viewId == R.id.cbtSoundsLoopedSetting) {
            MainActivity.isSoundLooped = checked;
            set.saveBooleanSettings("isSoundLooped", MainActivity.isSoundLooped);

        } else if (viewId == R.id.cbtSoundsMusicSetting) {
            MainActivity.isSoundMusic = checked;
            set.saveBooleanSettings("isSoundMusic", MainActivity.isSoundMusic);

        } else if (viewId == R.id.cbtDiceSoundSetting) {
            MainActivity.isSoundDice = checked;
            set.saveBooleanSettings("isSoundDice", MainActivity.isSoundDice);

        } else if (viewId == R.id.cbtSpeechSetting) {
            MainActivity.isSpeech = checked;
            set.saveBooleanSettings("isSpeech", MainActivity.isSpeech);
        }
    } // end save settings after clicked a check box.

    // Now for background volume percentage choosing method, radio buttons:
    public void onRadioBButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        int viewId = view.getId();

        if (viewId == R.id.rbRadioB1 && checked) {
            MainActivity.soundBackgroundPercentage = aBackgroundVolumes[1];
        } else if (viewId == R.id.rbRadioB2 && checked) {
            MainActivity.soundBackgroundPercentage = aBackgroundVolumes[2];
        } else if (viewId == R.id.rbRadioB3 && checked) {
            MainActivity.soundBackgroundPercentage = aBackgroundVolumes[3];
        } else if (viewId == R.id.rbRadioB4 && checked) {
            MainActivity.soundBackgroundPercentage = aBackgroundVolumes[4];
        } else if (viewId == R.id.rbRadioB5 && checked) {
            MainActivity.soundBackgroundPercentage = aBackgroundVolumes[5];
        }

        // Save the setting:
        Settings set = new Settings(getApplicationContext());
        set.saveIntSettings("soundBackgroundPercentage", MainActivity.soundBackgroundPercentage);
    } // end onRadioButtonClicked.

    // Now for music volume percentage choosing method, radio buttons:
    public void onRadioMButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        int viewId = view.getId();

        if (viewId == R.id.rbRadioM1 && checked) {
            MainActivity.soundMusicPercentage = aMusicVolumes[1];
        } else if (viewId == R.id.rbRadioM2 && checked) {
            MainActivity.soundMusicPercentage = aMusicVolumes[2];
        } else if (viewId == R.id.rbRadioM3 && checked) {
            MainActivity.soundMusicPercentage = aMusicVolumes[3];
        } else if (viewId == R.id.rbRadioM4 && checked) {
            MainActivity.soundMusicPercentage = aMusicVolumes[4];
        } else if (viewId == R.id.rbRadioM5 && checked) {
            MainActivity.soundMusicPercentage = aMusicVolumes[5];
        }

        // Save the setting:
        Settings set = new Settings(getApplicationContext());
        set.saveIntSettings("soundMusicPercentage", MainActivity.soundMusicPercentage);
    } // end onRadioButtonClicked.

} // end audio settings activity.
