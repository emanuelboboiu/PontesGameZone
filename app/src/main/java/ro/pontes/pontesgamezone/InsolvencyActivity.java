package ro.pontes.pontesgamezone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InsolvencyActivity extends Activity {

	private String name = "";
	private int money = 0;

	private boolean wasSent = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_insolvency);

		// Format the explanations text view:
		TextView tv = (TextView) findViewById(R.id.tvInsExplanations);
		tv.setText(String.format(getString(R.string.tv_ins_explanations),
				MainActivity.curNickname, Dealer.insolvency));

		// Call a method which contains the actions for insolvency:
		insolvencyActions();
	} // end onCreate method.

	// A method to go to main activity:
	public void startMainActivity(View view) {
		if (wasSent) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		} else {
			onKeyDown(KeyEvent.KEYCODE_BACK, null);
		}

	} // end startMainActivity

	// A method for actions at insolvency:
	private void insolvencyActions() {
		// First start playing a sound for this event, insolvency:
		playInsolvencySound();

		// Keep some values for post:
		money = Dealer.myTotalMoney;

		Settings set = new Settings(this);
		// Make money only $1000 all other money will be saved in the bank
		// account:
		Dealer.myTotalMoney = 1000;
		set.saveIntSettings("myTotalMoney", 1000);

		// Post statistics about this insolvency:
		Statistics.postStats("20", 1); // 20 is the id of the insolvency event
										// in DB.
	} // end insolvencyActions method.

	// Method to play sound from XML button:
	public void playInsolvency(View view) {
		playInsolvencySound();
	}

	// A method with sound of insolvency:
	private void playInsolvencySound() {
		SoundPlayer.playSimple(this, "insolvency_intro");
	} // end playInsolvencySound() method.

	public void postInsolvency(View view) {
		// Get the edit text content to have your name:
		EditText et = (EditText) findViewById(R.id.etName);
		name = et.getText().toString();

		// Check if the text contains at least 3 characters:
		if (name.length() >= 3) {
			// Post this statistic, and disable the edit text:
			et.setEnabled(false);
			// Disable also the send button:
			Button bt = (Button) findViewById(R.id.btInsSend);
			bt.setEnabled(false);

			Statistics.postInsolvency(name, money);
			wasSent = true;
			// Play a sound if information was sent:
			SoundPlayer.playSimple(this, "send_insolvency");
		} else {
			// Show an alert about this error:
			GUITools.alert(this, getString(R.string.warning),
					getString(R.string.name_not_written_correctly));
		}
	} // end post insolvency method.

	// To ask for confirmation if the performance was not sent:
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Handle the back button
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// Ask the user if they want to quit if the wasSent is false:
			if (!wasSent) {
				new AlertDialog.Builder(this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.confirmation)
						.setMessage(R.string.really_leave_page)
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										// Stop the activity
										InsolvencyActivity.this.finish();
									}

								}).setNegativeButton(R.string.no, null).show();

				return true;
			} else {
				return super.onKeyDown(keyCode, event);
			}

		} // end if it was not sent.
		else {
			// If it was already sent:
			InsolvencyActivity.this.finish();
			return super.onKeyDown(keyCode, event);
		}

	}
} // end InsolvencyActivity.
