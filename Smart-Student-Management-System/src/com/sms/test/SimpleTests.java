package com.sms.test;

import com.sms.exception.DuplicateRecordException;
import com.sms.exception.InvalidInputException;
import com.sms.exception.RecordNotFoundException;
import com.sms.exception.SMSException;
import com.sms.model.Course;
import com.sms.model.GradeRecord;
import com.sms.model.LetterGrade;
import com.sms.model.Student;
import com.sms.service.CourseService;
import com.sms.service.GradeService;
import com.sms.service.ReportService;
import com.sms.service.StudentService;
import com.sms.storage.FileStorage;
import com.sms.util.InputValidator;
import com.sms.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight validation test runner (no external libraries required).
 * Run with:  java -cp out com.sms.test.SimpleTests
 */
public class SimpleTests {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Running validation tests...\n");

        testValidators();
        testMathUtil();
        testGradeMapping();
        testStudentCsvRoundTrip();
        testStudentCrud();
        testDuplicateAndMissingRecords();
        testEnrollmentAndGpa();
        testReports();

        System.out.println("\n---------------------------------------");
        System.out.println("PASSED: " + passed + "   FAILED: " + failed);
        System.out.println("---------------------------------------");
        if (failed > 0) {
            System.exit(1);
        }
    }

    /* ----------------------------- tests ----------------------------- */

    private static void testValidators() {
        try {
            check("valid student id accepted", "S101".equals(InputValidator.requireStudentId("s101")));
        } catch (InvalidInputException ex) {
            check("valid student id accepted", false);
        }
        check("empty name rejected", throwsInvalid(new Runnable() {
            public void run() {
                try {
                    InputValidator.requireText("Name", "   ");
                } catch (InvalidInputException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }));
        check("bad email rejected", throwsInvalid(new Runnable() {
            public void run() {
                try {
                    InputValidator.requireEmail("not-an-email");
                } catch (InvalidInputException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }));
        check("marks above 100 rejected", throwsInvalid(new Runnable() {
            public void run() {
                try {
                    InputValidator.requireMarks("140");
                } catch (InvalidInputException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }));
    }

    private static void testMathUtil() {
        List<Double> values = new ArrayList<Double>();
        values.add(80.0);
        values.add(90.0);
        values.add(70.0);
        check("recursive sum", MathUtil.sum(values) == 240.0);
        check("average of list", MathUtil.average(values) == 80.0);
        check("overloaded array average", MathUtil.average(new double[]{50, 100}) == 75.0);
        check("max / min", MathUtil.max(values) == 90.0 && MathUtil.min(values) == 70.0);
        check("rounding", MathUtil.round(8.6666, 2) == 8.67);
    }

    private static void testGradeMapping() {
        check("95 maps to S", LetterGrade.fromPercentage(95) == LetterGrade.S);
        check("72 maps to B", LetterGrade.fromPercentage(72) == LetterGrade.B);
        check("31 maps to F", LetterGrade.fromPercentage(31) == LetterGrade.F);
        GradeRecord record = new GradeRecord("S101", "cse2006", 88);
        check("course code normalised", "CSE2006".equals(record.getCourseCode()));
        check("percentage computed", record.getPercentage() == 88.0);
        check("pass flag via default method", record.isPass());
        check("fail flag", !new GradeRecord("S101", "CSE2006", 20).isPass());
    }

    private static void testStudentCsvRoundTrip() {
        Student student = new Student("S200", "Test User", 20, "test@vit.ac.in", "CSE", 2);
        student.enroll("CSE2006");
        student.enroll("MAT2002");
        Student restored = Student.fromCsv(student.toCsv());
        check("csv round trip keeps name", "Test User".equals(restored.getName()));
        check("csv round trip keeps courses", restored.getEnrolledCourses().size() == 2);
        check("duplicate enrollment blocked", !restored.enroll("CSE2006"));
        check("polymorphic role", "Student".equals(restored.getRole()));
    }

    private static void testStudentCrud() {
        StudentService service = new StudentService(new FileStorage("data/test"));
        try {
            service.addStudent(new Student("S301", "Riya Singh", 19, "riya@vit.ac.in", "CSE", 1));
            service.addStudent(new Student("S302", "Arjun Rao", 21, "arjun@vit.ac.in", "IT", 4));
            check("students added", service.count() == 2);
            service.updateStudent("S301", "Riya S", null, null, null, 2);
            check("student updated", "Riya S".equals(service.getStudent("S301").getName()));
            check("search by name", service.search("arjun").size() == 1);
            check("overloaded search with department", service.search("", "IT").size() == 1);
            service.deleteStudent("S302");
            check("student deleted", service.count() == 1);
        } catch (SMSException ex) {
            check("student crud flow", false);
        }
    }

    private static void testDuplicateAndMissingRecords() {
        StudentService service = new StudentService(new FileStorage("data/test"));
        boolean duplicateCaught = false;
        boolean missingCaught = false;
        try {
            service.addStudent(new Student("S401", "Nia", 19, "nia@vit.ac.in", "CSE", 1));
            service.addStudent(new Student("S401", "Nia Copy", 19, "nia2@vit.ac.in", "CSE", 1));
        } catch (DuplicateRecordException ex) {
            duplicateCaught = true;
        }
        try {
            service.getStudent("S999");
        } catch (RecordNotFoundException ex) {
            missingCaught = true;
        }
        check("duplicate student rejected", duplicateCaught);
        check("missing student reported", missingCaught);
    }

    private static void testEnrollmentAndGpa() {
        FileStorage storage = new FileStorage("data/test");
        StudentService studentService = new StudentService(storage);
        CourseService courseService = new CourseService(storage, studentService);
        GradeService gradeService = new GradeService(storage, studentService, courseService);
        try {
            studentService.addStudent(new Student("S501", "Meera", 20, "meera@vit.ac.in", "CSE", 3));
            courseService.addCourse(new Course("CSE2006", "Programming in Java", 3, "Dr. A"));
            courseService.addCourse(new Course("CSE2001", "Data Structures", 4, "Dr. B"));

            boolean blocked = false;
            try {
                gradeService.recordMarks("S501", "CSE2006", 90);
            } catch (RecordNotFoundException ex) {
                blocked = true;                 // not enrolled yet
            }
            check("marks blocked before enrollment", blocked);

            courseService.enroll("S501", "CSE2006");
            courseService.enroll("S501", "CSE2001");
            gradeService.recordMarks("S501", "CSE2006", 95);   // S -> 10 points, 3 credits
            gradeService.recordMarks("S501", "CSE2001", 75);   // B ->  8 points, 4 credits

            check("grade records stored", gradeService.getByStudent("S501").size() == 2);
            check("average marks", gradeService.averageMarks("S501") == 85.0);
            double expectedGpa = MathUtil.round((10 * 3 + 8 * 4) / 7.0, 2);
            check("credit weighted gpa", gradeService.calculateGpa("S501") == expectedGpa);
            check("students in course", courseService.studentsInCourse("CSE2006").size() == 1);
        } catch (SMSException ex) {
            check("enrollment and gpa flow", false);
        }
    }

    private static void testReports() {
        FileStorage storage = new FileStorage("data/test");
        StudentService studentService = new StudentService(storage);
        CourseService courseService = new CourseService(storage, studentService);
        GradeService gradeService = new GradeService(storage, studentService, courseService);
        ReportService reportService =
                new ReportService(studentService, courseService, gradeService, storage);
        try {
            courseService.addCourse(new Course("CSE2006", "Programming in Java", 3, "Dr. A"));
            studentService.addStudent(new Student("S601", "High Scorer", 20, "h@vit.ac.in", "CSE", 3));
            studentService.addStudent(new Student("S602", "Low Scorer", 20, "l@vit.ac.in", "CSE", 3));
            courseService.enroll("S601", "CSE2006");
            courseService.enroll("S602", "CSE2006");
            gradeService.recordMarks("S601", "CSE2006", 95);
            gradeService.recordMarks("S602", "CSE2006", 40);

            check("rank list ordered by gpa",
                    "S601".equals(reportService.rankList().get(0).getId()));
            check("top performers limit", reportService.topPerformers(1).size() == 1);
            check("report card contains student name",
                    reportService.reportCard("S601").contains("High Scorer"));
            check("course statistics has pass percentage",
                    reportService.courseStatistics("CSE2006").contains("Pass percentage"));
            check("marks matrix rendered",
                    reportService.marksMatrix().contains("MARKS MATRIX"));
            check("institute summary rendered",
                    reportService.instituteSummary().contains("INSTITUTE SUMMARY"));
        } catch (SMSException ex) {
            check("reporting flow", false);
        }
    }

    /* ----------------------------- helpers ---------------------------- */

    private static boolean throwsInvalid(Runnable action) {
        try {
            action.run();
            return false;
        } catch (RuntimeException ex) {
            return ex.getCause() instanceof InvalidInputException;
        }
    }

    private static void check(String label, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            failed++;
            System.out.println("  [FAIL] " + label);
        }
    }
}
