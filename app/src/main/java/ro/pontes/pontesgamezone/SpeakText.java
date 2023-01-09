package ro.pontes.pontesgamezone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;

public class SpeakText {

	private TextToSpeech mTTS;

	// The constructor:
	public SpeakText(Context context) {
		// For TextToSpeech:
		mTTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if (status != TextToSpeech.ERROR) {
					// mTTS.setLanguage(locale.US);
				}
			}
		});
		// end for TextToSpeech.
	} // end constructor.

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void say(final String toSay, final boolean interrupt) {
		if (MainActivity.isSpeech) {
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// Do something after 100ms:

					int speakMode = 0;
					if (interrupt) {
						speakMode = TextToSpeech.QUEUE_FLUSH;
					} else {
						speakMode = TextToSpeech.QUEUE_ADD;
					} // end if is not interruption.
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						mTTS.speak(toSay, speakMode, null, null);
					} else {
						mTTS.speak(toSay, speakMode, null);
					}

				}
			}, 250);

		} // end if isSpeech.
	} // end say method.

	public void stop() {
		mTTS.stop();
	} // end stop method of the SpeakText class.
} // end SpeakText class.
