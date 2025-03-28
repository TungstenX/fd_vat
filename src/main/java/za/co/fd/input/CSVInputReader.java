package za.co.fd.input;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVInputReader {

    public static List<String> readInCSVFileIn(String fileNameIn) {
        return readInCSVFileIn(new File(fileNameIn));
    }
    public static List<String> readInCSVFileIn(File fileNameIn) {
        List<String> list = new ArrayList<>();
        try (Stream<String> stream = Files.lines(fileNameIn.toPath())) {
            list = stream
                    .filter(line -> line.startsWith("20"))
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
