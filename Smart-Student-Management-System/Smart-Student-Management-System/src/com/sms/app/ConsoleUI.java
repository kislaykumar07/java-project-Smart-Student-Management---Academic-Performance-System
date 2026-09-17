package com.sms.app;

import com.sms.exception.DuplicateRecordException;
import com.sms.exception.InvalidInputException;
import com.sms.exception.RecordNotFoundException;
import com.sms.exception.SMSException;
import com.sms.model.Course;
import com.sms.model.GradeRecord;
import com.sms.model.Student;
import com.sms.service.AutoSaveService;
import com.sms.service.CourseService;
import com.sms.service.GradeService;
import com.sms.service.ReportService;
import com.sms.service.StudentService;
import com.sms.util.InputValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Text based front end. Handles all user interaction, input validation
 * and error reporting (Unit 1: flow control, Unit 3: exception handling).
 */
public class ConsoleUI {

    private final BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));

    private final StudentService studentService;
    private final CourseService courseService;
    private final GradeService gradeService;
    private final ReportService reportService;
    private final AutoSaveService autoSaveService;

    public ConsoleUI(StudentService studentService, CourseService courseService,
                     GradeService gradeService, ReportService reportService,
                     AutoSaveService autoSaveService) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.gradeService = gradeService;
        this.reportService = reportService;
        this.autoSaveService = autoSaveService;
    }

    public void start() {
        banner();
        boolean running = true;
        while (running) {
            mainMenu();
            String choice = prompt("Choose an option");
            try {
                switch (choice) {
                    case "1": studentMenu(); break;
                    case "2": courseMenu(); break;
                    case "3": reportMenu(); break;
                    case "4": saveNow(); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid option. Please enter 0-4.");
                }
            } catch (SMSException ex) {
                System.out.println("  [ERROR] " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("  [FILE ERROR] " + ex.getMessage());
            }
        }
        System.out.println("\nSaving data before exit...");
        autoSaveService.saveAll();
        System.out.println("Goodbye!");
    }

    /* ------------------------------------------------------------------ */
    /* MODULE 1 : STUDENT MANAGEMENT                                       */
    /* ------------------------------------------------------------------ */

    private void studentMenu() throws SMSException {
        System.out.println("\n--- STUDENT MANAGEMENT ---");
        System.out.println(" 1. Add student");
        System.out.println(" 2. View all students");
        System.out.println(" 3. Search student by name");
        System.out.println(" 4. Update student");
        System.out.println(" 5. Delete student");
        System.out.println(" 0. Back");
        String choice = prompt("Choose an option");
        switch (choice) {
            case "1": addStudent(); break;
            case "2": listStudents(); break;
            case "3": searchStudents(); break;
            case "4": updateStudent(); break;
            case "5": deleteStudent(); break;
            case "0": break;
            default: System.out.println("Invalid option.");
        }
    }

    private void addStudent() throws InvalidInputException, DuplicateRecordException {
        String id = InputValidator.requireStudentId(prompt("Student ID (e.g. S101)"));
        if (studentService.exists(id)) {
            throw new DuplicateRecordException("Student " + id + " already exists.");
        }
        String name = InputValidator.requireText("Name", prompt("Full name"));
        int age = InputValidator.requireAge(prompt("Age"));
        String email = InputValidator.requireEmail(prompt("Email"));
        String department = InputValidator.requireText("Department", prompt("Department"));
        int semester = InputValidator.requireSemester(prompt("Semester (1-8)"));
        studentService.addStudent(new Student(id, name, age, email, department, semester));
        System.out.println("  Student " + id + " added successfully.");
    }

    private void listStudents() {
        List<Student> students = studentService.listSorted();
        if (students.isEmpty()) {
            System.out.println("  No students on record.");
            return;
        }
        System.out.println(String.format("%-6s %-22s %-4s %-14s %-6s %s",
                "ID", "NAME", "AGE", "DEPARTMENT", "SEM", "COURSES"));
        for (Student student : students) {
            System.out.println(student);
        }
        System.out.println("  Total: " + students.size() + " student(s).");
    }

    private void searchStudents() {
        List<Student> found = studentService.search(prompt("Name contains"));
        if (found.isEmpty()) {
            System.out.println("  No matching students.");
            return;
        }
        for (Student student : found) {
            System.out.println(student);
        }
    }

    private void updateStudent() throws SMSException {
        Student student = studentService.getStudent(prompt("Student ID to update"));
        System.out.println("  Current: " + student);
        System.out.println("  Leave a field blank to keep the existing value.");
        String name = blankToNull(prompt("New name"));
        String ageInput = blankToNull(prompt("New age"));
        String email = blankToNull(prompt("New email"));
        String department = blankToNull(prompt("New department"));
        String semesterInput = blankToNull(prompt("New semester"));

        Integer age = ageInput == null ? null : InputValidator.requireAge(ageInput);
        Integer semester = semesterInput == null ? null : InputValidator.requireSemester(semesterInput);
        if (email != null) {
            email = InputValidator.requireEmail(email);
        }
        studentService.updateStudent(student.getId(), name, age, email, department, semester);
        System.out.println("  Updated: " + studentService.getStudent(student.getId()));
    }

    private void deleteStudent() throws SMSException {
        String id = prompt("Student ID to delete");
        Student student = studentService.getStudent(id);
        String confirm = prompt("Delete " + student.getName() + "? (y/n)");
        if (confirm.equalsIgnoreCase("y")) {
            studentService.deleteStudent(student.getId());
            gradeService.removeRecordsOfStudent(student.getId());
            System.out.println("  Student deleted along with related grade records.");
        } else {
            System.out.println("  Cancelled.");
        }
    }

    /* ------------------------------------------------------------------ */
    /* MODULE 2 : COURSE & ENROLLMENT MANAGEMENT                           */
    /* ------------------------------------------------------------------ */

    private void courseMenu() throws SMSException {
        System.out.println("\n--- COURSE & ENROLLMENT ---");
        System.out.println(" 1. Add course");
        System.out.println(" 2. View all courses");
        System.out.println(" 3. Enroll student in course");
        System.out.println(" 4. Remove enrollment");
        System.out.println(" 5. View students of a course");
        System.out.println(" 6. Record / update marks");
        System.out.println(" 7. Delete course");
        System.out.println(" 0. Back");
        String choice = prompt("Choose an option");
        switch (choice) {
            case "1": addCourse(); break;
            case "2": listCourses(); break;
            case "3": enrollStudent(); break;
            case "4": unenrollStudent(); break;
            case "5": listCourseStudents(); break;
            case "6": recordMarks(); break;
            case "7": deleteCourse(); break;
            case "0": break;
            default: System.out.println("Invalid option.");
        }
    }

    private void addCourse() throws SMSException {
        String code = InputValidator.requireCourseCode(prompt("Course code (e.g. CSE2006)"));
        if (courseService.exists(code)) {
            throw new DuplicateRecordException("Course " + code + " already exists.");
        }
        String title = InputValidator.requireText("Title", prompt("Course title"));
        int credits = InputValidator.requireInt("Credits", prompt("Credits (1-6)"));
        if (credits < 1 || credits > 6) {
            throw new InvalidInputException("Credits must be between 1 and 6.");
        }
        String faculty = InputValidator.requireText("Faculty", prompt("Faculty name"));
        courseService.addCourse(new Course(code, title, credits, faculty));
        System.out.println("  Course " + code + " added successfully.");
    }

    private void listCourses() {
        List<Course> courses = courseService.listAll();
        if (courses.isEmpty()) {
            System.out.println("  No courses on record.");
            return;
        }
        for (Course course : courses) {
            System.out.println(course);
        }
        System.out.println("  Total: " + courses.size() + " course(s).");
    }

    private void enrollStudent() throws SMSException {
        String studentId = prompt("Student ID");
        String courseCode = prompt("Course code");
        courseService.enroll(studentId, courseCode);
        System.out.println("  Enrollment successful.");
    }

    private void unenrollStudent() throws SMSException {
        String studentId = prompt("Student ID");
        String courseCode = prompt("Course code");
        courseService.unenroll(studentId, courseCode);
        System.out.println("  Enrollment removed.");
    }

    private void listCourseStudents() throws SMSException {
        List<Student> students = courseService.studentsInCourse(prompt("Course code"));
        if (students.isEmpty()) {
            System.out.println("  No students enrolled in this course.");
            return;
        }
        for (Student student : students) {
            System.out.println(student);
        }
    }

    private void recordMarks() throws SMSException {
        String studentId = prompt("Student ID");
        String courseCode = prompt("Course code");
        double marks = InputValidator.requireMarks(prompt("Marks out of 100"));
        GradeRecord record = gradeService.recordMarks(studentId, courseCode, marks);
        System.out.println("  Recorded: " + record);
    }

    private void deleteCourse() throws SMSException {
        String code = prompt("Course code to delete");
        courseService.deleteCourse(code);
        System.out.println("  Course deleted and enrollments cleared.");
    }

    /* ------------------------------------------------------------------ */
    /* MODULE 3 : ANALYTICS & REPORTING                                    */
    /* ------------------------------------------------------------------ */

    private void reportMenu() throws SMSException, IOException {
        System.out.println("\n--- PERFORMANCE ANALYTICS ---");
        System.out.println(" 1. Student report card");
        System.out.println(" 2. Rank list (by GPA)");
        System.out.println(" 3. Top performers");
        System.out.println(" 4. Course statistics");
        System.out.println(" 5. Marks matrix");
        System.out.println(" 6. Institute summary");
        System.out.println(" 7. Export report to file");
        System.out.println(" 0. Back");
        String choice = prompt("Choose an option");
        switch (choice) {
            case "1":
                System.out.println(reportService.reportCard(prompt("Student ID")));
                break;
            case "2":
                printRanked(reportService.rankList());
                break;
            case "3":
                int limit = InputValidator.requireInt("Count", prompt("How many top students"));
                printRanked(reportService.topPerformers(limit));
                break;
            case "4":
                System.out.println(reportService.courseStatistics(prompt("Course code")));
                break;
            case "5":
                System.out.println(reportService.marksMatrix());
                break;
            case "6":
                System.out.println(reportService.instituteSummary());
                break;
            case "7":
                String file = reportService.exportReport();
                System.out.println("  Report exported to data/" + file);
                break;
            case "0":
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void printRanked(List<Student> students) {
        if (students.isEmpty()) {
            System.out.println("  No students on record.");
            return;
        }
        System.out.println(String.format("%-6s %-24s %-14s %-8s %s",
                "RANK", "NAME", "DEPARTMENT", "AVG", "GPA"));
        int rank = 1;
        for (Student student : students) {
            System.out.println(String.format("%-6d %-24s %-14s %-8.2f %.2f", rank++,
                    student.getName(), student.getDepartment(),
                    gradeService.averageMarks(student.getId()),
                    gradeService.calculateGpa(student.getId())));
        }
    }

    /* ------------------------------------------------------------------ */

    private void saveNow() {
        autoSaveService.saveAll();
        System.out.println("  Data saved. Total saves this session: "
                + autoSaveService.getSaveCount());
    }

    private void mainMenu() {
        System.out.println("\n==========  MAIN MENU  ==========");
        System.out.println(" 1. Student Management");
        System.out.println(" 2. Course & Enrollment");
        System.out.println(" 3. Performance Analytics");
        System.out.println(" 4. Save data now");
        System.out.println(" 0. Exit");
    }

    private void banner() {
        System.out.println("=================================================");
        System.out.println("  SMART STUDENT MANAGEMENT & ACADEMIC");
        System.out.println("  PERFORMANCE SYSTEM  (Java Console Application)");
        System.out.println("=================================================");
    }

    private String prompt(String message) {
        System.out.print(message + ": ");
        try {
            String input = reader.readLine();
            return input == null ? "0" : input.trim();
        } catch (IOException ex) {
            System.out.println("  Could not read input: " + ex.getMessage());
            return "";
        }
    }

    private static String blankToNull(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }
}
