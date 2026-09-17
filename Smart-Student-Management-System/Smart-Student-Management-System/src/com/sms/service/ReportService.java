package com.sms.service;

import com.sms.exception.RecordNotFoundException;
import com.sms.model.Course;
import com.sms.model.GradeRecord;
import com.sms.model.LetterGrade;
import com.sms.model.Student;
import com.sms.storage.FileStorage;
import com.sms.util.MathUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MODULE 3b - Analytics & Reporting.
 * Produces report cards, rank lists, course statistics and a
 * marks matrix built on a 2-D array (Unit 4: 2-D arrays).
 */
public class ReportService {

    private final StudentService studentService;
    private final CourseService courseService;
    private final GradeService gradeService;
    private final FileStorage storage;

    public ReportService(StudentService studentService, CourseService courseService,
                         GradeService gradeService, FileStorage storage) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.gradeService = gradeService;
        this.storage = storage;
    }

    /** Individual report card for one student. */
    public String reportCard(String studentId) throws RecordNotFoundException {
        Student student = studentService.getStudent(studentId);
        StringBuilder sb = new StringBuilder();
        sb.append(line()).append('\n');
        sb.append("REPORT CARD - ").append(student.getName())
          .append(" (").append(student.getId()).append(")\n");
        sb.append("Department : ").append(student.getDepartment())
          .append("    Semester : ").append(student.getSemester()).append('\n');
        sb.append(line()).append('\n');
        sb.append(String.format("%-10s %-28s %-8s %-8s %s%n",
                "CODE", "COURSE", "MARKS", "GRADE", "RESULT"));

        List<GradeRecord> records = gradeService.getByStudent(student.getId());
        if (records.isEmpty()) {
            sb.append("No marks recorded yet.\n");
        }
        for (GradeRecord record : records) {
            String title;
            try {
                title = courseService.getCourse(record.getCourseCode()).getTitle();
            } catch (RecordNotFoundException ex) {
                title = "(course removed)";
            }
            sb.append(String.format("%-10s %-28s %-8.2f %-8s %s%n",
                    record.getCourseCode(), title, record.getMarks(),
                    record.getLetterGrade().name(), record.isPass() ? "PASS" : "FAIL"));
        }
        sb.append(line()).append('\n');
        sb.append(String.format("Average marks : %.2f%n", gradeService.averageMarks(student.getId())));
        sb.append(String.format("GPA (10 scale): %.2f%n", gradeService.calculateGpa(student.getId())));
        sb.append(line());
        return sb.toString();
    }

    /** Rank list of all students ordered by GPA, highest first. */
    public List<Student> rankList() {
        List<Student> students = studentService.listAll();
        Collections.sort(students, new Comparator<Student>() {
            @Override
            public int compare(Student first, Student second) {
                double gpaFirst = gradeService.calculateGpa(first.getId());
                double gpaSecond = gradeService.calculateGpa(second.getId());
                return Double.compare(gpaSecond, gpaFirst);      // descending
            }
        });
        return students;
    }

    public List<Student> topPerformers(int limit) {
        List<Student> ranked = rankList();
        return ranked.subList(0, Math.min(limit, ranked.size()));
    }

    /** Statistics for one course: average, highest, lowest, pass percentage. */
    public String courseStatistics(String courseCode) throws RecordNotFoundException {
        Course course = courseService.getCourse(courseCode);
        List<GradeRecord> records = gradeService.getByCourse(course.getCode());
        StringBuilder sb = new StringBuilder();
        sb.append(line()).append('\n');
        sb.append("COURSE STATISTICS - ").append(course.getCode())
          .append(" : ").append(course.getTitle()).append('\n');
        sb.append(line()).append('\n');

        if (records.isEmpty()) {
            sb.append("No marks recorded for this course yet.\n").append(line());
            return sb.toString();
        }

        List<Double> marks = new ArrayList<Double>();
        int passed = 0;
        Map<LetterGrade, Integer> distribution = new LinkedHashMap<LetterGrade, Integer>();
        for (LetterGrade grade : LetterGrade.values()) {
            distribution.put(grade, 0);
        }
        for (GradeRecord record : records) {
            marks.add(record.getMarks());
            if (record.isPass()) {
                passed++;
            }
            LetterGrade grade = record.getLetterGrade();
            distribution.put(grade, distribution.get(grade) + 1);
        }

        sb.append(String.format("Students evaluated : %d%n", records.size()));
        sb.append(String.format("Average marks      : %.2f%n", MathUtil.round(MathUtil.average(marks), 2)));
        sb.append(String.format("Highest marks      : %.2f%n", MathUtil.max(marks)));
        sb.append(String.format("Lowest marks       : %.2f%n", MathUtil.min(marks)));
        sb.append(String.format("Pass percentage    : %.2f%%%n",
                MathUtil.round((passed * 100.0) / records.size(), 2)));
        sb.append("Grade distribution :\n");
        for (Map.Entry<LetterGrade, Integer> entry : distribution.entrySet()) {
            sb.append(String.format("   %-2s %s%n", entry.getKey().name(),
                    bar(entry.getValue())));
        }
        sb.append(line());
        return sb.toString();
    }

    /**
     * Builds a students x courses marks matrix using a 2-D array and
     * renders it as a text table. Empty cells are shown as "-".
     */
    public String marksMatrix() {
        List<Student> students = studentService.listSorted();
        List<Course> courses = courseService.listAll();
        if (students.isEmpty() || courses.isEmpty()) {
            return "Nothing to show: add students and courses first.";
        }

        double[][] matrix = new double[students.size()][courses.size()];
        for (int i = 0; i < students.size(); i++) {
            for (int j = 0; j < courses.size(); j++) {
                matrix[i][j] = -1;                     // -1 marks "not evaluated"
            }
        }
        for (int i = 0; i < students.size(); i++) {
            for (GradeRecord record : gradeService.getByStudent(students.get(i).getId())) {
                for (int j = 0; j < courses.size(); j++) {
                    if (courses.get(j).getCode().equals(record.getCourseCode())) {
                        matrix[i][j] = record.getMarks();
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(line()).append('\n').append("MARKS MATRIX\n").append(line()).append('\n');
        sb.append(String.format("%-22s", "STUDENT"));
        for (Course course : courses) {
            sb.append(String.format("%-10s", course.getCode()));
        }
        sb.append(String.format("%-8s", "AVG")).append('\n');

        for (int i = 0; i < students.size(); i++) {
            sb.append(String.format("%-22s", trim(students.get(i).getName(), 20)));
            List<Double> rowMarks = new ArrayList<Double>();
            for (int j = 0; j < courses.size(); j++) {
                if (matrix[i][j] < 0) {
                    sb.append(String.format("%-10s", "-"));
                } else {
                    sb.append(String.format("%-10.1f", matrix[i][j]));
                    rowMarks.add(matrix[i][j]);
                }
            }
            sb.append(String.format("%-8.2f", MathUtil.average(rowMarks))).append('\n');
        }
        sb.append(line());
        return sb.toString();
    }

    /** Full institute summary, also used by the export feature. */
    public String instituteSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(line()).append('\n').append("INSTITUTE SUMMARY\n").append(line()).append('\n');
        sb.append("Generated on : ")
          .append(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date())).append('\n');
        sb.append("Total students : ").append(studentService.count()).append('\n');
        sb.append("Total courses  : ").append(courseService.count()).append('\n');
        sb.append("Grade records  : ").append(gradeService.listAll().size()).append('\n');
        sb.append(line()).append('\n');
        sb.append(String.format("%-6s %-24s %-14s %-8s %s%n",
                "RANK", "STUDENT", "DEPARTMENT", "AVG", "GPA"));
        int rank = 1;
        for (Student student : rankList()) {
            sb.append(String.format("%-6d %-24s %-14s %-8.2f %.2f%n", rank++,
                    trim(student.getName(), 22), student.getDepartment(),
                    gradeService.averageMarks(student.getId()),
                    gradeService.calculateGpa(student.getId())));
        }
        sb.append(line());
        return sb.toString();
    }

    /** Writes the summary and matrix to a text file inside the data folder. */
    public String exportReport() throws IOException {
        String fileName = "report_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
        String content = instituteSummary() + "\n\n" + marksMatrix() + "\n";
        storage.writeText(fileName, content);
        return fileName;
    }

    private static String bar(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append('#');
        }
        return sb.append(' ').append(count).toString();
    }

    private static String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + ".";
    }

    private static String line() {
        return "------------------------------------------------------------------------";
    }
}
