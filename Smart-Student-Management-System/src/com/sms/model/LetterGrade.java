package com.sms.model;

/**
 * Enum with a constructor, fields and methods (Unit 2: Java enum Class).
 * Maps a percentage to a letter grade and its grade point.
 */
public enum LetterGrade {

    S("Outstanding", 10.0, 90.0),
    A("Excellent",    9.0, 80.0),
    B("Good",         8.0, 70.0),
    C("Average",      7.0, 60.0),
    D("Pass",         6.0, 50.0),
    F("Fail",         0.0,  0.0);

    private final String description;
    private final double gradePoint;
    private final double minPercentage;

    LetterGrade(String description, double gradePoint, double minPercentage) {
        this.description = description;
        this.gradePoint = gradePoint;
        this.minPercentage = minPercentage;
    }

    public String getDescription() {
        return description;
    }

    public double getGradePoint() {
        return gradePoint;
    }

    public double getMinPercentage() {
        return minPercentage;
    }

    /** Converts a percentage into the matching letter grade. */
    public static LetterGrade fromPercentage(double percentage) {
        for (LetterGrade grade : values()) {
            if (percentage >= grade.minPercentage) {
                return grade;
            }
        }
        return F;
    }

    @Override
    public String toString() {
        return name() + " (" + description + ")";
    }
}
