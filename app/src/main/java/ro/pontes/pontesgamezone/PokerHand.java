package ro.pontes.pontesgamezone;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;

public class PokerHand extends Hand {

	// creates an array for all values of the cards in hand.
	private int[] aValues;
	private int[] aColors;
	private long relativeRank; // relativeRank is the value of a type of
								// absoluteRank.
	private int absoluteRank; // absoluteRank is for the type of hand.

	// The constructor:
	public PokerHand(Context context) {
		super(context); // the Hand class in this package.
	} // end constructor.

	/*
	 * A method which over reads the super method from Hand class, but reverse
	 * the order of the cards to be from biggest to the smallest. The ace is
	 * considered the biggest card, even it is 1 in the Card object.
	 */
	@Override
	public void sortByValue() {
		// We call first the super.sortByValue() method, to have them sort
		// ascendent:
		super.sortByValue();

		// Reverse the order of the cards excepting the ace which will remain
		// first, even it is 1 as value in the Card class:
		ArrayList<Card> newHand = new ArrayList<Card>();

		int pos = hand.size() - 1; // the biggest position in the hand array
									// list.
		while (hand.size() > 0) {
			Card c = hand.get(pos);
			if (c.getValue() == 1) {
				// If is an ace, let's add it to the 0 index, to be first in the
				// hand:
				newHand.add(0, c);
			} else {
				newHand.add(c);
			}
			hand.remove(pos);
			pos--;
		} // end while.

		hand = newHand;
	} // end sort by value method.

	/**
	 * Computes and returns a numeric value of this hand in the game of Pontes
	 * Poker. 0 means Straight flush, 8 means no_pair, 9 means no cards in hand.
	 */
	public int getPokerValue() {
		int val = 8; // default is 8, no_pair.
		fillValuesAndColorsArrays(); // to have arrays with values and colours
										// for processing.

		if (getCardCount() == 0) {
			// no cards in hand, we return 9:
			val = 9;
		} else if (hand.get(0).getValue() == 0) {
			// It means there is at least a card with 0 as value, a reverse of
			// card:
			val = 9;
		} else {
			// If there are cards in hand, we detect what kind of hand we have:
			if (isStraightFlush()) {
				val = 0;
			} else if (isFourOfAKind()) {
				val = 1;
			} else if (isFullHouse()) {
				val = 2;
			} else if (isFlush()) {
				val = 3;
			} else if (isStraight()) {
				val = 4;
			} else if (isThreeOfAKind()) {
				val = 5;
			} else if (isTwoPair()) {
				val = 6;
			} else if (isPair()) {
				val = 7;
			} else {
				// No-pair, high card situation:
				// We reverse the order, the biggest value to be the first:
				// We calculate the rank of this type of hand, in case the hands
				// we'll be equals:
				relativeRank = calculateRelativeRank(reverseIntArray(aValues));
				// val = 8;
			}
		} // end if there are cards in hand.
		absoluteRank = val;
		return absoluteRank;
	} // end getPokerValue()

	// A method to create arrays of values and colours as integers:
	private void fillValuesAndColorsArrays() {
		aValues = new int[getCardCount()];
		aColors = new int[getCardCount()];
		for (int i = 0; i < aValues.length; i++) {
			Card c = getCard(i);
			aValues[i] = c.getValue();
			aColors[i] = c.getSuit();

			// We make the Ace from 1 to 14, is better to make comparisons:
			if (aValues[i] == 1) {
				aValues[i] = 14;
			} // end replace the value of an Ace with 14.
		} // end for.

		// Only tests:
		/*
		 * if(aValues.length==5) { aValues[0]=4; aValues[1]=4; aValues[2]=16;
		 * aValues[3]=9; aValues[4]=1;
		 * 
		 * } // end if there are 5 cards.
		 */
		// end tests.

		Arrays.sort(aValues);
		Arrays.sort(aColors);
	} // end method to fill the arrays with values and colours in hand.

	// A method to detect if is a Straight flush:
	private boolean isStraightFlush() {
		boolean isSF = false;
		// It must be a Straight and a Flush together:
		if (isFlush() && isStraight()) {
			isSF = true;
		}
		return isSF;
	} // end detect if is a Straight flush.

