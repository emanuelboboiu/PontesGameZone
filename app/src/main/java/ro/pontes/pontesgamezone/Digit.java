package ro.pontes.pontesgamezone;

import android.content.Context;

public class Digit {

    private static String resDigitNameString = "none"; // the resource with
    // placeholder for a
    // digit name. // the
    // string to be returned
    // by toString method.

    // The value of this instance of Digit class:
    private final int value;

    /*
     * The constructor. Creates a digit with a specified suit and value. The
     * value must be in the range 0 through 9.
     */
    public Digit(int value, Context context) {
        this.value = value;
        if (resDigitNameString.equals("none")) {
            resDigitNameString = context.getString(R.string.digit_name);
        } // end charge the string for a number face.
    } // end constructor.

    // A method which returns the value:
    public int getValue() {
        return value;
    }

    // A method to return the file name of current digit as string:
    public String toFileName() {
        return "num" + value;
    } // end toFileName method.

    // Say the digit as string:
    @Override
    public String toString() {
        return String.format(resDigitNameString, value);
    } // end toString() method.
} // end Digit class.
