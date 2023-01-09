package ro.pontes.pontesgamezone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/*
 * A class which keep track of money.
 * It calculates bet money, wins, loses etc
 */
public class Dealer {

	public static int myTotalMoney;
	public int currentBet; // this copies the value from last bet.
	public int lastBet; // this is the most important, changed even when user
						// write it.
	public boolean isBet = false;
	public static final int insolvency = 1000000;

	// About bonus:
	public static int moneyWonAsBonus; // from beginning.
	public static int moneyWonAsBonus24; // money spent in last 24 hours.
	public static int curMoneyWonAsBonus; // in current session.

	// We need also a context:
	private Context context;

	// We need also a Settings object to save and get money from settings file:
	private Settings set;

	/*
	 * To know if we want to be saved the bet, for instance in Slot Machine we
	 * don't want to have one, two or three coins save as bet. We set this in
	 * constructor as a parameter.
	 */
	private boolean isSaveBet = true;

	// We need a global alertToShow as alert to be able to dismiss it when
	// needed and other things:
	private AlertDialog alertToShow;

	// We need a SpeakText object to interrupt in add bet method the voices,
	// this way is OK when new bet is with shuffling:
	SpeakText speak;

	// The constructor to initialise context etc:
	public Dealer(Context context, boolean isSaveBet) {
		this.context = context;
		// this.isSaveBet = isSaveBet;

		set = new Settings(context);
		speak = new SpeakText(context);

		myTotalMoney = getMoney();
		if (isSaveBet) {
			lastBet = getLastBet();
		} else {
			lastBet = 1;
		} // end if is without saving the bet.
		currentBet = lastBet;
	} // end constructor.

	// A function to get from saved settings the money:
	private int getMoney() {
		return set.getIntSettings("myTotalMoney");
	} // end get the value of wallet.

	// A function to get from saved settings the last bet:
	private int getLastBet() {
		return set.getIntSettings("lastBet");
	} // end get the value of wallet.

	// A method to save the money into settings file:
	public void saveMoney(int cuantum) {
		set.saveIntSettings("myTotalMoney", cuantum);
	} // end save money into settings.

	// A method to save the last bet into settings file:
	public void saveLastBet(int cuantum) {
		if (this.isSaveBet) {
			set.saveIntSettings("lastBet", cuantum);
		}
	} // end save money into settings.

	// Add a bet on the middle of the table:
	public boolean addBet() {
		boolean success = false; // we started with the idea that's bad , not
									// enough money.

		if (lastBet <= myTotalMoney) {
			// Play the bet sound only if there is something bet:
			if (lastBet > 0) {
				SoundPlayer.playSimple(context, "bet_money");
			} // end if was bet at least one dollar.
			currentBet = lastBet;
			isBet = true;
			success = true;
		} // end if there are enough money.
		else {
			// An alert which announces that you haven't enough money.
			String title = context.getString(R.string.warning);
			String message = String.format(
					context.getString(R.string.not_enough_money_to_bet),
					MainActivity.nickname, "" + lastBet, "" + myTotalMoney);
			if (myTotalMoney <= 0) {
				// You can play just for fun:
				message = String.format(
						context.getString(R.string.play_just_for_fun),
						MainActivity.curNickname);
			}
			GUITools.alert(context, title, message);
		}

		return success;
	} // end add bet.

