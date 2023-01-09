package ro.pontes.pontesgamezone;

/*
 * Class started on 06 October 2014, 02:15 by Manu
 * This is a simple dice game, like Greater and Double, something obscure.
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class WhoIsGreaterActivity extends Activity {

    // The following fields are used for the shake detection:
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    // End fields declaration for shake detector.

    // For music background:
    SoundPlayer sndMusic;

    Context c;
    Dealer dealer;
    SpeakText speak;
    Dice[] diceHands; // an array of hands of dice.

    int whoIs = 1; // whose turn is for the corresponding hand of cards war.
    boolean isStarted = false; // to know if a hand is started or not.
    int myScore = 0;
    int dealersScore = 0;
    int maxScore = 6;

    // For statistics:
    int numberOfPlayedHands = 0;
    int moneyWon = 0;
    int moneyLost = 0;

    // Let's have the tvBjStatus and other text views globally, not to
    // findViewById so often:
    TextView tvGwStatus;
    TextView tvMyMoney;
    TextView tvMyBet;
    TextView tvMyGwTotal;
    TextView tvDealersGwTotal;

    // Some global strings which are formated periodically:
    String tempMessage; // to use for updating the status.
    String strMyMoney;
    String strMyBet;
    String strMyGwTotal;
    String strDealersGwTotal;
    String strActualScore;
    String resPossesionString; // for content description: Dealer is 0, player
    // is 1.
    String[] aPossesion; // the array which contains at 0 Dealer, 1 player.

    // Let's declare variables for all the buttons, we will use them to enable
    // or disable depending of the game status:
    Button btGwBet;
    Button btGwNew;
    Button btGwThrow;
    Button btGwAbandon;

    // A variable to detect if bet was changed, this way we call the mHandler to
    // update text views for dealer:
    private int myTempBet; // we use it in the setTimer method, and we
    // initialise it in onCreate.

    // A variable to know if player thrown the dice, this way the handler will
    // send a message for dealer to throw dice:
    boolean playerThrown = false;
    boolean bothThrown = false; // we know when a battle is ended, let's say the
    // score.

    // For a timer:
    private Timer t;

    // Messages for handler to manage the interface:
    private static final int UPDATE_VIEWS_VIA_HANDLER = 1; // a message to be
    // sent to the
    // handler.
    private static final int DEALER_MUST_THROW_HANDLER = 2; // a message to be
    // sent to the
    // handler.
    private static final int BOTH_THROWN_HANDLER = 3; // a message to be sent to
    // the handler.

    // A static inner class for handler:
    static class MyHandler extends Handler {
        WeakReference<WhoIsGreaterActivity> gwActivity;

        MyHandler(WhoIsGreaterActivity aGwActivity) {
            gwActivity = new WeakReference<>(aGwActivity);
        }
    } // end static class for handler.

    // this handler will receive a delayed message
    private final MyHandler mHandler = new MyHandler(this) {
        @Override
        public void handleMessage(Message msg) {
            // Do task here
            // WhoIsGreaterActivity theActivity = gwActivity.get();

            if (msg.what == UPDATE_VIEWS_VIA_HANDLER) {
                redrawTextViews(); // update text on text views.
            }
            if (msg.what == DEALER_MUST_THROW_HANDLER) {
                // playerThrown = false;

                throwDiceActions(0);

                // Now is the moment for things after both thrown dice:
                bothThrown = true;

            } // end if dealer thrown the dice.
            if (msg.what == BOTH_THROWN_HANDLER) {
                afterABattle();
            } // end if both thrown the dice. we came here after a battle.
        }
    };

    // End handler stuff.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_who_is_greater);

        c = getApplicationContext();
        dealer = new Dealer(this, true);
        speak = new SpeakText(this);

        // Get the text views into their global variables:
        tvGwStatus = findViewById(R.id.tvGwStatus);
        tvMyMoney = findViewById(R.id.tvMyMoney);
        tvMyBet = findViewById(R.id.tvMyBet);
        tvMyGwTotal = findViewById(R.id.tvMyGwTotal);
        tvDealersGwTotal = findViewById(R.id.tvDealersGwTotal);

        // Charge global strings from values strings:
        strMyMoney = getString(R.string.my_money);
        strMyBet = getString(R.string.my_bet);
        strMyGwTotal = getString(R.string.my_gw_total);
        strDealersGwTotal = getString(R.string.dealers_gw_total);
        strActualScore = getString(R.string.gw_actual_score);

        Resources res = getResources();
        aPossesion = res.getStringArray(R.array.possesion_array);
        resPossesionString = res.getString(R.string.die_name_extended);
        aPossesion[1] = MainActivity.curNickname;

        // Get the buttons into their global variables, we will use them to
        // enable or disable periodically:
        btGwBet = findViewById(R.id.btGwBet);
        btGwNew = findViewById(R.id.btGwNew);
        btGwThrow = findViewById(R.id.btGwThrow);
        btGwAbandon = findViewById(R.id.btGwAbandon);

        // Redraw for first time, to have 0 as scores and the bet and wallet:
        redrawTextViews();
        enableOrDisableButtons();

        myTempBet = dealer.currentBet; // we check in setTimer if it is changed,
        // to update text views for dealer.

        // Initialise hand of dice in array:
        diceHands = new Dice[2];
        for (int i = 0; i < diceHands.length; i++) {
            diceHands[i] = new Dice(2, this);
        }

        // To keep screen awake:
        if (MainActivity.isWakeLock) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } // end wake lock.

        // ShakeDetector initialisation
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setShakeThresholdGravity(MainActivity.onshakeMagnitude);
        /*
         * method you would use to setup whatever you want done once the
         * device has been shook.
         */
        mShakeDetector.setOnShakeListener(this::handleShakeEvent);
        // End initialisation of the shake detector.

    } // end onCreate method.

    public void setTheTimer() {
        // Set the timer to send messages to the mHandler:
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(() -> {
                    // Send a message to the handler:
                    // This is to update the text views for bets found in
                    // the upper part of the screen, if the bet was changed:
                    if (myTempBet != dealer.currentBet) {
                        myTempBet = dealer.currentBet;
                        mHandler.sendEmptyMessageDelayed(UPDATE_VIEWS_VIA_HANDLER, 0); // 0 means the
                        // delay in
                        // milliseconds.
                    } // end if event change current bet text field
                    // occurred.
                    if (playerThrown && !Dice.isDiceSpeaking) {
                        // Now dealer must also throw dice:
                        mHandler.sendEmptyMessageDelayed(DEALER_MUST_THROW_HANDLER, 100); // x means
                        // the
                        // delay
                        // in
                        // milliseconds.
                        playerThrown = false;
                    } // end dealers turn event occurred.
                    if (bothThrown && !Dice.isDiceSpeaking) {
                        // Now afterBattle actions must occur:
                        mHandler.sendEmptyMessageDelayed(BOTH_THROWN_HANDLER, 100); // x means the
                        // delay in
                        // milliseconds.
                        bothThrown = false;
                    } // end if both thrown, afterBattleActions must occur.
                });
            }
        }, 1000, 200); // 1000 means start from 1 second, and the second 1000 is
        // do the loop each 1 second.
        // end set the timer.
    } // end setTheTimer method.

    @Override
    public void onResume() {
        super.onResume();

        // Because we come sometimes from bar area, we want to update the wallet
        // status:
        updateTextViewsForMoney();

        setTheTimer();

        if (MainActivity.isShake) {
            // Add the following line to register the Session Manager Listener
            // onResume
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }

        // Generate a new track:
        sndMusic = new SoundPlayer();
        sndMusic.playMusic(this);

    } // end onResume method.

    @Override
    public void onPause() {
        // Add here what you want to happens on pause:

        t.cancel();
        t = null;

        // Only if there are wars played, post the statistics:
        if (numberOfPlayedHands > 0) {
            Statistics.postStats("9", numberOfPlayedHands); // 9 is the id of
            // the Who is
            // Greater in
            // soft_counts table
            // in DB.
            numberOfPlayedHands = 0;
            moneyWon = 0;
            moneyLost = 0;
        }
        // end post the statistics.

        if (MainActivity.isShake) {
            // Add the following line to unregister the Sensor Manager onPause
            mSensorManager.unregisterListener(mShakeDetector);
        }

        sndMusic.stopLooped();

        super.onPause();
    } // end onPause method.

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.who_is_greater, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.mnuGwStatistics) {
            int ballance = moneyWon - moneyLost;

            double ballancePerHand = 0;
            if (numberOfPlayedHands > 0) {
                // We avoid division by 0:
                ballancePerHand = (double) ballance / (double) numberOfPlayedHands;
                ballancePerHand = Math.round(ballancePerHand * 100.0) / 100.0;
            }
            String title = getString(R.string.title_statistics_alert);
            String message = String.format(getString(R.string.statistics_for_who_is_greater), "" + numberOfPlayedHands, "" + moneyWon, "" + moneyLost, "" + ballance, "" + ballancePerHand);
            GUITools.alert(this, title, message);
        } // end statistics alert.
        else if (id == R.id.mnuMoneyWonAsBonus) {
            GUITools.showBonusStatistics(this, Dealer.curMoneyWonAsBonus);
        } // end if bonus statistics is chosen in menu.
        else if (id == R.id.mnuHelp) {
            GUITools.openHelp(this);
        } // end help option in menu.
        else if (id == R.id.mnuSaloon) {
            GUITools.goToSaloon(this);
        } // end got o saloon option.
        else if (id == R.id.mnuPawnshop) {
            GUITools.goToPawnshop(this);
        } // end go to pawn-shop option.
        else if (id == R.id.mnuBathroom) {
            GUITools.goToBathroom(this);
        } // end go to bathroom option.
        else if (id == R.id.mnuVirtualTime) {
            GUITools.showVirtualTime(this);
        } // end show virtual time.

        return super.onOptionsItemSelected(item);
    }

    public void changeBet(View view) {
        dealer.changeBet();
    } // end function change bet.

    public void handleShakeEvent(int count) {
        // It depends of the status of the game, started or not:
        if (isStarted) {
            startNewBattleActions();
        } else {
            newSetActions();
        }
    } // end handle shake detection.

    // The method to start a new set:
    public void newSet(View view) {
        newSetActions();
    }

    public void newSetActions() {
        if (!isStarted) {

            if (dealer.addBet()) {
                isStarted = true;

                // Stop previously speaking:
                speak.stop();

                // Increase number of hands:
                numberOfPlayedHands++;

                enableOrDisableButtons();

                // Reset some values:
                dealersScore = 0;
                myScore = 0;
                updateTextViewsForTotals(myScore, dealersScore);

                // Empty dice hands, make all 0 and draw them again:
                diceHands[0].removeAllDice();
                drawHandsOfDice(0);
                diceHands[1].removeAllDice();
                drawHandsOfDice(1);

                // Show and say about this start:
                tempMessage = getString(R.string.gw_started);
                updateStatus(tempMessage);

            } // end if add bet method was processed correctly.
        } // end if is not started.
    } // end new set actions.

    // The method to start a new battle:
    public void startNewBattle(View view) {
        startNewBattleActions();
    }

    public void startNewBattleActions() {
        if (!Dice.isDiceSpeaking) {
            // Only if dice are not spoken by voice, not to throw quickly many
            // dice.
            throwDiceActions(1);
            playerThrown = true; // now also dealer throws dice.
        } // end if dice are not speaking.
    } // end startNewBattleActions.

    public void afterABattle() {

        // Check who won this battle:
        if (diceHands[1].isDouble() && !diceHands[0].isDouble()) {
            // User had a double, but dealer not:
            myScore += 2;
        } else if (!diceHands[1].isDouble() && diceHands[0].isDouble()) {
            // Dealer has a double:
            dealersScore += 2;
        } else if (diceHands[1].isDouble() && diceHands[0].isDouble()) {
            // Both had doubles:
            if (diceHands[1].getTotal() > diceHands[0].getTotal()) {
                // Player's double was greater than the dealer's one:
                myScore += 1;
            } else if (diceHands[1].getTotal() < diceHands[0].getTotal()) {
                // Dealer had a greater double:
                dealersScore += 1;
            }
        } // end if there were two doubles.

        // now if there were not doubles, who is greater:
        else if (diceHands[1].getTotal() > diceHands[0].getTotal()) {
            // Player was greater without double:
            myScore += 1;
        } else if (diceHands[1].getTotal() < diceHands[0].getTotal()) {
            // Dealer was greater without double:
            dealersScore += 1;
        } else if (diceHands[0].getTotal() == diceHands[1].getTotal()) {
            // They are equal without double:
            if (diceHands[1].aDice[0] > diceHands[0].aDice[0]) {
                // First die of the player was greater than the first die of the
                // dealer:
                myScore += 1;
            } else if (diceHands[1].aDice[0] < diceHands[0].aDice[0]) {
                // First die of the dealer was greater than the first die of the
                // player:
                dealersScore += 1;
            }
        } // end if they were equals without double.

        // Note! We don't count something if they were equals absolutely, same
        // dice in hands..

        tempMessage = String.format(strActualScore, "" + myScore, "" + dealersScore);
        updateStatus(tempMessage);
        updateTextViewsForTotals(myScore, dealersScore);

        // If the set is ready, at least one reached maxScore:
        if (myScore >= maxScore || dealersScore >= maxScore) {
            isStarted = false;
            finalActions();
        }
    } // end after a battle method.

    // A method with actions when a new throw of dice occurs:
    public void throwDiceActions(int whoIs) {
        diceHands[whoIs].throwDice();
        drawHandsOfDice(whoIs);
    } // end throwDiceActions method.

    // A method to abandon:
    public void abandon(View view) {
        isStarted = false;
        dealersScore = maxScore;
        finalActions();
    }

    public void finalActions() {
        // If the set is finished, or the games is not started because a
        // abandon, let's see who won the set:
        if (!isStarted) {
            // without a reason.
            enableOrDisableButtons();

            if (dealersScore > myScore) {
                // Dealer is the final winner:
                moneyLost = moneyLost + dealer.currentBet;
                dealer.lose();

                tempMessage = getString(R.string.gw_dealer_won_the_set);
                tempMessage = String.format(tempMessage, "" + dealer.currentBet);

            } else if (myScore > dealersScore) {
                // Player is the winner:
                moneyWon = moneyWon + dealer.currentBet;
                dealer.win();

                tempMessage = getString(R.string.gw_you_won_the_set);
                tempMessage = String.format(tempMessage, "" + dealer.currentBet);

            } else {
                // It's a draw:
                dealer.draw();

                tempMessage = getString(R.string.gw_draw_set);

            } // end the 3 possibilities for winner.
            updateTextViewsForTotals(myScore, dealersScore);
            updateTextViewsForMoney();
            updateStatus(tempMessage);

        } // end if the set is finished or withdraw.
    } // end finalActions method.

    // A method to draw dice on screen:
    public void drawHandsOfDice(int whose) {
        LinearLayout ll;

        // Find the corresponding linear layout, depending of whose turn is:
        // If is dealer hand of dice:
        if (whose == 0) {
            ll = findViewById(R.id.llDealerDice);
        } else {
            ll = findViewById(R.id.llPlayerDice);
        }

        diceHands[whose].draw(ll, whose);
    } // end draw hands of dice.

    // A method to update some TextViews:
    public void updateTextViewsForMoney() {

        // First change the tvMyMoney TextView:

        tvMyMoney.setText(String.format(strMyMoney, "" + Dealer.myTotalMoney));

        // Now change the tvMyBet TextView:
        tvMyBet.setText(String.format(strMyBet, "" + dealer.currentBet));

    } // end updateTextViews method.

    // A method to update TextViews for totals in hands:
    public void updateTextViewsForTotals(int myScore, int dealersScore) {

        // First change the tvMyGwTotal TextView:
        tvMyGwTotal.setText(String.format(strMyGwTotal, "" + myScore));

        // Now change the tvDealersCwTotal TextView:
        tvDealersGwTotal.setText(String.format(strDealersGwTotal, "" + dealersScore));
    } // end updateTextViews with totals in hands method.

    // A method to update the tvCardsWarStatus TextView:
    public void updateStatus(String text) {
        tvGwStatus.setText(text);
        speak.say(text, false);
    } // end updateBjStatus method.

    // A method to make buttons state active or inactive, depending of the
    // status of the game:
    public void enableOrDisableButtons() {

        // If is started or not:
        if (isStarted) {
            btGwBet.setEnabled(false);
            btGwNew.setEnabled(false);
            btGwThrow.setEnabled(true);
            btGwAbandon.setEnabled(true);
        } else {
            btGwBet.setEnabled(true);
            btGwNew.setEnabled(true);
            btGwThrow.setEnabled(false);
            btGwAbandon.setEnabled(false);
        } // end if is started or not.
    } // end enableOrDisableButtons.

    // A method to update periodically the TextViews:
    public void redrawTextViews() {
        updateTextViewsForMoney();

        updateTextViewsForTotals(myScore, dealersScore);
    } // end the method to update text views.

} // end WhoIsGreaterActivity.
