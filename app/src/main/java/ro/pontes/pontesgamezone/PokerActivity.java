package ro.pontes.pontesgamezone;

/*
 * Class started on 19 October 2014, 23:52 by Manu
 * This activity is for a Poker game.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PokerActivity extends Activity {

    // The following fields are used for the shake detection:
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    // End fields declaration for shake detector.

    // For music background:
    SoundPlayer sndMusic;

    // For hands of cards:
    PokerHand[] hands;

    // Other variables:
    Context c;
    SpeakText speak;
    Dealer dealer;
    Deck deck = null;
    int whoIs = 1; // whose turn is for the corresponding hand of cards war.
    long dealersScore = 0;
    long myScore = 0;
    boolean isStarted = false; // to know if a hand is started or not.
    boolean isDrawPhase = false;
    int sortType = 1; // 1 means by value, 2 by colour, 0 without sorting.
    String myHand; // the type of hand as String, calculated in updateTextViews.
    String dealersHand;
    ArrayList<Card> discardedIndexes = new ArrayList<>();

    // For statistics:
    int numberOfPlayedHands = 0;
    int moneyWon = 0;
    int moneyLost = 0;

    // Let's have the tvPkStatus and other text views globally, not to
    // findViewById so often:
    TextView tvPkStatus;
    TextView tvMyMoney;
    TextView tvMyBet;
    TextView tvMyPkHand;
    TextView tvDealersPkHand;

    // An array for player's images, to be easy their changes:
    ImageView[] mImages;

    // Some global strings which are formated periodically:
    String tempMessage; // to use for updating the status.
    String strDiscard; // for the Draw button, when there are not cards to
    // discard.
    String strDiscardX; // for the Draw button, when are x cards to be
    // discarded.
    String strMyMoney;
    String strMyBet;
    String strMyPkHand;
    String strPkNothing; // for the moment when nothing is in hand.
    String strDealersPkHand;
    String strCardDiscarded;
    String strCardNotDiscarded;
    String resPossesionString; // for content description: Dealer is 0, player
    // is 1.
    String[] aPossesion; // the array which contains at 0 Dealer, 1 player.
    String[] aHandsTypes; // an array which contains kind of hands in poker.
    String[] aPkAboutHand; // an array for TTS, when announcing: Your hand,
    // Dealer's hand.

    // We need the context as this class in some listeners as final variable:
    final Context mFinalContext = this;

    // Let's declare variables for all the buttons, we will use them to enable
    // or disable depending of the game status:
    Button btPkBet;
    Button btPkNew;
    Button btPkDiscard;
    Button btPkStand;
    Button btPkRaise;
    Button btPkFold;

    // We need a global alertToShow to be possible to work with it from other
    // point, when IME_DONE button of the keyboard is used:
    private AlertDialog alertToShow;

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
    private static final int SAY_ALL_PLAYER_CARDS = 2; // a message to be send
    // when we want to be
    // spoken all the
    // player's cards.

    // A static inner class for handler:
    static class MyHandler extends Handler {
        WeakReference<PokerActivity> pkActivity;

        MyHandler(PokerActivity aPkActivity) {
            pkActivity = new WeakReference<>(aPkActivity);
        }
    } // end static class for handler.

    // this handler will receive a delayed message
    private final MyHandler mHandler = new MyHandler(this) {
        @Override
        public void handleMessage(Message msg) {
            // Do task here
            // PokerActivity theActivity = pkActivity.get();

            if (msg.what == UPDATE_VIEWS_VIA_HANDLER) {
                redrawTextViews(); // update text on text views.
            } else if (msg.what == SAY_ALL_PLAYER_CARDS) {
                sayAllCards(1);
            }

        }
    };

    // End handler stuff.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poker);

        // Let's determine actual context to use it in many places:
        c = getApplicationContext();
        // Instantiate an SpeakText object:
        speak = new SpeakText(this);

        // Initialise the dealer:
        dealer = new Dealer(this, true);

        // Initialise the deck of cards:
        deck = new Deck(0, true, c); // false means without joker, second
        // false means deck without sound,
        // context is needed in constructor.

        // Initialise the hands:
        hands = new PokerHand[2];
        hands[0] = new PokerHand(this);
        hands[1] = new PokerHand(this);

        // Get the text views into their global variables:
        tvPkStatus = findViewById(R.id.tvPkStatus);
        tvMyMoney = findViewById(R.id.tvMyMoney);
        tvMyBet = findViewById(R.id.tvMyBet);
        tvMyPkHand = findViewById(R.id.tvMyPkHand);
        tvDealersPkHand = findViewById(R.id.tvDealersPkHand);

        // Charge global strings from values strings:
        strDiscardX = getString(R.string.pk_discard_x); // x is the number of
        // cards discarded, a
        // text for this state
        // of the button, like
        // Draw 3.
        strDiscard = getString(R.string.pk_discard); // the simple phase of the
        // Draw button, when no
        // card is selected to
        // be discarded.
        strMyMoney = getString(R.string.my_money);
        strMyBet = getString(R.string.my_bet);
        strMyPkHand = getString(R.string.my_pk_hand);
        strDealersPkHand = getString(R.string.dealers_pk_hand);
        strPkNothing = getString(R.string.pk_nothing);
        strCardDiscarded = getString(R.string.pk_card_discarded);
        strCardNotDiscarded = getString(R.string.pk_card_not_discarded);

        Resources res = getResources();
        aPossesion = res.getStringArray(R.array.possesion_array);
        resPossesionString = res.getString(R.string.card_name_extended);
        // The aPossesion will contain at index 1 the curNickname, not your
        // until now:
        aPossesion[1] = MainActivity.curNickname;
        aHandsTypes = res.getStringArray(R.array.pk_hands_array);
        aPkAboutHand = res.getStringArray(R.array.pk_about_hand_array);

        // Get the buttons into their global variables, we will use them to
        // enable or disable periodically:
        btPkBet = findViewById(R.id.btPkBet);
        btPkNew = findViewById(R.id.btPkNew);
        btPkDiscard = findViewById(R.id.btPkDiscard);
        btPkStand = findViewById(R.id.btPkStand);
        btPkRaise = findViewById(R.id.btPkRaise);
        btPkFold = findViewById(R.id.btPkFold);

        // Redraw for first time, to have nothing as totals in hands and the bet
        // and wallet:
        // The money line is redrawn also in onResume because we come sometimes
        // from bar:
        redrawTextViews();
        enableOrDisableButtons();

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

    } // end on create method.

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

        // Only if there are hands played, post the statistics:
        if (numberOfPlayedHands > 0) {
            Statistics.postStats("10", numberOfPlayedHands); // 10 is the id of
            // the cards
            // Poker in
            // soft_counts
            // table in DB.
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
        getMenuInflater().inflate(R.menu.poker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        // Statistics alert if chosen from this game menu:
        if (id == R.id.mnuPkStatistics) {
            int ballance = moneyWon - moneyLost;

            double ballancePerHand = 0;
            if (numberOfPlayedHands > 0) {
                // We avoid division by 0:
                ballancePerHand = (double) ballance / (double) numberOfPlayedHands;
                ballancePerHand = Math.round(ballancePerHand * 100.0) / 100.0;
            }
            String title = getString(R.string.title_statistics_alert);
            String message = String.format(getString(R.string.statistics_for_poker), "" + numberOfPlayedHands, "" + moneyWon, "" + moneyLost, "" + ballance, "" + ballancePerHand);
            GUITools.alert(this, title, message);
        } // end statistics alert.
        else if (id == R.id.mnuMoneyWonAsBonus) {
            GUITools.showBonusStatistics(this, Dealer.curMoneyWonAsBonus);
        } // end if bonus statistics is chosen in menu.
        else if (id == R.id.mnuPkSortByValue) {
            sortType = 1;
            sortHandsOfCards(sortType, true);
        } else if (id == R.id.mnuPkSortByColor) {
            sortType = 2;
            sortHandsOfCards(sortType, true);
        } else if (id == R.id.mnuPkSortByNothing) {
            sortType = 0;
            sortHandsOfCards(sortType, true);
        } // end sorting options in menu.
        else if (id == R.id.mnuHelp) {
            GUITools.openHelp(this);
        } // end help option in menu.

        // Other options in menu, first is Saloon:

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

    // Methods for buttons in layout:

    public void handleShakeEvent(int count) {
        // It depends of the status of the game, started or not:
        if (isStarted) {
            if (isDrawPhase && discardedIndexes.size() > 0) {
                discardActions();
            } // end if shake means discard cards.
            else if (discardedIndexes.size() == 0) {
                showdownActions();
            } // end if shake means show down.
        } else {
            newHandActions();
        }
    } // end handle shake detection.

    public void changeBet(View view) {
        dealer.changeBet();
    } // end function change bet.

    public void newHand(View view) {
        newHandActions();
    } // end new hand method.

    public void newHandActions() {
        if (!isStarted) {

            if (dealer.addBet()) {
                isStarted = true;

                // Stop previously speaking:
                speak.stop();

                // Increase number of hands:
                numberOfPlayedHands++;

                // If there are less than 10 cards in deck, we shuffle it again:
                if (deck.cardsLeft() < 10) {
                    deck.shuffle();
                } // end if there are less than 10 cards left in our deck.

                enableOrDisableButtons();

                // Initiate the two hands, just to have the possibility to clean
                // the layouts for cards:
                // The hands are empty, this way the draw method will draw
                // nothing, but will clear the remaining cards from last war:
                hands[0].clear();
                hands[1].clear();
                drawHandsOfCards(0); // clear the dealer's cards.
                drawHandsOfCards(1); // clear my cards.

                // Empty also the discardedIndexes:
                discardedIndexes.clear();

                // Point also to null the mImages array:
                mImages = null;

                // Show and say about this start:
                tempMessage = getString(R.string.pk_started);
                updateStatus(tempMessage);

                // Let's take five cards for user, the first 5 cards of the
                // hand, before discarding:
                whoIs = 1; // user's turn.
                for (int i = 0; i < 5; i++) {
                    takeCardActions(whoIs);
                }

                // Let's sort them if necessary:
                if (sortType == 1) {
                    sortHandOfCardsByValue(whoIs, false);
                } else if (sortType == 2) {
                    sortHandOfCardsByColor(whoIs, false);
                } // end sort cards in player hand if necessary.

                // let's draw them:
                drawHandsOfCards(whoIs); // 0 dealer, greater player.

                // Let's set the listener for draw phase, to be possible the
                // choosing of the discarded cards:
                setImagesListeners(whoIs);

                // Let's see on the screen what we have in hand, what types of
                // hands:
                redrawTextViews();

                // Say now all extracted cards:
                sayAllCards(whoIs);

                // We can set the flag isDrawPhase to true:
                isDrawPhase = true;

                // Now we show also the reverse of the dealer's cards:
                whoIs = 0;
                for (int i = 0; i < 5; i++) {
                    hands[whoIs].addCard(new Card(0, 0, c));
                } // end for add five reverses of cards for dealer.
                // let's draw them:
                drawHandsOfCards(whoIs); // 0 dealer, greater player.

                enableOrDisableButtons();
            } // end if add bet method was processed correctly.
        } // end if is not started.
    } // end newHandActions method.

    public void takeCardActions(int whoIs) {
        if (isStarted) {
            // Extract a card from the deck:
            Card card = deck.dealCard();

            // Add it into the poker hand:
            hands[whoIs].addCard(card);

        }// end if isStarted is true.
    } // end take card method.

    // A method to sort the hands:
    public void sortHandOfCardsByValue(int whoIs, boolean isSortSound) {
        hands[whoIs].sortByValue();
        drawHandsOfCards(whoIs);
        if (isSortSound) {
            SoundPlayer.playSimple(c, "sort_descendent");
        }
    } // end sort the hand of cards by value.

    public void sortHandOfCardsByColor(int whoIs, boolean isSortSound) {
        hands[whoIs].sortBySuit();
        drawHandsOfCards(whoIs);
        if (isSortSound) {
            SoundPlayer.playSimple(c, "sort_color");
        }
    } // end sort the hand of cards by value.

    public void sortHandOfCardsByNothing(int whoIs, boolean isSortSound) {
        hands[whoIs].shuffle();
        drawHandsOfCards(whoIs);
        if (isSortSound) {
            SoundPlayer.playSimple(c, "sort_shuffle");
        }
    } // end sort the hand of cards by nothing, shuffle them in hand.

    // / A method for discarding clicking the corresponding button:
    public void discard(View view) {
        discardActions();
    } // end discard method for the button.

    // A method for discard actions selected cards, the draw round of the game:
    public void discardActions() {
        isDrawPhase = false;
        enableOrDisableButtons();

        whoIs = 1; // user's turn.
        // Let's remove the discarded cards:
        for (int i = 0; i < discardedIndexes.size(); i++) {
            hands[whoIs].removeCard(discardedIndexes.get(i)); // discardedIndexes
            // contains
            // Cards
            // objects.
        } // end for remove the chosen cards from hand of player.

        // Let's take new cards for user.
        // We need to take from deck as many cards as we discarded:
        for (int i = 0; i < discardedIndexes.size(); i++) {
            takeCardActions(whoIs);
        }

        // Let's sort them if necessary:
        if (sortType == 1) {
            sortHandOfCardsByValue(whoIs, false);
        } else if (sortType == 2) {
            sortHandOfCardsByColor(whoIs, false);
        } // end sort cards in player hand if necessary.

        // let's draw them:
        drawHandsOfCards(whoIs); // 0 dealer, greater player.

        // Redraw the text views, it is almost sure the hand type was changed
        // for player:
        redrawTextViews();

        // Empty the discardedIndexes, not to have the illusion we have cards to
        // discard anymore:
        discardedIndexes.clear();

        // Announce the new hand of the player:
        sayAllCards(whoIs);

        enableOrDisableButtons();
        // Show a message in the status zone:
        updateStatus(getString(R.string.pk_discarded));
    } // end discard method.

    // A method for raise:
    public void raise(View view) {
        // This is only in draw phase if is started:
        if (isStarted && isDrawPhase) {
            // A string to get from resource the texts:
            String tempMessage;
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            // We need the current object, the word this to be used in a
            // listener as a final variable:

            // The title:
            tempMessage = getString(R.string.pk_raise_title);
            alert.setTitle(tempMessage);

            // The body:
            tempMessage = getString(R.string.pk_raise_message);
            alert.setMessage(tempMessage);

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setText("" + dealer.currentBet);
            // Add also an action listener:
            input.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Next two lines are also found in the listener of the
                    // OK button of this alert dialog:
                    String tempBet = input.getText().toString();
                    checkAndChangeBet(tempBet);
                    alertToShow.dismiss();
                }
                return false;
            });
            // End add action listener for the IME done button of the keyboard..

            alert.setView(input);

            alert.setPositiveButton("Ok", (dialog, whichButton) -> {
                // Next to lines are also above for listener of the
                // IME_DONE button of the keyboard.
                String tempBet = input.getText().toString();
                checkAndChangeBet(tempBet);
                // Alina
            });

            alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
                // cancelled.
            });

            alertToShow = alert.create();
            alertToShow.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            alertToShow.show();
            // end of alert dialog with edit sequence.

        } // end if is started and is draw phase.
    } // end raise method.

    // A method to be used after raise, to check if OK button or IME_DONE were
    // pressed:
    public void checkAndChangeBet(String tempBet) {

        if (Dealer.isNumeric(tempBet)) {
            // We extract from here the result if is a numeric value::
            int newBet = Integer.parseInt(tempBet);
            newBet = Math.abs(newBet);

            // Check if this new attempt is greater than total money:
            if ((dealer.currentBet + newBet) > Dealer.myTotalMoney) {
                String title = getString(R.string.warning);
                String message = getString(R.string.not_enough_money_to_bet);
                message = String.format(message, MainActivity.curNickname, "" + (dealer.currentBet + newBet), "" + Dealer.myTotalMoney);
                if (Dealer.myTotalMoney <= 0) {
                    // You can play just for fun:
                    message = String.format(getString(R.string.play_just_for_fun), MainActivity.curNickname);
                }

                GUITools.alert(mFinalContext, title, message);
            } // end if is a greater value than 0.
            else if (newBet < dealer.currentBet) {
                String title = getString(R.string.warning);
                String message = getString(R.string.pk_not_enough_raise);
                message = String.format(message, "" + dealer.currentBet);
                GUITools.alert(mFinalContext, title, message);
            } else {
                dealer.changeBetByForce(dealer.currentBet + newBet);
                SoundPlayer.playSimple(mFinalContext, "bet_money");
            }

        } // end if is a numeric value inserted in edit text.
    } // end method to check and change bet through raise.

    // A method to fold:
    public void fold(View view) {
        // Only if the hand is started:
        if (isStarted) {

            // Redraw the player cards if the fold button was pressed without
            // discarding, not to have cards as buttons:
            if (isDrawPhase) {
                drawHandsOfCards(1);
            }
            isDrawPhase = false; // maybe is a temporal statement, this must be
            // in discard method only.
            dealerPlays();
            isStarted = false;

            // We lost the money player has in the pot:
            dealer.lose();
            updateTextViewsForMoney();
            String temp = getString(R.string.pk_fold_message);
            updateStatus(temp);

            enableOrDisableButtons();
        } // end if is started.
    } // end fold method.

    // A method for show down clicking the button:
    public void showdown(View view) {
        showdownActions();
    } // end show down method for the corresponding button.

    // A method to stand, show the cards to dealer, the show down point:
    public void showdownActions() {

        // Redraw the player cards if the show down button was pressed without
        // discarding, not to have cards as buttons:
        if (isDrawPhase) {
            drawHandsOfCards(1);
        }

        isDrawPhase = false; // maybe is a temporal statement, this must be in
        // discard method only.
        dealerPlays();

        // We call the final function which checks who won the hand:
        isStarted = false;
        finalActions();

        enableOrDisableButtons();
    } // end show down method.

    // A method for dealer to play its hand:
    public void dealerPlays() {

        // Let's take five cards for dealer, the first 5 cards of the hand,
        // before discarding:
        whoIs = 0; // dealer's turn.
        // First we need to send to garbage those 5 reverses of cards:
        hands[whoIs].clear();

        for (int i = 0; i < 5; i++) {
            takeCardActions(whoIs);
        }

        // Let's sort them if necessary:
        if (sortType == 1) {
            sortHandOfCardsByValue(whoIs, false);
        } else if (sortType == 2) {
            sortHandOfCardsByColor(whoIs, false);
        } // end sort cards in dealer's hand if necessary.

        // / let's draw them:
        drawHandsOfCards(whoIs); // 0 dealer, greater player.

        // Let's see on the screen what we have in hand, what types of hands:
        updateTextViewsForTotals(hands[1].getPokerValue(), hands[0].getPokerValue());

        // Say now all extracted cards by dealer:
        sayAllCards(whoIs);

    } // end method for dealer to play.

    // The final actions method, who won etc:
    public void finalActions() {
        // If the hand is finished:
        if (!isStarted) {
            // without a reason.
            enableOrDisableButtons();

            // We change the values of the scores, this is because 0 is Straight
            // flush, but we want to be greater than 8 which is now One pair.
            // 8 becomes 0 and 0 becomes 8, this is because there are 9 types of
            // hands, from 0 to 8:
            myScore = 9 - myScore;
            dealersScore = 9 - dealersScore;

            // Determine which hand is better if type of hand is the same:
            if ((myScore == dealersScore) && myScore > 0) {
                myScore = hands[1].getRelativeRankOfAType();
                dealersScore = hands[0].getRelativeRankOfAType();
            } // end if both hands have the same type, detect whose is better.

            // // GUITools.toast(""+myScore+", "+dealersScore, 1000, c);

            if (dealersScore > myScore) {
                // Dealer is the final winner:
                moneyLost = moneyLost + dealer.currentBet;
                dealer.lose();

                tempMessage = getString(R.string.pk_dealer_won_the_hand);
                tempMessage = String.format(tempMessage, "" + dealer.currentBet);

            } else if (myScore > dealersScore) {
                // Player is the winner:
                moneyWon = moneyWon + dealer.currentBet;
                dealer.win();

                tempMessage = getString(R.string.pk_you_won_the_hand);
                tempMessage = String.format(tempMessage, "" + dealer.currentBet);

            } else {
                // It's a draw:
                dealer.draw();

                tempMessage = getString(R.string.pk_draw_hand);

            } // end the 3 possibilities for winner.
            dealer.currentBet = dealer.lastBet; // in chase it was a raise
            // before.
            updateTextViewsForMoney();
            updateStatus(tempMessage);

        } // end if the set is finished or withdraw.

    } // end final actions method.

    public void sortHandsOfCards(int sortType, boolean isSortSound) {
        if (isDrawPhase && discardedIndexes.size() > 0) {
            // Say that's not allowed the sort in draw phase when there are
            // cards to be discarded:
            String tempTitle = getString(R.string.warning);
            String tempMessage = getString(R.string.pk_sort_not_allowed);
            GUITools.alert(this, tempTitle, tempMessage);
        } else {
            // About sortType: 0 means not sorted, 1 sort by value, 2 sort by
            // colour.
            if (sortType == 0) {
                // The cards in hand are shuffled:
                sortHandOfCardsByNothing(1, isSortSound);
                sortHandOfCardsByNothing(0, false); // otherwise we would have
                // two sounds played in same
                // time.
            } else if (sortType == 1) {
                // The cards in hand are sorted by value:
                sortHandOfCardsByValue(1, isSortSound);
                sortHandOfCardsByValue(0, false); // otherwise we would have two
                // sounds played in same
                // time.
            } else if (sortType == 2) {
                // The cards in hand are sorted by colour:
                sortHandOfCardsByColor(1, isSortSound);
                sortHandOfCardsByColor(0, false); // otherwise we would have two
                // sounds played in same
                // time.
            }

            // Announce the new order of the cards via handler:
            mHandler.sendEmptyMessageDelayed(SAY_ALL_PLAYER_CARDS, 150); // x
            // means
            // the
            // delay
            // in
            // milliseconds.

            // Now, if the phase is for discard, we must set again the listeners
            // for player's images:
            if (isDrawPhase) {
                setImagesListeners(1);
            }
        } // end if sort is allowed, not draw phase with cards to discard.
    } // end sort hands of cards.

    // A method to draw the cards in hands:
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

    // A method to update TextViews for hands type:
    public void updateTextViewsForTotals(int myNumericTypeOfHand, int dealersNumericTypeOfHand) {

        // We update also the dealer's and player's score:
        myScore = myNumericTypeOfHand;
        dealersScore = dealersNumericTypeOfHand;

        // Detect if it's nothing, the value returned by getPokerValue method of
        // the pokerHand class is 9:
        if (myNumericTypeOfHand > 8) {
            myHand = strPkNothing;
        } else {
            myHand = aHandsTypes[myNumericTypeOfHand];
        }
        if (dealersNumericTypeOfHand > 8) {
            dealersHand = strPkNothing;
        } else {
            dealersHand = aHandsTypes[dealersNumericTypeOfHand];
        }

        // First change the tvMyPkHand TextView:
        tvMyPkHand.setText(String.format(strMyPkHand, "" + myHand));

        // Now change the tvDealersPkHand TextView:
        tvDealersPkHand.setText(String.format(strDealersPkHand, "" + dealersHand));
    } // end updateTextViews with hands type method.

    // A method to update the tvPokerStatus TextView:
    public void updateStatus(String text) {
        tvPkStatus.setText(text);
        speak.say(text, false);
    } // end updateBjStatus method.

    // A method to make buttons state active or inactive, depending of the
    // status of the game:
    public void enableOrDisableButtons() {

        // If is started or not:
        if (isStarted) {
            btPkBet.setEnabled(false);
            btPkNew.setEnabled(false);
            btPkFold.setEnabled(true);
        } else {
            btPkBet.setEnabled(true);
            btPkNew.setEnabled(true);
            btPkRaise.setEnabled(false);
            btPkFold.setEnabled(false);
        } // end if is started or not.

        // If is phase of discarding and there are cards to discard, enable the
        // button for Draw:
        if (isDrawPhase && discardedIndexes.size() > 0) {
            btPkDiscard.setEnabled(true);
            btPkDiscard.setText(String.format(strDiscardX, "" + discardedIndexes.size()));
        } else {
            btPkDiscard.setText(strDiscard);
            btPkDiscard.setEnabled(false);
        } // end about discard / draw button.

        // If the game is started and there are not cards to be discarded,
        // enable the stand / Show down button:
        // end about stand / show down button.
        btPkStand.setEnabled(isStarted && discardedIndexes.size() == 0);

        // The raise button is available only in discarding phase:
        btPkRaise.setEnabled(isDrawPhase);
    } // end enableOrDisableButtons.

    // A method to update periodically the TextViews:
    public void redrawTextViews() {
        updateTextViewsForMoney();

        updateTextViewsForTotals(hands[1].getPokerValue(), hands[0].getPokerValue());
    } // end the method to update text views.

    // A method to say all cards in a hand:
    public void sayAllCards(int whose) {
        String tempMessage = String.format(aPkAboutHand[whose], hands[whose].cardsToString(), aHandsTypes[hands[whose].getAbsoluteRank()]);
        speak.say(tempMessage, false);
    } // end say all cards in a hand.

    // A method to set listeners to player's cards images:
    public void setImagesListeners(int whose) {
        // The base id for dealer is 1000, it means the cards will be from 1000
        // to 1004:
        int baseId = 1000;
        // If is about player cards, the baseId will be 2000, the IDs from 2000
        // to 2004:
        if (whose > 0) {
            baseId = 2000;
        }
        // We make the array for images, to have them for work:
        mImages = new ImageView[hands[whose].getCardCount()];
        for (int i = 0; i < hands[whose].getCardCount(); i++) {
            final int imageId = baseId + i;
            mImages[i] = findViewById(imageId);
            mImages[i].setOnClickListener(view -> cardImageClicked(imageId));
        } // end for.
    } // end set images listeners.

    // A method which will fire when an image is clicked in draw phase of the
    // Poker game:
    public void cardImageClicked(int which) {
        // We do the index of the card which is the which minus 1000 or 2000:
        int cardIndex;
        int whose = 1;
        if (which >= 2000) {
            cardIndex = which - 2000;
            // my cards images.
        } else {
            cardIndex = which - 1000;
            whose = 0; // dealer's cards images.
        }
        Card tempC = hands[1].getCard(cardIndex);
        if (isInArrayList(tempC)) {
            speak.say(String.format(strCardNotDiscarded, tempC.toString()), true);
            // Set the background to white:
            mImages[cardIndex].setBackgroundColor(Color.WHITE);
            String tempS = String.format(resPossesionString, aPossesion[whose], tempC);
            mImages[cardIndex].setContentDescription(tempS);
            discardedIndexes.remove(tempC);
        } else {
            String tempS = String.format(strCardDiscarded, tempC.toString());
            speak.say(tempS, true);
            // Set the background to dark red:
            mImages[cardIndex].setBackgroundColor(Color.rgb(139, 0, 0));
            mImages[cardIndex].setContentDescription(tempS);
            discardedIndexes.add(tempC);
        }

        enableOrDisableButtons();

        // GUITools.toast(""+discardedIndexes.size(), 1000, c);
    } // end method for a card image clicked.

    // A method which searches for a value in the ArrayList for
    // discardedIndexes:
    public boolean isInArrayList(Card card) {
        boolean isInArrayList = false;

        for (int i = 0; i < discardedIndexes.size(); i++) {
            if (discardedIndexes.get(i) == card) {
                isInArrayList = true;
                break;
            }
        } // end for.

        return isInArrayList;
    } // end detect if an element is present in an ArrayList.

} // end poker activity.
