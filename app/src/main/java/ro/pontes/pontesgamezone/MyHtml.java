package ro.pontes.pontesgamezone;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;

/* I did this class on 12 August 2017 to have the deprecated fromHtml method only in one place. This way wit will be easier to deal with these deprecated methods. */
public class MyHtml {

	@SuppressWarnings("deprecation")
	@TargetApi(24)
	public static Spanned fromHtml(String source) {
		Spanned spanned = null;
		if (Build.VERSION.SDK_INT >= 24) {
			spanned = Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
		} else {
			spanned = Html.fromHtml(source);
		}

		return spanned;
	} // end fromHtml() method.

} // end MyHtml class.
