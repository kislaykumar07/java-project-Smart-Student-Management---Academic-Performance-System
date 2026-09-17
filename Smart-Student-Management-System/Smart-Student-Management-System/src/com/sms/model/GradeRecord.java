package com.sms.model;

/**
 * Links a student to a course with the marks scored.
 * Implements the Gradable interface (Unit 2: Polymorphism).
 */
public class GradeRecord implements Gradable {

    public static final double MAX_MARKS = 100.0;

    private final String studentId;
    private final String courseCode;
    private double marks;

    public GradeRecord(String studentId, String courseCode, double marks) {
        this.studentId = studentId;
        this.courseCode = courseCode.toUpperCase();
        this.marks = marks;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }

    /** Unique key used by the grade service map. */
    public String key() {
        return studentId + "::" + courseCode;
    }

    @Override
    public double getPercentage() {
        return (marks / MAX_MARKS) * 100.0;
    }

    @Override
    public LetterGrade getLetterGrade() {
        return LetterGrade.fromPercentage(getPercentage());
    }

    public String toCsv() {
        return String.join(",", studentId, courseCode, String.valueOf(marks));
    }

    public static GradeRecord fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 3) {
            throw new IllegalArgumentException("Malformed grade record: " + line);
        }
        return new GradeRecord(parts[0], parts[1], Double.parseDouble(parts[2]));
    }

    @Override
    public String toString() {
        return String.format("%-8s %-8s %6.2f   %-18s %s",
                studentId, courseCode, marks, getLetterGrade(), isPass() ? "PASS" : "FAIL");
    }
}
