package edu.tuberlin.dima.textmining.jedi.core.util;

import org.apache.commons.lang.StringUtils;

/**
 * Date: 28.10.2014
 * Time: 20:27
 *
 * @author Johannes Kirschnick
 */
public class StringComparision {

    public static boolean determineSimilar(String left, String right) {

        final String commonPrefix = StringUtils.getCommonPrefix(new String[]{left, right});
        final String commonSuffix = StringUtils.getCommonPrefix(new String[]{StringUtils.reverse(left), StringUtils.reverse(right)});

        final int commonLength = commonPrefix.length() + commonSuffix.length();
        final double weightedSimilar = Math.sqrt((commonLength / (float) left.length()) * (commonLength / (float) right.length()));
        int distance = LevenshteinDistance.computeLevenshteinDistance(right, commonPrefix + StringUtils.reverse(commonSuffix));
        return weightedSimilar >= 0.85 || (distance < 3 && (commonPrefix.length() > 0) && (commonSuffix.length() > 0));
    }
}
