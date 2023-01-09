package ro.pontes.pontesgamezone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.speech.tts.TextToSpeech;

public class SpeakText {

    private final TextToSpeech mTTS;

    // The constructor:
    public SpeakText(Context context) {
        // For TextToSpeech:
        mTTS = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                // mTTS.setLanguage(locale.US);
            }
        });
        // end for TextToSpeech.
    } // end constructor.

    @SuppressLint("NewApi")
    public void say(final String toSay, final boolean interrupt) {
        if (MainActivity.isSpeech) {
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                // Do something after 100ms:

                int speakMode;
                if (interrupt) {
                    speakMode = TextToSpeech.QUEUE_FLUSH;
                } else {
                    speakMode = TextToSpeech.QUEUE_ADD;
                } // end if is not interruption.
                mTTS.speak(toSay, speakMode, null, null);

            }, 250);

        } // end if isSpeech.
    } // end say method.

    public void stop() {
        mTTS.stop();
    } // end stop method of the SpeakText class.
} // end SpeakText class.
