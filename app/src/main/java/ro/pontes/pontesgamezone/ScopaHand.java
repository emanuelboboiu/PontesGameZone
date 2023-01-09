package ro.pontes.pontesgamezone;

import java.util.ArrayList;

import android.content.Context;

public class ScopaHand extends Hand {

    // An array for cards values in hand:
    public int[] aValues;
    public int[] aSuits;

    // The constructor:
    public ScopaHand(Context context) {
        super(context); // the Hand class in this package.
        super.aPossesion[0] = "";
    } // end constructor.

    @Override
    public void addCard(Card c) {
        super.addCard(c);
        fillValuesAndSuitsArrays();
    }

    @Override
    public void removeCard(Card c) {
        super.removeCard(c);
        fillValuesAndSuitsArrays();
    }

    @Override
    public void removeCard(int position) {
        super.removeCard(position);
        fillValuesAndSuitsArrays();
    }

    @Override
    public void sortByValue() {
        super.sortByValue();
        fillValuesAndSuitsArrays();
    }

    // A method to create arrays of values and colours as integers:
    private void fillValuesAndSuitsArrays() {
        this.aValues = new int[getCardCount()];
        this.aSuits = new int[getCardCount()];
        for (int i = 0; i < this.aValues.length; i++) {
            Card c = getCard(i);
            this.aValues[i] = c.getValue();
            this.aSuits[i] = c.getSuit();
        } // end for.
    } // end method to fill the arrays with values and suits in hand.

    // A method which returns number of diamonds in hand:
    public int getNumberOfDiamonds() {
        int nr = 0;
        for (int aSuit : aSuits) {
            if (aSuit == 2) {
                nr++;
            }
        }
        return nr;
    } // end getNumberOfDiamonds.

    // A method which returns number of sevens in hand:
    public int getNumberOfSevens() {
        int nr = 0;
        for (int aValue : aValues) {
            if (aValue == 7) {
                nr++;
            }
        }
        return nr;
    } // end getNumberOfSevens.

    // A method which detect if the hand has the seven of diamond:
    public boolean hasSevenOfDiamond() {
        boolean has = false;
        for (int i = 0; i < aValues.length; i++) {
            if (aValues[i] == 7 && aSuits[i] == 2) {
                has = true;
                break;
            }
        }
        return has;
    } // end hasSevenOfDiamond() method.

    /*
     * A method to make a clever move depending of the card given at parameter.
     * This method will be used for table hand only.
     */
    public ArrayList<Card> decideCardsToBeExtracted(Card c) {
        /*
         * // An array of integers, to know which positions will be taken of the
         * table:
         */
        ArrayList<Card> toTakeList = new ArrayList<>();

        // To set a stop in this method after a good solution is found:
        boolean isContinue = true;

        /*
         * First check of isContinue is just for fun, it is not necessary. We
         * check also if is not a Escoba variant, for those variants this if
         * branch is not necessary:
         */
        if (ScopaActivity.lsVariant != 2 && ScopaActivity.lsVariant != 4) {
            /*
             * First check if the value of current put cards exists on the
             * table:
             */
            /*
             * We need a variable to know if a diamond exists, it will be the
             * candidate to be given:
             */
            int whereIsDiamond = 0;
            for (int i = 0; i < aValues.length; i++) {
                if (c.getValue() == aValues[i]) {
                    isContinue = false;
                    toTakeList.add(getCard(i));
                    // Check now for diamond:
                    if (aSuits[i] == 2) {
                        whereIsDiamond = toTakeList.size() - 1;
                    } // end check for diamond position if it exists.
                } // end check if values are the same on table and in hand..
            } // end for.
            /*
             * If there are more cards of same value, we keep the diamond one if
             * it exists, this will be given to player. One card must remain
             * also:
             */
            if (toTakeList.size() > 1) {
                // If diamond is in another place than 0, we move it at 0 index:
                if (whereIsDiamond > 0) {
                    toTakeList.set(0, toTakeList.get(whereIsDiamond));
                } // end if diamond is in another position than 0.
                // Remove other cards, keep only 0 index:
                for (int i = toTakeList.size() - 1; i > 0; i--) {
                    toTakeList.remove(i);
                } // end for remove other positions than 0.
            } // end if there are more cards of same value.
        } // end if it was isContinue for cards of same type.

        /*
         * Check if the value of the card put down is a sum of cards on the
         * table. It happens only if it isContinue:
         */
        if (isContinue) {
            /*
             * We create an ArrayList of ArrayLists to take the possible
             * combinations of cards:
             */
            ArrayList<ArrayList<Card>> combinations = getSumsOfCards(c.getValue());
            // Only if there is at least a combination:
            if (combinations.size() > 0) {
                int best = decideBestCombination(combinations);
                toTakeList = combinations.get(best);
            } // end if combinations exist.
        } // end if isContinue for sum of cards.

        return toTakeList;
    } // end makeCleverMove() method.

