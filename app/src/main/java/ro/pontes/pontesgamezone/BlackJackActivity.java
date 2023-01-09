package ro.pontes.pontesgamezone;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import ro.pontes.pontesgamezone.ShakeDetector.OnShakeListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

public class BlackJackActivity extends Activity {

	// The following fields are used for the shake detection:
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private ShakeDetector mShakeDetector;
	// End fields declaration for shake detector.

	// Other variables:
	Context c;
	SpeakText speak;
	Dealer dealer;
	Deck deck;
	BlackjackHand[] blackjackHands;
	int whoIs; // whose turn is for the corresponding hand of blackjack.
	int whoWon; // who is the winner, to know what to do in a corresponding
				// function.
	boolean isStarted = false; // to know if a hand is started or not.
	boolean isBlackjack = false;
	boolean isDealerBusted = false; // to know it was a bust in some situations
									// is useful.
	Card dealerSecondCard; // to save the dealer's card when it is reversed.

	// For music background:
	SoundPlayer sndMusic;

	// For statistics:
	int numberOfPlayedHands = 0;
	int moneyWon = 0;
	int moneyLost = 0;

	// Let's have the tvBjStatus and other text views globally, not to
	// findViewById so often:
	TextView tvBjStatus;
	TextView tvMyMoney;
	TextView tvMyBet;
	TextView tvMyBjTotal;
	TextView tvDealersBjTotal;

	// Some global strings which are formated periodically:
	String tempMessage; // to use for updating the status.
	String strMyMoney;
	String strMyBet;
	String strMyBjTotal;
	String strDealersBjTotal;

	// Let's declare variables for all the buttons, we will use them to enable
	// or disable depending of the game status:
	Button btBjBet;
	Button btBjNew;
	Button btBjHit;
	Button btBjStand;
	Button btBjDouble;
	Button btBjSurrender;

	// For a timer:
	private Timer t;

	// Messages for handler to manage the interface:
	private static final int UPDATE_VIEWS_VIA_HANDLER = 1; // a message to be
															// sent to the
															// handler.
	private static final int UPDATE_BET_VIA_HANDLER = 2; // a message to be sent
															// to the handler to
															// say that the bet
															// must be again
															// lastBet after a
															// double.
	private static final int millisBeforeResetCurrentBet = 3000; // after
																	// blackjack
																	// hand,
																	// double or
																	// surrender
																	// we must
																	// reset the
																	// bet back
																	// to
																	// default
																	// bet:
																	// dealer.lastBet.

	// A static inner class for handler:
	static class MyHandler extends Handler {
		WeakReference<BlackJackActivity> bjActivity;

		MyHandler(BlackJackActivity aBjActivity) {
			bjActivity = new WeakReference<BlackJackActivity>(aBjActivity);
		}
	}

	// this handler will receive a delayed message
	private MyHandler mHandler = new MyHandler(this) {
		@Override
		public void handleMessage(Message msg) {
			// Do task here
			// BlackJackActivity theActivity = bjActivity.get();

			if (msg.what == UPDATE_VIEWS_VIA_HANDLER) {
				redrawTextViews(); // update text on text views.
				enableOrDisableButtons();
			}
			if (msg.what == UPDATE_BET_VIA_HANDLER) {
				// we must reset currentBet to lastBet after a blackjack, a
				// double down or a surrender.
				dealer.currentBet = dealer.lastBet;
			}
		}
	};

	// End handler stuff.

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Let's determine actual context to use it in many places:
		c = getApplicationContext();

		// Instantiate an SpeakText object:
		speak = new SpeakText(this);

		// Initialise the deck of cards:
		deck = new Deck(0, true, c); // false means without joker, true
										// means with sounds, context is
										// needed in constructor.

		// Initialise the dealer:
		dealer = new Dealer(this, true);

		// Initialise blackjackHands:
		blackjackHands = new BlackjackHand[2];

