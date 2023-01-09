package ro.pontes.pontesgamezone;

/*
 * Class started by Manu on Sunday, 29 March 2015, 14:30.
 * This class takes care of the pawn shop in this game. 
 */

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

public class PawnshopActivity extends Activity {

	public final Context mFinalContext = this; // for other threads or
												// listeners.

	private Settings set;

	private int psBonus = 20; // 20% is the added tax for taking back the
								// product from pawn shop.
	// An array of boolean values to know which object is sold or not at pawn
	// shop:
	public static boolean[] psIsSold = new boolean[7];

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
		WeakReference<PawnshopActivity> psActivity;

		MyHandler(PawnshopActivity aPsActivity) {
			psActivity = new WeakReference<PawnshopActivity>(aPsActivity);
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
		setContentView(R.layout.activity_pawnshop);

		// Instantiate with constructor the set variable to save and charge
		// things from SharedSettings:
		set = new Settings(this);

		// Get the arrays to fill the product names and prices:
		Resources res = getResources();
		productNames = res.getStringArray(R.array.ps_products_to_sell_array);
		productPrices = res.getStringArray(R.array.ps_product_prices_array);

		// Update the welcome message:
		TextView wtv = (TextView) findViewById(R.id.tvWelcomeToPawnshop);
		wtv.setText(String.format(getString(R.string.ps_title),
				MainActivity.curNickname));

		// Load the totalMoney text view:
		tvMyMoney = (TextView) findViewById(R.id.tvMyMoney);
		strMyMoney = getString(R.string.my_money);

		updateTextViews();

		setImagesStatus();

		// Set the virtual time for first time, after it will be set via handler
		// each minute:
		updateVirtualTime();

		// To keep screen awake:
		if (MainActivity.isWakeLock) {
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} // end if is wake lock.

	} // end onCreate method.

	// A method to set content description, the background for already sold
	// products etc:
	private void setImagesStatus() {
		// Set content description for image buttons with products found in the
		// menu:
		String tempMessage = "";

		for (int i = 1; i < productNames.length; i++) {
			String ibtName = "ibtPawnshop" + i;
			int resID = getResources().getIdentifier(ibtName, "id",
					getPackageName());
			ImageButton ibt = (ImageButton) findViewById(resID);

			// If is sold or not, different actions:
			if (psIsSold[i] == false) {
				tempMessage = String
						.format(getString(R.string.ps_content_description_for_products),
								productNames[i], "" + productPrices[i]);
				// Set also the background to green:
				ibt.setBackgroundColor(Color.GREEN);
			} else {
				// Add 20% to the sum, it is the bonus for pawn shop:
				int price = Integer.parseInt(productPrices[i]);
				price = price + (psBonus * price / 100);
				tempMessage = String
						.format(getString(R.string.ps_content_description_for_sold_products),
								productNames[i], "" + price);
				ibt.setBackgroundColor(Color.RED);
			} // end if is already sold.

			ibt.setContentDescription(tempMessage);
		} // end for set contentDescription for ImageButtons.
	} // end setImagesStatus method.

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

		setTheTimer();

