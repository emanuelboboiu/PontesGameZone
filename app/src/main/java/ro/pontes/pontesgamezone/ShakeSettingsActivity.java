package ro.pontes.pontesgamezone;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

public class ShakeSettingsActivity extends Activity {

    public static Float[] aMagnitudes = new Float[]{0.0F, 1.5F, 1.9F, 2.2F,
            2.6F, 3.0F};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake_settings);

        // For shake detector:
        CheckBox cbtOnshakeToggle = findViewById(R.id.cbtOnshakeToggle);
        cbtOnshakeToggle.setChecked(MainActivity.isShake);

        // Check the radio button depending of magnitude chosen:
        // Search in aMagnitudes which position is the current value:

        int whichRadio;
        for (whichRadio = 0; whichRadio < aMagnitudes.length; whichRadio++) {
            if (MainActivity.onshakeMagnitude == aMagnitudes[whichRadio]) {
                break;
            }
        } // end for search in aMagnitudes.
        // Just a check:

        int resID = getResources().getIdentifier("rbRadio" + whichRadio, "id",
                getPackageName());
        RadioButton radioButton = findViewById(resID);
        radioButton.setChecked(true);

    } // end onCreate method.

    // Let's see what happens when a check box is clicked in on shake settings:
    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        Settings set = new Settings(getApplicationContext());

        int viewId = view.getId();

        if (viewId == R.id.cbtOnshakeToggle) {
            MainActivity.isShake = checked;
            set.saveBooleanSettings("isShake", MainActivity.isShake);
        }
    } // end onCheckboxClicked() method.

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        int viewId = view.getId();

        if (viewId == R.id.rbRadio1 && checked) {
            MainActivity.onshakeMagnitude = aMagnitudes[1];
        } else if (viewId == R.id.rbRadio2 && checked) {
            MainActivity.onshakeMagnitude = aMagnitudes[2];
        } else if (viewId == R.id.rbRadio3 && checked) {
            MainActivity.onshakeMagnitude = aMagnitudes[3];
        } else if (viewId == R.id.rbRadio4 && checked) {
            MainActivity.onshakeMagnitude = aMagnitudes[4];
        } else if (viewId == R.id.rbRadio5 && checked) {
            MainActivity.onshakeMagnitude = aMagnitudes[5];
        }

        // Save the setting
        Settings set = new Settings(getApplicationContext());
        set.saveFloatSettings("onshakeMagnitude", MainActivity.onshakeMagnitude);
    } // end onRadioButtonClicked.

} // end on shake settings class.
