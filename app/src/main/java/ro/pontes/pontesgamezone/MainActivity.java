package ro.pontes.pontesgamezone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity implements OnItemSelectedListener {

    // Various variables and constants:
    public final static String EXTRA_MESSAGE = "ro.pontes.pontesgamezone.MESSAGE";

    // Settings or global program variables:
    public static String userName = null; // the name of the google account.
    public static String nickname = null; // the nickname used in this game,
    // changeable from main settings or
    // in main interface.
    public static String curNickname = null; // the nickname used in this game,
    // it can be simple visitor if a
    // nickname is not filled in
    // main interface or menu.
    public static final int minLengthOfNickname = 2;
    public static String currentLanguage = "en";
    public static boolean isSpeech = true;
    public static boolean isSound = true;
    public static boolean isSoundLooped = true;
    public static boolean isSoundMusic = true;
    public static int soundBackgroundPercentage = 30;
    public static int soundMusicPercentage = 10;
    public static boolean isSoundDice = true;
    public static boolean isWarSounds = false; // for cards war game, if there
    // are war sounds or simple deck
    // sounds.
    public static int scopaTargetScore = 11;
    public static int diceSortMethod = 2; // default is descendant.
    public static boolean isShake = true;
    public static float onshakeMagnitude = 2.2F; // the value for moderate, see
    // ShakeSettingsActivity.
    public static boolean isWakeLock = true;
    public static boolean isVibration = true;
    // end settings variables.

    // For background sound:
    SoundPlayer sndBackground;

    Spinner dropdown; // to have it globally, to select first item in
    // onResume() method.

    /*
     * We need a global alertToShow as alert to be able to dismiss it when
     * needed and other things:
     */
    private AlertDialog alertToShow;

    // Other global variables:
    Context c;

    // We need the context as this class in some listeners as final variable:
    final Context mFinalContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Determine the context to be used in some places:
        c = getApplicationContext();

        Settings set = new Settings(this);
        set.chargeSettings();

        userName = getString(R.string.visitor);

        setContentView(R.layout.activity_main);

        /*
         * We moved the call to setPersonalizedContent method in onResume
         * method, because sometimes we come here with back button from bar area
         * or other dependences. It is possible this way to have the wallet
         * content changed because we drank at bar area, or we did some actions
         * in pawn shop.
         */

        // A stringBuilder with all the games titles:
        StringBuilder sb = new StringBuilder();
        // An arrays with all R.ids of title games:
        int[] gamesIds = new int[]{R.string.ls_choose_item, R.string.black_jack_game, R.string.poker_game, R.string.cards_war_game, R.string.who_is_greater_game, R.string.cf_game_name, R.string.sm_game_name, R.string.ls_game_name};

        // Create the StringBuilder declared above:
        for (int i = 0; i < gamesIds.length; i++) {
            sb.append(getString(gamesIds[i]));
            if (i < gamesIds.length - 1) {
                sb.append("|");
            } // end append vertical bar.
        } // end for create the StringBuilder.

        dropdown = findViewById(R.id.spinnerChoose);
        String[] items = sb.toString().split("\\|");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);

        // Set last visit text view:
        setLastVisit();

        // Save last visit as seconds since Unix age:
        Calendar cal = Calendar.getInstance();
        int curTime = (int) (cal.getTimeInMillis() / 1000);
        set.saveIntSettings("lastVisit", curTime);
        // GUITools.alert(this, "", ""+curTime);

        // To keep screen awake:
        if (MainActivity.isWakeLock) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } // end wake lock.

        getCredentials();
    } // end onCreate method.

    // The method to get different information about user:
    private void getCredentials() {
        // userName = getMyAccountName();
        userName = getString(R.string.visitor);
    } // end getCredentials() method.

    // Methods for implement of this class:
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        // String chosen = parent.getItemAtPosition(position).toString();
        startAGame(position);
    } // end implemented method for chosen item.

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub

    } // end onNothingSelected() method.
    // end implements methods.

    @Override
    public void onResume() {
        super.onResume();

        // Change some values on the screen, mainly TextViews:
        setPersonalizedContent();

        sndBackground = new SoundPlayer();
        sndBackground.playLooped(this, "main_background1");

        dropdown.setSelection(0);

    } // end onResume method.

    @Override
    public void onPause() {
        // Add here what you want to happens on pause:

        sndBackground.stopLooped();

        super.onPause();
    } // end onPause method.

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present:
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.mnuChangeNickname) {
            changeNicknameAction();
        } // end if is chosen change nickname in More Options.
        else if (id == R.id.mnuSaloon) {
            GUITools.goToSaloon(this);
        } // end got o saloon option.
        else if (id == R.id.mnuPawnshop) {
            GUITools.goToPawnshop(this);
        } // end go to pawn-shop option.
        else if (id == R.id.mnuBathroom) {
            GUITools.goToBathroom(this);
        } // end go to bathroom option.
        else if (id == R.id.mnuAudioSettings) {
            goToAudioSettings();
        } // end if is Audio Settings clicked in menu.
        else if (id == R.id.mnuLanguageSettings) {
            goToLanguageSettings();
        } // end if is language settings.
        else if (id == R.id.mnuOnshakeSettings) {
            goToOnshakeSettings();
        } // end if on shake settings was chose in main menu.
        else if (id == R.id.mnuMiscellaneousSettings) {
            goToMiscellaneousSettings();
        } // end if miscellaneous settings was chosen in main menu of main
        // activity windows.
        else if (id == R.id.mnuVirtualTime) {
            GUITools.showVirtualTime(this);
        } // end show virtual time.
        else if (id == R.id.mnuDefaultSettings) {
            // Get the strings:
            String tempTitle = getString(R.string.title_default_settings);
            String tempBody = String.format(getString(R.string.body_default_settings), curNickname);
            new AlertDialog.Builder(this).setTitle(tempTitle).setMessage(tempBody).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                Settings set = new Settings(c);
                set.setDefaultSettings();
                set.chargeSettings();
                setPersonalizedContent();
                Statistics.postStats("19", 1); // 19 is the
                // id of the
                // game
                // reset in
                // DB.

            }).setNegativeButton(android.R.string.no, null).show();
        } // end if is for set to defaults clicked in main menu.
        else if (id == R.id.mnuAboutDialog) {
            GUITools.aboutDialog(this);
        } // end about dialog option in menu.
        else if (id == R.id.mnuHelp) {
            GUITools.openHelp(this);
        } // end help option in menu.

        return super.onOptionsItemSelected(item);
    }

    // Methods to open an activity from menu:

    /**
     * Called when the user clicks the audio settings option in menu:
     */
    public void goToAudioSettings() {
        Intent intent = new Intent(this, AudioSettingsActivity.class);
        String message;
        message = "Pontes GameZone"; // without a reason, just to be something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end function which performs when the option in menu is clicked.

    /**
     * Called when the user clicks the language settings option in menu:
     */
    public void goToLanguageSettings() {
        Intent intent = new Intent(this, LanguageSettingsActivity.class);
        String message;
        message = "Pontes GameZone"; // without a reason, just to be something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end function which performs when the option Language Settings in menu
    // is clicked.

    // When on shake settings was chosen:
    public void goToOnshakeSettings() {
        Intent intent = new Intent(this, ShakeSettingsActivity.class);
        String message;
        message = "Pontes GameZone"; // without a reason, just to be something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end method to open on shake settings..

    // A method to open miscellaneous settings:
    public void goToMiscellaneousSettings() {
        Intent intent = new Intent(this, MiscellaneousSettingsActivity.class);
        String message;
        message = "Pontes GameZone"; // without a reason, just to be something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end method to open miscellaneous settings..

    // End methods to open activities from menu.

    // a method to update some labels and what we need at the start of the game,
    // MainActivity GUI:
    public void setPersonalizedContent() {

        String str; // this is used below for some text views.
        TextView tv; // also use below for text view.

        // Set the GMail name on the welcome TextView:
        /*
         * Deleted in 1.2 beta, it is not important yet. tv =
         * (TextView)findViewById(R.id.tvRegisteredMessage); str =
         * getString(R.string.registered_message); tv.setText(String.format(str,
         * userName));
         */

        // About nickname:
        // If there is no a nickname set yet:
        if (nickname == null) {
            str = getString(R.string.no_nickname);
            curNickname = getString(R.string.nickname_visitor);
        } else {
            // This is when there is a nickname:
            str = getString(R.string.nickname);
            str = String.format(str, nickname);
            curNickname = nickname;
        }
        tv = findViewById(R.id.tvNickname);
        tv.setText(str);
        // End nickname.

        // Set the wallet contains text view:
        tv = findViewById(R.id.tvWalletContains);
        tv.setText(String.format(getString(R.string.tv_wallet_contains), "" + Dealer.myTotalMoney));
    } // end setPersonalizedContent() method.

    public void changeNickname(View view) {
        changeNicknameAction();
    } // end change nickname method.

    public void changeNicknameAction() {
        // A string to get from resource the texts:
        String tempMessage;
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        // The title:
        tempMessage = getString(R.string.change_nickname_title);
        alert.setTitle(tempMessage);

        // The body:
        tempMessage = getString(R.string.change_nickname_message);
        alert.setMessage(tempMessage);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        tempMessage = getString(R.string.change_nickname_hint);
        input.setHint(tempMessage);
        // Add also an action listener:
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Next two line are also at the done button pressing:
                String newNickname = input.getText().toString();
                changeNicknameFinishing(newNickname);
                alertToShow.dismiss();
            }
            return false;
        });
        // End add action listener for the IME done button of the keyboard..

        alert.setView(input);

        // end if OK was pressed.
        alert.setPositiveButton("Ok", (dialog, whichButton) -> {
            // Next two line are also at the done button pressing:
            String newNickname = input.getText().toString();
            changeNicknameFinishing(newNickname);
        });

        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            // cancelled.
        });

        alertToShow = alert.create();
        alertToShow.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertToShow.show();
        // end of alert dialog with edit sequence.
    } // end change nickname action method.

    // A method to check and finish the nickname changing:
    public void changeNicknameFinishing(String newNickname) {
        // Check if the nickname is longer than one character:
        if (newNickname.length() < minLengthOfNickname) {
            GUITools.alert(mFinalContext, getString(R.string.error), getString(R.string.change_nickname_error_short));
        } else if (!GUITools.isAlphanumeric(newNickname)) {
            GUITools.alert(mFinalContext, getString(R.string.error), getString(R.string.change_nickname_error_non_alphanumeric));
        } else {
            // A good nickname was written:
            nickname = newNickname;
            Settings set = new Settings(mFinalContext);
            set.saveStringSettings("nickname", nickname);
            setPersonalizedContent();
            GUITools.alert(mFinalContext, getString(R.string.change_nickname_title), String.format(getString(R.string.change_nickname_success), nickname));
            SoundPlayer.playSimple(this, "miscellaneous_action");
        }
    } // end check and change the nickname method.

    // Start Blackjack activity:
    public void startBlackJackGame() {
        Intent intent = new Intent(this, BlackJackActivity.class);
        String message;
        message = "Pontes GameZone"; // without a reason, just to be something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end startBlackJackGameActivity.

    // Start Poker activity:
    public void startPokerGame() {
        Intent intent = new Intent(this, PokerActivity.class);
        String message;
        message = "Pontes GameZone"; // without a reason, just to be something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end startBlackJackGameActivity.

    // Start Cards War activity:
    public void startCardsWarGame() {
        Intent intent = new Intent(this, CardsWarActivity.class);
        String message;
        message = "Pontes GameZone"; // without a reason, just to be something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end startCardsWarGameActivity.

    // Start WhoIsGreaterActivity:
    public void startWhoIsGreaterGame() {
        Intent intent = new Intent(this, WhoIsGreaterActivity.class);
        String message;
        message = "Pontes GameZone"; // without a reason, just to be something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end startWhoIsGreaterGameActivity.

    // Start ConnectFourActivity:
    public void startConnectFourGame() {
        Intent intent = new Intent(this, ConnectFourActivity.class);
        String message;
        message = "Pontes GameZone"; // without a reason, just to be something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end startConnectFourGameActivity.

    // Start SlotMachineActivity:
    public void startSlotMachineGame() {
        Intent intent = new Intent(this, SlotMachineActivity.class);
        String message;
        message = "Pontes GameZone"; // without a reason, just to be something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end startSlotMachineGameActivity.

    // Start ScopaActivity:
    public void startScopaGame() {
        Intent intent = new Intent(this, ScopaActivity.class);
        String message;
        message = "Pontes GameZone"; // without a reason, just to be something
        // sent by the intent.
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    } // end startScopaGameActivity.

    // A method to open a game depending of the item chosen in spinner:
    public void startAGame(int chosen) {
        switch (chosen) {
            case 0:
                // Do nothing:
                break;
            case 1:
                startBlackJackGame();
                break;
            case 2:
                startPokerGame();
                break;
            case 3:
                startCardsWarGame();
                break;
            case 4:
                startWhoIsGreaterGame();
                break;
            case 5:
                startConnectFourGame();
                break;
            case 6:
                startSlotMachineGame();
                break;
            case 7:
                startScopaGame();
                break;
        } // end switch.
    } // end openAGame() method.

    private void setLastVisit() {
        Settings set = new Settings(this);
        String message;
        long curTime = (long) set.getIntSettings("lastVisit") * 1000;

        if (curTime > 0) {

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(curTime);

            // Now format the string:
            // See if it is today or yesterday:
            int today = getIsToday(curTime);
            String dayOfWeek;
            if (today == 1) {
                dayOfWeek = getString(R.string.today);
            } else if (today == 2) {
                dayOfWeek = getString(R.string.yesterday);
            } else {
                dayOfWeek = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            }

            // Make the hour and minute with 0 in front if they are less
            // than 10:
            String curHour;
            int iHour = cal.get(Calendar.HOUR_OF_DAY);
            if (iHour < 10) {
                curHour = "0" + iHour;
            } else {
                curHour = "" + iHour;
            }
            String curMinute;
            int iMinute = cal.get(Calendar.MINUTE);
            if (iMinute < 10) {
                curMinute = "0" + iMinute;
            } else {
                curMinute = "" + iMinute;
            }

            message = String.format(getString(R.string.tv_last_visit), dayOfWeek, "" + cal.get(Calendar.DAY_OF_MONTH), "" + cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()), "" + cal.get(Calendar.YEAR), curHour, curMinute);
        } else {
            message = getString(R.string.tv_last_visit_not_known);
        } // end if there is no a last visit saved.

        // Get the last visit text view and fill it:
        TextView tv = findViewById(R.id.tvLastVisit);
        tv.setText(message);
    } // end setLastVisit() method.

    /*
     * This method returns 1 if a date in milliseconds at parameter is today, 2
     * if it was yesterday or 0 on another date.
     */
    private int getIsToday(long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis);

        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return 1; // today.
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return 2; // yesterday.
        } else if (smsTime.get(Calendar.DATE) - now.get(Calendar.DATE) == 1) {
            return 3; // tomorrow.
        } else {
            return 0; // another date.
        }
    } // end determine if a date is today or yesterday.

} // end MainActivityClass.

