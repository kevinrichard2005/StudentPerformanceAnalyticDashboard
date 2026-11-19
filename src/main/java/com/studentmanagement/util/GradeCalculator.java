package com.studentmanagement.util;

/**
 * Single source of truth for the grading scale.
 *
 * <pre>
 * 90–100 O  Excellent | 80–89 A+ Outstanding | 70–79 A Very Good
 * 60–69  B+ Good      | 50–59 B  Average     | 40–49 C Pass | 0–39 F Fail
 * </pre>
 */
public final class GradeCalculator {

    public static final double MIN_MARKS = 0.0;
    public static final double MAX_MARKS = 100.0;
    public static final double PASS_MARK = 40.0;

    private GradeCalculator() {
        // Utility class — prevent instantiation
    }

    /** Immutable grade/result pair. */
    public record GradeResult(String grade, String result) { }

    public static GradeResult evaluate(double marks) {
        if (marks < MIN_MARKS || marks > MAX_MARKS || Double.isNaN(marks)) {
            throw new IllegalArgumentException("Marks must be between 0 and 100");
        }
        if (marks >= 90) return new GradeResult("O", "Excellent");
        if (marks >= 80) return new GradeResult("A+", "Outstanding");
        if (marks >= 70) return new GradeResult("A", "Very Good");
        if (marks >= 60) return new GradeResult("B+", "Good");
        if (marks >= 50) return new GradeResult("B", "Average");
        if (marks >= 40) return new GradeResult("C", "Pass");
        return new GradeResult("F", "Fail");
    }

    public static String calculateGrade(double marks) {
        return evaluate(marks).grade();
    }

    public static String calculateResult(double marks) {
        return evaluate(marks).result();
    }

    public static boolean isPass(double marks) {
        return marks >= PASS_MARK;
    }
}
