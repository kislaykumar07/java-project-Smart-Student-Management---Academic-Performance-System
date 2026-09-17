package com.sms.app;

import com.sms.exception.DuplicateRecordException;
import com.sms.exception.SMSException;
import com.sms.model.Course;
import com.sms.model.Student;
import com.sms.service.AutoSaveService;
import com.sms.service.CourseService;
import com.sms.service.GradeService;
import com.sms.service.ReportService;
import com.sms.service.StudentService;
import com.sms.storage.FileStorage;

import java.io.IOException;

/**
 * Application entry point. Wires the layers together, loads saved data,
 * starts the background auto-save thread and launches the console UI.
 */
public class Main {

    private static final long AUTO_SAVE_INTERVAL_MS = 30000;   // 30 seconds

    public static void main(String[] args) {
        FileStorage storage = new FileStorage("data");

        StudentService studentService = new StudentService(storage);
        CourseService courseService = new CourseService(storage, studentService);
        GradeService gradeService = new GradeService(storage, studentService, courseService);
        ReportService reportService =
                new ReportService(studentService, courseService, gradeService, storage);

        try {
            studentService.load();
            courseService.load();
            gradeService.load();
            System.out.println("Loaded " + studentService.count() + " student(s) and "
                    + courseService.count() + " course(s) from " + storage.getPath());
        } catch (IOException ex) {
            System.err.println("Could not load saved data: " + ex.getMessage());
        }

        if (studentService.count() == 0 && courseService.count() == 0) {
            seedSampleData(studentService, courseService, gradeService);
        }

        AutoSaveService autoSave = new AutoSaveService(
                studentService, courseService, gradeService, AUTO_SAVE_INTERVAL_MS);
        autoSave.start();                       // NEW -> RUNNABLE

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                autoSave.shutdown();
            }
        });

        new ConsoleUI(studentService, courseService, gradeService, reportService, autoSave).start();
        autoSave.shutdown();
    }

    /** Loads a small demo dataset so the app is usable on first run. */
    private static void seedSampleData(StudentService studentService,
                                       CourseService courseService,
                                       GradeService gradeService) {
        try {
            courseService.addCourse(new Course("CSE2006", "Programming in Java", 3, "Dr. Ashwin"));
            courseService.addCourse(new Course("CSE2001", "Data Structures", 4, "Dr. Lakshmi"));
            courseService.addCourse(new Course("MAT2002", "Discrete Mathematics", 3, "Dr. Sandip"));

            studentService.addStudent(new Student("S101", "Aarav Sharma", 19, "aarav@vit.ac.in", "CSE", 3));
            studentService.addStudent(new Student("S102", "Diya Verma", 20, "diya@vit.ac.in", "CSE", 3));
            studentService.addStudent(new Student("S103", "Kabir Nair", 19, "kabir@vit.ac.in", "IT", 3));

            String[] codes = {"CSE2006", "CSE2001", "MAT2002"};
            String[] ids = {"S101", "S102", "S103"};
            double[][] marks = {
                    {88, 92, 76},
                    {74, 65, 81},
                    {45, 58, 69}
            };
            for (int i = 0; i < ids.length; i++) {
                for (int j = 0; j < codes.length; j++) {
                    courseService.enroll(ids[i], codes[j]);
                    gradeService.recordMarks(ids[i], codes[j], marks[i][j]);
                }
            }
            System.out.println("Sample data loaded (3 students, 3 courses, 9 grade records).");
        } catch (DuplicateRecordException ex) {
            System.err.println("Seed skipped: " + ex.getMessage());
        } catch (SMSException ex) {
            System.err.println("Seed failed: " + ex.getMessage());
        }
    }
}
