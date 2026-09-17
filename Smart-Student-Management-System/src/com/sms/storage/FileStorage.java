package com.sms.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Character-oriented file persistence layer (Unit 4: Java I/O Streams,
 * Reader/Writer). Every entity is stored as a simple CSV text file.
 */
public class FileStorage {

    private final File directory;

    public FileStorage(String directoryPath) {
        this.directory = new File(directoryPath);
        if (!directory.exists() && !directory.mkdirs()) {
            System.err.println("Warning: could not create data directory " + directoryPath);
        }
    }

    public String getPath() {
        return directory.getAbsolutePath();
    }

    /** Reads every non-empty line of a file; returns an empty list if absent. */
    public List<String> readLines(String fileName) throws IOException {
        List<String> lines = new ArrayList<String>();
        File file = new File(directory, fileName);
        if (!file.exists()) {
            return lines;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return lines;
    }

    /** Overwrites a file with the given lines. */
    public void writeLines(String fileName, List<String> lines) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(directory, fileName)));
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /** Writes a plain text block, used for exported reports. */
    public void writeText(String fileName, String content) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(directory, fileName)));
            writer.write(content);
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
