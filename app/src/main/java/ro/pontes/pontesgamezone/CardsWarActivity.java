package ro.pontes.pontesgamezone;

/*
 * Started on 28 September 2014, 20:15, by Manu.
 *
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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class CardsWarActivity extends Activity {

    // The following fields are used for the shake detection:
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    // End fields declaration for shake detector.

    // For music background:
    SoundPlayer sndMusic;

    // For hands of cards:
    CardsWarHand[] hands;

    // For sounds:
    private String[] fires;

    // Other variables:
    Context c;
    SpeakText speak;
    Dealer dealer;
    Deck deck = null;
    int whoIs = 1; // whose turn is for the corresponding hand of cards war.
    boolean isStarted = false; // to know if a hand is started or not.
    boolean isBattleStarted = false;
    int myTotalCards = 0;
    int dealersTotalCards = 0;

    // For statistics:
    int numberOfPlayedHands = 0;
    int moneyWon = 0;
    int moneyLost = 0;

    // Let's have the tvBjStatus and other text views globally, not to
    // findViewById so often:
    TextView tvCwStatus;
    TextView tvMyMoney;
    TextView tvMyBet;
    TextView tvMyCwTotal;
    TextView tvDealersCwTotal;

    // Some global strings which are formated periodically:
    String tempMessage; // to use for updating the status.
    String strMyMoney;
    String strMyBet;
    String strMyCwTotal;
    String strDealersCwTotal;
    String resPossesionString; // for content description: Dealer is 0, player
    // is 1.
    String[] aPossesion; // the array which contains at 0 Dealer, 1 player.

    // Let's declare variables for all the buttons, we will use them to enable
    // or disable depending of the game status:
    Button btCwBet;
    Button btCwNew;
    Button btCwFire;
    Button btCwWithdraw;
    CheckBox cbtWarSounds;

    // A variable to detect if bet was changed, this way we call the mHandler to
    // update text views for dealer:
    private int myTempBet; // we use it in the setTimer method, and we
    // initialise it in onCreate.

    // For a timer:
    private Timer t;

    // Messages for handler to manage the interface:
    private static final int UPDATE_VIEWS_VIA_HANDLER = 1; // a message to be
    // sent to the
    // handler.

    // A static inner class for handler:
    static class MyHandler extends Handler {
        WeakReference<CardsWarActivity> cwActivity;

        MyHandler(CardsWarActivity aCwActivity) {
            cwActivity = new WeakReference<>(aCwActivity);
        }
    } // end static class for handler.

    // this handler will receive a delayed message
    private final MyHandler mHandler = new MyHandler(this) {
        @Override
        public void handleMessage(Message msg) {
            // Do task here
            // CardsWarActivity theActivity = cwActivity.get();

            if (msg.what == UPDATE_VIEWS_VIA_HANDLER) {
                redrawTextViews(); // update text on text views.
            }
        }
    };

    // End handler stuff.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cards_war);

        // Let's determine actual context to use it in many places:
        c = getApplicationContext();
        // Instantiate an SpeakText object:
        speak = new SpeakText(this);

        // Initialise the dealer:
        dealer = new Dealer(this, true);

        // Get the text views into their global variables:
        tvCwStatus = findViewById(R.id.tvCwStatus);
        tvMyMoney = findViewById(R.id.tvMyMoney);
        tvMyBet = findViewById(R.id.tvMyBet);
        tvMyCwTotal = findViewById(R.id.tvMyCwTotal);
        tvDealersCwTotal = findViewById(R.id.tvDealersCwTotal);

        // Charge global strings from values strings:
        strMyMoney = getString(R.string.my_money);
        strMyBet = getString(R.string.my_bet);
        strMyCwTotal = getString(R.string.my_cw_total);
        strDealersCwTotal = getString(R.string.dealers_cw_total);

        Resources res = getResources();
        aPossesion = res.getStringArray(R.array.possesion_array);
        resPossesionString = res.getString(R.string.card_name_extended);

        // Get the buttons into their global variables, we will use them to
        // enable or disable periodically:
        btCwBet = findViewById(R.id.btCwBet);
        btCwNew = findViewById(R.id.btCwNew);
        btCwFire = findViewById(R.id.btCwFire);
        btCwWithdraw = findViewById(R.id.btCwWithdraw);
        // Determine if the check_box war sounds is checked or not:
        cbtWarSounds = findViewById(R.id.cbtWarSounds);
        cbtWarSounds.setChecked(MainActivity.isWarSounds); // also enable or
        // disable it,
        // depending of the
        // state.

        // Redraw for first time, to have 0 as totals in hands and the bet and
        // wallet:
        redrawTextViews();
        enableOrDisableButtons();

        // Initialise the hands:
        hands = new CardsWarHand[2];

        // Initialise the sounds:
        fires = new String[6];
        // Fill the array with string sounds filenames:
        for (int i = 0; i < fires.length; i++) {
            fires[i] = "cw_fire" + (i + 1);
        } // end for fill the sounds fires array.

        initializeSounds();

        myTempBet = dealer.currentBet; // we check in setTimer if it is changed,
        // to update text views for dealer.

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
                    }
                });
            }
        }, 1000, 1000); // 1000 means start from 1 second, and the second 1000
        // is do the loop each 1 second.
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
            Statistics.postStats("8", numberOfPlayedHands); // 8 is the id of
            // the cards war in
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
        getMenuInflater().inflate(R.menu.cards_war, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.mnuCwStatistics) {
            int ballance = moneyWon - moneyLost;

            double ballancePerHand = 0;
            if (numberOfPlayedHands > 0) {
                // We avoid division by 0:
                ballancePerHand = (double) ballance / (double) numberOfPlayedHands;
                ballancePerHand = Math.round(ballancePerHand * 100.0) / 100.0;
            }
            String title = getString(R.string.title_statistics_alert);
            String message = String.format(getString(R.string.statistics_for_cards_war), "" + numberOfPlayedHands, "" + moneyWon, "" + moneyLost, "" + ballance, "" + ballancePerHand);
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

    // To specify the corresponding sound files to a SoundPlayer object:
    public void initializeSounds() {
        // Nothing yet.
    } // end initialise sounds method.

    public void playRandomFireSound() {
        // Only if war sounds are enabled:
        if (MainActivity.isWarSounds) {
            int rand = (int) (Math.random() * (6));
            int rand2 = (int) (Math.random() * (6));

            SoundPlayer.playTwoSoundsInSequence(fires[rand], fires[rand2], c);
        } // end if isWarSounds is true.
    } // end playRandomFireSound method.

    public void changeBet(View view) {
        dealer.changeBet();
    } // end function change bet.

    public void handleShakeEvent(int count) {
        // It depends of the status of the game, started or not:
        if (isStarted) {
            startBattleActions();
        } else {
            startNewWarActions();
        }
    } // end handle shake detection.

    public void startNewWar(View view) {
        startNewWarActions();
    } // end start new war by button click.

    public void startNewWarActions() {
        if (!isStarted) {

            if (dealer.addBet()) {
                isStarted = true;

                // Stop previously speaking:
                speak.stop();

                // Increase number of hands:
                numberOfPlayedHands++;

                enableOrDisableButtons();

                // Reset some values:
                dealersTotalCards = 0;
                myTotalCards = 0;
                updateTextViewsForTotals(myTotalCards, dealersTotalCards);

                // Initiate the two hands, just to have the possibility to clean
                // the layouts for cards:
                // The hands are empty, this way the draw method will draw
                // nothing, but will clear the remaining cards from last war:
                hands[0] = new CardsWarHand(this);
                hands[1] = new CardsWarHand(this);
                drawHandsOfCards(0); // clear the dealer's cards.
                drawHandsOfCards(1); // clear my cards.
                pointHandsToNull();

                // Initialise the deck of cards:
                // If isWarSounds true, then the deck will be without sounds,
                // the second parameter of the constructor will be false:
                deck = new Deck(0, !MainActivity.isWarSounds, this); // false
                // means
                // without
                // joker,
                // second
                // false
                // means
                // deck
                // without
                // sound,
                // context
                // is
                // needed
                // in
                // constructor.

                // Show and say about this start:
                tempMessage = getString(R.string.cw_started);
                updateStatus(tempMessage);

            } // end if add bet method was processed correctly.
        } // end if is not started.
    } // end start new war actions.

    public void startBattle(View view) {
        startBattleActions();
    } // end startBattleActions method.

    public void startBattleActions() {
        // Interrupt the speaking if it's a desire to play faster:
        playRandomFireSound();
        speak.say("", true);
        if (!isBattleStarted) {
            isBattleStarted = true;
            // Initialise the hands of cards:
            hands[0] = new CardsWarHand(this);
            hands[1] = new CardsWarHand(this);
            bothFire();
        } // end if is not a battle started.
        else { // if a battle is started, more cards in hand:
            bothFire();
        }
    } // end start battle method.

    // A method to take two cards, for each soldier:
    public void bothFire() {
        whoIs = 1;
        takeCardActions(whoIs);
        whoIs = 0;
        takeCardActions(whoIs);
        checkBattleWinner();
        finalActions();
    } // end bothFire method.

    public void takeCardActions(int whoIs) {
        // Extract a card from deck:
        Card card = deck.dealCard();

        // Add it into the cardsWarHand:
        hands[whoIs].addCard(card);
        drawHandsOfCards(whoIs); // 0 dealer, greater player, draw them on the
        // screen..

        // The message is different depending of the whoIs, dealer or player:
        String tempString = String.format(resPossesionString, aPossesion[whoIs], card.toString());
        speak.say(tempString, false);

    } // end take card actions.

    // A method to check if someone is the winner of the battle:
    public void checkBattleWinner() {
        // If it is still equal hands:
        if (hands[0].getCardsWarValue() == hands[1].getCardsWarValue()) {
            // play a sound for an equal.
            isBattleStarted = true;

            tempMessage = getString(R.string.cw_battle_continues);
        } else if (hands[0].getCardsWarValue() > hands[1].getCardsWarValue()) {
            // Dealer is the winner of the battle:
            isBattleStarted = false;
            dealersTotalCards = dealersTotalCards + hands[0].getCardCount() * 2; // there
            // are
            // two
            // hands
            // of
            // cards.
            pointHandsToNull();

            tempMessage = getString(R.string.cw_dealer_won_battle);
        } else {
            // Player is the winner of the battle:
            isBattleStarted = false;
            myTotalCards = myTotalCards + hands[0].getCardCount() * 2; // there
            // are
            // two
            // hands
            // of
            // cards.
            pointHandsToNull();

            tempMessage = getString(R.string.cw_you_won_battle);
        } // end possibilities of the battle.
        updateStatus(tempMessage);
        updateTextViewsForTotals(myTotalCards, dealersTotalCards);
    } // end checkBattleWinner method.

    public void withdraw(View view) {
        isBattleStarted = false;
        isStarted = false;
        // Give remaining cards to the dealer:
        dealersTotalCards = dealersTotalCards + deck.cardsLeft();
        updateTextViewsForTotals(myTotalCards, dealersTotalCards);

        finalActions();
        enableOrDisableButtons();
    } // end withdraw method.

    public void finalActions() {
        // If the deck of cards is finished, or the games is not started because
        // a withdraw, let's see who won the war:
        if (deck.cardsLeft() == 0 || !isStarted) {
            isStarted = false;
            isBattleStarted = false;
            enableOrDisableButtons();

            if (dealersTotalCards > myTotalCards) {
                // Dealer is the final winner:
                moneyLost = moneyLost + dealer.currentBet;
                dealer.lose();

                tempMessage = getString(R.string.cw_dealer_won_the_war);
                tempMessage = String.format(tempMessage, "" + dealer.currentBet);

            } else if (myTotalCards > dealersTotalCards) {
                // Player is the winner:
                moneyWon = moneyWon + dealer.currentBet;
                dealer.win();

                tempMessage = getString(R.string.cw_you_won_the_war);
                tempMessage = String.format(tempMessage, "" + dealer.currentBet);

            } else {
                // It's a draw:
                dealer.draw();

                tempMessage = getString(R.string.cw_draw_war);

            } // end the 3 possibilities for winner.
            updateTextViewsForMoney();
            updateStatus(tempMessage);
            pointHandsToNull();

        } // end if the deck of cards is finished or withdraw.
    } // end finalActions method.

    public void pointHandsToNull() {
        // Point to null the two hands of cards, this way it's sure garbage
        // Collector can do useful things for us.:
        hands[0] = null;
        hands[1] = null;
    }

    public void drawHandsOfCards(int whose) {
        LinearLayout ll;

        // Find the corresponding linear layout, depending of whose turn is:
        // If is dealer hand of cards:
        if (whose == 0) {
            ll = findViewById(R.id.llDealerCards);
        } else {
            ll = findViewById(R.id.llPlayerCards);
        }

        hands[whose].drawCards(ll, whose);
    } // end draw hands of cards.

    // A method to update some TextViews:
    public void updateTextViewsForMoney() {

        // First change the tvMyMoney TextView:

        tvMyMoney.setText(String.format(strMyMoney, "" + Dealer.myTotalMoney));

        // Now change the tvMyBet TextView:
        tvMyBet.setText(String.format(strMyBet, "" + dealer.currentBet));

    } // end updateTextViews method.

    // A method to update TextViews for totals in hands:
    public void updateTextViewsForTotals(int myHand, int dealersHand) {

        // First change the tvMyBjTotal TextView:
        tvMyCwTotal.setText(String.format(strMyCwTotal, "" + myHand));

        // Now change the tvDealersCwTotal TextView:
        tvDealersCwTotal.setText(String.format(strDealersCwTotal, "" + dealersHand));
    } // end updateTextViews with totals in hands method.

    // A method to update the tvCardsWarStatus TextView:
    public void updateStatus(String text) {
        tvCwStatus.setText(text);
        speak.say(text, false);
    } // end updateBjStatus method.

    // A method to make buttons state active or inactive, depending of the
    // status of the game:
    public void enableOrDisableButtons() {

        // If is started or not:
        if (isStarted) {
            btCwBet.setEnabled(false);
            btCwNew.setEnabled(false);
            btCwFire.setEnabled(true);
            btCwWithdraw.setEnabled(true);
        } else {
            btCwBet.setEnabled(true);
            btCwNew.setEnabled(true);
            btCwFire.setEnabled(false);
            btCwWithdraw.setEnabled(false);
        } // end if is started or not.

        // If is sound or not, enable or disable the war sounds check box:
        cbtWarSounds.setEnabled(MainActivity.isSound);
    } // end enableOrDisableButtons.

    // A method to update periodically the TextViews:
    public void redrawTextViews() {
        updateTextViewsForMoney();

        updateTextViewsForTotals(myTotalCards, dealersTotalCards);
    } // end the method to update text views.

    // A method to check and uncheck war specific sounds:
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        Settings set = new Settings(this); // to save changes.

        // Check which check box was clicked
        switch (view.getId()) {
            case R.id.cbtWarSounds:
                MainActivity.isWarSounds = checked;
                set.saveBooleanSettings("isWarSounds", MainActivity.isWarSounds);
                // Set also the value of the object deck variable isDeckSound:
                // Only if deck is already an object:
                if (deck != null) {
                    deck.isDeckSounds = !MainActivity.isWarSounds; // if war sounds
                    // is true, deck
                    // sounds is
                    // false and
                    // vice_versa.
                } // end if deck is not null.

                break;

        } // end switch.
    } // end enable or disable war sounds in this game.

} // end CardsWarActivity.
