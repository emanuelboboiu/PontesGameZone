package ro.pontes.pontesgamezone;

import android.content.Context;

/*
 * A class inspired by David J. Eck.
 */
public class BlackjackHand extends Hand {

	// The constructor:
	public BlackjackHand(Context context) {
		super(context); // the Hand class in this package.
	} // end constructor.

	/**
	 * Computes and returns the value of this hand in the game of Blackjack.
	 */
	public int getBlackjackValue() {

		int val; // The value computed for the hand.
		boolean ace; // This will be set to true if the
		// hand contains an ace.
		int cards; // Number of cards in the hand.

		val = 0;
		ace = false;
		cards = getCardCount(); // (method defined in class Hand.)

		for (int i = 0; i < cards; i++) {
			// Add the value of the i-nth card in the hand.
			Card card; // The i-nth card;
			int cardVal; // The blackjack value of the i-nth card.
			card = getCard(i);
			cardVal = card.getValue(); // The normal value, 1 to 13.
			if (cardVal > 10) {
				cardVal = 10; // For a Jack, Queen, or King.
			}
			if (cardVal == 1) {
				ace = true; // There is at least one ace.
			}
			val = val + cardVal;
		}

		// Now, val is the value of the hand, counting any ace as 1.
		// If there is an ace, and if changing its value from 1 to
		// 11 would leave the score less than or equal to 21,
		// then do so by adding the extra 10 points to val.
		if (ace == true && val + 10 <= 21) {
			val = val + 10;
		}

		return val;
	} // end getBlackjackValue()

} // end class BlackjackHand which extends hand class.