		// Play a background if is activated:
		sndBackground = new SoundPlayer();
		sndBackground.playLooped(this, "pawnshop_background1");

	} // end onResume method.

	@Override
	public void onPause() {
		// Add here what you want to happens on pause:

		sndBackground.stopLooped();

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

	// A method to update the text views in the bar area:
	private void updateTextViews() {

		// Update the total money text view, this is already charged, is a
		// global field of this class. It was charged in onCreate:
		tvMyMoney.setText(String.format(strMyMoney, "" + Dealer.myTotalMoney));

	} // end updateTextViews method.

	// A method to update the virtual time text view:
	public void updateVirtualTime() {
		TextView tv = (TextView) findViewById(R.id.tvPassedDay);
		if (PawnshopActivity.psIsSold[1] == false) {
			GUITools.getCurrentVirtualTime(this);
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

	public void sellOrBuyProduct1(View view) {
		sellOrBuySomething(1);
	}

	public void sellOrBuyProduct2(View view) {
		sellOrBuySomething(2);
	}

	public void sellOrBuyProduct3(View view) {
		sellOrBuySomething(3);
	}

	public void sellOrBuyProduct4(View view) {
		sellOrBuySomething(4);
	}

	public void sellOrBuyProduct5(View view) {
		sellOrBuySomething(5);
	}

	public void sellOrBuyProduct6(View view) {
		sellOrBuySomething(6);
	}

	// A method for ordering, this receive the product index via parameter:
	public void sellOrBuySomething(final int which) {

		// We must have a variable isSold to know what to show depending of the
		// status of chosen product:
		final boolean isSold = psIsSold[which];

		// The sound which must be played, depending of the product and
		// direction: pawn or take back:

		// Make an alert which yes and no buttons:
		// Get the strings:
		String tempTitle = "";
		String tempBody = "";

		if (isSold == false) {
			tempTitle = getString(R.string.ps_new_sell_title);
			tempBody = String.format(getString(R.string.ps_new_sell_message),
					MainActivity.curNickname, productNames[which], ""
							+ productPrices[which]);
		} else {
			// The product is already sold:
			tempTitle = getString(R.string.ps_old_sell_title);
			// Add 20% to the sum, it is the bonus for pawn shop:
			int price = Integer.parseInt(productPrices[which]);
			price = price + (psBonus * price / 100);

			tempBody = String.format(getString(R.string.ps_old_sell_message),
					MainActivity.curNickname, productNames[which], "" + price);
		}

		new AlertDialog.Builder(this)
				.setTitle(tempTitle)
				.setMessage(tempBody)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// If yes button was pressed, we do something
								// depending of the status of the product, sold
								// or not:
								if (isSold == false) {
									// Determine the price from productPrices
									// array:
									int price = Integer
											.parseInt(productPrices[which]);
									// Let's get money, add to our total:
									Dealer.myTotalMoney = Dealer.myTotalMoney
											+ price;
									// Post in statistics this sell:
									Statistics.postStats("16", price); // 16 is
																		// the
																		// id of
																		// the
																		// Pawn
																		// Shop
																		// sells
																		// in
																		// DB.
									// Change the value in the psIsSold array
									// from false to true:
									psIsSold[which] = true;
									SoundPlayer.playSimple(mFinalContext,
											"snd_pawnshop" + which);
								} else {
									// If the action is to take it back, isSold
									// was true:
									// Add 20% to the sum, it is the bonus for
									// pawn shop:
									int price = Integer
											.parseInt(productPrices[which]);
									price = price + (psBonus * price / 100);
									// We need to check if we have enough money:
									if (price <= Dealer.myTotalMoney) {
										// It means we have enough money to get
										// the product back:
										// Let's give money, subtract from our
										// total:
										Dealer.myTotalMoney = Dealer.myTotalMoney
												- price;
										// Post in statistics this sell:
										Statistics.postStats("17", price); // 17
																			// is
																			// the
																			// id
																			// of
																			// the
																			// Pawn
																			// Shop
																			// take
																			// back
																			// in
																			// DB.
										// Change the value in the psIsSold
										// array from true to false:
										psIsSold[which] = false;
										SoundPlayer.playSimple(mFinalContext,
												"sndb_pawnshop" + which);

									} else {
										// It means we haven't enough money to
										// take the product back:
										// We show an alert about this problem:
										GUITools.alert(
												mFinalContext,
												getString(R.string.warning),
												String.format(
														getString(R.string.ps_not_enough_money_to_take_product_back),
														MainActivity.curNickname,
														""
																+ Dealer.myTotalMoney));
									} // end if there are no enough money, price
										// was greater than Dealer.myTotalMoney.
								} // end if action is to take product back.

								// Indifferent of the action, give or take
								// product to pawn shop, we do something if yes
								// button was pressed:
								// First redraw the images to have different
								// status of the products:
								setImagesStatus();
								// Save the total money we have now:
								set.saveIntSettings("myTotalMoney",
										Dealer.myTotalMoney);
								// Save the sold products, a kind of array
								// serialise:
								set.saveSoldProducts();

								updateTextViews();
								updateVirtualTime();

							} // end if yes button was pressed.
						}).setNegativeButton(android.R.string.no, null).show();
		// End of yes / no alert.

	} // end order something method.

} // end PawnshopActivity class.

