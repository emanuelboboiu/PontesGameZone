package ro.pontes.pontesgamezone;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Hand {

    protected Context context;
    protected ArrayList<Card> hand; // The cards in the hand.

    private final String resPossesionString; // for content description: Dealer is 0,
    // player is 1.
    protected String[] aPossesion; // the array which contains at 0 Dealer, 1
    // player.

    // A static variable for padding of cards images shown on screen:
    int mPaddingDP;

    /**
     * Create a hand that is initially empty.
     */
    public Hand(Context context) {
        this.context = context;
        hand = new ArrayList<>();

        // Get the values to have strings to draw the images:
        Resources res = this.context.getResources();
        aPossesion = res.getStringArray(R.array.possesion_array);
        resPossesionString = res.getString(R.string.card_name_extended);
        // The 1 index of the aPossesion array will be current nickname, at
        // least temporary.
        // It was your until now:
        aPossesion[1] = MainActivity.curNickname;

        // Calculate the pixels in DP for mPaddingDP:
        int paddingPixel = 2;
        float density = context.getResources().getDisplayMetrics().density;
        mPaddingDP = (int) (paddingPixel * density);
    } // end constructor of Hand class.

    /**
     * Remove all cards from the hand, leaving it empty.
     */
    public void clear() {
        hand.clear();
    }

    /**
     * Add a card to the hand. It is added at the end of the current hand.
     *
     * @param c the non-null card to be added.
     * @throws NullPointerException if the parameter c is null.
     */
    public void addCard(Card c) {
        if (c == null) throw new NullPointerException("Can't add a null card to a hand.");
        hand.add(c);
    }

    // Change a card in hand:
    public void changeCard(int position, Card newCard) {
        if (position < 0 || position >= hand.size())
            throw new IllegalArgumentException("Position does not exist in hand: " + position);
        hand.set(position, newCard);
    } // end change card.

    /**
     * Remove a card from the hand, if present.
     *
     * @param c the card to be removed. If c is null or if the card is not in
     *          the hand, then nothing is done.
     */
    public void removeCard(Card c) {
        hand.remove(c);
    }

    /**
     * Remove the card in a specified position from the hand.
     *
     * @param position the position of the card that is to be removed, where
     *                 positions are starting from zero.
     * @throws IllegalArgumentException if the position does not exist in the hand, that is if the
     *                                  position is less than 0 or greater than or equal to the
     *                                  number of cards in the hand.
     */
    public void removeCard(int position) {
        if (position < 0 || position >= hand.size())
            throw new IllegalArgumentException("Position does not exist in hand: " + position);
        hand.remove(position);
    }

    /**
     * Returns the number of cards in the hand.
     */
    public int getCardCount() {
        return hand.size();
    }

    /**
     * Gets the card in a specified position in the hand. (Note that this card
     * is not removed from the hand!)
     *
     * @param position the position of the card that is to be returned
     * @throws IllegalArgumentException if position does not exist in the hand
     */
    public Card getCard(int position) {
        if (position < 0 || position >= hand.size())
            throw new IllegalArgumentException("Position does not exist in hand: " + position);
        return hand.get(position);
    }

    /**
     * Sorts the cards in the hand so that cards of the same suit are grouped
     * together, and within a suit the cards are sorted by value. Note that aces
     * are considered to have the lowest value, 1.
     */
    public void sortBySuit() {
        // Only if there are cards in hand:
        if (getCardCount() > 0) {
            ArrayList<Card> newHand = new ArrayList<>();
            while (hand.size() > 0) {
                int pos = 0; // Position of minimal card.
                Card c = hand.get(0); // Minimal card.
                for (int i = 1; i < hand.size(); i++) {
                    Card c1 = hand.get(i);
                    if (c1.getSuit() < c.getSuit() || (c1.getSuit() == c.getSuit() && c1.getValue() < c.getValue())) {
                        pos = i;
                        c = c1;
                    }
                }
                hand.remove(pos);
                newHand.add(c);
            }
            hand = newHand;
        } // end if there are cards in hand.
    }

    /**
     * Sorts the cards in the hand so that cards of the same value are grouped
     * together. Cards with the same value are sorted by suit. Note that aces
     * are considered to have the lowest value, 1.
     */
    public void sortByValue() {
        if (getCardCount() > 0) {
            ArrayList<Card> newHand = new ArrayList<>();
            while (hand.size() > 0) {
                int pos = 0; // Position of minimal card.
                Card c = hand.get(0); // Minimal card.
                for (int i = 1; i < hand.size(); i++) {
                    Card c1 = hand.get(i);
                    if (c1.getValue() < c.getValue() || (c1.getValue() == c.getValue() && c1.getSuit() < c.getSuit())) {
                        pos = i;
                        c = c1;
                    }
                }
                hand.remove(pos);
                newHand.add(c);
            }
            hand = newHand;
        } // end if there are cards in hand.
    }

    /**
     * Shuffle the cards in current hand.
     */
    public void shuffle() {
        if (getCardCount() > 0) {
            Random rand = new Random();
            ArrayList<Card> newHand = new ArrayList<>();

            while (hand.size() > 0) {
                int pos = rand.nextInt(hand.size());
                Card c = hand.get(pos);
                newHand.add(c);
                hand.remove(pos);
            } // end while.

            hand = newHand;
        } // end if there are cards in hand.
    } // end shuffle cards in current hand.

    // A method to draw this hand of cards in a LinearLayout passed as string
    // parameter:
    public void drawCards(LinearLayout paramLL, int whoIs) {
        /*
         * This is for images IDs, the final id will be the idBase plus the
         * index of the card: This way, the cards of dealer will be with the ID
         * from 1000 to 1004 if 5 cards are in hand. The cards for player will
         * be with IDs from 2000 to 2004 for 5 cards in hand.
         */
        int idBase = 1000;
        if (whoIs > 0) {
            // it means it is not the dealer, the idBase will be 2000:
            idBase = 2000;
        }

        // Show the cards on the screen in a linear layout view:
        // Find the LinearLayout:
        // Clear if there is something there:
        if (paramLL.getChildCount() > 0) paramLL.removeAllViews();

        // Just if there is at least card in this hand:
        if (getCardCount() > 0) {

            for (int i = 0; i < getCardCount(); i++) {

                Card tempCard = getCard(i);
                String cardFileName = tempCard.toFileName();

                ImageView mImage = new ImageView(context);
                String uri = "@drawable/" + cardFileName; // the name of the
                // image
                // dynamically.
                int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
                Drawable res = context.getResources().getDrawable(imageResource);
                mImage.setImageDrawable(res);
                mImage.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
                mImage.setId(idBase + i);
                String tempString = String.format(resPossesionString, aPossesion[whoIs], tempCard);
                mImage.setContentDescription(tempString);
                paramLL.addView(mImage);
            } // end for.
        } // end if there is at least a card in hand.
    } // end show image method.

    // A method to return all the cards in hand as string:
    public String cardsToString() {
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < getCardCount(); i++) {
            Card c = getCard(i);
            message.append(c.toString()).append(", ");
        } // end for.

        // Cut the last comma:
        message = new StringBuilder(message.substring(0, message.length() - 2));
        // Add a period:
        message.append(".");

        return message.toString();
    } // end cards to string.

} // end of class Hand.
