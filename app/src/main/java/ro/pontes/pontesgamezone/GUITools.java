package ro.pontes.pontesgamezone;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

/*
 * Started on 15 September 2014, at 00:10 by Manu.
 * This class has some useful things for the GUI, like alerts.
 */

public class GUITools {

    // A method to show an alert with title and message, just an OK button:
    public static void alert(Context context, String title, String message) {

        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        // The title:
        alert.setTitle(title);

        // The body creation:
        // Create a LinearLayout with ScrollView with all contents as TextViews:
        ScrollView sv = new ScrollView(context);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        String[] mParagraphs = message.split("\n");

        // A for for each paragraph in the message as TextView:
        for (String mParagraph : mParagraphs) {
            TextView tv = new TextView(context);
            // tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            tv.setText(mParagraph);
            ll.addView(tv);
        } // end for.

        // Add now the LinearLayout into ScrollView:
        sv.addView(ll);

        alert.setView(sv);

        alert.setPositiveButton("Ok", (dialog, whichButton) -> {
            // Do nothing yet...
        });
        alert.show();
    } // end alert static method.

    // A method for about dialog for this package:
    @SuppressLint("InflateParams")
    public static void aboutDialog(Context context) {
        // Inflate the about message contents
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View messageView = inflater.inflate(R.layout.about_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // builder.setIcon(R.drawable.app_icon);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.setPositiveButton("OK", null);
        builder.create();
        builder.show();
    } // end about dialog.

    // A method to play a tone, just to make tests:
    public static void beep() {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
    }

    // A method to give a toast, simple message on the screen:
    public static void toast(String message, int duration, Context context) {
        Toast.makeText(context, message, duration).show();
    } // end make toast.

    // A method to concatenate numbers and return them as a bigger integer
    // number:
    public static long concatenateNumbers(int... digits) {
        StringBuilder sb = new StringBuilder(digits.length);
        for (int digit : digits) {
            sb.append(digit);
        }
        return Long.parseLong(sb.toString());
    }

    // A method to verify if a string is alphanumeric:
    public static boolean isAlphanumeric(String str) {
        char[] forbiddenChars = new char[]{' ', '/', '\\', '\"', '\''};

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // We check if the value is one of the forbidden characters:
            for (char forbiddenChar : forbiddenChars) {
                if (c == forbiddenChar) {
                    return false;
                }
            } // end inner for.
        } // end outer for.

        return true;
    } // end isAlphanumeric method.

    // A static method to get a random number between two integers:
    public static int random(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    } // end random method.

    // A method to open the browser with an URL:
    private static final String HTTPS = "https://";
    private static final String HTTP = "http://";

    public static void openBrowser(final Context context, String url) {

        if (!url.startsWith(HTTP) && !url.startsWith(HTTPS)) {
            url = HTTP + url;
        }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    } // end start browser with an URL in it.

    // A method to open the help online:
    public static void openHelp(final Context context) {
        openBrowser(context, "http://www.android.pontes.ro/pontesgamezone/help/");
    } // end open help online method.

    // A method to go to bar area, saloon:
    public static void goToSaloon(Context context) {
        Intent intent = new Intent(context, SaloonActivity.class);
        /*
         * String message = new String(); message="Pontes GameZone"; // without
         * a reason, just to be something sent by the intent.
         * intent.putExtra(EXTRA_MESSAGE, message);
         */
        context.startActivity(intent);
    } // end go to saloon option.

    // A method to go to bathroom:
    public static void goToBathroom(Context context) {
        Intent intent = new Intent(context, BathroomActivity.class);
        /*
         * String message = new String(); message="Pontes GameZone"; // without
         * a reason, just to be something sent by the intent.
         * intent.putExtra(EXTRA_MESSAGE, message);
         */
        context.startActivity(intent);
    } // end go to bathroom option.

    // A method to go to pawn shop:
    public static void goToPawnshop(Context context) {
        Intent intent = new Intent(context, PawnshopActivity.class);
        /*
         * String message = new String(); message="Pontes GameZone"; // without
         * a reason, just to be something sent by the intent.
         * intent.putExtra(EXTRA_MESSAGE, message);
         */
        context.startActivity(intent);
    } // end go to pawn shop option.

    // A method to go to casino entrance:
    public static void goToCasinoEntrance(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        /*
         * String message = new String(); message="Pontes GameZone"; // without
         * a reason, just to be something sent by the intent.
         * intent.putExtra(EXTRA_MESSAGE, message);
         */
        context.startActivity(intent);
    } // end go to casino entrance option.

    // A method to be called from pause() methods from activities, we need some
    // global things:
    public static void setCustomThingsAtPause() {
        // Nothing yet.
    } // end set custom things at pause.

    // A method to show statistics about bonus, it is called from menus in all
    // games:
    public static void showBonusStatistics(Context context, int curMoneyWonAsBonus) {
        GUITools.alert(context, context.getString(R.string.money_won_as_bonus_title), String.format(context.getString(R.string.money_won_as_bonus_message), MainActivity.curNickname, "" + curMoneyWonAsBonus, "" + Dealer.moneyWonAsBonus24, "" + Dealer.moneyWonAsBonus));
    } // end show statistics about money won as bonus.

    // A method to obtain the actual TimeStamp minute:
    public static int getCurrentTimeMinute() {
        return (int) (System.currentTimeMillis() / (1000 * 60));
    }

    // A method to obtain current hour of the virtual day:
    // We need statics variables for hour and minute which will be calculated in
    // the next method:
    public static int currentVirtualHour;
    public static int currentVirtualMinute;

    // A method to obtain current hour and minute of the virtual day:
    public static void getCurrentVirtualTime(Context context) {
        // Get the number of hours and minutes passed since last start of the
        // day:
        Settings set = new Settings(context);
        int dayStart = set.getIntSettings("currentTimeMinute");
        int dayPassed = GUITools.getCurrentTimeMinute() - dayStart;

        currentVirtualHour = dayPassed / 60;
        currentVirtualMinute = dayPassed % 60;
    } // end of set virtual time method.

    // A method to show an alert with virtual time:
    public static void showVirtualTime(Context context) {
        if (!PawnshopActivity.psIsSold[1]) {
            // It means the clock is available, the watch was not sold to pawn
            // shop:
            getCurrentVirtualTime(context);
            GUITools.alert(context, context.getString(R.string.saloon_virtual_time_title), String.format(context.getString(R.string.saloon_passed_day), GUITools.twoDigits(currentVirtualHour), GUITools.twoDigits(currentVirtualMinute)));
        } else {
            // The watch was sold to pawn shop:
            GUITools.alert(context, context.getString(R.string.saloon_virtual_time_title), context.getString(R.string.ps_clock_is_unavailable));
        }
    } // end show virtual time method.

    /*
     * A method to convert a number to string with two digits, for instance 9 to
     * be 09, 0 to be 00:
     */
    public static String twoDigits(int val) {
        if (val < 10) {
            return "0" + val;
        } else {
            return "" + val;
        }
    } // end twoDigits method.

} // end GUITools class.
