/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.netbeans.launcher.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author vpc
 */
public class ConvertFlatLafFile {

//    public static void main(String[] args) {
//        File file = new File("/home/vpc/.netbeans/Netbeans IDE 21 Scholar/config/LookAndFeel/FlatLaf.properties");
//        convertPropertiesFile(file);
//    }

    private static void convertPropertiesFile(File file) {

        List<String> lines = new ArrayList<>();
        Replacer replacer = new Replacer();
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (!isEmptyOrComment(line) && line.startsWith("colors.")) {
                    Entry e = Entry.parse(line);
                    replacer.colors.put(e.key.substring("colors.".length()).trim(), e.value);
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        try (PrintStream out = new PrintStream(file)) {
            for (String line : lines) {
                if (line.isEmpty() || line.startsWith("#")) {
                    out.println(line);
                } else {
                    Entry e = Entry.parse(line);
                    out.println(e.key + "=" + replacer.replace(e.value));
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static boolean isEmptyOrComment(String line) {
        if (line.startsWith("#") || line.isEmpty()) {
            return true;
        }
        return false;
    }

    private static class Replacer {

        Map<String, String> colors = new HashMap<>();

        public String replace(String value) {
            String u = colors.get(value);
            if (u != null) {
                return u;
            }
            return value;
        }

    }

    private static class Entry {

        String key;
        String value;

        public Entry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public static Entry parse(String line) {
            int i = line.indexOf('=');
            if (i < 0) {
                throw new IllegalArgumentException("invalid key=value : " + line);
            }
            String k = line.substring(0, i).trim();
            String v = line.substring(i + 1).trim();
            return new Entry(k, v);
        }
    }
}