	// A method to detect if is Four of a kind, the rank value is 1:
	private boolean isFourOfAKind() {
		boolean isFour = false;
		if (aValues.length < 5) {
			isFour = false;
		} // end if there are no 5 cards in hand.
		else {
			if (aValues[0] == aValues[3]) {
				isFour = true;
				// We take the one of the four and last card for relativeRank:
				relativeRank = calculateRelativeRank(aValues[2], aValues[4]);
			} else if (aValues[1] == aValues[4]) {
				// we take one of the four and the first card for relative rank:
				relativeRank = calculateRelativeRank(aValues[2], aValues[0]);
			}
		} // end if there are 5 cards in hand.
		return isFour;
	} // end if is Four of a kind.

	// A method to detect if is a Full house, the rank is 2:
	private boolean isFullHouse() {
		boolean isFull = false;
		if (aValues.length == 5) {
			if ((aValues[0] == aValues[1] && aValues[1] == aValues[2])
					&& (aValues[3] == aValues[4])) {
				isFull = true;
				// The relative rank is the number composed by the middle and
				// the last one found in aValues:
				relativeRank = calculateRelativeRank(aValues[2], aValues[4]);
			} else if ((aValues[0] == aValues[1])
					&& (aValues[2] == aValues[3] && aValues[3] == aValues[4])) {
				isFull = true;
				// The relative rank is the number composed by the middle and
				// the first one found in aValues:
				relativeRank = calculateRelativeRank(aValues[2], aValues[0]);
			}
		} // end if there are five cards in hand.
		return isFull;
	} // end detect if is a Full house.

	// A method to detect if is colour in hand, a flush, the rank value is 3:
	private boolean isFlush() {
		boolean isFlush = false;
		if (aValues.length == 5) {
			// Check if first card has the same value like the last one, if not,
			// it means is not a flush:
			if (aColors[0] == aColors[aColors.length - 1]) {
				isFlush = true;
				// The relative rank of a Flush is like for no-pair:
				relativeRank = calculateRelativeRank(reverseIntArray(aValues));
			}
		} // end if there are five cards in hand.
		return isFlush;
	} // end isFlush method.

	// A method to detect if is a straight, rank value = 4:
	private boolean isStraight() {
		boolean isStraight = false;
		boolean isTemp = true;
		boolean isTemp2 = true;

		if (aValues.length == 5) {
			int x = aValues[0];
			for (int i = 0; i < aValues.length; i++) {
				if (aValues[i] != (x + i)) {
					isTemp = false;
					break;
				}
			} // end for.

			// Let's try if is the smallest straight, the ace is 1, only if it
			// is not already a straight from checking before:
			if (!isTemp) {
				if (aValues[4] == 14) {
					for (int i = 0; i < aValues.length - 1; i++) {
						if (aValues[i] != (2 + i)) {
							isTemp2 = false;
							break;
						}
					}
				} else {
					isTemp2 = false;
				}
			} // end was checked the second possibility for a Straight, Ace as
				// smallest card.
		} // end if there are 5 cards in hand.

		if (aValues.length == 5 && (isTemp || isTemp2)) {
			isStraight = true;
			if (isTemp) {
				relativeRank = aValues[2]; // we compare the middle card when
											// two Straight.
			} else if (isTemp2) {
				relativeRank = aValues[1]; // we compare the middle card but the
											// middle is the second one, the ace
											// is the last in the smallest
											// Straight.
			}
		}
		return isStraight;
	} // end if is a straight.

	// A method to detect if is Three of a kind, the rank is 5:
	private boolean isThreeOfAKind() {
		boolean isThree = false;
		int x = 0;
		for (int i = 1; i < aValues.length; i++) {
			if (aValues[i] == aValues[i - 1]) {
				x++; // we increment the x hoping it will be 3.
				if (x >= 2) {
					isThree = true;
					// The relative rank will be calculated depending of the
					// position of the three cards::
					if (i == 2) {
						// It means the relative rank will be composed of the
						// one of the first 3 values, the 5th and the 4th
						// values:
						relativeRank = calculateRelativeRank(aValues[0],
								aValues[4], aValues[3]);
					} else if (i == 3) {
						// It means the relative rank will be composed of the
						// one of the 2-4 values, the 5th and the first value:
						relativeRank = calculateRelativeRank(aValues[1],
								aValues[4], aValues[0]);
					} else if (i == 4) {
						// It means the relative rank will be composed of the
						// one of the 3-5 values, the 2nd and the first value:
						relativeRank = calculateRelativeRank(aValues[2],
								aValues[1], aValues[0]);
					}
					break;
				}
			} else {
				x = 0; // x Becomes 0, it means it is not equals, there were not
						// 3 of a kind..
			}
		} // end for.
		return isThree;
	} // end detect if is Three of a kind.

