package com.sms.service;

import com.sms.exception.DuplicateRecordException;
import com.sms.exception.RecordNotFoundException;
import com.sms.model.Student;
import com.sms.storage.FileStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MODULE 1 - Student Management.
 * Full CRUD over students, backed by a LinkedHashMap
 * (Unit 4: Java Collections Framework).
 * Methods are synchronized because a background auto-save thread
 * reads the same data (Unit 3: Java Synchronization).
 */
public class StudentService {

    private static final String FILE_NAME = "students.csv";

    private final Map<String, Student> students = new LinkedHashMap<String, Student>();
    private final FileStorage storage;

    public StudentService(FileStorage storage) {
        this.storage = storage;
    }

    public synchronized void addStudent(Student student) throws DuplicateRecordException {
        if (students.containsKey(student.getId())) {
            throw new DuplicateRecordException("Student " + student.getId() + " already exists.");
        }
        students.put(student.getId(), student);
    }

    public synchronized Student getStudent(String id) throws RecordNotFoundException {
        Student student = students.get(id == null ? null : id.toUpperCase());
        if (student == null) {
            throw new RecordNotFoundException("No student found with ID " + id + ".");
        }
        return student;
    }

    public synchronized void updateStudent(String id, String name, Integer age,
                                           String email, String department, Integer semester)
            throws RecordNotFoundException {
        Student student = getStudent(id);
        if (name != null) {
            student.setName(name);
        }
        if (age != null) {
            student.setAge(age);
        }
        if (email != null) {
            student.setEmail(email);
        }
        if (department != null) {
            student.setDepartment(department);
        }
        if (semester != null) {
            student.setSemester(semester);
        }
    }

    public synchronized void deleteStudent(String id) throws RecordNotFoundException {
        getStudent(id);
        students.remove(id.toUpperCase());
    }

    public synchronized List<Student> listAll() {
        return new ArrayList<Student>(students.values());
    }

    public synchronized List<Student> listSorted() {
        List<Student> sorted = listAll();
        Collections.sort(sorted);              // uses Student.compareTo
        return sorted;
    }

    /** Search by partial name (case insensitive). */
    public synchronized List<Student> search(String nameFragment) {
        List<Student> result = new ArrayList<Student>();
        String query = nameFragment == null ? "" : nameFragment.toLowerCase();
        for (Student student : students.values()) {
            if (student.getName().toLowerCase().contains(query)) {
                result.add(student);
            }
        }
        return result;
    }

    /** Overloaded search: name fragment plus department filter. */
    public synchronized List<Student> search(String nameFragment, String department) {
        List<Student> result = new ArrayList<Student>();
        for (Student student : search(nameFragment)) {
            if (student.getDepartment().equalsIgnoreCase(department)) {
                result.add(student);
            }
        }
        return result;
    }

    public synchronized int count() {
        return students.size();
    }

    public synchronized boolean exists(String id) {
        return id != null && students.containsKey(id.toUpperCase());
    }

    public synchronized void load() throws IOException {
        students.clear();
        for (String line : storage.readLines(FILE_NAME)) {
            try {
                Student student = Student.fromCsv(line);
                students.put(student.getId(), student);
            } catch (RuntimeException ex) {
                System.err.println("Skipping corrupt student row: " + ex.getMessage());
            }
        }
    }

    public synchronized void save() throws IOException {
        List<String> lines = new ArrayList<String>();
        for (Student student : students.values()) {
            lines.add(student.toCsv());
        }
        storage.writeLines(FILE_NAME, lines);
    }
}
