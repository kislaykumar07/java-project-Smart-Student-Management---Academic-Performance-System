package com.sms.util;

import java.util.List;

/**
 * Small statistics helper showing method overloading and recursion
 * (Unit 2: Java Recursion, method overloading).
 */
public final class MathUtil {

    private MathUtil() {
        // utility class, no instances
    }

    /** Recursive sum of a list of marks. */
    public static double sum(List<Double> values) {
        return sum(values, 0);
    }

    private static double sum(List<Double> values, int index) {
        if (values == null || index >= values.size()) {
            return 0.0;                     // base case
        }
        return values.get(index) + sum(values, index + 1);   // recursive case
    }

    /** Overloaded sum for a primitive array. */
    public static double sum(double[] values) {
        double total = 0.0;
        for (double value : values) {
            total += value;
        }
        return total;
    }

    public static double average(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        return sum(values) / values.size();
    }

    /** Overloaded average for a primitive array. */
    public static double average(double[] values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }
        return sum(values) / values.length;
    }

    public static double max(List<Double> values) {
        double best = Double.NEGATIVE_INFINITY;
        for (Double value : values) {
            if (value > best) {
                best = value;
            }
        }
        return values.isEmpty() ? 0.0 : best;
    }

    public static double min(List<Double> values) {
        double worst = Double.POSITIVE_INFINITY;
        for (Double value : values) {
            if (value < worst) {
                worst = value;
            }
        }
        return values.isEmpty() ? 0.0 : worst;
    }

    public static double round(double value, int places) {
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }
}
