package ro.pontes.pontesgamezone;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * An object of type DigitsHand represents a hand of digits which belong to
 * Digit class. A hand is empty when it's created, and any number of digits can
 * be added to it. This class is inspired by David J. Eck, see
 * math.hws.edu/javanotes/ Class started on 12 August 2015 by Manu.
 */

public class DigitsHand {

	protected Context context;
	protected ArrayList<Digit> hand; // The digits in the hand.

	protected String resPossesionString; // for content description: Dealer is
											// 0,
	// player is 1.
	protected String[] aPossesion; // the array which contains at 0 Dealer, 1
	// player.

	// A static variable for padding of digits images shown on screen:
	protected int mPaddingDP = 2;

	/**
	 * The constructor creates a hand that is initially empty.
	 */
	public DigitsHand(Context context) {
		this.context = context;
		hand = new ArrayList<Digit>();

		// Get the values to have strings to draw the images:
		Resources res = this.context.getResources();
		aPossesion = res.getStringArray(R.array.possesion_array);
		resPossesionString = res.getString(R.string.digit_name_extended);
		// The 1 index of the aPossesion array will be current nickname, at
		// least temporary.
		// It was your until now:
		aPossesion[1] = MainActivity.curNickname;

		// Calculate the pixels in DP for mPaddingDP:
		int paddingPixel = 2;
		float density = context.getResources().getDisplayMetrics().density;
		mPaddingDP = (int) (paddingPixel * density);
	} // end constructor of DigitHand class.

	/**
	 * Remove all digits from the hand, leaving it empty.
	 */
	public void clear() {
		hand.clear();
	}

	/**
	 * Add a digit to the hand. It is added at the end of the current hand.
	 * 
	 * @param d
	 *            the non-null digit to be added.
	 * @throws NullPointerException
	 *             if the parameter d is null.
	 */
	public void addDigit(Digit d) {
		if (d == null)
			throw new NullPointerException("Can't add a null Digit to a hand.");
		hand.add(d);
	}

	// Change a digit in hand:
	public void changeDigit(int position, Digit newDigit) {
		if (position < 0 || position >= hand.size())
			throw new IllegalArgumentException(
					"Position does not exist in hand: " + position);
		hand.set(position, newDigit);
	} // end change digit method.

	/**
	 * Remove a digit from the hand, if present.
	 * 
	 * @param d
	 *            the digit to be removed. If d is null or if the digit is not
	 *            in the hand, then nothing is done.
	 */
	public void removeDigit(Digit d) {
		hand.remove(d);
	}

	/**
	 * Remove the digit in a specified position from the hand.
	 * 
	 * @param position
	 *            the position of the digit that is to be removed, where
	 *            positions are starting from zero.
	 * @throws IllegalArgumentException
	 *             if the position does not exist in the hand, that is if the
	 *             position is less than 0 or greater than or equal to the
	 *             number of digits in the hand.
	 */
	public void removeDigit(int position) {
		if (position < 0 || position >= hand.size())
			throw new IllegalArgumentException(
					"Position does not exist in hand: " + position);
		hand.remove(position);
	}

	/**
	 * Returns the number of digits in the hand.
	 */
	public int getDigitCount() {
		return hand.size();
	}

	/**
	 * Gets the digit in a specified position in the hand. (Note that this digit
	 * is not removed from the hand!)
	 * 
	 * @param position
	 *            the position of the digit that is to be returned
	 * @throws IllegalArgumentException
	 *             if position does not exist in the hand
	 */
	public Digit getDigit(int position) {
		if (position < 0 || position >= hand.size())
			throw new IllegalArgumentException(
					"Position does not exist in hand: " + position);
		return hand.get(position);
	}

	// A method to return the integer value of a digit in a specified position:
	public int getValue(int position) {
		int val = -1;
		if (position < getDigitCount() && position >= 0) {
			val = hand.get(position).getValue();
		}
		return val;
	} // end getValue() method.

	// A method to draw this hand of digits in a LinearLayout passed as string
	// parameter:
	public void drawDigits(LinearLayout paramLL, int whoIs) {
		/*
		 * This is for images IDs, the final id will be the idBase plus the
		 * index of the digit: This way, the digits of dealer will be with the
		 * ID from 1000 to 1004 if 5 digits are in hand. The digits for player
		 * will be with IDs from 2000 to 2004 for 5 digits in hand.
		 */
		int idBase = 1000;
		if (whoIs > 0) {
			// it means it is not the dealer, the idBase will be 2000:
			idBase = 2000;
		}

		// Show the digits on the screen in a linear layout view:
		// Find the LinearLayout:
		LinearLayout ll = paramLL;
		// Clear if there is something there:
		if (ll.getChildCount() > 0)
			ll.removeAllViews();

		// Just if there is at least digit in this hand:
		if (getDigitCount() > 0) {

			for (int i = 0; i < getDigitCount(); i++) {

				Digit tempDigit = getDigit(i);
				String digitFileName = tempDigit.toFileName();

				ImageView mImage = new ImageView(context);
				String uri = "@drawable/" + digitFileName; // the name of the
				// image
				// dynamically.
				int imageResource = context.getResources().getIdentifier(uri,
						null, context.getPackageName());
				@SuppressWarnings("deprecation")
				Drawable res = context.getResources()
						.getDrawable(imageResource);
				mImage.setImageDrawable(res);
				mImage.setPadding(mPaddingDP, mPaddingDP, mPaddingDP,
						mPaddingDP);
				mImage.setId(idBase + i);
				String tempString = String.format(resPossesionString,
						aPossesion[whoIs], tempDigit.toString());
				mImage.setContentDescription(tempString);
				ll.addView(mImage);
			} // end for.
		} // end if there is at least a digit in hand.
	} // end show image method.

	// A method to return all the digits in hand as string:
	public String digitsToString() {
		String message = "";
		for (int i = 0; i < getDigitCount(); i++) {
			Digit d = getDigit(i);
			message = message + d.toString() + ", ";
		} // end for.

		// Cut the last comma:
		message = message.substring(0, message.length() - 2);
		// Add a period:
		message = message + ".";

		return message;
	} // end digits to string.

} // end DigitsHand class.
