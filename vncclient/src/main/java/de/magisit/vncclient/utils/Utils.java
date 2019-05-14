package de.magisit.vncclient.utils;

public class Utils {
    //TODO Convert to kotlin

    public static boolean inArray(int search, int[] array) {
        for (int element : array) {
            if (element == search)
                return true;
        }

        return false;
    }

    public static byte reverseBitsByte(byte x) {
        int intSize = 8;
        byte y = 0;
        for (int position = intSize - 1; position > 0; position--) {
            y += ((x & 1) << position);
            x >>= 1;
        }
        return y;
    }
}
