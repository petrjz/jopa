package cz.cvut.kbss.jopa;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

public class Printer {
    public static long duration1 = 0L;
    public static long duration2 = 0L;

    public static File outputFile = new File("results.txt");
    public static File readOnlyOutputFile = new File("results-read-only.txt");

    public static void createFiles() throws IOException {
        outputFile.createNewFile();
        readOnlyOutputFile.createNewFile();
    }

    public static void aggregate(Long d1, Long d2) {
        duration1 += d1;
        duration2 += d2;
    }

    public static void print() {
        System.out.println(duration1 + " " + duration2);
        duration1 = 0L;
        duration2 = 0L;
    }

    public static void outputLineReadOnly() throws IOException {
        String line = Long.toString(duration2) + " " + Long.toString(duration1) + " " + System.lineSeparator();
        Files.write(readOnlyOutputFile.toPath(), line.getBytes(), new OpenOption[]{StandardOpenOption.APPEND});
    }

    public static void outputLine() throws IOException {
        String line = Long.toString(duration2) + " " + Long.toString(duration1) + " " + System.lineSeparator();
        Files.write(outputFile.toPath(), line.getBytes(), new OpenOption[]{StandardOpenOption.APPEND});
    }

}
