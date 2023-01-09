package ro.pontes.pontesgamezone;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SlotMachineHand extends DigitsHand {

    private static final int[] aFaces = new int[]{0, 1, 2, 3, 5, 7};

    // A constructor:
    public SlotMachineHand(Context context) {
        super(context);
        super.aPossesion[1] = context.getString(R.string.sm_cylinder_face);
        super.resPossesionString = context.getString(R.string.sm_cylinder_face_extended);
    } // end constructor.

    // A method which calculates how much a player won:
    public int getWinMultiplier() {
        int tempMultiplier = 0;
        if (getDigitCount() == 3) {
            /*
             * One array for corresponding multiplier when there are three of a
             * kind:
             */
            int[] aMultipliers = new int[]{0, 0, 14, 30, 0, 40, 0, 400, 0, 0};

            boolean isContinueChecking = true;

            /*
             * Check for ones, left, both left and centre, or all three. Winning
             * will be 2, 4, 8 respectively:
             */
            int base = 1;
            for (int i = 0; i < getDigitCount(); i++) {
                if (getValue(i) == 1) {
                    base = base * 2;
                    tempMultiplier = base;
                    isContinueChecking = false;
                } else {
                    break;
                }
            } // end for.

            // Check if there are 3 of a kind:
            if (isContinueChecking) {
                if (getValue(0) == getValue(1) && getValue(1) == getValue(2)) {
                    tempMultiplier = aMultipliers[getValue(0)];
                }
            } // end if isContinue checking, for three of a kind.
        } // end if there are three digits in hand.
        return tempMultiplier;
    } // end method which calculates how many times the bet among must be
    // multiplied.

    // A method to roll the cylinders:
    public void drawRollingCylinders(LinearLayout ll) {
        // An ArrayList for digits:
        ArrayList<Digit> tempHand = new ArrayList<>();
        // Fill the tempHand with 3 changed Digits:
        for (int i = 0; i < 3; i++) {
            int rand = aFaces[GUITools.random(0, aFaces.length - 1)];
            tempHand.add(new Digit(rand, context));
        } // end for fill tempHand with 3 digits.

        // Clear if there is something there:
        ll.removeAllViews();

        for (int i = 0; i < 3; i++) {

            Digit tempDigit = tempHand.get(i);
            String digitFileName = tempDigit.toFileName();

            ImageView mImage = new ImageView(context);
            String uri = "@drawable/" + digitFileName; // the name of the
            // image
            // dynamically.
            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
            Drawable res = context.getResources().getDrawable(imageResource);
            mImage.setImageDrawable(res);
            mImage.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);

            String tempString = String.format(resPossesionString, aPossesion[1], tempDigit);
            mImage.setContentDescription(tempString);
            ll.addView(mImage);
        } // end for.
    } // end drawRollingCylinders method.

    // A method which return a digit random for a possible face:
    public static int getRandomFace() {
        // An array with accepted faces:
        return aFaces[GUITools.random(0, aFaces.length - 1)];
    } // end getRandomFace().

} // end SlotMachineHand class.
