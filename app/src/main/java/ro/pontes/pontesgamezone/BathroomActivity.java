package ro.pontes.pontesgamezone;

/*
 * Class started by Manu on Wednesday, 08 April 2015, 21:30.
 * This class takes care of the bathroom in this game.
 */

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class BathroomActivity extends Activity {

    // listeners.

    // A variable to hold the status number as global, saved also in shared
    // settings:
    public static int bathStatus = 0;
    public final int minimumOrdersBeforeToilet = 3;

    private Settings set;

    // For speak:
    private SpeakText speak;

    // An array for object names found in bathroom taken from values strings:
    private String[] objectNames;
    private String[] statusMessages;

    // For a timer:
    private Timer t;

    // Messages for handler to manage the interface:
    private static final int UPDATE_TIME_VIA_HANDLER = 1; // a message to be
    // sent to the
    // handler.

    // A static inner class for handler:
    static class MyHandler extends Handler {
        WeakReference<BathroomActivity> bathActivity;

        MyHandler(BathroomActivity aBathActivity) {
            bathActivity = new WeakReference<>(aBathActivity);
        }
    } // end static class for handler.

    // this handler will receive a delayed message
    private final MyHandler mHandler = new MyHandler(this) {
        @Override
        public void handleMessage(Message msg) {
            // Do task here
            // BathroomActivity theActivity = bathActivity.get();

            if (msg.what == UPDATE_TIME_VIA_HANDLER) {
                updateVirtualTime();
            }

        }
    };

    // End handler stuff.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bathroom);

        // Instantiate with constructor the set variable to save and charge
        // things from SharedSettings:
        set = new Settings(this);

        // Instantiate the speak object:
        speak = new SpeakText(this);

        // Get the arrays to fill the product names and prices:
        Resources res = getResources();
        objectNames = res.getStringArray(R.array.bath_actions_array);
        statusMessages = res.getStringArray(R.array.bath_status_array);

        // Update the welcome message:
        TextView wtv = findViewById(R.id.tvWelcomeToBathroom);
        wtv.setText(getString(R.string.bath_title));

        // A variable for bath status text view:
        int requiredActionStatus;
        // First if it is mandatory a toilet use, before sync and hand drier:
        if (SaloonActivity.nrOfDrinks >= minimumOrdersBeforeToilet && bathStatus == 0) {
            requiredActionStatus = 1;
        } else if (bathStatus == 1) {
            // If sync is required:
            requiredActionStatus = 2;
        } else if (bathStatus == 2) {
            // The hand drier is required:
            requiredActionStatus = 3;
        } else {
            // Nothing is requred:
            requiredActionStatus = 0;
        }
        // Update the status:
        updateStatus(statusMessages[requiredActionStatus]);

        updateTextViews();

        setImagesStatus();

        // Set the virtual time for first time, after it will be set via handler
        // each minute:
        updateVirtualTime();

        // To keep screen awake:
        if (MainActivity.isWakeLock) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } // end if is wake lock.

    } // end onCreate method.

    // A method to set content description:
    private void setImagesStatus() {
        // Set content description for image buttons with objects found in the
        // bathroom:
        for (int i = 1; i < objectNames.length; i++) {
            String ibtName = "ibtBathroom" + i;
            int resID = getResources().getIdentifier(ibtName, "id", getPackageName());
            ImageButton ibt = findViewById(resID);
            String tempMessage = String.format(getString(R.string.bath_use_objects), objectNames[i]);
            ibt.setContentDescription(tempMessage);
        } // end for set contentDescription for ImageButtons.
    } // end setImagesStatus method.

    public void setTheTimer() {
        // Set the timer to send messages to the mHandler:
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(() -> {
                    // Send a message to the handler:
                    // This is to update the text views for virtual time:
                    mHandler.sendEmptyMessageDelayed(UPDATE_TIME_VIA_HANDLER, 0); // 0 means the
                    // delay in
                    // milliseconds.
                });
            }
        }, 60000, 60000); // 1000 means start from 1 second, and the second 1000
        // is do the loop each 1 second.
        // end set the timer.
    } // end setTheTimer method.

    @Override
    public void onResume() {
        super.onResume();

        setTheTimer();
        SoundPlayer.playSimple(this, "bath_door");

    } // end onResume method.

    @Override
    public void onPause() {
        // Add here what you want to happens on pause:

        t.cancel();
        t = null;

        super.onPause();
    } // end onPause method.

    // For menu, go to different areas:
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.go_to, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.mnuSaloon) {
            GUITools.goToSaloon(this);
        } // end got o saloon option.
        else if (id == R.id.mnuPawnshop) {
            GUITools.goToPawnshop(this);
        } // end go to pawn-shop option.
        else if (id == R.id.mnuBathroom) {
            GUITools.goToBathroom(this);
        } // end go to bathroom option.
        else if (id == R.id.mnuCasinoEntrance) {
            GUITools.goToCasinoEntrance(this);
        } // end if go to start was pressed in menu.
        else if (id == R.id.mnuGoBack) {
            this.finish();
        } // end go back.

        return super.onOptionsItemSelected(item);
    } // end handle the items chosen in menu.

    // A method to update the text views in the bathroom:
    private void updateTextViews() {

    } // end updateTextViews method.

    // A method to update the virtual time text view:
    public void updateVirtualTime() {
        TextView tv = findViewById(R.id.tvPassedDay);
        if (!PawnshopActivity.psIsSold[1]) {
            GUITools.getCurrentVirtualTime(this);
            int curHour = GUITools.currentVirtualHour;
            int curMinute = GUITools.currentVirtualMinute;
            tv.setText(String.format(getString(R.string.saloon_passed_day), "" + curHour, "" + curMinute));
        } else {
            // If virtual time doesn't exist, the watch was sold:
            tv.setText(getString(R.string.tv_clock_is_unavailable));
        }
    } // end update virtual time method.

    // Methods for each button pressed in bathroom menu:

    public void bathAction1(View view) {
        bathAction(1);
    }

    public void bathAction2(View view) {
        bathAction(2);
    }

    public void bathAction3(View view) {
        bathAction(3);
    }

    // A method for using, this receive the object index via parameter:
    public void bathAction(final int which) {
        // A variable to know what action is required, 1 toilet, 2 sync, 3 hand
        // drier:
        int curBathStatus = 0;

        // First if it is mandatory a toilet use, before sync and hand drier:
        if (SaloonActivity.nrOfDrinks >= minimumOrdersBeforeToilet && bathStatus == 0) {
            curBathStatus = 1;
        } else if (bathStatus == 1) {
            // If sync is required:
            curBathStatus = 2;
        } else if (bathStatus == 2) {
            // The hand drier is required:
            curBathStatus = 3;
        }

        // Check if curBathStatus is the same value as which, the button pressed
        // by player:
        if (curBathStatus == which) {
            // We increment the bathStatus, the global static variable for
            // status of the bath:
            bathStatus = bathStatus + 1;

            // Post in statistics this use of an object:
            Statistics.postStats("18", 1); // 18 is the id of the bathroom using
            // in DB.
            // Play a corresponding sound:
            SoundPlayer.playSimple(this, "bath" + which);
            // Check if the action is 1, it means nrOfDrinks will become again
            // 0:
            if (which == 1) {
                SaloonActivity.nrOfDrinks = 0;
                set.saveIntSettings("nrOfDrinks", SaloonActivity.nrOfDrinks);
            }

            // Update the bath status:
            // Make it 0 if the actions was 3, it means we return to no action
            // is required:
            if (curBathStatus >= 3) {
                curBathStatus = -1;
            }
            updateStatus(statusMessages[curBathStatus + 1]);
        } // end if there is a correct action.
        else {
            // If there was not the correct action chosen:
            // We give a general warning alert message:
            GUITools.alert(this, getString(R.string.warning), String.format(getString(R.string.bath_wrong_action_chosen), MainActivity.curNickname));
        } // end if there was not the correct action chosen.

        // Now check something and save statuses:
        if (bathStatus > 2) {
            // It means is more than 2 which is the use of hand drier:
            bathStatus = 0;
        }
        // Save the bathStatus:
        set.saveIntSettings("bathStatus", bathStatus);

    } // end order something method.

    // A method to update the bathStatus TextView:
    public void updateStatus(String text) {
        TextView tv = findViewById(R.id.tvBathroomStatus);
        tv.setText(text);
        speak.say(text, false);
    } // end update bath status method.

} // end bathroomActivity class.

