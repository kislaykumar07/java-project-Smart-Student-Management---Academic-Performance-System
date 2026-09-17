package com.sms.model;

/** Plain model class for a course offered by the institute. */
public class Course {

    private final String code;
    private String title;
    private int credits;
    private String faculty;

    public Course(String code, String title, int credits, String faculty) {
        this.code = code.toUpperCase();
        this.title = title;
        this.credits = credits;
        this.faculty = faculty;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String toCsv() {
        return String.join(",", code, title, String.valueOf(credits), faculty);
    }

    public static Course fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 4) {
            throw new IllegalArgumentException("Malformed course record: " + line);
        }
        return new Course(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3]);
    }

    @Override
    public String toString() {
        return String.format("%-8s %-30s %d credits   %s", code, title, credits, faculty);
    }
}
