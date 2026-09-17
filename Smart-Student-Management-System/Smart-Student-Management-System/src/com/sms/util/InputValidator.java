package com.sms.util;

import com.sms.exception.InvalidInputException;

/**
 * Central validation rules. Every method throws InvalidInputException
 * so the UI layer can report a single, consistent error type.
 */
public final class InputValidator {

    private InputValidator() {
    }

    public static String requireText(String field, String value) throws InvalidInputException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(field + " cannot be empty.");
        }
        return value.trim();
    }

    /** Student ids look like S101, course codes like CSE2006. */
    public static String requireStudentId(String value) throws InvalidInputException {
        String id = requireText("Student ID", value).toUpperCase();
        if (!id.matches("S\\d{3,5}")) {
            throw new InvalidInputException("Student ID must be S followed by 3-5 digits, e.g. S101.");
        }
        return id;
    }

    public static String requireCourseCode(String value) throws InvalidInputException {
        String code = requireText("Course code", value).toUpperCase();
        if (!code.matches("[A-Z]{2,4}\\d{3,4}")) {
            throw new InvalidInputException("Course code must look like CSE2006.");
        }
        return code;
    }

    public static int requireAge(String value) throws InvalidInputException {
        int age = requireInt("Age", value);
        if (age < 15 || age > 60) {
            throw new InvalidInputException("Age must be between 15 and 60.");
        }
        return age;
    }

    public static int requireSemester(String value) throws InvalidInputException {
        int semester = requireInt("Semester", value);
        if (semester < 1 || semester > 8) {
            throw new InvalidInputException("Semester must be between 1 and 8.");
        }
        return semester;
    }

    public static String requireEmail(String value) throws InvalidInputException {
        String email = requireText("Email", value);
        if (!email.matches("[\\w.+-]+@[\\w-]+\\.[\\w.]{2,}")) {
            throw new InvalidInputException("Email format is not valid.");
        }
        return email;
    }

    public static double requireMarks(String value) throws InvalidInputException {
        double marks;
        try {
            marks = Double.parseDouble(requireText("Marks", value));
        } catch (NumberFormatException ex) {
            throw new InvalidInputException("Marks must be a number.");
        }
        if (marks < 0 || marks > 100) {
            throw new InvalidInputException("Marks must be between 0 and 100.");
        }
        return marks;
    }

    public static int requireInt(String field, String value) throws InvalidInputException {
        try {
            return Integer.parseInt(requireText(field, value));
        } catch (NumberFormatException ex) {
            throw new InvalidInputException(field + " must be a whole number.");
        }
    }
}
