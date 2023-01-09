package ro.pontes.pontesgamezone;

/*
 * Class started by Manu on Monday, 19 January 2015, 00:50.
 * This class takes care of the saloon in this game. 
 */

import java.lang.ref.WeakReference;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

public class SaloonActivity extends Activity {

	// The following fields are used for the shake detection:
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private ShakeDetector mShakeDetector;
	// End fields declaration for shake detector.

	private Context c;
	public final Context mFinalContext = this; // for other threads or
												// listeners.

	private Settings set;

	private static int shookCount = 0;

	public static int spentMoney = 0; // today.
	public static int numberOfGamesForOrder = 0;
	public final int minimumGamesBeforeOrder = 5;
	public static int actualBonusPercentage = 0;
	public static int nrOfDrinks = 0;

	private SoundPlayer sndBackground; // to play a background sound if needed.

	// An array for product names taken from values strings:
	private static String[] productNames;
	// An parallel array for prices of each product found in the array above:
	private static String[] productPrices;

	// Strings:
	private String strMyMoney;

	// For a timer:
	private Timer t;

	// Text views:
	private TextView tvMyMoney;

	// Messages for handler to manage the interface:
	private static final int UPDATE_TIME_VIA_HANDLER = 1; // a message to be
															// sent to the
															// handler.

	// A static inner class for handler:
	static class MyHandler extends Handler {
		WeakReference<SaloonActivity> slActivity;

		MyHandler(SaloonActivity aSlActivity) {
			slActivity = new WeakReference<SaloonActivity>(aSlActivity);
		}
	} // end static class for handler.

	// this handler will receive a delayed message
	private MyHandler mHandler = new MyHandler(this) {
		@Override
		public void handleMessage(Message msg) {
			// Do task here
			// SaloonActivity theActivity = slActivity.get();

			if (msg.what == UPDATE_TIME_VIA_HANDLER) {
				updateVirtualTime();
			}

		}
	};

