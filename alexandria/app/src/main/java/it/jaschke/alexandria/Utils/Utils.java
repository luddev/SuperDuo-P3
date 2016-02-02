package it.jaschke.alexandria.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by lud on 03-02-2016.
 */
public class Utils {

    public static boolean getNetworkConnectivity(Context context)   {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }


    /*

    The calculation of an ISBN-13 check digit begins with the first 12 digits of the thirteen-digit ISBN (thus excluding the check digit itself). Each digit, from left to right, is alternately multiplied by 1 or 3, then those products are summed modulo 10 to give a value ranging from 0 to 9. Subtracted from 10, that leaves a result from 1 to 10. A zero (0) replaces a ten (10), so, in all cases, a single check digit results.

    For example, the ISBN-13 check digit of 978-0-306-40615-? is calculated as follows:

    s = 9×1 + 7×3 + 8×1 + 0×3 + 3×1 + 0×3 + 6×1 + 4×3 + 0×1 + 6×3 + 1×1 + 5×3
      =   9 +  21 +   8 +   0 +   3 +   0 +   6 +  12 +   0 +  18 +   1 +  15
      = 93
    93 / 10 = 9 remainder 3
    10 –  3 = 7


    ISBN-10 to ISBN-13 conversion
    The conversion is quite simple as one only needs to prefix "978" to the existing number and calculate the new checksum using the ISBN-13 algorithm.


    Source : https://en.wikipedia.org/wiki/International_Standard_Book_Number
     */

    public static String ISBN10toISBN13( String isbn10 ) {
        String isbn13  = isbn10;
        if(isbn10.length() == 13)
            return isbn10;
        isbn13 = "978" + isbn13.substring(0,9);
        int d;
        int sum = 0;
        for (int i = 0; i < isbn13.length(); i++) {
            d = ((i % 2 == 0) ? 1 : 3);
            sum += ((((int) isbn13.charAt(i)) - 48) * d);
        }
        sum = 10 - (sum % 10);
        isbn13 += sum;

        return isbn13;
    }
}
