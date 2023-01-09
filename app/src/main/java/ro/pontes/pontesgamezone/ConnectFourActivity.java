package ro.pontes.pontesgamezone;

/*
 * Class started on 19 March 2015 by Manu. 		
 */

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class ConnectFourActivity extends Activity {

	// For music background:
	SoundPlayer sndMusic;

	// Global fields:
	public static int cfLevel = 5;
	public final static int CF_DEFAULT_LEVEL = 5;
	public static boolean isStarted = false;
	private static int gameType = 0; // 0 is against dealer, 1 is against a
										// partner.
	private static byte isTurn = 1;
	private boolean isAbandon = false;
	private static int myScore = 0;
	private static int partnersScore = 0;
	private static int numberOfMoves = 1;
	final Context finalContext = this;

	// The TableLayout for grid, found in XML layout file:
	private TableLayout tlGrid;
	private int rows = 6;
	private int cols = 7;

	// Some global text views:
	TextView tvCfStatus;
	TextView tvMyMoney;
	TextView tvMyBet;

	// Some global strings from strings.xml:
	private String strMyMoney;
	private String strMyBet;

	// Some global buttons:
	// Let's declare variables for all the buttons, we will use them to enable
	// or disable depending of the game status:
	private Button btCfBet;
	private Button btCfNew;
	private Button btCfAbandon;
	private Button btCfChangeGameType;

	// An array for Connect four Grid.
	public static byte[][] grid;
	// A static instance of Board class:
	private static Board board;
	// A static instance of ConnectFourAI class:
	private static ConnectFourAI ai;
	// An array for possession:
	private String[] aPossession = new String[3];

	// For statistics:
	private int numberOfPlayedHands = 0;
	private int moneyWon = 0;
	private int moneyLost = 0;

	// Some global objects:
	private SpeakText speak;
	private Dealer dealer;

	// A variable to detect if bet was changed, this way we call the mHandler to
	// update text views for dealer:
	private int myTempBet; // we use it in the setTimer method, and we
							// initialise it in onCreate.

	// For a timer:
	private Timer t;

	// Messages for handler to manage the interface:
	private final int MAKE_AI_MOVE_VIA_HANDLER = 0; // a message to be sent to
													// the handler.
	private final int UPDATE_VIEWS_VIA_HANDLER = 1; // a message to be sent to
													// the handler.

	// A static inner class for handler:
	static class MyHandler extends Handler {
		WeakReference<ConnectFourActivity> cfActivity;

		MyHandler(ConnectFourActivity aCfActivity) {
			cfActivity = new WeakReference<ConnectFourActivity>(aCfActivity);
		}
	} // end static class for handler.

	// This handler will receive a delayed message
	private MyHandler mHandler = new MyHandler(this) {
		@Override
		public void handleMessage(Message msg) {
			// Do task here
			// ConnectFourActivity theActivity = cfActivity.get();

			if (msg.what == UPDATE_VIEWS_VIA_HANDLER) {
				updateTextViewsForMoney();
			}
			if (msg.what == MAKE_AI_MOVE_VIA_HANDLER) {
				makeAIMove();
			}
		}
	};

	// End handler stuff.

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect_four);

		// Get some global text views:
		tvCfStatus = (TextView) findViewById(R.id.tvCfStatus);
		tvMyMoney = (TextView) findViewById(R.id.tvMyMoney);
		tvMyBet = (TextView) findViewById(R.id.tvMyBet);

		// Charge global strings from values strings:
		strMyMoney = getString(R.string.my_money);
		strMyBet = getString(R.string.my_bet);

		// Get the buttons into their global variables, we will use them to
		// enable or disable periodically:
		btCfBet = (Button) findViewById(R.id.btCfBet);
		btCfNew = (Button) findViewById(R.id.btCfNew);
		btCfAbandon = (Button) findViewById(R.id.cfAbandon);
		btCfChangeGameType = (Button) findViewById(R.id.cfChangeGameType);

		// Fill the aPossession array:
		aPossession[0] = "";
		aPossession[1] = MainActivity.curNickname;
		// For the other player the value is set in enableOrDisableButtons(),
		// from dealer into partner and vice versa.
		// aPossession[2] = getString(R.string.dealer);

		// Initialise the speak object:
		speak = new SpeakText(this);

		// Initialise the dealer:
		dealer = new Dealer(this, true);

		myTempBet = dealer.currentBet; // we check in setTimer if it is changed,
										// to update text views for dealer.

		updateTextViewsForMoney();
		updateTextViewsForTotals(myScore, partnersScore);
		enableOrDisableButtons();

		// Get the TableLayout tlGrid:
		tlGrid = (TableLayout) findViewById(R.id.tlGrid);

		// Make things if it's an old game:
		if (isStarted) {
			updateNumberOfMovesStatus();
			drawGrid(); // a method to draw the grid based on the grid array.
		} // end if is not started yet.

		// To keep screen awake:
		if (MainActivity.isWakeLock) {
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} // end wake lock.

	} // end onCreate method.

	// A method to set the timer, in onResume() method:
	public void setTheTimer() {
		// Set the timer to send messages to the mHandler:
		t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						// Send a message to the handler:
						// This is to update the text views for bets found in
						// the upper part of the screen, if the bet was changed:
						if (myTempBet != dealer.currentBet) {
							myTempBet = dealer.currentBet;
							mHandler.sendEmptyMessageDelayed(
									UPDATE_VIEWS_VIA_HANDLER, 1); // means the
																	// delay in
																	// milliseconds.
						} // end if event change current bet text field
							// occurred.
							// If turn is 2 in a game against dealer:
						if (gameType == 0 && isTurn == 2) {
							mHandler.sendEmptyMessageDelayed(
									MAKE_AI_MOVE_VIA_HANDLER, 2000); // means
																		// the
																		// delay
																		// in
																		// milliseconds.
						} // end event for dealer turn to make AI move.
					}
				});
			}
		}, 1000, 200); // 1000 means start from 1 second, and the 200 is do the
						// loop interval.
		// end set the timer.
	} // end setTheTimer method.

	@Override
	public void onResume() {
		super.onResume();

		// Because we come sometimes from bar area, we want to update the wallet
		// status:
		updateTextViewsForMoney();

		setTheTimer();

		// Generate a new track:
		sndMusic = new SoundPlayer();
		sndMusic.playMusic(this);

	} // end onResume method.

	@Override
	public void onPause() {
		// Add here what you want to happens on pause:

		t.cancel();
		t = null;

		// Only if there are grids played, post the statistics:
		if (numberOfPlayedHands > 0) {
			Statistics.postStats("14", numberOfPlayedHands); // 14 is the id of
																// the Connect
																// Four game in
																// soft_counts
																// table in DB.
			numberOfPlayedHands = 0;
			moneyWon = 0;
			moneyLost = 0;
		}
		// end post the statistics.

		sndMusic.stopLooped();

		super.onPause();
	} // end onPause method.

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.connect_four, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.mnuCfStatistics) {
			int ballance = moneyWon - moneyLost;

			double ballancePerHand = 0;
			if (numberOfPlayedHands > 0) {
				// We avoid division by 0:
				ballancePerHand = (double) ballance
						/ (double) numberOfPlayedHands;
				ballancePerHand = Math.round(ballancePerHand * 100.0) / 100.0;
			}
			String title = getString(R.string.title_statistics_alert);
			String message = String.format(
					getString(R.string.statistics_for_connectfour), ""
							+ numberOfPlayedHands, "" + moneyWon, ""
							+ moneyLost, "" + ballance, ballancePerHand);
			GUITools.alert(this, title, message);
		} // end if statistics is chosen in menu.
		else if (id == R.id.mnuMoneyWonAsBonus) {
			GUITools.showBonusStatistics(this, Dealer.curMoneyWonAsBonus);
		} // end if bonus statistics is chosen in menu.
		else if (id == R.id.mnuHelp) {
			GUITools.openHelp(this);
		} // end help option in menu.
		else if (id == R.id.mnuCfLevel) {
			chooseDifficultyLevel();
		} // end difficulty level in menu.
		else if (id == R.id.mnuSaloon) {
			GUITools.goToSaloon(this);
		} // end got o saloon option.
		else if (id == R.id.mnuPawnshop) {
			GUITools.goToPawnshop(this);
		} // end go to pawn-shop option.
		else if (id == R.id.mnuBathroom) {
			GUITools.goToBathroom(this);
		} // end go to bathroom option.
		else if (id == R.id.mnuVirtualTime) {
			GUITools.showVirtualTime(this);
		} // end show virtual time.

		return super.onOptionsItemSelected(item);
	}

	private void chooseDifficultyLevel() {
		AlertDialog levelDialog;

		// Strings to Show In Dialog with Radio Buttons
		final CharSequence[] items = { getString(R.string.cf_easy),
				getString(R.string.cf_medium), getString(R.string.cf_hard),
				getString(R.string.cf_impossible) };
		final int[] cfLevels = { 3, 5, 7, 8 };

		// Creating and Building the Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.cf_choose_level));
		// Determine current item selected:
		int cfLevelPosition = -1;
		for (int i = 0; i < cfLevels.length; i++) {
			if (cfLevels[i] == cfLevel) {
				cfLevelPosition = i;
				break;
			}
		} // end for search current position of the current level chosen before.
		builder.setSingleChoiceItems(items, cfLevelPosition,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						switch (item) {
						case 0:
							// Your code when first option selected, easy:
							ConnectFourActivity.cfLevel = cfLevels[item];
							break;
						case 1:
							// Your code when 2nd option selected, medium:
							ConnectFourActivity.cfLevel = cfLevels[item];

							break;
						case 2:
							// Your code when 3rd option selected, hard:
							ConnectFourActivity.cfLevel = cfLevels[item];
							break;
						case 3:
							// Your code when 4th option selected, impossible:
							ConnectFourActivity.cfLevel = cfLevels[item];
							break;

						} // end switch.

					}
				});

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Save the choice for difficulty level:
				Settings set = new Settings(finalContext);
				set.saveIntSettings("cfLevel", cfLevel);
				ConnectFourAI.MAX_DEPTH = ConnectFourActivity.cfLevel;
				//
			}
		});
		levelDialog = builder.create();
		levelDialog.show();

	} // end chooseDifficultyLevel() method.

	public void changeBet(View view) {
		dealer.changeBet();
	} // end function change bet.

	// The method to start a new set:
	public void newGridGame(View view) {
		newGridGameActions();
	}

	// A method for abandon or stop the game:
	public void abandonGame(View view) {
		isStarted = false;
		isAbandon = true;
		checkForFinish();
	} // end abandonGame() method.

	// A method to change the game type:
	public void changeGameType(View view) {

		// If gameType is 0 make it 1 and vice versa:
		if (gameType == 1) {
			gameType = 0;
			// Change the second value of the string aPossession into dealer:
			aPossession[2] = getString(R.string.dealer);
			btCfChangeGameType.setText(getString(R.string.cf_game_type1));
		} else {
			gameType = 1;
			aPossession[2] = getString(R.string.partner);
			btCfChangeGameType.setText(getString(R.string.cf_game_type0));
		}
		SoundPlayer.playSimple(this, "reverse");
		updateTextViewsForTotals(myScore, partnersScore);
	} // end change game type method.

	public void newGridGameActions() {
		if (!isStarted) {

			// If is a game against partner, we don't need to place a bet:
			if (gameType == 1 || dealer.addBet()) {
				isStarted = true;

				// Stop previously speaking:
				speak.stop();

				SoundPlayer.playSimple(this, "new_event");
				enableOrDisableButtons();

				isTurn = 1;
				numberOfMoves = 1;
				isAbandon = false;

				// Increase number of hands:
				numberOfPlayedHands++;

				// Show and say about this start:
				String tempMessage = getString(R.string.cf_started);
				updateStatus(tempMessage);

				// Initialise the grid array:
				grid = new byte[rows][cols];
				newGrid(); // a method to make 0 in each cell of the array.
				drawGrid(); // a method to draw the grid based on the grid
							// array.

				// Initialise the ConnectFourAI object created globally as
				// static:
				// Only if it's a game against dealer:
				if (gameType == 0) {
					board = new Board(rows, cols, 4);
					ai = new ConnectFourAI(board);
				}

			} // end if add bet method was processed correctly.
		} // end if is not started.
	} // end new grid game actions.

	// A method to initialise the array for grid when a new game is started:
	private void newGrid() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				grid[i][j] = 0;
			} // end for vertical movement in the grid array.
		} // end for horizontal movement in the grid array.

	} // end new grid method.

	// A method to draw the grid depending of the grid array:
	private void drawGrid() {
		// Get the size of the screen:
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		// int screenWidth = size.x;
		int screenHeight = size.y;
		/*
		 * The height of the grid will be 1/3 from screenHeight. The height of a
		 * TextView, a cell of the table will be 1/3 all divided by number of
		 * rows.
		 */
		int tvHeight = screenHeight / (3 * rows);

		// A StringBuilder for letters:
		StringBuilder sb = new StringBuilder("ABCDEFGHIJ");
		tlGrid.removeAllViews();
		// A ParamLayout for all text views, weight is 1 for each:
		LayoutParams lp = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, 1f);
		for (int i = rows - 1; i >= 0; i--) {
			TableRow tr = new TableRow(this);
			for (int j = 0; j < cols; j++) {
				TextView tv = new TextView(this);
				tv.setLayoutParams(lp);
				tv.setHeight(tvHeight);

				// Fill the cell with corresponding shape:
				String shapeName = "cell_shape" + grid[i][j]; // 0, 1 or 2,
																// depending of
																// the player
																// there or
																// blank.
				int res = getResources().getIdentifier(shapeName, "drawable",
						getPackageName());
				tv.setBackgroundResource(res);
				final String cellName = "" + sb.charAt(j) + "" + (i + 1);
				// Determine if there is something there for contentDescription
				// from the grid and aPossession array:
				String tempMessage = cellName + " " + aPossession[grid[i][j]];
				tv.setContentDescription(tempMessage);
				// Set an ID for the text view:
				final int tvId = 1000 + ((10 * i) + j);
				tv.setId(tvId);
				// Make final x and y variables for coordinate as parameters for
				// changeCell() method:
				final int x = i;
				final int y = j;
				tv.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (isTurn == 1 || gameType == 1) {
							changeCell(tvId, x, y);
						}
					}
				});
				tr.addView(tv);
			} // end for horizontal movement in grid, from 1 to 7.
			tlGrid.addView(tr);
		} // end for vertical movement in grid, from 1 to 6.
	} // end draw grid.

	// A method to change a cell in grid depending of the game status:
	private void changeCell(int cellId, int x, int y) {
		if (isStarted) {
			TextView tv = (TextView) findViewById(cellId);
			String cellName = tv.getContentDescription().toString();

			// Check if there is still a 0 value, nothing yet:
			if (grid[x][y] == 0) {

				// Check if there is something below in current column or x row
				// is 0:
				if (x == 0 || grid[x - 1][y] > 0) {
					SoundPlayer.playSimple(this, "cf_turn");

					grid[x][y] = isTurn;
					// Make string for content description and speak action,
					// name put on current cell:
					String tempMessage = cellName + " " + aPossession[isTurn];

					tv.setContentDescription(tempMessage);
					speak.say(tempMessage, true);

					// Fill the cell with corresponding shape:
					String shapeName = "cell_shape" + isTurn; // 1 or 2,
																// depending of
																// the who is
																// turn now.
					int res = getResources().getIdentifier(shapeName,
							"drawable", getPackageName());
					tv.setBackgroundResource(res);

					// Give to AI the column accessed by player if it's his turn
					// and it's a game against dealer:
					if (isTurn == 1 && gameType == 0) {
						// Randomize a little the level:
						int min = ConnectFourActivity.cfLevel - 1;
						int max = (ConnectFourActivity.cfLevel < 8) ? (ConnectFourActivity.cfLevel + 1)
								: ConnectFourActivity.cfLevel;
						ConnectFourAI.MAX_DEPTH = GUITools.random(min, max);
						board.makeMovePlayer(y);
					}
					checkForFinish();
					// Now, if is still started:
					// Last things happens only if is not a finish:
					if (isStarted) {
						numberOfMoves = numberOfMoves + 1; // increase the
															// number of moves
															// to show them in
															// status text view.
						changeTurn();
					}
				} else {
					SoundPlayer.playSimple(this, "forbidden");
					;
					updateStatus(String.format(
							getString(R.string.cf_cell_forbidden), cellName));
				} // end if there is not something below or y == 0.
			} else {
				SoundPlayer.playSimple(this, "forbidden");
				;
				speak.say(String.format(
						getString(R.string.cf_cell_already_used), cellName),
						true);
			} // end if there is already something there, the value in grid is
				// greater than 0, in fact 1 or 2.
		} else {
			SoundPlayer.playSimple(this, "forbidden");
			;
		}
	} // end change cell method.

	// A method to make an AI move:
	private void makeAIMove() {
		if (isStarted && isTurn == 2) {

			int aiColumn = ai.makeTurn();
			// Determine also the valid row:
			int aiRow = 0;
			for (int i = 0; i < rows; i++) {
				if (grid[i][aiColumn] == 0) {
					aiRow = i;
					break;
				}
			} // end for to determine the aiRow.

			// Determine also the cellID depending of the x and y coordinates:
			int tvId = 1000 + ((10 * aiRow) + aiColumn);
			// Make now the move:
			changeCell(tvId, aiRow, aiColumn);
		} // end if is started.
	} // end make AI move.

	// A method to change turn:
	private void changeTurn() {
		if (isStarted) {
			if (isTurn == 1) {
				isTurn = 2;
			} else {
				isTurn = 1;
			}
			// Now update the number of moves status:
			updateNumberOfMovesStatus();
		} // end if is started.
	} // end change turn method.

	// A method to detect if is a win or a draw:
	private void checkForFinish() {
		// Determine if there is or not four in a row or a draw:
		int who = isFourInARow();
		// Check if an abandon:
		if (isAbandon) {
			who = 2;
		}
		// If who is greater than 0, because 0 means nothing, the game
		// continues:
		if (who > 0) {
			String tempMessage = "";
			if (who == 2) {
				// Dealer or partner is the final winner:
				// Dealer class is needed only if is against dealer game:
				if (gameType == 0) {
					moneyLost = moneyLost + dealer.currentBet;
					dealer.lose();
				} else {
					SoundPlayer.playSimple(this, "lose_game");
				} // to have the win game sound if it's 0 as bet.
				partnersScore = partnersScore + 1;
				tempMessage = getString(R.string.cf_lose);
				// SoundPlayer.playSimple(this, "lose_game");
			} else if (who == 1) {
				// Player is the winner:
				if (gameType == 0) {
					moneyWon = moneyWon + dealer.currentBet;
					dealer.win();
				} else {
					SoundPlayer.playSimple(this, "win_game");
				} // end else if to have the win game sound if it's 0 as bet.
				myScore = myScore + 1;
				tempMessage = getString(R.string.cf_win);
				// SoundPlayer.playSimple(this, "win_game");
			} else {
				// It's a draw:
				if (gameType == 0) {
					dealer.draw();
				}
				tempMessage = getString(R.string.cf_draw);
			} // end the 3 possibilities for winner.
			updateTextViewsForTotals(myScore, partnersScore);
			updateTextViewsForMoney();
			updateStatus(tempMessage);
			enableOrDisableButtons();
		} // end is it was a finish, four in a row or a draw.
	} // end check for finish.

	// A method to check for four in a row:
	private int isFourInARow() {
		int who = 0;

		if (isStarted) {
			// First check for four in a row horizontally:
			outerloop: for (int i = 0; i < rows; i++) {
				int nr = 1; // this must become 4 if there are four in a row.
				for (int j = 1; j < cols; j++) {
					if (grid[i][j] > 0 && grid[i][j] == grid[i][j - 1]) {
						nr = nr + 1;
					} else {
						nr = 1; // reset the nrVariable back to 1 for other
								// tries.
					}
					// Check if there are 4 in a row:
					if (nr >= 4) {
						who = grid[i][j]; // we detect who was with this
											// connection done.
						isStarted = false;
						break outerloop;
					} // end check if there are four, the nrVariable is 4.
				} // end going through columns, from left to right in a row.
			} // end for going through rows.
		} // end if isStarted.

		// If is still started, check if there are four in a row vertically:
		if (isStarted) {

			outerloop: for (int j = 0; j < cols; j++) {
				int nr = 1;
				for (int i = 1; i < rows; i++) {
					if (grid[i][j] > 0 && grid[i][j] == grid[i - 1][j]) {
						nr = nr + 1;
					} else {
						nr = 1; // reset the nrVariable back to 1 for other
								// tries.
					}
					// Check if there are 4 in a row:
					if (nr >= 4) {
						who = grid[i][j]; // we detect who was with this
											// connection done.
						isStarted = false;
						break outerloop;
					} // end check if there are four, the nrVariable is 4.
				} // end for rows, inner loop.
			} // end for columns, outer loop.
		} // end if is still started to check for vertical rows.

		// If is still started for diagonal from left to right:
		if (isStarted) {
			outerloop: for (int i = 0; i < rows - 3; i++) {
				for (int j = 0; j < cols - 3; j++) {
					if (grid[i][j] > 0 && grid[i][j] == grid[i + 1][j + 1]
							&& grid[i][j] == grid[i + 2][j + 2]
							&& grid[i][j] == grid[i + 3][j + 3]) {
						who = grid[i][j]; // we detect who was with this
											// connection done.
						isStarted = false;
						break outerloop;
					}

				} // end going through columns, from left to right in a row.
			} // end for rows.
		} // end if game is started for checking the left to right diagonal.

		// If is still started for diagonal from right to left:
		if (isStarted) {
			outerloop: for (int i = 0; i < rows - 3; i++) {
				for (int j = 3; j < cols; j++) {
					if (grid[i][j] > 0 && grid[i][j] == grid[i + 1][j - 1]
							&& grid[i][j] == grid[i + 2][j - 2]
							&& grid[i][j] == grid[i + 3][j - 3]) {
						who = grid[i][j]; // we detect who was with four this
											// connection done.
						isStarted = false;
						break outerloop;
					}
				} // end going through columns, from left to right in a row.
			} // end for rows.
		} // end if game is still started for checking the right to left
			// diagonal.

		// If is still started but no win yet, after all four checks:
		// There where maximum number of moves and nobody won:
		if (isStarted) {
			if (numberOfMoves >= rows * cols) {
				who = 3; // a draw.
				isStarted = false;
			}
		} // end if it is still started to check for a draw.

		return who;
	} // end check for four in a row.

	// A method to update some TextViews:
	private void updateTextViewsForMoney() {

		// First change the tvMyMoney TextView:

		tvMyMoney.setText(String.format(strMyMoney, "" + Dealer.myTotalMoney));

		// Now change the tvMyBet TextView:
		tvMyBet.setText(String.format(strMyBet, "" + dealer.currentBet));

	} // end updateTextViews method.

	// A method to update TextViews for totals in hands:
	public void updateTextViewsForTotals(int myScore, int partnersScore) {

		// First change the tvMyCfTotal TextView:
		// Get the TextView:
		TextView tvMyCfTotal = (TextView) findViewById(R.id.tvMyCfTotal);
		tvMyCfTotal.setText(String.format(getString(R.string.my_cf_total), ""
				+ myScore));

		// Now change the tvDealersCwTotal TextView:
		TextView tvPartnersCfTotal = (TextView) findViewById(R.id.tvPartnersCfTotal);
		// This text view will be depending of the gameType, against dealer or
		// against a partner:
		if (gameType == 1) {
			tvPartnersCfTotal.setText(String.format(
					getString(R.string.partners_cf_total), "" + partnersScore));
		} else {
			tvPartnersCfTotal.setText(String.format(
					getString(R.string.dealers_cf_total), "" + partnersScore));
		}
	} // end updateTextViews with totals in hands method.

	// A method to update the tvCardsWarStatus TextView:
	public void updateStatus(String text) {
		tvCfStatus.setText(text);
		speak.say(text, false);
	} // end updateBjStatus method.

	// A method to update the status with number of moves message:
	private void updateNumberOfMovesStatus() {
		String text = String.format(getString(R.string.cf_current_move), ""
				+ numberOfMoves, aPossession[isTurn]);
		tvCfStatus.setText(text);
	} // end update status with number of moves message.

	// A method to make buttons state active or inactive, depending of the
	// status of the game:
	public void enableOrDisableButtons() {

		// If is started or not:
		if (isStarted) {
			btCfBet.setEnabled(false);
			btCfNew.setEnabled(false);
			btCfAbandon.setEnabled(true);
			btCfChangeGameType.setEnabled(false);
		} else {
			btCfBet.setEnabled(true);
			btCfNew.setEnabled(true);
			btCfAbandon.setEnabled(false);
			btCfChangeGameType.setEnabled(true);
		} // end if is started or not.

		// Depending of the game type:
		if (gameType == 0) {
			// Change the second value of the string aPossession into dealer:
			aPossession[2] = getString(R.string.dealer);
			btCfChangeGameType.setText(getString(R.string.cf_game_type1));
		} else {
			// Change the second value of the string aPossession into partner:
			aPossession[2] = getString(R.string.partner);
			btCfChangeGameType.setText(getString(R.string.cf_game_type0));
		}
	} // end enableOrDisableButtons.

} // end ConnectFourActivity class.