		blackjackHands[0] = new BlackjackHand(this); // dealer.
		blackjackHands[1] = new BlackjackHand(this); // player.

		// Let's initialise first things about blackjack:
		// Get the message from the intent
		Intent intent = getIntent();
		String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
		message.length();

		setContentView(R.layout.activity_black_jack);

		// Get the text views into their global variables:
		tvBjStatus = (TextView) findViewById(R.id.tvBjStatus);
		tvMyMoney = (TextView) findViewById(R.id.tvMyMoney);
		tvMyBet = (TextView) findViewById(R.id.tvMyBet);
		tvMyBjTotal = (TextView) findViewById(R.id.tvMyBjTotal);
		tvDealersBjTotal = (TextView) findViewById(R.id.tvDealersBjTotal);

		// Charge global strings from values strings:
		strMyMoney = getString(R.string.my_money);
		strMyBet = getString(R.string.my_bet);
		strMyBjTotal = getString(R.string.my_bj_total);
		strDealersBjTotal = getString(R.string.dealers_bj_total);

		// Get the buttons into their global variables, we will use them to
		// enable or disable periodically:
		btBjBet = (Button) findViewById(R.id.btBjBet);
		btBjNew = (Button) findViewById(R.id.btBjNew);
		btBjHit = (Button) findViewById(R.id.btBjHit);
		btBjStand = (Button) findViewById(R.id.btBjStand);
		btBjDouble = (Button) findViewById(R.id.btBjDouble);
		btBjSurrender = (Button) findViewById(R.id.btBjSurrender);

		// To be very fast changed the textViews.
		redrawTextViews(); // update text on text views.

		// To keep screen awake:
		if (MainActivity.isWakeLock) {
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} // end wake lock.

