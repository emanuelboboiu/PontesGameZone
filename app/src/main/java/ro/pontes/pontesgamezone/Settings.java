package ro.pontes.pontesgamezone;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * Class started on Tuesday, 19 August 2014, created by Manu.
 * This class contains useful methods like save or get settings.
 * */

public class Settings {

    // The file name for save and load preferences:
    private final static String PREFS_NAME = "pgzSettings";

    private final Context context;

    public Settings(Context context) {
        this.context = context;
    }

    // A method to detect if a preference exist or not:
    public boolean isPreference(String key) {
        boolean value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.contains(key);
        return value;
    } // end detect if a preference exists or not.

    // Methods for save and read preferences with SharedPreferences:

    // Save a boolean value:
    public void saveBooleanSettings(String key, boolean value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        // Commit the edits!
        editor.apply();
    } // end save boolean.

    // Read boolean preference:
    public boolean getBooleanSettings(String key) {
        boolean value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getBoolean(key, false);

        return value;
    } // end get boolean preference from SharedPreference.

    // Save a integer value:
    public void saveIntSettings(String key, int value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        // Commit the edits!
        editor.apply();
    } // end save integer.

    // Read integer preference:
    public int getIntSettings(String key) {
        int value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getInt(key, 0);

        return value;
    } // end get integer preference from SharedPreference.

    // For float values in shared preferences:
    public void saveFloatSettings(String key, float value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        // Commit the edits!
        editor.apply();
    } // end save integer.

    // Read integer preference:
    public float getFloatSettings(String key) {
        float value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getFloat(key, 2.2F); // a default value like the value
        // for moderate magnitude.

        return value;
    } // end get float preference from SharedPreference.

