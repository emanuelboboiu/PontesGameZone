package ro.pontes.pontesgamezone;

import android.content.Context;

/*
 * Class started on 29 September 2014, 01:30, by Manu.
 */

public class CardsWarHand extends Hand {

    // The constructor:
    public CardsWarHand(Context context) {
        super(context); // the Hand class in this package.
    } // end constructor.

    /**
     * Computes and returns the value of this hand in the game of Blackjack.
     */
    public int getCardsWarValue() {

        int val; // The value computed for the hand.
        int cards; // Number of cards in the hand.

        val = 0;

        cards = getCardCount(); // (method defined in superclass Hand.)

        for (int i = 0; i < cards; i++) {
            // Add the value of the i-nth card in the hand.
            Card card; // The i-nth card;
            int cardVal; // The cards war value of the i-nth card.
            card = getCard(i);
            cardVal = card.getValue(); // The normal value, 1 to 13.
            if (cardVal == 1) {
                cardVal = 14; // For an ace.
            }
            val = val + cardVal;
        }

        // Now, val is the value of the hand, counting any ace as 14.

        return val;
    } // end getBlackjackValue()

} // end class CardsWarHand which extends hand class.