		// ShakeDetector initialisation
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mShakeDetector = new ShakeDetector();
		mShakeDetector.setShakeThresholdGravity(MainActivity.onshakeMagnitude);
		mShakeDetector.setOnShakeListener(new OnShakeListener() {

			@Override
			public void onShake(int count) {
				/*
				 * method you would use to setup whatever you want done once the
				 * device has been shook.
				 */
				handleShakeEvent(count);
			}
		});
		// End initialisation of the shake detector.
	} // end onCreate method.

	public void setTheTimer() {
		// Set the timer to send messages to the mHandler:
		t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						// Send a message to the handler:
						// This is to update the text views found in the upper
						// part of the screen:
						mHandler.sendEmptyMessageDelayed(
								UPDATE_VIEWS_VIA_HANDLER, 0); // 0 means the
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

		setTheTimer();

		if (MainActivity.isShake) {
			// Add the following line to register the Session Manager Listener
			// onResume
			mSensorManager.registerListener(mShakeDetector, mAccelerometer,
					SensorManager.SENSOR_DELAY_UI);
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
			Statistics.postStats("6", numberOfPlayedHands); // 6 is the id of
															// the Blackjack in
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
	public void onDestroy() {
		super.onDestroy();
		// ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM,
		// 100);
		// toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
	} // end on destroy method.

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.black_jack, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.mnuBjStatistics) {
			int ballance = moneyWon - moneyLost;

			double ballancePerHand = 0;
			if (numberOfPlayedHands > 0) {
				// We avoid division by 0:
				ballancePerHand = (double) ballance
						/ (double) numberOfPlayedHands;
				ballancePerHand = Math.round(ballancePerHand * 100.0) / 100.0;
			}
			String title = getString(R.string.title_statistics_alert);
			String message = String.format(
					getString(R.string.statistics_for_blackjack), ""
							+ numberOfPlayedHands, "" + moneyWon, ""
							+ moneyLost, "" + ballance, ballancePerHand);
			GUITools.alert(this, title, message);
		} // end if statistics is chosen in menu.
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

	public void handleShakeEvent(int count) {
		// It depends of the status of the game, started or not:
		if (isStarted) {
			takeCardActions(whoIs);
		} else {
			newBlackJackActions();
		}
	} // end handle shake detection.

	public void newBlackJack(View view) {
		newBlackJackActions();
	} // end new game when clicked on the button new.

	public void newBlackJackActions() {
		if (!isStarted) {

			// Increase number of hands:
			numberOfPlayedHands++;
			// Only if the add method of the Dealer class was processed
			// successfully:
			if (dealer.addBet()) {
				isStarted = true; // the game state machine is started.

				// Stop previously speaking:
				speak.stop();

				// If there are less than 10 cards in deck, we shuffle it again:
				if (deck.cardsLeft() < 10) {
					deck.shuffle();
				} // end if there are less than 10 cards left in our deck.

				// Add two hands of blackjack, the dealer and user ones::
				// The index of the hands array will point to another new
				// created object of type blackjackHands:
				blackjackHands[0] = new BlackjackHand(this); // dealer.
				blackjackHands[1] = new BlackjackHand(this); // player.

				// Other initial things:
				isBlackjack = false;
				isDealerBusted = false;

				// Give two cards to player, two to dealer:
				// The take method of this activity takes care of the messages,
				// if is first or second card of dealer etc:
				// first card to player:
				takeCardActions(1);
				// second card of player:
				takeCardActions(1);
				// first card of dealer:
				takeCardActions(0);
				// second card of dealer:
				takeCardActions(0);

				// It is user's first hand turn:
				this.whoIs = 1;

			} // end if the add method of the dealer class was processed
				// successfully.
		} // end if is not started.
	} // end actions for a new Blackjack game.

	public void changeBet(View view) {
		dealer.changeBet();
	} // end function change bet.

	public void takeCard(View view) {
		takeCardActions(whoIs);
	}

	public void takeCardActions(int whoIs) {
		if (isStarted) {
			// Extract a card from the deck:
			Card card = deck.dealCard();

			// Add it into the blackjackHand:
			blackjackHands[whoIs].addCard(card);

			// Reverse the second card of dealer, we save it in a global Card
			// variable of this class:
			if (whoIs == 0 && blackjackHands[0].getCardCount() == 2) {
				// Save the second card of the dealer, reverse it:
				dealerSecondCard = blackjackHands[0].getCard(1);
				// put the reversed one:
				blackjackHands[0].changeCard(1, new Card(0, 0, c)); // 0 0 are
																	// constructor
																	// parameters.
			} // end if is second card of dealer, reversion action.

			drawHandsOfCards(whoIs); // 0 dealer, greater player.

			// The message is different depending of the period of the game:
			if (whoIs == 0 && blackjackHands[0].getCardCount() == 2) {
				// silence, is the invisible card of the dealer.
			} else if (whoIs == 0 && blackjackHands[0].getCardCount() == 1) {
				// The visible card of dealer:
				if (!isBlackjack) {
					tempMessage = String.format(
							getString(R.string.msg_dealers_visible_card),
							card.toString());
					updateStatus(tempMessage);
				}
			} else if (whoIs == 1 && blackjackHands[1].getCardCount() == 1) {
				// First card of player, without announcing the total:
				tempMessage = card.toString() + ". "; // it is not necessary to
														// have a string
														// resource for this.
				updateStatus(tempMessage);
			} else {
				tempMessage = String.format(
						getString(R.string.msg_drawing_cards), card.toString(),
						"" + blackjackHands[whoIs].getBlackjackValue());
				updateStatus(tempMessage);
			}

			checkForBlackjack(whoIs);

			checkForBust(whoIs);

			finalActions();
		} // end if isStarted.
	} // end take card method.

	// A method which checks if one of the players busted:
	public void checkForBust(int whoIs) {
		if (blackjackHands[whoIs].getBlackjackValue() > 21) {
			if (whoIs == 0) {
				// Dealer has busted:
				tempMessage = getString(R.string.msg_dealer_has_busted);
				updateStatus(tempMessage);
				whoWon = 1;
				isDealerBusted = true;
				isStarted = false;
			} else {
				tempMessage = getString(R.string.msg_you_have_busted);
				updateStatus(tempMessage);
				whoWon = 0;
				isStarted = false;
			}
		}
	} // end check for busted method.

	// Check for blackJack:
	public void checkForBlackjack(int whoIs) {
		// If there are only 2 cards:
		if (whoIs > 0 && blackjackHands[whoIs].getCardCount() == 2) {
			if (blackjackHands[whoIs].getBlackjackValue() == 21) {
				tempMessage = getString(R.string.msg_you_had_blackjack);
				updateStatus(tempMessage);
				// Change the money payed by dealer:
				int tempWonMoney = 0;
				// Let round the amount if 50 percent is not an integer:
				if (dealer.currentBet % 2 == 0) {
					tempWonMoney = dealer.currentBet
							+ (dealer.currentBet * 1 / 2);
				} else {
					tempWonMoney = dealer.currentBet
							+ (dealer.currentBet * 1 / 2 + 1);
				}
				dealer.changeBetByForce(tempWonMoney);
				whoWon = 1;
				isBlackjack = true;
				isStarted = false;
				mHandler.sendEmptyMessageDelayed(UPDATE_BET_VIA_HANDLER,
						millisBeforeResetCurrentBet); // reset currentBet to
														// lastBet in dealer
														// object.
			} // end if total is 21.
		} // end if there are only two cards in hand.
	} // end check for blackjack.

	public void checkWhoIsGreater() {
		// It can be not started because dealer has busted:
		if (isStarted) {
			if (blackjackHands[0].getBlackjackValue() == blackjackHands[1]
					.getBlackjackValue()) {
				// It's a draw:
				whoWon = -1;
			} else if (blackjackHands[0].getBlackjackValue() < blackjackHands[1]
					.getBlackjackValue()) {
				// Player is the winner:
				whoWon = 1;
			} else {
				whoWon = 0;
			}

			isStarted = false;

			// Call the final actions method:
			finalActions();
		} // end if is started.
	} // end check who is greater.

	public void stand(View view) {
		standActions();
	} // end stand method called by the GUI button.

	public void standActions() {
		if (isStarted) {
			whoIs = 0; // dealer's turn:

			// Reverse back the second card of dealer:
			blackjackHands[0].changeCard(1, dealerSecondCard);
			drawHandsOfCards(0); // to be sure it is showed.
			// Announce it via TextToSpeech:
			tempMessage = String.format(
					getString(R.string.msg_second_dealer_card),
					dealerSecondCard.toString(),
					"" + blackjackHands[0].getBlackjackValue());
			updateStatus(tempMessage);

			// Dealer takes cards until 17:
			while (blackjackHands[0].getBlackjackValue() < 17) {
				takeCardActions(0);
			} // end while.

			// Check if dealer has not busted, to compare:
			if (!isDealerBusted) {
				checkWhoIsGreater();
			} // end if is not dealer busted.
		} // end if is started.
	} // end stand method.

	public void doubleDown(View view) {
		// If is started and is first 2 cards given:
		if (isStarted && blackjackHands[1].getCardCount() >= 2) {
			if (Dealer.myTotalMoney >= (dealer.currentBet * 2)) {
				dealer.changeBetByForce(dealer.currentBet * 2);
				mHandler.sendEmptyMessageDelayed(UPDATE_VIEWS_VIA_HANDLER, 0); // 0
																				// means
																				// the
																				// delay
																				// in
																				// milliseconds.
				// Take only one more card:
				takeCardActions(whoIs);

				// If I have busted, it means the game is not started anymore:
				// It means we must call the stand method:
				if (isStarted) {
					standActions();
				} // end if is started.
				mHandler.sendEmptyMessageDelayed(UPDATE_BET_VIA_HANDLER,
						millisBeforeResetCurrentBet);
			} // end if there are enough money to double the bet.
			else {
				// there are no enough money to double the bet:
				String title = getString(R.string.warning);
				String message = getString(R.string.not_enough_money_to_double);
				message = String.format(message, "" + Dealer.myTotalMoney);
				GUITools.alert(this, title, message);
			}
		} // end if is started.
	} // end doubleDown method.

	public void surrender(View view) {
		// Only if is a game started and there are two cards in player's hand:
		if (isStarted && blackjackHands[1].getCardCount() == 2) {
			// we change by force the bet to a half:
			dealer.changeBetByForce(dealer.currentBet / 2);
			isStarted = false;
			whoWon = 0;
			finalActions();
			mHandler.sendEmptyMessageDelayed(UPDATE_BET_VIA_HANDLER,
					millisBeforeResetCurrentBet); // reset bet to lastBet.
		} // end if is started.
	} // end surrender method.

	public void finalActions() {

		// Is not still started, final actions are truly required:
		if (!isStarted) {

			if (whoWon == 0) {
				tempMessage = String.format(
						getString(R.string.msg_blackjack_lost), ""
								+ dealer.currentBet);
				updateStatus(tempMessage);
				moneyLost = moneyLost + dealer.currentBet;
				dealer.lose();
			} else if (whoWon > 0) {
				tempMessage = String.format(
						getString(R.string.msg_blackjack_won), ""
								+ dealer.currentBet);
				updateStatus(tempMessage);
				moneyWon = moneyWon + dealer.currentBet;
				dealer.win();
			} else {
				tempMessage = getString(R.string.msg_blackjack_draw);
				updateStatus(tempMessage);
				dealer.draw();
			}

		} // end if is not started.

	} // end final action method.

	public void drawHandsOfCards(int whose) {
		LinearLayout ll;

		// Find the corresponding linear layout, depending of whose turn is:
		// If is dealer hand of cards:
		if (whose == 0) {
			ll = (LinearLayout) findViewById(R.id.llDealerCards);
		} else {
			ll = (LinearLayout) findViewById(R.id.llPlayerCards);
		}

		blackjackHands[whose].drawCards(ll, whose);
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
		tvMyBjTotal.setText(String.format(strMyBjTotal, "" + myHand));

		// Now change the tvDealersBjTotal TextView:
		tvDealersBjTotal.setText(String.format(strDealersBjTotal, ""
				+ dealersHand));
	} // end updateTextViews with totals in hands method.

	// A method to update the tvBlackjackStatus TextView:
	public void updateStatus(String text) {
		tvBjStatus.setText(text);
		speak.say(text, false);
	} // end updateBjStatus method.

	// A method to make buttons state active or inactive, depending of the
	// status of the game:
	public void enableOrDisableButtons() {

		// If is started or not:
		if (isStarted) {
			btBjBet.setEnabled(false);
			btBjNew.setEnabled(false);
			btBjHit.setEnabled(true);
			btBjStand.setEnabled(true);
			btBjDouble.setEnabled(true);
		} else {
			btBjBet.setEnabled(true);
			btBjNew.setEnabled(true);
			btBjHit.setEnabled(false);
			btBjStand.setEnabled(false);
			btBjDouble.setEnabled(false);
		} // end if is started or not.

		// The surrender button must be active only after the second card of
		// player:
		if (isStarted && blackjackHands[1].getCardCount() == 2) {
			btBjSurrender.setEnabled(true);
		} else {
			btBjSurrender.setEnabled(false);
		} // end for surrender button.
	} // end enableOrDisableButtons.

	// A method to update periodically through a handler the TextViews:
	public void redrawTextViews() {
		updateTextViewsForMoney();

		updateTextViewsForTotals(blackjackHands[1].getBlackjackValue(),
				blackjackHands[0].getBlackjackValue());

	} // end the method to update text views.

} // end BlackJackActivity class.