	// End handler stuff.

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_saloon);

		c = getApplicationContext();

		// Instantiate with constructor the set variable to save and charge
		// things from SharedSettings:
		set = new Settings(c);

		// Get the arrays to fill the product names and prices:
		Resources res = getResources();
		productNames = res.getStringArray(R.array.saloon_product_names_array);
		productPrices = res.getStringArray(R.array.saloon_product_prices_array);

		// Update the welcome message:
		TextView wtv = (TextView) findViewById(R.id.tvWelcomeToBarArea);
		wtv.setText(String.format(getString(R.string.saloon_title),
				MainActivity.curNickname));

		// Load the totalMoney text view:
		tvMyMoney = (TextView) findViewById(R.id.tvMyMoney);
		strMyMoney = getString(R.string.my_money);

		updateTextViews();

		// Set content description for image buttons with products found in the
		// menu:
		String tempMessage = getString(R.string.saloon_content_description_for_products);
		for (int i = 1; i < productNames.length; i++) {
			String ibtName = "ibtSaloon" + i;
			int resID = getResources().getIdentifier(ibtName, "id",
					getPackageName());
			ImageButton ibt = (ImageButton) findViewById(resID);
			ibt.setContentDescription(String.format(tempMessage,
					productNames[i], "" + productPrices[i]));
		} // end for set contentDescription for ImageButtons.

		// Set the virtual time for first time, after it will be set via handler
		// each minute:
		updateVirtualTime();

		// To keep screen awake:
		if (MainActivity.isWakeLock) {
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} // end wake lock.

		shookCount = 0; // how many times the phone was shook.

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

	// A method which is called when the phone is shook:
	public void handleShakeEvent(int count) {
		somethingIsBroken();
	} // end if phone was shook.

	public void setTheTimer() {
		// Set the timer to send messages to the mHandler:
		t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						// Send a message to the handler:
						// This is to update the text views for virtual time:
						mHandler.sendEmptyMessageDelayed(
								UPDATE_TIME_VIA_HANDLER, 0); // 0 means the
																// delay in
																// milliseconds.
					}
				});
			}
		}, 60000, 60000); // 1000 means start from 1 second, and the second 1000
							// is do the loop each 1 second.
		// end set the timer.
	} // end setTheTimer method.

	@Override
	public void onResume() {
		super.onResume();

		if (Dealer.myTotalMoney <= 0) {
			exitBarWithoutMoney(getString(R.string.warning), String.format(
					getString(R.string.saloon_not_allowed),
					MainActivity.curNickname));
		}
		setTheTimer();

		// Add the following line to register the Session Manager Listener
		// onResume
		mSensorManager.registerListener(mShakeDetector, mAccelerometer,
				SensorManager.SENSOR_DELAY_UI);

		// Play a background if is activated:
		sndBackground = new SoundPlayer();
		sndBackground.playLooped(this, "saloon_background1");
	} // end onResume method.

	@Override
	public void onPause() {
		// Add here what you want to happens on pause:

		sndBackground.stopLooped();

		t.cancel();
		t = null;

		// Add the following line to unregister the Sensor Manager onPause
		mSensorManager.unregisterListener(mShakeDetector);

		super.onPause();
	} // end onPause method.

	// For menu:
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

	// A method to update the text views in the bar area:
	private void updateTextViews() {

		// update the spent money text view:

		spentMoney = set.getIntSettings("spentMoney");

		TextView tv = (TextView) findViewById(R.id.tvSpentMoney);
		tv.setText(String.format(getString(R.string.saloon_spent_money), ""
				+ spentMoney));

		// Update the text view with number of games for order since last order:
		// Get the value of number of games for order from SharedSettings:
		numberOfGamesForOrder = set.getIntSettings("numberOfGamesForOrder");
		tv = (TextView) findViewById(R.id.tvNumberOfGamesForOrder);
		tv.setText(String.format(
				getString(R.string.saloon_number_of_games_for_order), ""
						+ numberOfGamesForOrder));

		// Update the bonus percentage text view:
		tv = (TextView) findViewById(R.id.tvActualBonusPercentage);
		tv.setText(String.format(
				getString(R.string.saloon_actual_bonus_percentage), ""
						+ actualBonusPercentage));

		// Update the total money text view, this is already charged, is a
		// global field of this class:
		tvMyMoney.setText(String.format(strMyMoney, "" + Dealer.myTotalMoney));

	} // end updateTextViews method.

	// A method to update the virtual time text view:
	public void updateVirtualTime() {
		TextView tv = (TextView) findViewById(R.id.tvPassedDay);
		if (PawnshopActivity.psIsSold[1] == false) {
			GUITools.getCurrentVirtualTime(c);
			int curHour = GUITools.currentVirtualHour;
			int curMinute = GUITools.currentVirtualMinute;
			tv.setText(String.format(getString(R.string.saloon_passed_day),
					GUITools.twoDigits(curHour), GUITools.twoDigits(curMinute)));
		} else {
			// If virtual time doesn't exist, the watch was sold:
			tv.setText(getString(R.string.tv_clock_is_unavailable));
		}

	} // end update virtual time method.

	// Methods for each button pressed in bar menu:

	public void orderProduct1(View view) {
		orderSomething(1);
	}

	public void orderProduct2(View view) {
		orderSomething(2);
	}

	public void orderProduct3(View view) {
		orderSomething(3);
	}

	public void orderProduct4(View view) {
		orderSomething(4);
	}

	public void orderProduct5(View view) {
		orderSomething(5);
	}

	public void orderProduct6(View view) {
		orderSomething(6);
	}

	// A method for ordering, this receive the product index via parameter:
	public void orderSomething(final int which) {

		// Only if there are minimum 5 orders at the bar:
		if (numberOfGamesForOrder >= minimumGamesBeforeOrder) {

			// Check if there are not too many drinks drank, this way the
			// customer must go to bathroom:
			if (nrOfDrinks < 3 || which >= 6) {

				// Make an alert which yes and no buttons:
				// Get the strings:
				String tempTitle = getString(R.string.saloon_new_order_title);
				String tempBody = String.format(
						getString(R.string.saloon_new_order_message),
						MainActivity.curNickname, productNames[which], ""
								+ productPrices[which]);
				new AlertDialog.Builder(this)
						.setTitle(tempTitle)
						.setMessage(tempBody)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {

										int price = Integer
												.parseInt(productPrices[which]);

										// Check if the wallet contains that
										// money:
										if (price <= Dealer.myTotalMoney) {

											// Save the money spent if
											// everything is OK:
											spentMoney = spentMoney + price;
											set.saveIntSettings("spentMoney",
													spentMoney);

											// Set to 0 the number of games
											// before a new order possible:
											numberOfGamesForOrder = 0;
											set.saveIntSettings(
													"numberOfGamesForOrder", 0);

											// Save also the actual bonus
											// percentage:
											actualBonusPercentage = calculateActualBonusPercentage(spentMoney);
											set.saveIntSettings(
													"actualBonusPercentage",
													actualBonusPercentage);

											// Take money from wallet and save
											// the new value in SharedSettings:
											Dealer.myTotalMoney = Dealer.myTotalMoney
													- price;
											set.saveIntSettings("myTotalMoney",
													Dealer.myTotalMoney);

											// Calculate also the number of
											// drinks for double-u:
											if (which < 6) {
												// 6 means cigar and is not
												// mandatory for double-u:
												nrOfDrinks = nrOfDrinks + 1;
												set.saveIntSettings(
														"nrOfDrinks",
														nrOfDrinks);
											} // end if it was a drink.

											updateTextViews();

											// Play a corresponding sound:
											SoundPlayer.playSimple(
													mFinalContext, "snd_saloon"
															+ which);

											// Post in statistics this order:
											Statistics.postStats("12", price); // 12
																				// is
																				// the
																				// id
																				// of
																				// the
																				// bar
																				// area
																				// in
																				// DB.

										} // end if the wallet contains
											// necessary money.
										else {
											// We haven't money:
											GUITools.alert(
													mFinalContext,
													getString(R.string.warning),
													String.format(
															getString(R.string.saloon_not_enough_money),
															MainActivity.curNickname));
										}
									}
								}).setNegativeButton(android.R.string.no, null)
						.show();
				// End of yes / no alert.

				// end if there are not too many drinks drank.
			} else {
				GUITools.alert(this, getString(R.string.warning), String
						.format(getString(R.string.saloon_too_many_drinks),
								MainActivity.curNickname));
			} // end if user drank too many drinks.

		} else {
			GUITools.alert(
					this,
					getString(R.string.warning),
					String.format(
							getString(R.string.saloon_you_must_play_more),
							MainActivity.curNickname,
							""
									+ (minimumGamesBeforeOrder - numberOfGamesForOrder)));
		} // end if else, there are not at least 5 orders to make a new order.
	} // end order something method.

	// A method to calculate the bonus percentage, one percent per $10 spent at
	// bar:
	public int calculateActualBonusPercentage(int baseValue) {
		float temp = baseValue / 5;
		return Math.round(temp); // 5 is a constant, one percent per 5 dollars
									// spent at bar.
	} // end method to calculate the actual percentage.

	// A method which occurs when phone is shook in bar area, something is
	// broken:
	private void somethingIsBroken() {

		// If user shake the phone more than 2 times, he break something and
		// must pay:
		if (shookCount >= 2) {
			shookCount = 0;
			SoundPlayer.playSimple(this, "saloon_break");
			// Determine the compensation:
			int compensation = GUITools.random(25, 75);
			;
			// Take money from wallet and save the new value in SharedSettings:
			Dealer.myTotalMoney = Dealer.myTotalMoney - compensation;

			// Check if there is enough money to pay the compensation:
			if (Dealer.myTotalMoney < 0) {
				Dealer.myTotalMoney = 0;
				exitBarWithoutMoney(
						getString(R.string.saloon_broken_title),
						String.format(
								getString(R.string.saloon_not_enough_money_for_compensation),
								MainActivity.curNickname));
			} else {
				GUITools.alert(this, getString(R.string.saloon_broken_title),
						String.format(
								getString(R.string.saloon_bottles_broken),
								MainActivity.curNickname, compensation));
			}
			updateTextViews();
			// Post this statistic:
			Statistics.postStats("15", compensation); // 15 is the id of the bar
														// compensation when
														// bottles are broken in
														// bar area.
			set.saveIntSettings("myTotalMoney", Dealer.myTotalMoney);
			Vibration.makeSOS(this);
		} else {
			// If is only a move of bottles:
			shookCount = shookCount + 1;
			SoundPlayer.playSimple(this, "saloon_move");
			// GUITools.toast(getString(R.string.saloon_bottles_moved), 1000,
			// this);
			Vibration.makeSOS(this);
		} // end if there are first shakes, only moving the bottles in bar area.
	} // end if something is broken in bar area.

	// A method to exit the bar if there are not enough money to pay the
	// compensation.
	private void exitBarWithoutMoney(String title, String message) {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		// The title:
		alert.setTitle(title);

		// The body:
		alert.setMessage(message);

		alert.setCancelable(false);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				SaloonActivity.this.finish();
			}
		});
		alert.show();

	} // end exit if not enough money for compensation.

} // end saloon class.

