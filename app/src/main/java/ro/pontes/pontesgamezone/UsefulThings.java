package ro.pontes.pontesgamezone;

/*
 * Started by Manu on 21 August 2015, 00:45.
 * Some useful computations, for instance decimal to binary array.
 * The methods in this class will be static.
 * */

public class UsefulThings {

    public static String toBinaryAsString(int n) {
        if (n == 0) {
            return "0";
        }
        StringBuilder binary = new StringBuilder();
        while (n > 0) {
            int rem = n % 2;
            binary.insert(0, rem);
            n = n / 2;
        }
        return binary.toString();
    } // end toBinaryAsString() method.

    /*
     * A method which returns an array of bytes, the binary representation of a
     * decimal number as true or false on each position.
     */
    public static byte[] toBinaryAsArrayOfByte(int n, int representationLength) {
        if (n == 0) {
            return new byte[]{0};
        }
        byte[] binary = new byte[representationLength];
        int step = 0;
        while (n > 0) {
            step++;
            byte rem = (byte) (n % 2);
            binary[representationLength - step] = rem;
            n = n / 2;
        }
        return binary;
    } // end toBinaryAsArrayOfByte() method.

    // A method to convert an array of bytes to a string:
    public static String arrayToString(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        for (byte b : arr) {
            sb.append(b);
        } // end for.
        return sb.toString();
    } // end arrayToString() method.

} // end UsefulThings class.