	// A method to detect if is Two pair:
	private boolean isTwoPair() {
		boolean isTwoPair = false;
		if (aValues.length > 4
				&& (aValues[0] == aValues[1] && aValues[2] == aValues[3])) {
			isTwoPair = true;
			// The relative rank is the number composed by 3th, the first and
			// the last value:
			relativeRank = calculateRelativeRank(aValues[2], aValues[0],
					aValues[4]);
		} else if (aValues.length > 4
				&& (aValues[0] == aValues[1] && aValues[3] == aValues[4])) {
			isTwoPair = true;
			// The relative rank is the number composed by 4th the first and the
			// middle value:
			relativeRank = calculateRelativeRank(aValues[3], aValues[0],
					aValues[2]);
		} else if (aValues.length > 4
				&& (aValues[1] == aValues[2] && aValues[3] == aValues[4])) {
			isTwoPair = true;
			// The relative rank is the number composed by 4th, the 2nd and the
			// first value:
			relativeRank = calculateRelativeRank(aValues[3], aValues[1],
					aValues[0]);
		}
		return isTwoPair;
	} // end detect if is Two pair.

	// A method to return true if is a pair of cards, rank value is 7:
	public boolean isPair() {
		boolean isPair = false;
		for (int i = 1; i < aValues.length; i++) {
			if (aValues[i] == aValues[i - 1]) {
				isPair = true;
				// We make an array with aValues.length - 1 for the values to
				// pass to calculateRelativeRank method:
				int[] aTempValues = new int[aValues.length - 1];
				aTempValues[0] = aValues[i]; // the first is the pair cards
												// value.
				int x = 0; // for the position in the new array.
				for (int j = aValues.length - 1; j >= 0; j--) {
					// If is not the value from pair, the values will be added
					// in descendant order here from aValues:
					if (!(j == i || (j == i - 1))) {
						x++;
						aTempValues[x] = aValues[j];
					}
				} // end for.
				relativeRank = calculateRelativeRank(aTempValues);
				break;
			}
		} // end for.
		return isPair;
	} // end isPair method.

	/*
	 * A method to calculate the relative rank of a hand, we have as arguments
	 * the integer values from the hand. For instance, the values as arguments
	 * for a No_pai hand are all the cards in descending order. For Four of a
	 * kind, the values passed here are the value of one of the four cards and
	 * the value of the fifth one. This is necessary when both have same type of
	 * hand.
	 */
	private long calculateRelativeRank(int... values) {
		long temp = 0;
		int[] aTemp = new int[values.length];
		/*
		 * Now we make the square root of each value in the numbers found as
		 * variable arguments. We try to have integers numbers for each value
		 * with two digits. We multiply each value by 10, after we get the floor
		 * of them.
		 */
		for (int i = 0; i < values.length; i++) {
			double x = Math.sqrt(values[i]);
			x = x * 10;
			aTemp[i] = (int) Math.floor(x);
		} // end for.

		temp = GUITools.concatenateNumbers(aTemp);

		return temp;
	} // end calculateRelativeRank.

	/*
	 * A method which returns the absoluteRank of a hand, 0 for Straight flush,
	 * 1 for Four of a kind etc..
	 */
	public int getAbsoluteRank() {
		return absoluteRank;
	} // end getAbsoluteRank method.

	/*
	 * A method to return the relative value of a type of hand.
	 */
	public long getRelativeRankOfAType() {
		return relativeRank;
	} // end get relative rank.

	// A method to reverse an array of integers:
	private int[] reverseIntArray(int[] array) {

		for (int i = 0; i < array.length / 2; i++) {
			int temp = array[i];
			array[i] = array[array.length - i - 1];
			array[array.length - i - 1] = temp;
		} // end for.

		return array;
	} // end method to reverse an array of integers.

} // end class PokerHand.
