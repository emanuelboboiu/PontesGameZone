package ro.pontes.pontesgamezone;

/*
 * Class started on 06 October 2014, 01:20 by Manu.
 * This class contains all necessary properties and methods for a hand of various number of dice.
 */

import java.util.Arrays;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Dice {

	Context context; // we need context in this class.
	public int numberOfDice = 0;
	public int[] aDice; // an array for number of dice.
	private SpeakText speak;
	private static Random rand;
	public static boolean isDiceSpeaking = false;

	//
	private String resPossesionString; // for content description: Dealer is 0,
										// player is 1.
	private String[] aPossesion; // the array which contains at 0 Dealer, 1
									// player.

	// The constructor:
	public Dice(int numberOfDice, Context context) {
		this.context = context;
		aDice = new int[numberOfDice]; // initialise the dice set array.
		this.numberOfDice = numberOfDice;
		rand = new Random();
		speak = new SpeakText(this.context);
		removeAllDice(); // make all dice as 0.

		// Get the values to have strings for drawing the images:
		Resources res = this.context.getResources();
		aPossesion = res.getStringArray(R.array.possesion_array);
		resPossesionString = res.getString(R.string.die_name_extended);
		aPossesion[1] = MainActivity.curNickname;
	} // end constructor.

	// The method to throw dice:
	public void throwDice() {

		SoundPlayer.playWaitFinal(context, "dice");

		// We need a Random object, it is instanced at the beginning of the
		// class.:

		// Let's generate the dice here:
		for (int i = 0; i < aDice.length; i++) {
			// One die:
			int die = rand.nextInt(6) + 1;
			// Put it into the array:
			aDice[i] = die;
		}

		// Sort the dice if is set 1 or 2 for sortMethod:
		if (MainActivity.diceSortMethod == 1) {
			// Ascendant sorting:
			Arrays.sort(aDice);
		} else if (MainActivity.diceSortMethod == 2) {
			// Descendant sorting:
			Arrays.sort(aDice);
			// Now let's reverse the order:
			for (int i = 0; i < aDice.length / 2; i++) {
				int temp;
				temp = aDice[i];
				aDice[i] = aDice[aDice.length - (i + 1)];
				aDice[aDice.length - (i + 1)] = temp;
			} // end for.
		} // end if must be sort in descendant order.

		speakAllDiceWithVoice();
		// If dice are not spoken vocally, let's try to speak them with TTS:
		if (!MainActivity.isSoundDice) {
			speakAllDiceWithTTS();
		}

	} // end throwDice method.

	// A method to speak all dice with voice:
	public void speakAllDiceWithVoice() {
		// Play dice sounds if activated:
		if (MainActivity.isSoundDice == true) {
			// Let's try playing sound in a new thread:
			new Thread(new Runnable() {
				public void run() {
					isDiceSpeaking = true;
					if (MainActivity.isSound) {
						try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} // end if isSound is true.

					for (int i = 0; i < aDice.length; i++) {
						if (i < aDice.length - 1) {
							SoundPlayer.playWaitFinal(context,
									MainActivity.currentLanguage + aDice[i]);
						} else {
							SoundPlayer.playWaitFinal(context,
									MainActivity.currentLanguage + aDice[i]
											+ "a");
						}
					} // end for.
					isDiceSpeaking = false;
				}
			}).start();
			// End the thread for playing dice.
		} // end say numbers if is activated.
	} // end speak dice with voice method.

	// A method to speak dice with TTS:
	public void speakAllDiceWithTTS() {
		String message = "";
		// Create the string from the array:
		for (int i = 0; i < aDice.length; i++) {
			message += aDice[i] + ", ";
		}
		// Cut the last comma:
		message = message.substring(0, message.length() - 2);
		speak.say(message, false);
		message = null;
	} // end speak dice with TTS.

	// A method to remove a die from the array aDice:
	public void removeADie(int position) {
		// We don't really remove the die, we make it 0.
		aDice[position] = 0;
	} // end remove a dice from the array aDice.

	public void removeAllDice() {
		for (int i = 0; i < aDice.length; i++) {
			aDice[i] = 0;
		}
	} // end remove all dice.

	public int getTotal() {
		int total = 0;
		for (int i = 0; i < aDice.length; i++) {
			total += aDice[i];
		}
		return total;
	} // end give the total of all thrown dice.

	public boolean isDouble() {
		boolean isDouble = true;
		if (aDice.length < 2) {
			// There are now dice, there are less than 2 dice:
			isDouble = false;
		} else {

			int x = aDice[0];
			for (int i = 1; i < aDice.length; i++) {
				if (x != aDice[i]) {
					isDouble = false;
					break;
				}
			} // end for.
		} // end if there are at least 2 dice.

		return isDouble;
	} // end method to say if it is a double.

	// A method to draw this hand of cards in a LinearLayout passed as string
	// parameter:
	public void draw(LinearLayout paramLL, int whoIs) {

		// Show the dice on the screen in a linear layout view:
		// Find the LinearLayout:
		LinearLayout ll = paramLL;
		// Clear if there is something there:
		if (ll.getChildCount() > 0)
			ll.removeAllViews();

		// Just if there is at least a die in this hand:
		if (numberOfDice > 0) {

			for (int i = 0; i < numberOfDice; i++) {

				int tempDie = aDice[i];
				String dieFileName = "d" + tempDie;

				ImageView mImage = new ImageView(context);
				String uri = "@drawable/" + dieFileName; // the name of the
															// image
															// dynamically.
				int imageResource = context.getResources().getIdentifier(uri,
						null, context.getPackageName());
				@SuppressWarnings("deprecation")
				Drawable res = context.getResources()
						.getDrawable(imageResource);
				mImage.setImageDrawable(res);
				String tempString = String.format(resPossesionString,
						aPossesion[whoIs], "" + tempDie);
				mImage.setContentDescription(tempString);
				ll.addView(mImage);
			} // end if there is at least a die in hand.
		} // end for.

	} // end show images method.

} // end class Dice.