	// A function to change the bet when a currentBet is not in progress:
	public void changeBet() {
		// Only if there is no a hand in progress:
		if (!isBet) {

			// A string to get from resource the texts:
			String tempMessage;
			AlertDialog.Builder alert = new AlertDialog.Builder(context);

			// The title:
			tempMessage = context.getString(R.string.bet_dialog_title);
			alert.setTitle(tempMessage);

			// The body:
			tempMessage = String.format(
					context.getString(R.string.bet_dialog_body),
					MainActivity.curNickname);
			alert.setMessage(tempMessage);

			// Set an EditText view to get user input
			final EditText input = new EditText(context);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			// Add also an action listener:
			input.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						// Next two lines are also found in the listener of the
						// OK button of this alert dialog:
						String tempBet = input.getText().toString();
						checkAndChangeBet(tempBet);
						alertToShow.dismiss();
					}
					return false;
				}
			});
			// End add action listener for the IME done button of the keyboard..

			alert.setView(input);

			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// Next two lines are also found in the listener of
							// the IME done of the keyboard:
							String tempBet = input.getText().toString();
							checkAndChangeBet(tempBet);
						}
					});

			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// cancelled.
						}
					});

			alertToShow = alert.create();
			alertToShow.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			alertToShow.show();

			// end of alert dialog with edit sequence.

			saveLastBet(lastBet);
		} // end if is not a bet in progress.
	} // end changeBet method.

	// A method which is performed when OK or Done button of the keyboard is
	// pressed in change bet method:
	private void checkAndChangeBet(String tempBet) {

		if (isNumeric(tempBet)) {
			// We extract from here the result if is a numeric value::
			int newBet = Integer.parseInt(tempBet);
			newBet = Math.abs(newBet);

			// Check if this new attempt is greater than total money:
			if (newBet > myTotalMoney) {
				String title = context.getString(R.string.warning);
				String message = String.format(
						context.getString(R.string.not_enough_money_to_bet),
						MainActivity.nickname, "" + newBet, "" + myTotalMoney);
				if (myTotalMoney <= 0) {
					// You can play just for fun:
					message = String.format(
							context.getString(R.string.play_just_for_fun),
							MainActivity.curNickname);
				}
				GUITools.alert(context, title, message);
			} // end if is a greater value than 0.
			else {
				lastBet = newBet;
				// Play a sound for this action;
				if (lastBet > 0) {
					SoundPlayer.playSimple(context, "bet_changed");
				} // end if was bet at least one dollar.
			}

			currentBet = lastBet;
			if (this.isSaveBet) {
				saveLastBet(lastBet);
			}
		} // end if is a numeric value inserted in edit text.
	} // end check and change bet method when OK or done IME button is pressed.

	// A method to change bet by game, not by user:
	public void changeBetByForce(int newBet) {
		/*
		 * If is not save bet, it means we can change also the last bet, for
		 * instance we need this in Slot Machine. This value will not be saved:
		 */
		if (this.isSaveBet) {
			currentBet = newBet;
		} else {
			lastBet = newBet;
			currentBet = lastBet;
		}
	} // end change bet by force, for instance when is double in a blackjack.

	public void win() {
		// We calculate first the bonus, based on actualBonusPercentage, a
		// static field of the saloon class:
		float bonus = SaloonActivity.actualBonusPercentage * currentBet / 100;
		int iBonus = Math.round(bonus);
		myTotalMoney = myTotalMoney + currentBet + iBonus;
		saveMoney(myTotalMoney);

		// Calculate bonus totals, for current session and from beginning:
		// The current session bonus will be set to 0 when the game is restarted
		// from scratch:
		curMoneyWonAsBonus = curMoneyWonAsBonus + iBonus;
		// Money won as bonus in last 24 hours will be saved in Shared Settings:
		moneyWonAsBonus24 = moneyWonAsBonus24 + iBonus;
		set.saveIntSettings("moneyWonAsBonus24", moneyWonAsBonus24);
		// The bonus since start of client status will be saved in
		// SharedSettings:
		moneyWonAsBonus = moneyWonAsBonus + iBonus;
		set.saveIntSettings("moneyWonAsBonus", moneyWonAsBonus);

		// currentBet = lastBet;
		increaseNumberOfGamesForOrder();
		if (lastBet > 0) {
			SoundPlayer.playSimple(context, "won_money");
		} // end if was bet at least one dollar.
		else {
			SoundPlayer.playSimple(context, "win_game");
		} // end if was not something bet, another sound for win and lose.
		isBet = false;

		// Check each time is a win if it is an insolvency:
		checkForInsolvency();
	} // end of win.

	public void draw() {
		// We need this method to set isBet to false, to be possible to change
		// bet when a game ends as draw:
		increaseNumberOfGamesForOrder();
		isBet = false;
	} // end draw method.

	public void lose() {
		// Play sound only if it was at least one dollar bet:
		if (lastBet > 0) {
			SoundPlayer.playSimple(context, "lost_money");
		} // end if was bet at least one dollar.
		else {
			SoundPlayer.playSimple(context, "lose_game");
		} // end if was not something bet, another sound for win and lose.
		myTotalMoney -= currentBet;
		// We don't let it to be minus in poket:
		if (myTotalMoney < 0) {
			myTotalMoney = 0;
		}
		saveMoney(myTotalMoney);
		// currentBet = lastBet;
		increaseNumberOfGamesForOrder();
		isBet = false;
	} // end of lose.

	// A boolean function to check if a string is number:
	public static boolean isNumeric(String str) {
		try {
			int d = Integer.parseInt(str);
			d = d + 1; // just for fun, not to appear an unused d.
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	} // end if String is number.

	// A method to increase the number of games before an order:
	public void increaseNumberOfGamesForOrder() {
		// Increase the numberOfOrders since last order:
		int numberOfGamesForOrder = set.getIntSettings("numberOfGamesForOrder");
		numberOfGamesForOrder = numberOfGamesForOrder + 1;
		SaloonActivity.numberOfGamesForOrder = numberOfGamesForOrder;
		set.saveIntSettings("numberOfGamesForOrder",
				SaloonActivity.numberOfGamesForOrder);
	} // end method to increase the number of games before a new order.

	// A method to go to InsolvencyActivity:
	private void goToInsolvencyActivity() {
		Intent intent = new Intent(context, InsolvencyActivity.class);
		context.startActivity(intent);
	} // end goToInsolvencyActivity.

	public void checkForInsolvency() {
		// Check if the myMoney variable is greater than insolvency:
		if (myTotalMoney >= insolvency) {
			goToInsolvencyActivity();
		} // end if player has more money than the insolvency threshold.
	} // end check for insolvency.

} // end of class Dealer.
