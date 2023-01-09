package ro.pontes.pontesgamezone;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import ro.pontes.pontesgamezone.ShakeDetector.OnShakeListener;
import android.app.Activity;
import android.content.Context;
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

/*
 * Started on 29 July 2015, 22:50, by Manu.
 *  
 */

public class SlotMachineActivity extends Activity {

	// The following fields are used for the shake detection:
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private ShakeDetector mShakeDetector;
	// End fields declaration for shake detector.

	// For music background:
	private SoundPlayer sndMusic;

	// Other variables:
	private SpeakText speak;
	private Dealer dealer;
	private int curBet = 1;
	private boolean isStarted = false; // to know if a hand is started or not.
	private boolean isSpinning = false;
	private final int spinningDrawInterval = 75;
	private final int spinningDuration = 5000; // how long will be the cylinders
												// rolling if there are no
												// sounds activated.
	private int spinningDurationPassed = 0;

	private SlotMachineHand hand;

	// For statistics:
	private int numberOfPlayedHands = 0;
	private int moneyWon = 0;
	private int moneyLost = 0;

	// The layout which is used dynamic:
	public LinearLayout ll;

	// Let's have the tvBjStatus and other text views globally, not to
	// findViewById so often:
	private TextView tvSmStatus;
	private TextView tvMyMoney;
	private TextView tvMyBet;

	// Some global strings which are formated periodically:
	private String tempMessage; // to use for updating the status.
	private String strMyMoney;
	private String strMyBet;

	// Let's declare variables for all the buttons, we will use them to enable
	// or disable depending of the game status:
	private Button btSmNew;
	private Button btSmNew2;
	private Button btSmNew3;
	private Button btSmFire;

	// A variable to detect if bet was changed, this way we call the mHandler to
	// update text views for dealer:
	// initialise it in onCreate.

	// For a timer:
	private Timer t;

	// Messages for handler to manage the interface:
	private static final int UPDATE_VIEWS_VIA_HANDLER = 1; // a message to be
															// sent to the
															// handler.
	private static final int FINISHED_PLAYING_VIA_HANDLER = 2; // to know when
																// the spinning
																// is finished
																// as sound.
	private static final int ROLL_AGAIN = 3;

	// A static inner class for handler:
	static class MyHandler extends Handler {
		WeakReference<SlotMachineActivity> smActivity;

		MyHandler(SlotMachineActivity aSmActivity) {
			smActivity = new WeakReference<SlotMachineActivity>(aSmActivity);
		}
	} // end static class for handler.

	// this handler will receive a delayed message
	private MyHandler mHandler = new MyHandler(this) {
		@Override
		public void handleMessage(Message msg) {
			// Do task here
			// SlotMachineActivity theActivity = smActivity.get();

			if (msg.what == UPDATE_VIEWS_VIA_HANDLER) {
				redrawTextViews(); // update text on text views.
			}
			if (msg.what == FINISHED_PLAYING_VIA_HANDLER) {
				showAndSayCylinders();
			}
			if (msg.what == ROLL_AGAIN) {
				drawSpinningCylinders();
			}
		}
	};

