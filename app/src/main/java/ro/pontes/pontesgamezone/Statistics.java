package ro.pontes.pontesgamezone;

/*
 * Class started on 24 September 2014 by Manu
 *Rewriten on 9 January 2023.
 * Methods for statistics, like postStatistics.
 */

public class Statistics {

    // A method to post a new game and the number of hands played during the sessions:
    public static void postStats(final String gameIdInDB, final int numberOfGamesPlayed) {
/*
        // Run in another thread:
        new Thread(new Runnable() {
            public void run() {

                // Create a new HttpClient and Post Header
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://www.pontes.ro/ro/divertisment/games/soft_counts.php");

                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("pid", gameIdInDB));
                    nameValuePairs.add(new BasicNameValuePair("score", "" + numberOfGamesPlayed));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpClient.execute(httppost);
                    response.toString();

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }

            }
        }).start();
*/
    } // end post data.

    // A method to post a new game and the number of hands played during the sessions:
    public static void postInsolvency(final String name, final int money) {
/*
        // Run in another thread:
        new Thread(new Runnable() {
            public void run() {

                // Create a new HttpClient and Post Header
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://www.android.pontes.ro/pontesgamezone/stats/stats.php");

                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                    nameValuePairs.add(new BasicNameValuePair("cont", MainActivity.userName));
                    nameValuePairs.add(new BasicNameValuePair("nume", name));
                    nameValuePairs.add(new BasicNameValuePair("suma", "" + money));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpClient.execute(httppost);
                    response.toString();

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }

            }
        }).start();
*/
    } // end post data for insolvency.

} // end statistics class.
