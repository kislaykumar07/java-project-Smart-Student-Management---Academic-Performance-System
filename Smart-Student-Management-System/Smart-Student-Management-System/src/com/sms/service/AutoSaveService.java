package com.sms.service;

import java.io.IOException;

/**
 * Background worker thread (Unit 3: Multithreading, Thread life cycle,
 * synchronization). It periodically flushes all in-memory data to disk so
 * nothing is lost if the application is closed abruptly.
 */
public class AutoSaveService extends Thread {

    private final StudentService studentService;
    private final CourseService courseService;
    private final GradeService gradeService;
    private final long intervalMillis;
    private volatile boolean running = true;
    private int saveCount = 0;

    public AutoSaveService(StudentService studentService, CourseService courseService,
                           GradeService gradeService, long intervalMillis) {
        super("auto-save-thread");
        this.studentService = studentService;
        this.courseService = courseService;
        this.gradeService = gradeService;
        this.intervalMillis = intervalMillis;
        setDaemon(true);                 // must not block application exit
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(intervalMillis);     // RUNNABLE -> TIMED_WAITING
                saveAll();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;                            // graceful shutdown
            }
        }
    }

    /** Synchronized so a manual save and the auto save never overlap. */
    public synchronized void saveAll() {
        try {
            studentService.save();
            courseService.save();
            gradeService.save();
            saveCount++;
        } catch (IOException ex) {
            System.err.println("Auto-save failed: " + ex.getMessage());
        }
    }

    public synchronized int getSaveCount() {
        return saveCount;
    }

    public void shutdown() {
        running = false;
        interrupt();
    }
}
