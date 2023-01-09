package ro.pontes.pontesgamezone;

import android.content.Context;
import android.os.Vibrator;

public class Vibration {

	// A method to vibrate simple:
	@SuppressWarnings("deprecation")
	public static void makeVibration(Context context, int millis) {
		if (MainActivity.isVibration) {
			Vibrator vibrator = (Vibrator) context
					.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(millis);
		} // end if vibration is allowed.
	} // end makeVibration() method.

	// A method to vibrate with a pattern:
	public static void vibratePattern(final Context context,
			final long[] pattern, final int loop) {
		if (MainActivity.isVibration) {
			// This will happen in a new thread:
			new Thread(new Runnable() {
				@SuppressWarnings("deprecation")
				public void run() {
					// For vibrations initialisation:
					Vibrator vibrator = (Vibrator) context
							.getSystemService(Context.VIBRATOR_SERVICE);

					// In the pattern array, first number means start
					// immediately, after are vibration, pause, vibration, pause
					// and so on:

					// Do not repeat, -1 means no repeat:
					vibrator.vibrate(pattern, loop);
				}
			}).start();
		} // end if vibrations are allowed in settings.
	} // end vibratePattern() static method.

	// A method for SOS in morse:
	public static void makeSOS(final Context context) {
		if (MainActivity.isVibration) {
			// This will happen in a new thread:
			new Thread(new Runnable() {
				@SuppressWarnings("deprecation")
				public void run() {
					// For vibrations initialisation:
					Vibrator vibrator = (Vibrator) context
							.getSystemService(Context.VIBRATOR_SERVICE);

					// This example will cause the phone to vibrate "SOS" in
					// Morse
					// Code:
					// In Morse Code, "s" = "dot-dot-dot", "o" =
					// "dash-dash-dash".
					// There are pauses to separate dots/dashes, letters, and
					// words.
					// The following numbers represent millisecond lengths
					int dot = 100; // Length of a Morse Code "dot" in
									// milliseconds.
					int dash = 250; // Length of a Morse Code "dash" in
									// milliseconds.
					int short_gap = 100; // Length of Gap Between dots/dashes.
					int medium_gap = 250; // Length of Gap Between Letters.
					int long_gap = 500; // Length of Gap Between Words.

					long[] pattern = { 0, // Start immediately
							dot, short_gap, dot, short_gap, dot, // s
							medium_gap, dash, short_gap, dash, short_gap, dash, // o
							medium_gap, dot, short_gap, dot, short_gap, dot, // s
							long_gap }; // end pattern creation for s o s.
					// Only perform this pattern one time (-1 means
					// "do not repeat"):
					vibrator.vibrate(pattern, -1);
				}
			}).start();
			// Try to make sleep until the vibration is done:
			/*
			 * try { Thread.sleep(50); } catch (InterruptedException e) { //
			 * e.printStackTrace(); }
			 */
		} // end if vibrations are allowed in settings.
	} // end makeSOS() method.

} // end vibration class.
