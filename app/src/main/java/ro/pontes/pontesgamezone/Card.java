package ro.pontes.pontesgamezone;

import android.content.Context;
import android.content.res.Resources;

/**
 * An object of type Card represents a playing card from a standard Poker deck,
 * including Jokers. The card has a suit, which can be spades, hearts, diamonds,
 * clubs, or joker. A spade, heart, diamond, or club has one of the 13 values:
 * ace, 2, 3, 4, 5, 6, 7, 8, 9, 10, jack, queen, or king. Note that "ace" is
 * considered to be the smallest value. A joker can also have an associated
 * value; this value can be anything and can be used to keep track of several
 * different jokers.
 */

public class Card {

	// Codes for the 4 suits, plus Joker.
	public final static int CLUBS = 1;
	public final static int DIAMONDS = 2;
	public final static int HEARTS = 3;
	public final static int SPADES = 4;
	public final static int JOKER = 5;
	public final static int BACK_COLOR = 0; // the reverse of a card.

	public final static int ACE = 1; // Codes for the non-numeric cards.
	public final static int JACK = 11; // Cards 2 through 10 have their
	public final static int QUEEN = 12; // numerical values for their codes.
	public final static int KING = 13;
	public final static int BACK = 0; // the reverse of the card.

	// Two arrays for file name of the images:
	private static String[] valuesFirstLetter = new String[] { "x", "ace",
			"two", "three", "four", "five", "six", "seven", "eight", "nine",
			"ten", "jack", "queen", "king" };
	private static String[] suitsFirstLetter = new String[] { "x", "c", "d",
			"h", "s" };

	/**
	 * This card's suit, one of the constants SPADES, BACK_CARD, HEARTS,
	 * DIAMONDS, CLUBS, or JOKER. The suit cannot be changed after the card is
	 * constructed.
	 */
	private final int suit;

	/**
	 * The card's value. For a normal card, this is one of the values 1 through
	 * 13, with 1 representing ACE. For a JOKER, the value can be anything. The
	 * value cannot be changed after the card is constructed.
	 */
	private final int value;

	/*
	 * Two arrays to have the names of cards in a certain language:
	 */
	private static String[] suitNames;
	private static String[] valueNames;
	private static String resCardNameString = "none"; // the resource with
														// placeholder for a
														// card name. // the
														// string to be returned
														// by toString method.

	/*
	 * We need also a context for different things:
	 */
	Context context;

	/**
	 * Creates a card with a specified suit and value.
	 * 
	 * @param theValue
	 *            the value of the new card. For a regular card (non-joker), the
	 *            value must be in the range 1 through 13, with 1 representing
	 *            an Ace. You can use the constants Card.ACE, Card.JACK,
	 *            Card.QUEEN, and Card.KING. For a Joker, the value can be
	 *            anything.
	 * @param theSuit
	 *            the suit of the new card. This must be one of the values
	 *            Card.SPADES, Card.HEARTS, Card.DIAMONDS, Card.CLUBS, or
	 *            Card.JOKER.
	 * @throws IllegalArgumentException
	 *             if the parameter values are not in the permissible ranges
	 */
	public Card(int theValue, int theSuit, Context context) {
		this.context = context;
		/*
		 * if(theSuit != SPADES && theSuit != HEARTS && theSuit != DIAMONDS &&
		 * theSuit != CLUBS && theSuit != JOKER && theSuit != BACK_COLOR) throw
		 * new IllegalArgumentException("Illegal playing card suit"); if
		 * (theSuit != BACK && theSuit != JOKER && (theValue < 1 || theValue >
		 * 13)) throw new
		 * IllegalArgumentException("Illegal playing card value");
		 */

		value = theValue;
		suit = theSuit;

		// Ask for resources strings only first time:
		if (resCardNameString.equals("none")) {
			// Get things to have good strings:
			Resources res = this.context.getResources();
			suitNames = res.getStringArray(R.array.suits_array);
			valueNames = res.getStringArray(R.array.values_array);
			resCardNameString = res.getString(R.string.card_name);
		} // end if is first time, the last string is not null like when it's
			// declared at the beginning of the class..

	} // end constructor.

	/**
	 * Returns the suit of this card.
	 * 
	 * @returns the suit, which is one of the constants Card.SPADES,
	 *          Card.HEARTS, Card.DIAMONDS, Card.CLUBS, or Card.JOKER
	 */
	public int getSuit() {
		return suit;
	}

	/**
	 * Returns the value of this card.
	 * 
	 * @return the value, which is one of the numbers 1 through 13, inclusive
	 *         for a regular card, and which can be any value for a Joker.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Returns a String representation of the card's suit.
	 * 
	 * @return one of the strings "Spades", "Hearts", "Diamonds", "Clubs" or
	 *         "Joker".
	 */
	public String getSuitAsString() {
		return suitNames[this.suit];
	} // end return suit as string.

	/**
	 * Returns a String representation of the card's value.
	 * 
	 * @return for a regular card, one of the strings "Ace", "2", "3", ...,
	 *         "10", "Jack", "Queen", or "King". For a Joker, the string is
	 *         always numerical.
	 */
	public String getValueAsString() {
		if (suit == JOKER)
			return "" + value;
		else {
			return valueNames[this.value];
		}

	} // end return value as string.

	// A method to return the file name of current card as string:
	public String toFileName() {
		return valuesFirstLetter[getValue()] + "_"
				+ suitsFirstLetter[getSuit()];
	} // end toFileName method.

	/**
	 * Returns a string representation of this card, including both its suit and
	 * its value (except that for a Joker with value 1, the return value is just
	 * "Joker"). Sample return values are: "Queen of Hearts", "10 of Diamonds",
	 * "Ace of Spades", "Joker", "Joker #2"
	 */
	public String toString() {
		if (suit == JOKER) {
			if (value == 1)
				return "Joker";
			else
				return "Joker #" + value;
		} else {
			String cardName = String.format(resCardNameString,
					valueNames[this.value], suitNames[this.suit]);
			return cardName;
		}
	}

} // end class Card
