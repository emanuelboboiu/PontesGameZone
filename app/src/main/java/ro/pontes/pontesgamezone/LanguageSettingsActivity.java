package ro.pontes.pontesgamezone;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

public class LanguageSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_settings);

        // Check the radio button depending of language for dice voice chosen:
        String rb = "rbRadio_" + MainActivity.currentLanguage;
        int resID = getResources().getIdentifier(rb, "id", getPackageName());
        RadioButton radioButton = findViewById(resID);
        radioButton.setChecked(true);

    } // end on create method.

    // See and save the radio button clicked:
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked:
        switch (view.getId()) {
            case R.id.rbRadio_en:
                if (checked) {
                    MainActivity.currentLanguage = "en";
                }
                break;
            case R.id.rbRadio_it:
                if (checked) {
                    MainActivity.currentLanguage = "it";
                }
                break;
            case R.id.rbRadio_ro:
                if (checked) {
                    MainActivity.currentLanguage = "ro";
                }
                break;
        } // } // end switch.

        // Save now the setting:
        Settings set = new Settings(getApplicationContext());
        set.saveStringSettings("currentLanguage", MainActivity.currentLanguage);
    } // end onRadioButtonClicked.

}// end class Language settings activity.
