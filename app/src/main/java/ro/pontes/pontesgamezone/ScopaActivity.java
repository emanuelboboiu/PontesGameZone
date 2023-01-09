package ro.pontes.pontesgamezone;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import ro.pontes.pontesgamezone.ShakeDetector.OnShakeListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class ScopaActivity extends Activity {

	// The following fields are used for the shake detection:
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private ShakeDetector mShakeDetector;
	// End fields declaration for shake detector.

	// For music background:
	private SoundPlayer sndMusic;

	// For hands of cards:
	private ScopaHand[] hands;

	// Other variables:
	private Context c;
	private final Context finalContext = this;
	private SpeakText speak;
	private Dealer dealer;
	private Deck deck;
	private History mHistory;
	private int whoIs = 1; // whose turn is for the corresponding hand of Scopa.
	private boolean isStarted = false; // to know if a hand is started or not.
	private int[] aLsTotals;
	private boolean toBeNewRound = true;
	private boolean toGiveCards = true;
	private int lastTaker = 0; // to know who took the last from the table.
	private int whoStarts = 1; // first round is player.
	public static int lsVariant = 1; // 1 means Scopa, 2 Escoba, 3 Scopone.

	// For statistics:
	private int numberOfPlayedHands = 0;
	private int moneyWon = 0;
	private int moneyLost = 0;

	// Let's have the tvLsStatus and other text views globally, not to
	// findViewById so often:
	TextView tvLsStatus;
	TextView tvMyMoney;
	TextView tvMyBet;
	TextView tvMyLsTotal;
	TextView tvDealersLsTotal;

	// An array for player's images, to be easy their changes:
	ImageView[] mImages;

	// Some global strings which are formated periodically:
	private String tempMessage; // to use for updating the status.
	private String strMyMoney;
	private String strMyBet;
	private String strMyLsTotal;
	private String strDealersLsTotal;
	private String strLsTakenCardsMessage;
	private String strLsCardPutMessage;
	private String strLsScopaEvent;
	private String strLsNewRound;

	private String[] aPossesion; // the array which contains at 0 Dealer, 1
									// player.
	private String[] aLsAboutHand; // an array for TTS.

	// Let's declare variables for all the buttons, we will use them to enable
	// or disable depending of the game status:
	private Button btLsBet;
	private Button btLsNew;
	private Button btLsAbandon;
	private Button btLsSayAllVisibleCards;
	private Button btLsHistory;

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
	private static final int MAKE_DEALER_MOVE = 2; // a message to be send if we
													// want the dealer to put a
													// card down.
	private static final int NEW_ROUND = 3; // to start a new round after the
											// deck is finished.
	private static final int GIVE_CARDS = 4; // to give cards when hands are
												// empty.
	private static final int UPDATE_STATUS_VARIANT_CHOSEN = 5; // to change the
																// status about
																// a variant
																// chosen.

	// A static inner class for handler:
	static class MyHandler extends Handler {
		WeakReference<ScopaActivity> lsActivity;

		MyHandler(ScopaActivity aLsActivity) {
			lsActivity = new WeakReference<ScopaActivity>(aLsActivity);
		}
	} // end static class for handler.

	// this handler will receive a delayed message
	private MyHandler mHandler = new MyHandler(this) {
		@Override
		public void handleMessage(Message msg) {
			// Do task here
			// ScopaActivity theActivity = lsActivity.get();

			if (msg.what == UPDATE_VIEWS_VIA_HANDLER) {
				redrawTextViews(); // update text on text views.
			}
			if (msg.what == MAKE_DEALER_MOVE) {
				dealerPlays();
			}
			if (msg.what == NEW_ROUND) {
				newRound();
			}
			if (msg.what == GIVE_CARDS) {
				giveCards();
			}
			if (msg.what == UPDATE_STATUS_VARIANT_CHOSEN) {
				updateStatus(tempMessage);
			}
		}
	};

	// End handler stuff.

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scopa);

		// Let's determine actual context to use it in many places:
		c = getApplicationContext();
		// Instantiate an SpeakText object:
		speak = new SpeakText(this);

		// Initialise the dealer:
		dealer = new Dealer(this, true);

		// Initialise the hands:
		hands = new ScopaHand[5];
		hands[0] = new ScopaHand(this);
		hands[1] = new ScopaHand(this);
		hands[2] = new ScopaHand(this);
		hands[3] = new ScopaHand(this);
		hands[4] = new ScopaHand(this);

		// Get the text views into their global variables:
		tvLsStatus = (TextView) findViewById(R.id.tvLsStatus);
		tvMyMoney = (TextView) findViewById(R.id.tvMyMoney);
		tvMyBet = (TextView) findViewById(R.id.tvMyBet);
		tvMyLsTotal = (TextView) findViewById(R.id.tvMyLsHand);
		tvDealersLsTotal = (TextView) findViewById(R.id.tvDealersLsHand);

		// Charge global strings from values strings:
		strMyMoney = getString(R.string.my_money);
		strMyBet = getString(R.string.my_bet);
		strMyLsTotal = getString(R.string.my_ls_score);
		strDealersLsTotal = getString(R.string.dealers_ls_score);
		strLsTakenCardsMessage = getString(R.string.ls_taken_cards_message);
		strLsCardPutMessage = getString(R.string.ls_card_put_message);
		strLsScopaEvent = getString(R.string.ls_scopa_event);
		strLsNewRound = getString(R.string.ls_new_round);

		Resources res = getResources();
		aPossesion = res.getStringArray(R.array.ls_subject_array);
		// The aPossesion will contain at index 1 the curNickname, not "your"
		// until now:
		aPossesion[1] = MainActivity.curNickname;
		aLsAboutHand = res.getStringArray(R.array.ls_hands_array);

		// Get the buttons into their global variables, we will use them to
		// enable or disable periodically:
		btLsBet = (Button) findViewById(R.id.btLsBet);
		btLsNew = (Button) findViewById(R.id.btLsNew);
		btLsAbandon = (Button) findViewById(R.id.btLsAbandon);
		btLsSayAllVisibleCards = (Button) findViewById(R.id.btLsSayAllVisibleCards);
		btLsHistory = (Button) findViewById(R.id.btLsHistory);

		// Initiate the array for totals:
		aLsTotals = new int[] { 0, 0, 0 };

		// Redraw for first time, to have nothing as totals in hands and the bet
		// and wallet:
		// The money line is redrawn also in onResume because we come sometimes
		// from bar:
		redrawTextViews();
		enableOrDisableButtons();

		myTempBet = dealer.currentBet; // we check in setTimer if it is changed,
										// to update text views for dealer.

		// Initialise the history object:
		mHistory = new History(this);

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

	} // end on create method.

	private void setTheTimer() {
		// Set the timer to send messages to the mHandler:
		t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						// Send a message to the handler:
						// This is to update the text views for bets found in
						// the upper part of the screen, if the bet was changed:
						if (myTempBet != dealer.currentBet) {
							myTempBet = dealer.currentBet;
							mHandler.sendEmptyMessageDelayed(
									UPDATE_VIEWS_VIA_HANDLER, 0); // 0 means the
																	// delay in
																	// milliseconds.
						}
						// If a variant was chosen:
						// For new round:
						if (toBeNewRound && isStarted && !areCardsInHands()
								&& deck.cardsLeft() == 0) {
							toBeNewRound = false;
							mHandler.sendEmptyMessageDelayed(NEW_ROUND, 5000);
						} // end for a new round.
							// For giving cards to players:
						else if (toGiveCards && isStarted && !areCardsInHands()
								&& deck.cardsLeft() > 0) {
							toGiveCards = false;
							mHandler.sendEmptyMessageDelayed(GIVE_CARDS, 2000);
						} // end for giving cards to players.
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

		postTheStatistics();

		if (MainActivity.isShake) {
			// Add the following line to unregister the Sensor Manager onPause
			mSensorManager.unregisterListener(mShakeDetector);
		}

		sndMusic.stopLooped();

		super.onPause();
	} // end onPause method.

	/*
	 * We need the statistics post to be a method, we call it also when changing
	 * game variant, not only when the game is onPause.:
	 */
	private void postTheStatistics() {
		// Only if there are hands played, post the statistics:
		if (numberOfPlayedHands > 0) {
			// Determine the number in DataBase for current variant:
			int nrVar = 21 + ScopaActivity.lsVariant;
			Statistics.postStats("" + nrVar, numberOfPlayedHands);
			numberOfPlayedHands = 0;
			moneyWon = 0;
			moneyLost = 0;
		}
		// end post the statistics.

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	} // end onDestroy() method.

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scopa, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		// Statistics alert if chosen from this game menu:
		if (id == R.id.mnuLsStatistics) {
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
					getString(R.string.statistics_for_la_scopa), ""
							+ numberOfPlayedHands, "" + moneyWon, ""
							+ moneyLost, "" + ballance, ballancePerHand);
			GUITools.alert(this, title, message);
		} // end statistics alert.
		else if (id == R.id.mnuMoneyWonAsBonus) {
			GUITools.showBonusStatistics(this, Dealer.curMoneyWonAsBonus);
		} // end if bonus statistics is chosen in menu.
		else if (id == R.id.mnuChooseVariant) {
			chooseScopaVariant();
		} // end if choose variant of Scopa item was chosen in menu.
		else if (id == R.id.mnuSetLimitScore) {
			setLimitScore();
		} // end if change limit score item was chosen in menu.
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
	} // end on item selected from menu.

	// Methods for buttons in layout:

	public void handleShakeEvent(int count) {
		// It depends of the status of the game, started or not:
		if (!isStarted) {
			newHandActions();
		} else {
			// Say all cards on the table and cards visible.
			speak.stop();
			sayAllCards(0);
			sayAllCards(1);
		}
	} // end handle shake detection.

	public void changeBet(View view) {
		dealer.changeBet();
	} // end function change bet.

	public void newHand(View view) {
		newHandActions();
	} // end new hand method.

	private void newHandActions() {
		if (!isStarted) {

			if (dealer.addBet()) {

				// Show and say about this start:
				tempMessage = getString(R.string.ls_started);
				updateStatus(tempMessage);

				// Increase number of hands:
				numberOfPlayedHands++;

				// Stop previously speaking:
				speak.stop();

				isStarted = true; // a little bit later this flag.

				// Reset the array for totals to 0s:
				aLsTotals = new int[] { 0, 0, 0 };

				whoIs = 1; // it is player's turn at beginnings.
				whoStarts = 1; // each new game is started by player.
				newRound();

				enableOrDisableButtons();
			} // end if add bet method was processed correctly.
		} // end if is not started.
	} // end newHandActions method.

	private void newRound() {
		if (isStarted) {
			// Announce this new round:
			tempMessage = String.format(strLsNewRound, aPossesion[whoStarts]);
			updateStatus(tempMessage);
			// Initialise the deck of cards:
			deck = new Deck(2, true, c); // context is needed in constructor, 2
											// means deck with 40 cards for
											// Scopa,
											// another constructor of the class.

			// Initiate all hands, just to have the possibility to clean the
			// layouts for cards:
			/*
			 * The hands are empty, this way the draw method will draw nothing,
			 * but will clear the remaining cards from last war:
			 */
			hands[0].clear(); // table.
			hands[1].clear(); // player.
			hands[2].clear(); // dealer.
			hands[3].clear(); // player collection, cards captured.
			hands[4].clear(); // dealer's collection, cards captured.
			drawHandsOfCards(0); // clear as visible the table's cards.
			drawHandsOfCards(1); // clear as visible player's cards.

			// Point also to null the mImages array:
			mImages = null;

			// Give cards to player and dealer for first time:
			giveCards();

			// Let's see on the screen what was changed if necessary:
			redrawTextViews();

			// Now we put and show also the cards on the table:
			deck.isDeckSounds = false; // disable sounds in deck.
			// We put a cards on the table just for fun and we extract it back:
			Card inexistentCard = new Card(1, 2, this);
			hands[0].addCard(inexistentCard);
			// Now we delete that card:
			hands[0].removeCard(inexistentCard);
			for (int i = 0; i < 4; i++) {
				// Only if is not Scopone or Escoba Scopone variant, 3 or 4 as
				// integer of lsVariant:
				if (ScopaActivity.lsVariant < 3) {
					takeCardActions(0); // 0 is the table.
				}
			} // end for add four cards on the table.
			deck.isDeckSounds = true; // re-enable sounds in deck.
			// let's draw them, the cards on the table:
			drawHandsOfCards(0); // 0 on the table, 1 player, 2 dealer.
			/*
			 * Announce the cards on the table if is not a Scopone variant, in
			 * scopone cards are not on the table at start:
			 */
			if (ScopaActivity.lsVariant < 3) {
				sayAllCards(0);
			}

			/*
			 * If is a round when dealer starts, send this information to the
			 * handler:
			 */
			if (whoStarts == 2) {
				mHandler.sendEmptyMessageDelayed(MAKE_DEALER_MOVE, 6000);
				whoStarts = 1; // for next round.
				whoIs = 2; // we adjust the whoIs because is a new round.
			} else {
				// We make it 2 because next round will be dealer's turn first:
				whoStarts = 2;
				whoIs = 1; // we adjust the whoIs because is a new round.
			}
		} // end if isStarted.
	} // end newRound method.

	// A method to give 3 or 10 cards to player and dealer:
	public void giveCards() {
		if (!areCardsInHands()) {
			// Let's decide how many cards must be given for each user:
			int limit = 3; // this is for Scopa and Escoba.
			if (ScopaActivity.lsVariant >= 3) {
				/*
				 * It means this is a scopone variant, 10 cards will be given to
				 * each player:
				 */
				limit = 10;
			} // end if it's a Scopone variant.
			for (int i = 0; i < limit; i++) {
				/*
				 * Disable sounds if the loop is at a point greater than 5. In
				 * Scopone variants it is too long time the cards put down
				 * sound:
				 */
				if (i >= 5) {
					deck.isDeckSounds = false; // disable sounds in deck.
				}
				takeCardActions(1); // 1 means the player.
				// Reactivate sounds in deck:
				if (i >= 5) {
					deck.isDeckSounds = true; // re-enable sounds in deck.
				}
			}
			// We sort them to be easier for player to remember them:
			hands[1].sortByValue();
			// Say now all extracted cards:
			// Announce them if necessary:
			sayAllCards(1); // player's cards.

			// let's draw them:
			drawHandsOfCards(1); // 0 dealer, greater player.

			// Let's set the listener for draw phase, to be possible the
			// choosing of
			// the cards:
			setImagesListeners(1);

			// Let's take 3 cards for dealer:
			deck.isDeckSounds = false; // disable sounds in deck.
			for (int i = 0; i < limit; i++) {
				takeCardActions(2);
			}
			deck.isDeckSounds = true; // re-enable sound in deck.
			/*
			 * We sort dealer's cards because this way he plays better, he puts
			 * down the most left card if combinations are not available:
			 */
			hands[2].sortByValue();
		} // end if there were no cards in hand.
	} // end giveCards method.

	public void takeCardActions(int whose) {
		if (isStarted) {
			// Extract a card from the deck:
			Card card = deck.dealCard();

			// Add it into the Scopa hand:
			hands[whose].addCard(card);
		}// end if isStarted is true.
	} // end take card method.

	// A method to return true if there are cards in player's hands:
	private boolean areCardsInHands() {
		boolean areCards = true;
		if (hands[1].getCardCount() == 0 && hands[2].getCardCount() == 0) {
			areCards = false;
		}
		return areCards;
	} // end areCardsInHands() method.

	// A method to draw the cards in hands:
	private void drawHandsOfCards(int whose) {
		LinearLayout ll;

		// Find the corresponding linear layout, depending of whose turn is:
		// If is table cards:
		if (whose == 0) {
			ll = (LinearLayout) findViewById(R.id.llDealerCards);
		} else {
			ll = (LinearLayout) findViewById(R.id.llPlayerCards);
		}

		hands[whose].drawCards(ll, whose);
	} // end draw hands of cards.

	// A method to increase with one point in some situations:
	private void increaseTotals(int whose) {
		aLsTotals[whose]++;
		updateTextViewsForTotals(aLsTotals[1], aLsTotals[2]);
	} // end increaseTotals() method.

	// A method to update some TextViews:
	public void updateTextViewsForMoney() {

		// First change the tvMyMoney TextView:

		tvMyMoney.setText(String.format(strMyMoney, "" + Dealer.myTotalMoney));

		// Now change the tvMyBet TextView:
		tvMyBet.setText(String.format(strMyBet, "" + dealer.currentBet));

	} // end updateTextViews method.

	// A method to update TextViews for scores:
	public void updateTextViewsForTotals(int myScore, int dealersScore) {
		// First change the tvMyLsHand TextView:
		tvMyLsTotal.setText(String.format(strMyLsTotal, "" + myScore));

		// Now change the tvDealersLsHand TextView:
		tvDealersLsTotal.setText(String.format(strDealersLsTotal, ""
				+ dealersScore));
	} // end updateTextViews with hands type method.

	// A method to update the tvScopaStatus TextView:
	public void updateStatus(String text) {
		mHistory.add(text);
		tvLsStatus.setText(text);
		speak.say(text, false);
	} // end updateLSStatus method.

	// A method to make buttons state active or inactive, depending of the
	// status of the game:
	public void enableOrDisableButtons() {

		// If is started or not:
		if (isStarted) {
			btLsBet.setEnabled(false);
			btLsNew.setEnabled(false);
			btLsAbandon.setEnabled(true);
			btLsSayAllVisibleCards.setEnabled(true);
			btLsHistory.setEnabled(true);
		} else {
			btLsBet.setEnabled(true);
			btLsNew.setEnabled(true);
			btLsAbandon.setEnabled(false);
			btLsSayAllVisibleCards.setEnabled(false);
			// btLsHistory.setEnabled(false);
		} // end if is started or not.
	} // end enableOrDisableButtons.

	// A method to update periodically the TextViews:
	public void redrawTextViews() {
		updateTextViewsForMoney();

		updateTextViewsForTotals(aLsTotals[1], aLsTotals[2]);
	} // end the method to update text views.

	// A method to say all cards in a hand:
	public void sayAllCards(int whose) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < hands[whose].getCardCount(); i++) {
			Card tempC = hands[whose].getCard(i);
			sb.append(tempC.toString());
			// Append comma between cards:
			if (i < hands[whose].getCardCount() - 1) {
				sb.append(", ");
			}
		} // end for.
		String tempMessage = String.format(aLsAboutHand[whose], sb.toString());
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
			mImages[i] = (ImageView) findViewById(imageId);
			mImages[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					cardImageClicked(imageId);
				}
			});
		} // end for.
	} // end set images listeners.

	// A method which will fire when an image is clicked in draw phase of the
	// Scopa game:
	public void cardImageClicked(int which) {
		// Only if is player's turn:
		if (whoIs == 1) {
			// We do the index of the card which is the which minus 1000 or
			// 2000:
			int cardIndex = 0;
			int whose = 1;
			if (which >= 2000) {
				cardIndex = which - 2000;
				whose = 1; // my cards images.
			} else {
				cardIndex = which - 1000;
				whose = 0; // dealer's cards images.
			}

			actionsAtCardPutDown(whose, cardIndex);

			// Send the information that the dealer must play:
			mHandler.sendEmptyMessageDelayed(MAKE_DEALER_MOVE, 3500);

			enableOrDisableButtons();
		} // end if is player's turn, whoIs == 1.
		else {
			SoundPlayer.playSimple(this, "forbidden");
		}
	} // end method for a card image clicked.

	// A method for dealer to play a card:
	public void dealerPlays() {
		// Only if there is at least a card in dealer's hand:
		if (hands[2].getCardCount() > 0) {
			// indexCard is the index in dealer's hand:
			int indexCard = hands[0].aiBestMove(hands[2]);
			actionsAtCardPutDown(2, indexCard);
		} // end if there are cards in dealer's hand.
	} // end dealerPlays() method.

	// A method to put a card, do things when a card was put down by player or
	// dealer:
	public void actionsAtCardPutDown(int whose, int cardIndex) {
		SoundPlayer.playSimple(this, "card_take");
		changeTurn(); // this is not to be possible a click for player when is
						// dealer's turn.
		Card tempC = hands[whose].getCard(cardIndex);

		// A method which make the move for player and dealer after a card is
		// chosen:
		makeCleverMove(whose, tempC);
		hands[whose].removeCard(tempC);
		checkForScopa(whose);
		checkIfLastCardsAreTaken();

		if (whose == 1) {
			// It is player cards, let's redraw them and set again the
			// listeners:
			drawHandsOfCards(whose); // redraw player's cards.
			setImagesListeners(whose);
		} // end if is player turn, set again the listeners.

		// If deck is empty or player's hands are empty when there are no cards
		// in hands anymore:
		if (areCardsInHands()) {
			// Do nothing.
		} else if (deck.cardsLeft() == 0) {
			toBeNewRound = true;
		} else if (!areCardsInHands()) {
			toGiveCards = true;
		}

	} // end actionsAtCardPutDown() method.

	/*
	 * A method which make a clever move for player and dealer, depending of the
	 * card chosen to be put down.
	 */
	public void makeCleverMove(int whose, Card putCard) {
		ArrayList<Card> toTakeList;
		/*
		 * Here will be filled the array list, the makeCleverMove() method of
		 * the ScopaHand class, the table instance will return an array of cards
		 * which will be taken from the table:
		 */
		toTakeList = hands[0].decideCardsToBeExtracted(putCard);

		//
		// If toTakeList is 0 in length it means card will appear on the table,
		// otherwise
		// cards were taken:
		if (toTakeList.size() == 0) {
			hands[0].addCard(putCard);
			// Say and show this action of card put on the table:
			String message = String.format(strLsCardPutMessage,
					aPossesion[whose], putCard.toString());
			updateStatus(message);
		} else {
			/*
			 * If it's at least a card taken from the table, the toTakeList is
			 * greater than 0 in size:
			 */
			SoundPlayer.playSimple(this, "scopa_capture");

			/*
			 * Recreate the cards which were on the table // to put them in our
			 * or dealer collection of captured cards:
			 */
			// A String to enumerate the cards taken in a status message:
			StringBuilder takenCards = new StringBuilder();
			for (int i = 0; i < toTakeList.size(); i++) {
				Card x = toTakeList.get(i);
				// Remove the found card from the table:
				hands[0].removeCard(x);
				// Add now the card x into collected hand:
				hands[whose + 2].addCard(x);
				takenCards.append(x.toString());
				// If it's necessary to add a comma:
				if (i < toTakeList.size() - 1) {
					takenCards.append(", ");
				} // end if necessary to add a comma.
			} // end for.
				// Add also to collection the card which was in hand:
			hands[whose + 2].addCard(putCard);

			// Say and show this action of cards being taken:
			String message = String.format(strLsTakenCardsMessage,
					aPossesion[whose], takenCards.toString(),
					putCard.toString());
			updateStatus(message);

			// Set also who took the last from the table:
			lastTaker = whose;
		} // end it was not isContinue, cards were taken from table.

		// Redraw the table:
		drawHandsOfCards(0);
	} // end makeCleverMove() method.

	// A method to detect if it's a Scopa event:
	private void checkForScopa(int whose) {
		/*
		 * Only if there are no cards in deck and at least one of players hasn't
		 * cards in hand. Otherwise it means it's the finish and it is not a
		 * Scopa event.
		 */
		if (hands[0].getCardCount() == 0) {
			if (!(deck.cardsLeft() == 0 && (hands[1].getCardCount() == 0 && hands[2]
					.getCardCount() == 0))) {
				SoundPlayer.playSimple(this, "scopa_event");
				increaseTotals(whose);
				updateStatus(strLsScopaEvent);
				Vibration.makeVibration(this, 500);
			}
		}
	} // end detect if it's a Scopa event method.

	/*
	 * A method which checks if there are the last cards to be taken by one of
	 * the players, at the finish of a round:
	 */
	private void checkIfLastCardsAreTaken() {
		/*
		 * If there are no cards anymore in hands and in deck, it is the moment
		 * to give all remained cards to one of the players:
		 */
		if (deck.cardsLeft() == 0 && !areCardsInHands()) {
			SoundPlayer.playSimple(this, "finish");
			StringBuilder sb = new StringBuilder();
			int cardsLeftOnTable = hands[0].getCardCount();
			if (cardsLeftOnTable > 0) {
				for (int i = 0; i < cardsLeftOnTable; i++) {
					Card tempC = hands[0].getCard(0);
					hands[0].removeCard(0);
					hands[lastTaker + 2].addCard(tempC);
					sb.append(tempC.toString());
					if (i < cardsLeftOnTable - 1) {
						sb.append(", ");
					}
				} // end for.
				tempMessage = String.format(
						getString(R.string.ls_remained_cards_taken),
						aPossesion[lastTaker], sb.toString());
				mHistory.add(tempMessage);
				// Redraw the table: // now it is an empty table.
				drawHandsOfCards(0);
			} // end if it was at least a card remained.

			calculatePointsAtFinishOfAround();
		} // end if hands and deck are empty.
	} // end checkIfLastCardsAreTaken() method.

	/*
	 * A method to calculate points at the finish of a round. At the end of this
	 * method will be also the check for finish method.
	 */
	private void calculatePointsAtFinishOfAround() {
		StringBuilder sb = new StringBuilder();
		int whose = 0;

		// Point for number of cards:
		int myCards = hands[3].getCardCount();
		int dealersCards = hands[4].getCardCount();
		if (myCards > dealersCards) {
			whose = 1;
		} // end if player cards are more than dealer's.
		else if (myCards < dealersCards) {
			whose = 2;
		} else {
			// It's equal:
			whose = 0;
		} // end if it's equal number of cards taken.
			// Make the string for point at number of cards if it's no equal:
		if (whose > 0) {
			increaseTotals(whose);
			tempMessage = String.format(
					getString(R.string.ls_point_number_of_cards),
					aPossesion[whose], "" + hands[whose + 2].getCardCount());
			sb.append(tempMessage);
		} // end if someone has more cards taken.
		else {
			tempMessage = getString(R.string.ls_same_number_of_cards);
			sb.append(tempMessage);
		} // end if was same number of cards taken.

		// See who has more diamonds:
		int myDiamonds = hands[3].getNumberOfDiamonds();
		int dealersDiamonds = hands[4].getNumberOfDiamonds();
		if (myDiamonds > dealersDiamonds) {
			whose = 1;
		} // end if player diamonds are more than dealer's.
		else if (myDiamonds < dealersDiamonds) {
			whose = 2;
		} else {
			// It's equal:
			whose = 0;
		} // end if it's equal number of diamonds taken.
			// Make the string for point at number of diamonds if it's no equal:
		if (whose > 0) {
			increaseTotals(whose);
			tempMessage = String.format(
					getString(R.string.ls_point_number_of_diamonds),
					aPossesion[whose],
					"" + hands[whose + 2].getNumberOfDiamonds());
			sb.append("\n" + tempMessage);
		} // end if someone has more diamonds taken.
		else {
			tempMessage = getString(R.string.ls_same_number_of_diamonds);
			sb.append("\n" + tempMessage);
		} // end if was same number of diamonds taken.

		// See who has more sevens:
		int mySevens = hands[3].getNumberOfSevens();
		int dealersSevens = hands[4].getNumberOfSevens();
		if (mySevens > dealersSevens) {
			whose = 1;
		} // end if player sevens are more than dealer's.
		else if (mySevens < dealersSevens) {
			whose = 2;
		} else {
			// It's equal:
			whose = 0;
		} // end if it's equal number of sevens taken.
			// Make the string for point at number of sevens if it's no equal:
		if (whose > 0) {
			increaseTotals(whose);
			tempMessage = String.format(
					getString(R.string.ls_point_number_of_sevens),
					aPossesion[whose],
					"" + hands[whose + 2].getNumberOfSevens());
			sb.append("\n" + tempMessage);
		} // end if someone has more sevens taken.
		else {
			tempMessage = getString(R.string.ls_same_number_of_sevens);
			sb.append("\n" + tempMessage);
		} // end if was same number of sevens taken.

		// See who has seven of diamond:
		if (hands[3].hasSevenOfDiamond()) {
			// Player has it:
			whose = 1;
		} else {
			// Dealer has it:
			whose = 2;
		} // end if dealer has the seven of diamond.
		increaseTotals(whose); // this is because seven of diamonds is
								// mandatory.
		// Make the string for seven of diamond:
		tempMessage = String.format(
				getString(R.string.ls_point_seven_of_diamonds),
				aPossesion[whose]);
		sb.append("\n" + tempMessage);

		// Make the score string:
		tempMessage = String.format(getString(R.string.ls_score),
				aPossesion[1], "" + aLsTotals[1], aPossesion[2], ""
						+ aLsTotals[2]);
		sb.append("\n" + tempMessage);
		updateStatus(sb.toString());

		// Check if is a general finish, end of an entire game:
		checkForFinish();
	} // end calculatePointsAtFinishOfAround() method.

	// A method which checks for finish:
	private void checkForFinish() {
		/*
		 * Check if one of the players has more than 11 points and the
		 * difference is more than one point between them:
		 */
		if ((aLsTotals[1] >= MainActivity.scopaTargetScore || aLsTotals[2] >= MainActivity.scopaTargetScore)
				&& Math.abs(aLsTotals[1] - aLsTotals[2]) > 1) {
			int whose = 0;
			isStarted = false;

			if (aLsTotals[1] > aLsTotals[2]) {
				// Player wins:
				whose = 1;
				dealer.win();
				tempMessage = String.format(getString(R.string.ls_you_won), ""
						+ dealer.currentBet);
				updateStatus(tempMessage);
			} // end if player wins.
			else if (aLsTotals[1] < aLsTotals[2]) {
				// Dealer wins:
				whose = 2;
				dealer.lose();
				tempMessage = String.format(
						getString(R.string.ls_dealer_won_battle), ""
								+ dealer.currentBet);
				updateStatus(tempMessage);
			} // end if dealer wins.
			else {
				// It's a draw:
				whose = 0;
				// But a draw cannot be at the moment of this release.
			} // end if it's a draw.

			// Show the message:
			// Make an array for Congratulations or Sorry:
			String[] aTemp = new String[] { "",
					getString(R.string.congratulations),
					getString(R.string.sorry) };

			// Sort the aLsTotals to have the greatest first:
			Arrays.sort(aLsTotals);
			String score = "" + aLsTotals[2] + " - " + aLsTotals[1];

			tempMessage = String.format(getString(R.string.ls_won_entire_game),
					aTemp[whose], aPossesion[whose], score);

			/*
			 * We don't put the final message with updateStatus in the bottom
			 * LinearLayout, but in the player's cards LinearLayout:
			 */
			// Find the LinearLayout:
			LinearLayout ll = (LinearLayout) findViewById(R.id.llDealerCards);
			TextView tv = new TextView(this);
			LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f);
			tv.setLayoutParams(params);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
			tv.setText(tempMessage);
			ll.addView(tv);

			speak.say(tempMessage, false);
			mHistory.add(tempMessage);

			enableOrDisableButtons();
			updateTextViewsForMoney();
			// Here was the last thing happened in a game of Scopa.
		} // end if one of players has the target score and the difference is
			// greater than one point.
	} // end checkForFinish() method.

	// A method to change turn:
	private void changeTurn() {
		if (whoIs == 1) {
			whoIs = 2;
		} else {
			whoIs = 1;
		}
	} // end changeTurn() method.

	// A method when the Abandon button is pressed:
	public void abandon(View view) {
		isStarted = false;
		enableOrDisableButtons();
		aLsTotals[2] = MainActivity.scopaTargetScore;
		/*
		 * If dealer's score isn't greater than users with 2 points, we give to
		 * dealer other points:
		 */
		if (aLsTotals[2] - aLsTotals[1] < 2) {
			int dif = aLsTotals[2] - aLsTotals[1];
			aLsTotals[2] = aLsTotals[2] + (2 - dif);
		}
		updateTextViewsForTotals(aLsTotals[1], aLsTotals[2]);
		dealer.lose();
		tempMessage = String.format(getString(R.string.ls_dealer_won_battle),
				"" + dealer.currentBet);
		updateStatus(tempMessage);
		updateTextViewsForMoney();
		drawHandsOfCards(1); // remove the listeners from player's cards.
	} // end abandon() method.

	public void sayAllVisibleCards(View view) {
		speak.stop();
		sayAllCards(0);
		sayAllCards(1);
	} // end sayAllVisibleCards() method.

	// A method to show history of the game:
	public void history(View view) {
		mHistory.show(15);
	} // end history() Method.

	// A method to choose the limit score:
	private void setLimitScore() {
		if (!isStarted) {
			AlertDialog scoreDialog;

			// Strings to Show In Dialog with Radio Buttons
			final CharSequence[] items = { "6", "11", "16", "21" };
			final int[] lsLimits = { 6, 11, 16, 21 };

			// Creating and Building the Dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.ls_set_limit_score));
			// Determine current item selected:
			int lsLimitPosition = -1;
			for (int i = 0; i < lsLimits.length; i++) {
				if (lsLimits[i] == MainActivity.scopaTargetScore) {
					lsLimitPosition = i;
					break;
				}
			} // end for search current position of the current level chosen
				// before.
			builder.setSingleChoiceItems(items, lsLimitPosition,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {

							switch (item) {
							case 0:
								// Your code when first option selected, 6 as
								// limit:
								MainActivity.scopaTargetScore = lsLimits[item];
								break;
							case 1:
								// Your code when 2nd option selected, 11:
								MainActivity.scopaTargetScore = lsLimits[item];

								break;
							case 2:
								// Your code when 3rd option selected, 16:
								MainActivity.scopaTargetScore = lsLimits[item];
								break;
							case 3:
								// Your code when 4th option selected, 21:
								MainActivity.scopaTargetScore = lsLimits[item];
								break;

							} // end switch.

						}
					});

			builder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// Save the choice for scopa limit score:
							Settings set = new Settings(finalContext);
							set.saveIntSettings("scopaTargetScore",
									MainActivity.scopaTargetScore);
						}
					});
			scoreDialog = builder.create();
			scoreDialog.show();

		} else {
			GUITools.alert(this, getString(R.string.warning),
					getString(R.string.ls_change_limit_score_not_allowed));
		} // end if is started, not allowed.
	} // end setLimitScore() method.

	// A method to choose Scopa variant:
	private void chooseScopaVariant() {
		if (!isStarted) {
			/*
			 * We post also the statistics for current variant because the
			 * variant will be changed:
			 */
			postTheStatistics();

			AlertDialog variantDialog;

			// Strings to Show In Dialog with Radio Buttons
			final CharSequence[] items = {
					getString(R.string.ls_scopa_variant),
					getString(R.string.ls_escoba_variant),
					getString(R.string.ls_scopone_variant),
					getString(R.string.ls_scopone_escoba_variant) };
			final int[] lsVariants = { 1, 2, 3, 4 };

			// Creating and Building the Dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.ls_choose_variant));
			// Determine current item selected:
			int lsVariantPosition = -1;
			for (int i = 0; i < lsVariants.length; i++) {
				if (lsVariants[i] == ScopaActivity.lsVariant) {
					lsVariantPosition = i;
					break;
				}
			} // end for search current position of the current variant chosen
				// before.
			builder.setSingleChoiceItems(items, lsVariantPosition,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {

							switch (item) {
							case 0:
								/*
								 * Your code when first option selected, 1
								 * classic Scopa as variant:
								 */
								ScopaActivity.lsVariant = lsVariants[item];
								break;
							case 1:
								// Your code when 2nd option selected, 2 Escoba:
								ScopaActivity.lsVariant = lsVariants[item];

								break;
							case 2:
								// Your code when 3rd option selected, 3
								// Scopone:
								ScopaActivity.lsVariant = lsVariants[item];
								break;
							case 3:
								/*
								 * Your code when 4th option selected, 4 Scopone
								 * Escoba:
								 */
								ScopaActivity.lsVariant = lsVariants[item];
								break;
							default:
								// Nothing were chosen, will be the classic
								// variant:
								ScopaActivity.lsVariant = lsVariants[0];
								break;

							} // end switch.

						}
					});

			builder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							/*
							 * Save the choice for scopa variant, but we show in
							 * status the choice instead:
							 */
							tempMessage = String.format(
									getString(R.string.ls_chosen_variant),
									items[ScopaActivity.lsVariant - 1]);
							/*
							 * We send a message via handler to announce to
							 * change the status about variant chosen:
							 */
							mHandler.sendEmptyMessageDelayed(
									UPDATE_STATUS_VARIANT_CHOSEN, 200);
							// Save also the variant chosen:
							Settings set = new Settings(finalContext);
							set.saveIntSettings("lsVariant",
									ScopaActivity.lsVariant);
						}
					});
			variantDialog = builder.create();
			variantDialog.show();

		} else {
			GUITools.alert(this, getString(R.string.warning),
					getString(R.string.ls_choose_variant_not_allowed));
		} // end if is started, not allowed.
	} // end chooseScopaVariant() method.
} // end ScopaActivity class.