	// End handler stuff.

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_slot_machine);

		// Instantiate an SpeakText object:
		speak = new SpeakText(this);

		// Initialise the dealer:
		dealer = new Dealer(this, false);
		// Make the current bet member of dealer as curBet:
		dealer.currentBet = curBet;

		// Get the LinearLayout used for drawing the cylinders:
		ll = (LinearLayout) findViewById(R.id.llCylinders);

		// Get the text views into their global variables:
		tvSmStatus = (TextView) findViewById(R.id.tvSmStatus);
		tvMyMoney = (TextView) findViewById(R.id.tvMyMoney);
		tvMyBet = (TextView) findViewById(R.id.tvMyBet);

		// Charge global strings from values strings:
		strMyMoney = getString(R.string.my_money);
		strMyBet = getString(R.string.my_bet);

		// Get the buttons into their global variables, we will use them to
		// enable or disable periodically:
		btSmNew = (Button) findViewById(R.id.btSmNew);
		btSmNew2 = (Button) findViewById(R.id.btSmNew2);
		btSmNew3 = (Button) findViewById(R.id.btSmNew3);
		btSmFire = (Button) findViewById(R.id.btSmFire);

		// Redraw for first time, to have 0 as score in hands and the bet and
		// wallet:
		redrawTextViews();
		enableOrDisableButtons();

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
						// This is to update the text views for bets found in
						// the upper part of the screen, if the bet was changed:
						if (isStarted && isSpinning
								&& spinningDurationPassed >= spinningDuration) {
							isSpinning = false;
							mHandler.sendEmptyMessageDelayed(
									FINISHED_PLAYING_VIA_HANDLER, 0);
						}
					}
				});
			}
		}, 1000, spinningDrawInterval); // 1000 means start from 1 second, and
										// the second 1000
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

		// Only if there are machines played, post the statistics:
		if (numberOfPlayedHands > 0) {
			Statistics.postStats("21", numberOfPlayedHands); // 21 is the id of
																// the slot
																// machine in
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
		getMenuInflater().inflate(R.menu.slot_machine, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.mnuSmStatistics) {
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
					getString(R.string.statistics_for_slot_machine), ""
							+ numberOfPlayedHands, "" + moneyWon, ""
							+ moneyLost, "" + ballance, ballancePerHand);
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

	private void handleShakeEvent(int count) {
		// It depends of the status of the game, started or not:
		if (!isStarted) {
			startNewMachineActions(curBet);
		} else {
			shakeMachineActions();
		}
	} // end handle shake detection.

	public void startNewMachine1(View view) {
		startNewMachineActions(1);
	} // end start new machine by button New clicked.

	public void startNewMachine2(View view) {
		startNewMachineActions(2);
	} // end start new machine by button New2 clicked.

	public void startNewMachine3(View view) {
		startNewMachineActions(3);
	} // end start new machine by button New3 clicked.

	public void startNewMachineActions(int localCurBet) {
		if (!isStarted) {
			// Something different before addBet() method:
			curBet = localCurBet;
			dealer.changeBetByForce(curBet);
			if (dealer.addBet()) {
				isStarted = true;

				// Stop previously speaking:
				speak.stop();

				// Increase number of hands:
				numberOfPlayedHands++;

				hand = new SlotMachineHand(this);
				// Draw an empty hand at the start of the new game:
				drawCylinders();

				spinningDurationPassed = 0;

				enableOrDisableButtons();

				// Show and say about this start:
				tempMessage = getString(R.string.sm_started);
				updateStatus(tempMessage);
				redrawTextViews(); // update text on text views.
			} // end if add bet method was processed correctly.
		} // end if is not started.
	} // end start new slot machine actions.

	// A method to shake the machine:
	public void shakeMachine(View view) {
		shakeMachineActions();
	} // end shake machine by button in the bottom of the windows is clicked.

	// A method to shake effectively the machine:
	public void shakeMachineActions() {
		if (isStarted && !isSpinning) {
			btSmFire.setEnabled(false);
			SoundPlayer.playSimple(this, "slot_machine");
			isSpinning = true;
			drawSpinningCylinders();
		}
	} // end shakeMachineActions method.

	// A method to shake and show the cylinders:
	private void showAndSayCylinders() {
		// Stop the cylinders:
		StringBuilder sb = new StringBuilder();
		int numberOfCylinders = 3;
		for (int i = 0; i < numberOfCylinders; i++) {
			int curFace = SlotMachineHand.getRandomFace();
			Digit d = new Digit(curFace, this);
			hand.addDigit(d);
			sb.append(d.toString());
			if (i < numberOfCylinders - 1) {
				sb.append(", ");
			} // end if append a comma.
			else {
				sb.append(".");
			} // end if append a period.
		} // end for.
		drawCylinders();

		updateStatus(sb.toString());
		calculateFinish();
	} // end shakeAndShowCylinders() method.

	// A method to calculate what's happen and give or take money etc.:
	private void calculateFinish() {
		int multiplier = hand.getWinMultiplier();
		if (multiplier <= 0) {
			// The player lost:
			tempMessage = getString(R.string.sm_dealer_won);
			dealer.currentBet = curBet;
			dealer.lose();
			moneyLost = moneyLost + curBet;
		} else {
			// The player won, multiplier is greater than 0:
			tempMessage = getString(R.string.sm_you_won);
			int tempMoney = curBet * multiplier;
			dealer.currentBet = tempMoney;
			dealer.win();
			moneyWon = moneyWon + tempMoney;
			// If multiplier was 400, it means jackpot:
			if (multiplier >= 400) {
				SoundPlayer.playSimple(this, "jackpot");
			} // end if it's a jackpot.
		} // end if player won.

		// Format the string:
		tempMessage = String.format(tempMessage, dealer.currentBet);
		updateStatus(tempMessage);
		redrawTextViews(); // update text on text views.
		isStarted = false;
		enableOrDisableButtons();
	} // end calculateFinish() method.

	public void drawCylinders() {
		hand.drawDigits(ll, 1);
	} // end draw cylinders.

	// A method to draw cylinders if they are spinning:
	private void drawSpinningCylinders() {
		if (isSpinning) {
			// Increase the spinning duration:
			spinningDurationPassed += spinningDrawInterval;
			hand.drawRollingCylinders(ll);
			// Send the message via handler to be roll again:
			mHandler.sendEmptyMessageDelayed(ROLL_AGAIN, spinningDrawInterval);
		} // end if they are spinning.
	} // end drawSpinningCylinders() method.

	// A method to update some TextViews:
	public void updateTextViewsForMoney() {

		// First change the tvMyMoney TextView:

		tvMyMoney.setText(String.format(strMyMoney, "" + Dealer.myTotalMoney));

		// Now change the tvMyBet TextView:
		tvMyBet.setText(String.format(strMyBet, "" + curBet));
	} // end updateTextViews method.

	// A method to update the tvSlotMachineStatus TextView:
	public void updateStatus(String text) {
		tvSmStatus.setText(text);
		speak.say(text, false);
	} // end updateBjStatus method.

	// A method to make buttons state active or inactive, depending of the
	// status of the game:
	public void enableOrDisableButtons() {

		// If is started or not:
		if (isStarted) {
			btSmNew.setEnabled(false);
			btSmNew2.setEnabled(false);
			btSmNew3.setEnabled(false);
			btSmFire.setEnabled(true);

		} else {
			btSmNew.setEnabled(true);
			btSmNew2.setEnabled(true);
			btSmNew3.setEnabled(true);
			btSmFire.setEnabled(false);
		} // end if is started or not.

	} // end enableOrDisableButtons.

	// A method to update periodically the TextViews:
	public void redrawTextViews() {
		updateTextViewsForMoney();
	} // end the method to update text views.

} // end SlotMachineActivity.