    // Save a String value:
    public void saveStringSettings(String key, String value) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        // Commit the edits!
        editor.apply();
    } // end save String.

    // Read String preference:
    public String getStringSettings(String key) {
        String value;
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        value = settings.getString(key, null);

        return value;
    } // end get String preference from SharedPreference.
    // End read and write settings in SharedPreferences.

    // Charge Settings function:
    public void chargeSettings() {

        // Determine if is first launch of the program:
        boolean isNotFirstRunning = getBooleanSettings("isFirstRunning");

        if (!isNotFirstRunning) {
            saveBooleanSettings("isFirstRunning", true);
            // Make default values in SharedPrefferences:
            setDefaultSettings();
        }

        // Now charge settings:

        // Charge nickname:
        MainActivity.nickname = getStringSettings("nickname");

        // Play or not the sounds and speech:
        MainActivity.isSpeech = getBooleanSettings("isSpeech");
        MainActivity.isSound = getBooleanSettings("isSound");
        MainActivity.isSoundDice = getBooleanSettings("isSoundDice");
        MainActivity.isWarSounds = getBooleanSettings("isWarSounds");

        // For background sounds:
        // Only if the preference exists, this is because is a new setting:
        if (isPreference("isSoundLooped")) {
            MainActivity.isSoundLooped = getBooleanSettings("isSoundLooped");
        } // end if preference isSoundLooped exists.
        int tempPercentage = getIntSettings("soundBackgroundPercentage");
        if (tempPercentage > 0) {
            MainActivity.soundBackgroundPercentage = tempPercentage;
        } // end if there is a percentage saved for background volume.

        // For music ambiance:
        // Only if the preference exists, this is because is a new setting:
        if (isPreference("isSoundMusic")) {
            MainActivity.isSoundMusic = getBooleanSettings("isSoundMusic");
        } // end if preference isSoundMusic exists.
        tempPercentage = getIntSettings("soundMusicPercentage");
        if (tempPercentage > 0) {
            MainActivity.soundMusicPercentage = tempPercentage;
        } // end if there is a percentage saved for music volume.

        // Is shake detector or not:
        MainActivity.isShake = getBooleanSettings("isShake");
        // The magnitude of the shake detector:
        MainActivity.onshakeMagnitude = getFloatSettings("onshakeMagnitude");

        // Wake lock, keep screen awake:
        MainActivity.isWakeLock = getBooleanSettings("isWakeLock");

        // Vibrations:
        if (isPreference("isVibration")) {
            MainActivity.isVibration = getBooleanSettings("isVibration");
        } // end if isVibration setting exists.

        // Only if the preference for Scopa target score exists, this is because
        // is a new setting:
        if (isPreference("scopaTargetScore")) {
            MainActivity.scopaTargetScore = getIntSettings("scopaTargetScore");
        } // end if preference scopaTargetScore exists.

        // Only if the preference for Scopa variant exists, this is because
        // is a new setting:
        if (isPreference("lsVariant")) {
            ScopaActivity.lsVariant = getIntSettings("lsVariant");
        } // end if preference lsVariant exists.

        // The sort method for dice:
        MainActivity.diceSortMethod = getIntSettings("diceSortMethod");

        // Get the language for currentLanguage key in SharedPrefferences:
        String tempCurLocale = getStringSettings("currentLanguage");
        if (tempCurLocale.equals("en") || tempCurLocale.equals("it") || tempCurLocale.equals("ro") || tempCurLocale.equals("tr")) {
            MainActivity.currentLanguage = tempCurLocale;
        } else {
            // If is another language that one saved in settings, or another one which also doesn't exists, English is the default:
            MainActivity.currentLanguage = "en";
        }

        // Charge the total money:
        Dealer.myTotalMoney = getIntSettings("myTotalMoney");

        // Charge the money spent at bar:
        SaloonActivity.spentMoney = getIntSettings("spentMoney");

        // Charge the number of games for order:
        SaloonActivity.numberOfGamesForOrder = getIntSettings("numberOfGamesForOrder");

        // Charge the actual bonus percentage:
        SaloonActivity.actualBonusPercentage = getIntSettings("actualBonusPercentage");

        // Charge the money won as bonus since beginnings:
        Dealer.moneyWonAsBonus = getIntSettings("moneyWonAsBonus");

        // Charge the money won as bonus in last 24 hours:
        Dealer.moneyWonAsBonus24 = getIntSettings("moneyWonAsBonus24");

        // Now, if the day is to end or last start of the day doesn't exists,
        // set to 0:
        // Get the last start of the day:
        int dayStart = getIntSettings("currentTimeMinute");
        int dayPassed = GUITools.getCurrentTimeMinute() - dayStart;
        int minutesInADay = 60 * 24;
        if (dayPassed > minutesInADay || dayStart == 0) {
            // 24 hours has passed:
            SaloonActivity.spentMoney = 0;
            saveIntSettings("spentMoney", 0);
            Dealer.moneyWonAsBonus24 = 0;
            saveIntSettings("moneyWonAsBonus24", 0);
            SaloonActivity.actualBonusPercentage = 0;
            saveIntSettings("actualBonusPercentage", 0);
            saveIntSettings("currentTimeMinute", GUITools.getCurrentTimeMinute());
        } // end if for number of minutes since last start of the day.

        // For level in ConnectFour game:
        ConnectFourActivity.cfLevel = getIntSettings("cfLevel");
        if (ConnectFourActivity.cfLevel < 2) {
            ConnectFourActivity.cfLevel = ConnectFourActivity.CF_DEFAULT_LEVEL; // as
            // a
            // default
            // value..
        }
        ConnectFourAI.MAX_DEPTH = ConnectFourActivity.cfLevel;

        // Charge the status of products for pawn shop, is sold or not:
        String temp = getStringSettings("psIsSold");
        getSoldProducts(temp); // a method defined in this class for
        // PawnshopActivity.psIsSold boolean array.

        // For bath status:
        BathroomActivity.bathStatus = getIntSettings("bathStatus");

        // To know how many drinks we order at bar area, this is for toilet
        // needs:
        SaloonActivity.nrOfDrinks = getIntSettings("nrOfDrinks");

    } // end charge settings.

    public void setDefaultSettings() {

        // Set nickname to null:
        saveStringSettings("nickname", null);

        // Code language:
        // Get the system current locale: // Get the locale:
        String curLocale = context.getResources().getConfiguration().locale.getDisplayName();
        curLocale = curLocale.substring(0, 2);
        curLocale = curLocale.toLowerCase(Locale.US);
        // GUITools.alert(context, "Language", curLocale);
        if (curLocale.equals("en") || curLocale.equals("it") || curLocale.equals("ro") || curLocale.equals("tü")) {
            if (curLocale.equals("tü")) curLocale = "tr";
            saveStringSettings("currentLanguage", curLocale);
        } else {
            saveStringSettings("currentLanguage", "en");
        }
        // End save default number speaking language.

        // // Activate speech, sounds for dice and number speaking:
        saveBooleanSettings("isSpeech", true);
        saveBooleanSettings("isSound", true);
        saveBooleanSettings("isSoundLooped", true);
        saveIntSettings("soundBackgroundPercentage", 30);
        saveBooleanSettings("isSoundMusic", true);
        saveIntSettings("soundMusicPercentage", 10);
        saveBooleanSettings("isSoundDice", true); // if the dice are announced
        // vocally.
        saveBooleanSettings("isWarSounds", false); // to play war specific
        // sounds in Cards War.

        // Activate shake detection:
        saveBooleanSettings("isShake", true);

        // Set on shake magnitude to 2.2F: // now default value, medium.
        saveFloatSettings("onshakeMagnitude", 2.2F);

        // For keeping screen awake:
        saveBooleanSettings("isWakeLock", true);

        // For vibrations:
        saveBooleanSettings("isVibration", true);

        // For targetScore in Scopa game:
        saveIntSettings("scopaTargetScore", 11);

        // For Scopa variant, classic is default:
        saveIntSettings("lsVariant", 1);

        // For sorting dice method:
        saveIntSettings("diceSortMethod", 2);

        // For amount of money, we give 1000 dollars by default:
        saveIntSettings("myTotalMoney", 1000);
        Dealer.myTotalMoney = 1000;

        // We set the default bet value to 50 dollars:
        saveIntSettings("lastBet", 50);

        // Set to 0 the money spent at bar area:
        saveIntSettings("spentMoney", 0);

        // Set to 0 the number of games for order at bar since last order:
        saveIntSettings("numberOfGamesForOrder", 0);

        // Set to 0 the bonus percentage at bar:
        saveIntSettings("actualBonusPercentage", 0);

        // Set to 0 the money won as bonus since beginnings:
        saveIntSettings("moneyWonAsBonus", 0);
        // Set to 0 the money spent in last 24 hours:
        saveIntSettings("moneyWonAsBonus24", 0);
        // Because is a reset time too, we do also the bonus for current session
        // at 0, without saving it:
        Dealer.curMoneyWonAsBonus = 0;

        // Set the last start of the day to 0, for last 24 hour system for
        // bonus:
        saveIntSettings("currentTimeMinute", 0);

        // The level for connect four game:
        saveIntSettings("cfLevel", ConnectFourActivity.CF_DEFAULT_LEVEL);
        ConnectFourActivity.cfLevel = ConnectFourActivity.CF_DEFAULT_LEVEL;
        ConnectFourAI.MAX_DEPTH = ConnectFourActivity.cfLevel;

        // Set to default the products sold at pawn shop; default means not
        // sold, false all items:
        for (int i = 0; i < PawnshopActivity.psIsSold.length; i++) {
            PawnshopActivity.psIsSold[i] = false;
        } // end for all items in psIsSold array.
        saveSoldProducts(); // a method defined here to save in SharedSettings a
        // string for products sold at pawn shop.

        // For bath status:
        saveIntSettings("bathStatus", 0);
        BathroomActivity.bathStatus = 0;

        // For number of drinks in bar area:
        saveIntSettings("nrOfDrinks", 0);
        SaloonActivity.nrOfDrinks = 0;

    } // end setDefaultSettings function.

    // A method to charge the string and array for sold products at pawn shop:
    private void getSoldProducts(String temp) {

        if (temp == null || temp.length() != PawnshopActivity.psIsSold.length) {
            // This means there is no a correct string for this, we correct it
            // making all 0, seven times:
            temp = "0000000";
        } // end if there is no a correct string for sold products.

        // Fill the psIsSold array with boolean values, depending of the temp
        // string:
        StringBuilder sb = new StringBuilder(temp);

        for (int i = 0; i < PawnshopActivity.psIsSold.length; i++) {

            PawnshopActivity.psIsSold[i] = sb.charAt(i) != '0';
        } // end for all letter in temp string.
    } // end getSoldProducts.

    // A method to save current status of products in pawn shop:
    public void saveSoldProducts() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < PawnshopActivity.psIsSold.length; i++) {
            if (PawnshopActivity.psIsSold[i]) {
                // It means the product doesn't exist anymore and in the final
                // string the value will be 1, psIsSold is 1 true:
                sb.append("1");
            } else {
                sb.append("0");
            }
        } // end for all items in boolean array psIsSold.
        // Save now the string:
        saveStringSettings("psIsSold", sb.toString());
    } // end saveSoldProducts.

} // end Settings Class.
