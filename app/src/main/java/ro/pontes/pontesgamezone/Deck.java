package ro.pontes.pontesgamezone;

import android.content.Context;

/**
 * An object of type Deck represents a deck of playing cards. The deck is a
 * regular poker deck that contains 52 regular cards and that can also
 * optionally include two Jokers.
 */

public class Deck {

	/*
	 * A variable for context, to be used all over the class.
	 */
	private Context context;

	/*
	 * A variable to play or not sounds in deck:
	 */
	public boolean isDeckSounds = true;

	/*
	 * A variable to know if there is a deck with 40 cards for Scopa:
	 */
	public boolean isScopaDeck = false;

	/**
	 * An array of 52 or 54 cards. A 54-card deck contains two Jokers, in
	 * addition to the 52 cards of a regular poker deck.
	 */
	private Card[] deck;

	/**
	 * Keeps track of the number of cards that have been dealt from the deck so
	 * far.
	 */
	private int cardsUsed;

	/**
	 * Constructs a regular 52-card poker deck. Initially, the cards are in a
	 * sorted order. The shuffle() method can be called to randomize the order.
	 * (Note that "new Deck()" is equivalent to "new Deck(0)", a 52 standard
	 * deck with sounds activated.)
	 */
	public Deck(Context context) {
		this(0, true, context); // Just call the other constructor in this
								// class.
	}

	/**
	 * Constructs a poker deck of playing cards, The deck contains the usual 52
	 * cards and can optionally contain two Jokers in addition, for a total of
	 * 54 cards. Initially the cards are in a sorted order. The shuffle() method
	 * can be called to randomize the order.
	 * 
	 * @param deck
	 *            type if 1, two Jokers are included in the deck; if 0, there
	 *            are no Jokers in the deck of 52 cards, if 2 there is a 40
	 *            cards deck for Scopa.
	 */
	public Deck(int deckType, boolean isDeckSounds, Context context) {
		this.isDeckSounds = isDeckSounds;
		this.context = context;
		boolean includeJokers = false;
		int limitForValues = 13;

		if (deckType == 1) {
			// A 54 deck, standard plus jokers:
			includeJokers = true;
			deck = new Card[54];
		} else if (deckType == 2) {
			// A 40 cards deck for Scopa game:
			this.isScopaDeck = true;
			deck = new Card[40];
			limitForValues = 10;
		} else {
			deck = new Card[52];
		}

		int cardCt = 0; // How many cards have been created so far.
		for (int suit = 1; suit <= 4; suit++) {
			for (int value = 1; value <= limitForValues; value++) {
				deck[cardCt] = new Card(value, suit, this.context);
				cardCt++;
			}
		}
		if (includeJokers) {
			deck[52] = new Card(1, Card.JOKER, this.context);
			deck[53] = new Card(2, Card.JOKER, this.context);
		} // end if there are Jokers.
		cardsUsed = 0;
		this.shuffle(); // shuffle the cards for first time, when the deck is
						// created as a new object.
	} // end constructor.

	/**
	 * Put all the used cards back into the deck (if any), and shuffle the deck
	 * into a random order.
	 */
	public void shuffle() {

		// The corresponding sound for shuffling the deck:
		if (isDeckSounds) {
			SoundPlayer.playSimple(context, "cards_shuffle");
		}

		for (int i = deck.length - 1; i > 0; i--) {
			int rand = (int) (Math.random() * (i + 1));
			Card temp = deck[i];
			deck[i] = deck[rand];
			deck[rand] = temp;
		}
		cardsUsed = 0;
	}

	/**
	 * As cards are dealt from the deck, the number of cards left decreases.
	 * This function returns the number of cards that are still left in the
	 * deck. The return value would be 52 or 54 (depending on whether the deck
	 * includes Jokers) when the deck is first created or after the deck has
	 * been shuffled. It decreases by 1 each time the dealCard() method is
	 * called.
	 */
	public int cardsLeft() {
		return deck.length - cardsUsed;
	}

	/**
	 * Removes the next card from the deck and return it. It is illegal to call
	 * this method if there are no more cards in the deck. You can check the
	 * number of cards remaining by calling the cardsLeft() function.
	 * 
	 * @return the card which is removed from the deck.
	 * @throws IllegalStateException
	 *             if there are no cards left in the deck
	 */
	public Card dealCard() {
		if (cardsUsed == deck.length) {
			this.shuffle();
		}
		if (isDeckSounds) {
			// Play in another thread, this way it is possible to be better the
			// playWait method of the SoundPlayer class:
			new Thread(new Runnable() {
				public void run() {
					SoundPlayer.playSimple(context, "card_take");
				}
			}).start();
			try {
				Thread.sleep(GUITools.random(250, 400));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// end thread to play the throwing sound.
		} // end if is sound activated.
		cardsUsed++;
		return deck[cardsUsed - 1];
		// Programming note: Cards are not literally removed from the array
		// that represents the deck. We just keep track of how many cards
		// have been used.
	}

	/**
	 * Test whether the deck contains Jokers.
	 * 
	 * @return true, if this is a 54-card deck containing two jokers, or false
	 *         if this is a 52 card deck that contains no jokers.
	 */
	public boolean hasJokers() {
		return (deck.length == 54);
	}

} // end class Deck
