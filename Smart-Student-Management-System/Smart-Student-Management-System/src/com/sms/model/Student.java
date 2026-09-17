package com.sms.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete subclass of Person (Unit 2: Inheritance, super keyword,
 * method overriding, constructor overloading).
 */
public class Student extends Person implements Comparable<Student> {

    private String department;
    private int semester;
    private final List<String> enrolledCourses;

    /** Full constructor. */
    public Student(String id, String name, int age, String email,
                   String department, int semester) {
        super(id, name, age, email);          // call to base class constructor
        this.department = department;
        this.semester = semester;
        this.enrolledCourses = new ArrayList<String>();
    }

    /** Overloaded constructor: defaults to semester 1. */
    public Student(String id, String name, int age, String email, String department) {
        this(id, name, age, email, department, 1);
    }

    @Override
    public String getRole() {                 // method overriding
        return "Student";
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public List<String> getEnrolledCourses() {
        return new ArrayList<String>(enrolledCourses);   // defensive copy
    }

    public boolean enroll(String courseCode) {
        if (courseCode == null || isEnrolled(courseCode)) {
            return false;
        }
        return enrolledCourses.add(courseCode.toUpperCase());
    }

    public boolean unenroll(String courseCode) {
        return courseCode != null && enrolledCourses.remove(courseCode.toUpperCase());
    }

    public boolean isEnrolled(String courseCode) {
        return courseCode != null && enrolledCourses.contains(courseCode.toUpperCase());
    }

    /** Serialises the student into one CSV line for file storage. */
    public String toCsv() {
        StringBuilder courses = new StringBuilder();
        for (int i = 0; i < enrolledCourses.size(); i++) {
            if (i > 0) {
                courses.append('|');
            }
            courses.append(enrolledCourses.get(i));
        }
        return String.join(",", getId(), getName(), String.valueOf(getAge()),
                getEmail(), department, String.valueOf(semester), courses.toString());
    }

    /** Rebuilds a Student object from a CSV line. */
    public static Student fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 6) {
            throw new IllegalArgumentException("Malformed student record: " + line);
        }
        Student student = new Student(parts[0], parts[1], Integer.parseInt(parts[2]),
                parts[3], parts[4], Integer.parseInt(parts[5]));
        if (parts.length > 6 && !parts[6].trim().isEmpty()) {
            String[] courses = parts[6].split("\\|");
            for (String course : courses) {
                student.enroll(course);
            }
        }
        return student;
    }

    @Override
    public int compareTo(Student other) {
        return getName().compareToIgnoreCase(other.getName());
    }

    @Override
    public String toString() {
        return String.format("%-6s %-22s %-4d %-14s Sem-%d  Courses:%d",
                getId(), getName(), getAge(), department, semester, enrolledCourses.size());
    }
}
