package com.sms.service;

import com.sms.exception.DuplicateRecordException;
import com.sms.exception.RecordNotFoundException;
import com.sms.model.Course;
import com.sms.model.Student;
import com.sms.storage.FileStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MODULE 2 - Course & Enrollment Management.
 * Owns the course catalogue and links students to courses.
 */
public class CourseService {

    private static final String FILE_NAME = "courses.csv";

    private final Map<String, Course> courses = new LinkedHashMap<String, Course>();
    private final FileStorage storage;
    private final StudentService studentService;

    public CourseService(FileStorage storage, StudentService studentService) {
        this.storage = storage;
        this.studentService = studentService;
    }

    public synchronized void addCourse(Course course) throws DuplicateRecordException {
        if (courses.containsKey(course.getCode())) {
            throw new DuplicateRecordException("Course " + course.getCode() + " already exists.");
        }
        courses.put(course.getCode(), course);
    }

    public synchronized Course getCourse(String code) throws RecordNotFoundException {
        Course course = courses.get(code == null ? null : code.toUpperCase());
        if (course == null) {
            throw new RecordNotFoundException("No course found with code " + code + ".");
        }
        return course;
    }

    public synchronized void deleteCourse(String code) throws RecordNotFoundException {
        Course course = getCourse(code);
        courses.remove(course.getCode());
        for (Student student : studentService.listAll()) {
            student.unenroll(course.getCode());
        }
    }

    public synchronized List<Course> listAll() {
        return new ArrayList<Course>(courses.values());
    }

    public synchronized boolean exists(String code) {
        return code != null && courses.containsKey(code.toUpperCase());
    }

    public synchronized int count() {
        return courses.size();
    }

    /** Enrolls a student into a course after validating both exist. */
    public synchronized void enroll(String studentId, String courseCode)
            throws RecordNotFoundException, DuplicateRecordException {
        Student student = studentService.getStudent(studentId);
        Course course = getCourse(courseCode);
        if (!student.enroll(course.getCode())) {
            throw new DuplicateRecordException(
                    student.getName() + " is already enrolled in " + course.getCode() + ".");
        }
    }

    public synchronized void unenroll(String studentId, String courseCode)
            throws RecordNotFoundException {
        Student student = studentService.getStudent(studentId);
        Course course = getCourse(courseCode);
        if (!student.unenroll(course.getCode())) {
            throw new RecordNotFoundException(
                    student.getName() + " is not enrolled in " + course.getCode() + ".");
        }
    }

    /** All students registered for a given course. */
    public synchronized List<Student> studentsInCourse(String courseCode)
            throws RecordNotFoundException {
        Course course = getCourse(courseCode);
        List<Student> result = new ArrayList<Student>();
        for (Student student : studentService.listAll()) {
            if (student.isEnrolled(course.getCode())) {
                result.add(student);
            }
        }
        return result;
    }

    public synchronized void load() throws IOException {
        courses.clear();
        for (String line : storage.readLines(FILE_NAME)) {
            try {
                Course course = Course.fromCsv(line);
                courses.put(course.getCode(), course);
            } catch (RuntimeException ex) {
                System.err.println("Skipping corrupt course row: " + ex.getMessage());
            }
        }
    }

    public synchronized void save() throws IOException {
        List<String> lines = new ArrayList<String>();
        for (Course course : courses.values()) {
            lines.add(course.toCsv());
        }
        storage.writeLines(FILE_NAME, lines);
    }
}
