package com.sms.service;

import com.sms.exception.InvalidInputException;
import com.sms.exception.RecordNotFoundException;
import com.sms.model.Course;
import com.sms.model.GradeRecord;
import com.sms.model.Student;
import com.sms.storage.FileStorage;
import com.sms.util.MathUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MODULE 3a - Marks & Grade Management.
 * Stores one GradeRecord per (student, course) pair and derives
 * letter grades and GPA from it.
 */
public class GradeService {

    private static final String FILE_NAME = "grades.csv";

    private final Map<String, GradeRecord> records = new LinkedHashMap<String, GradeRecord>();
    private final FileStorage storage;
    private final StudentService studentService;
    private final CourseService courseService;

    public GradeService(FileStorage storage, StudentService studentService,
                        CourseService courseService) {
        this.storage = storage;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    /** Records or updates marks; the student must be enrolled in the course. */
    public synchronized GradeRecord recordMarks(String studentId, String courseCode, double marks)
            throws RecordNotFoundException, InvalidInputException {
        Student student = studentService.getStudent(studentId);
        Course course = courseService.getCourse(courseCode);
        if (!student.isEnrolled(course.getCode())) {
            throw new RecordNotFoundException(
                    student.getName() + " is not enrolled in " + course.getCode() + ".");
        }
        if (marks < 0 || marks > GradeRecord.MAX_MARKS) {
            throw new InvalidInputException("Marks must be between 0 and 100.");
        }
        GradeRecord record = new GradeRecord(student.getId(), course.getCode(), marks);
        records.put(record.key(), record);
        return record;
    }

    public synchronized GradeRecord getRecord(String studentId, String courseCode)
            throws RecordNotFoundException {
        String key = studentId.toUpperCase() + "::" + courseCode.toUpperCase();
        GradeRecord record = records.get(key);
        if (record == null) {
            throw new RecordNotFoundException(
                    "No marks recorded for " + studentId + " in " + courseCode + ".");
        }
        return record;
    }

    public synchronized List<GradeRecord> listAll() {
        return new ArrayList<GradeRecord>(records.values());
    }

    public synchronized List<GradeRecord> getByStudent(String studentId) {
        List<GradeRecord> result = new ArrayList<GradeRecord>();
        for (GradeRecord record : records.values()) {
            if (record.getStudentId().equalsIgnoreCase(studentId)) {
                result.add(record);
            }
        }
        return result;
    }

    public synchronized List<GradeRecord> getByCourse(String courseCode) {
        List<GradeRecord> result = new ArrayList<GradeRecord>();
        for (GradeRecord record : records.values()) {
            if (record.getCourseCode().equalsIgnoreCase(courseCode)) {
                result.add(record);
            }
        }
        return result;
    }

    /** Credit-weighted GPA on a 10 point scale. */
    public synchronized double calculateGpa(String studentId) {
        double weightedPoints = 0.0;
        int totalCredits = 0;
        for (GradeRecord record : getByStudent(studentId)) {
            int credits;
            try {
                credits = courseService.getCourse(record.getCourseCode()).getCredits();
            } catch (RecordNotFoundException ex) {
                credits = 3;                 // course removed later: assume default weight
            }
            weightedPoints += record.getLetterGrade().getGradePoint() * credits;
            totalCredits += credits;
        }
        if (totalCredits == 0) {
            return 0.0;
        }
        return MathUtil.round(weightedPoints / totalCredits, 2);
    }

    public synchronized double averageMarks(String studentId) {
        List<Double> marks = new ArrayList<Double>();
        for (GradeRecord record : getByStudent(studentId)) {
            marks.add(record.getMarks());
        }
        return MathUtil.round(MathUtil.average(marks), 2);
    }

    public synchronized void removeRecordsOfStudent(String studentId) {
        List<String> keys = new ArrayList<String>();
        for (GradeRecord record : records.values()) {
            if (record.getStudentId().equalsIgnoreCase(studentId)) {
                keys.add(record.key());
            }
        }
        for (String key : keys) {
            records.remove(key);
        }
    }

    public synchronized void load() throws IOException {
        records.clear();
        for (String line : storage.readLines(FILE_NAME)) {
            try {
                GradeRecord record = GradeRecord.fromCsv(line);
                records.put(record.key(), record);
            } catch (RuntimeException ex) {
                System.err.println("Skipping corrupt grade row: " + ex.getMessage());
            }
        }
    }

    public synchronized void save() throws IOException {
        List<String> lines = new ArrayList<String>();
        for (GradeRecord record : records.values()) {
            lines.add(record.toCsv());
        }
        storage.writeLines(FILE_NAME, lines);
    }
}