    /*
     * A method which return an ArrayList of ArrayLists of cards with
     * possibilities for correct sum of cards:
     */
    private ArrayList<ArrayList<Card>> getSumsOfCards(int expectedSum) {
        /*
         * A bad practice here for Escoba variants, expectedSum will be the
         * value of the card in hand, and the real expected sum will be 15:
         */
        int curCardValue = 0;
        if (ScopaActivity.lsVariant == 2 || ScopaActivity.lsVariant == 4) {
            curCardValue = expectedSum;
            expectedSum = 15;
        } // end if it's Escoba variant.

        // We need the ArrayList of ArrayLists to return:
        ArrayList<ArrayList<Card>> toReturn = new ArrayList<>();

        /*
         * First calculate the number of combinations, it is 2 raised to number
         * of cards minus 1:
         */
        int numberOfCards = getCardCount();
        int limit = (int) Math.pow(2, numberOfCards);
        /*
         * Now a for to go step by step for each possibility, from 0 to limit:
         */
        for (int i = 1; i < limit; i++) {
            byte[] b = UsefulThings.toBinaryAsArrayOfByte(i, numberOfCards);
            /*
             * We go through all positions and add the validate cards to see if
             * they match the correct sum:
             */
            int sum = 0;
            for (int j = 0; j < b.length; j++) {
                // Check if is a card in current position of combination:
                if (b[j] == 1) {
                    sum = sum + getCard(j).getValue();
                } // end if is a card chosen in combination.
            } // end for going through some cards to be added to calculate the
            // sum to see if is expectedSum.

            // See now if is a correct sum:
            if ((sum + curCardValue) == expectedSum) {
                ArrayList<Card> combination = new ArrayList<>();
                // Add in a for the cards into ArrayList above:
                for (int j = 0; j < b.length; j++) {
                    if (b[j] == 1) {
                        combination.add(getCard(j));
                    }
                } // end for to add cards in the combination ArrayList.
                // Add now the combination ArrayList to the big ArrayList:
                toReturn.add(combination);
            } // end if sums are equals, a correct combination.
        } // end big for, going step by step through each possibility.

        return toReturn;
    } // end getSumsOfCards() method.

    // A method to decide the best combination in the combinations ArrayList:
    private int decideBestCombination(ArrayList<ArrayList<Card>> arr) {
        int best = 0;
        // If is only a combination, return 0:
        if (arr.size() > 1) {
            // An array for marks of combinations:
            int[] marks = new int[arr.size()];
            // We go through all combinations:
            for (int i = 0; i < arr.size(); i++) {
                // The initial mark is the number of cards:
                int mark = arr.get(i).size();
                // Another for to go through cards of current combination:
                for (int j = 0; j < arr.get(i).size(); j++) {
                    // If a card is diamond we give 2 points:
                    if (arr.get(i).get(j).getSuit() == 2) {
                        mark = mark + 2;
                    } // end if is a diamond.
                    // We give also 2 points if is a seven:
                    if (arr.get(i).get(j).getValue() == 7) {
                        mark = mark + 3;
                    } // end if is a seven.
                } // end for going through cards of current combination.
                marks[i] = mark;
            } // end for going through all combinations.
            // Determine the biggest mark as position in array:
            int biggest = 0;
            for (int j = 0; j < marks.length; j++) {
                if (marks[j] > biggest) {
                    biggest = marks[j];
                    best = j;
                }
            } // end for determine the biggest mark position.
        } // end if there are more combinations than one.

        return best;
    }

    /*
     * A method for dealer to take the best decision. He will put each card on
     * the table and will see which has the best results:
     */
    public int aiBestMove(ScopaHand curHand) {
        int returnBest = 0;
        // Go through all cards in hand if there are more than one:
        if (curHand.getCardCount() > 1) {
            /*
             * We need an array of ArrayList for best combination of each card
             * in hand:
             */
            ArrayList<ArrayList<Card>> bestCombinations = new ArrayList<>();
            // A for for each card in hand:
            for (int i = 0; i < curHand.getCardCount(); i++) {
                /*
                 * We create an ArrayList of ArrayLists to take the possible
                 * combinations of cards for current card put down:
                 */
                Card c = curHand.getCard(i);
                ArrayList<ArrayList<Card>> combinations = getSumsOfCards(c.getValue());
                // Only if there is at least a combination:
                if (combinations.size() > 0) {
                    /*
                     * We add also the card in hand to each combination to see
                     * if it contributes to a better move:
                     */
                    for (int j = 0; j < combinations.size(); j++) {
                        // We get a sub ArrayList of cards to add current card:
                        ArrayList<Card> tempArr = combinations.get(j);
                        tempArr.add(c);
                        combinations.set(j, tempArr);
                    } // end for add current card in hand to combinations..
                    int curBest = decideBestCombination(combinations);
                    /*
                     * Add the best found combination to bestCombinations
                     * ArrayList:
                     */
                    bestCombinations.add(combinations.get(curBest));
                } // end if combinations exist for current card.
                else {
                    /*
                     * If combinations doesn't exist, we must put there the
                     * worst combination, just for rigour:
                     */// We add there a ace of spades which will be alone:
                    ArrayList<Card> falseCombination = new ArrayList<>();
                    falseCombination.add(new Card(1, 1, context));
                    bestCombinations.add(falseCombination);
                } // end if combinations doesn't exists for a card.
            } // end big for to go through each card in dealer's hand.

            /*
             * No we decide from bestCombinations of each card in dealer's hand
             * which is the best one. They must be a number of bestCombinations
             * equal to number of cards in dealer's hand.
             */
            returnBest = decideBestCombination(bestCombinations);
        } // end if there are more than one cards in hand.

        return returnBest;
    } // end dealerBestMove() method.

} // end class ScopaHand.
