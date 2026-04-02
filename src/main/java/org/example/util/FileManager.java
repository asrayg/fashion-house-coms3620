package org.example.util;

import java.io.*;
import java.util.*;

/**
 * Shared utility for all file-based storage.
 * Every controller uses this to read and write CSV data.
 */
public class FileManager {

    /** Read all non-blank lines from a CSV file. Returns empty list if file doesn't exist. */
    public static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return lines;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Could not read file: " + filePath);
        }
        return lines;
    }

    /** Overwrite the entire file with the given lines. */
    public static void writeLines(String filePath, List<String> lines) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Could not write file: " + filePath);
        }
    }

    /** Append a single line to a file (creates the file if it doesn't exist). */
    public static void appendLine(String filePath, String line) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Could not append to file: " + filePath);
        }
    }

    /** Returns the next available integer ID based on current record count. */
    public static int nextId(String filePath) {
        return readLines(filePath).size() + 1;
    }

    /** Check if a file exists and has at least one record. */
    public static boolean hasRecords(String filePath) {
        return !readLines(filePath).isEmpty();
    }
}
