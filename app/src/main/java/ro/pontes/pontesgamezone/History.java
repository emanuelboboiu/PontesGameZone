package ro.pontes.pontesgamezone;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class History {

    // An ArrayList with strings:
    private final ArrayList<String> mHistory = new ArrayList<>();

    // We need also a context at least for show method:
    private final Context context;

    // We need a space around text views:
    private final int mPaddingDP;

    // The constructor:
    public History(Context context) {
        this.context = context;

        // Calculate the pixels in DP for mPaddingDP, for TextViews of the lines
        // of SARADA:
        int paddingPixel = 3;
        float density = context.getResources().getDisplayMetrics().density;
        mPaddingDP = (int) (paddingPixel * density);
        // end calculate mPaddingDP
    } // end constructor.

    public void add(String message) {
        mHistory.add(message);
    } // end add() method.

    // A method to clear the history:
    public void clear() {
        mHistory.clear();
    } // end clear() method.

    // A method to show history in an alert:
    public void show(int lastEntries) {
        // Reduce the history to lastEntries entries:
        int limit = mHistory.size() - lastEntries;
        for (int i = 0; i < limit; i++) {
            mHistory.remove(0);
        } // end for reduce the history size().

        // Get the textSize of the text in TextViews:
        int textSize = 17;

        // Create a LinearLayout with ScrollView with all contents as TextViews:
        ScrollView sv = new ScrollView(context);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        TextView tv;

        // A for for each message in the history as TextView:
        for (int i = mHistory.size() - 1; i >= 0; i--) {
            tv = new TextView(context);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            tv.setPadding(mPaddingDP, mPaddingDP, mPaddingDP, mPaddingDP);
            tv.setText(mHistory.get(i));
            ll.addView(tv);
        } // end for.
        sv.addView(ll);

        // Create now the alert:
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(context.getString(R.string.game_history));
        // alertDialog.setMessage("Ramona Gherman");
        alertDialog.setView(sv);
        alertDialog.setPositiveButton(context.getString(R.string.bt_close), null);
        AlertDialog alert = alertDialog.create();
        alert.show();

    } // end show() method.

} // end History class.
