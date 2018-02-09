package io.github.grovertb.ytdl_core;

/**
 * Created by grove on 8/02/2018.
 */

public class utils {

    /**
     * Extract string inbetween another.
     * @param haystack body
     * @param left texto de inicio
     * @param right texto final
     * @return {String}
     */
    public static String between(String haystack, String left, String right) {
        Integer pos;
        pos = haystack.indexOf(left);

        if (pos == -1) { return ""; }
        haystack = haystack.substring(pos + left.length());


        pos = haystack.indexOf(right);
        if (pos == -1) { return ""; }
        haystack = haystack.substring(0, pos);
        return haystack;
    }
}
