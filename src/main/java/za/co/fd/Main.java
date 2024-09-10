package za.co.fd;

import za.co.fd.data.Entry;
import za.co.fd.input.CSVInputReader;
import za.co.fd.input.PDFInputReader;
import za.co.fd.output.CSVWriter;
import za.co.fd.output.ExcelWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        String fileNameRules;
        List<String> fileNameIn = new LinkedList<>();
        String fileNameOut;
        int startMonth;
        int endMonth;

        if (args.length < 5) {
            final String[] questions = {
                    "\u16D2 Rules (path/to/rules/file):        ",
                    "\u16D2 Input file(s) (path/to/input/file) (submit empty line to stop):   ",
                    "\u16D2 Output file (path/to/output/file): ",
                    "\u16D2 Start month (1-12):                ",
                    "\u16D2 End month (1-12):                  ",
            };
            final List<String> answers = new LinkedList<>();
            for (int i = 0; i < questions.length; i++) {
                int answerLength = 0;
                String question = questions[i];
                Scanner sc = new Scanner(System.in);
                System.out.println(question);
                do {
                    System.out.print("\u16D2 ");
                    String answer = sc.nextLine();
                    answerLength = answer.length();
                    if(answerLength > 0) {
                        answers.add(answer);
                    }
                } while(i == 1 && answerLength > 0);
            }
            if (answers.size() < 5) {
                System.exit(0);
            }
            fileNameRules = answers.get(0);
            int numberOfInputFiles = answers.size() - questions.length + 1;
            for(int i = 0; i < numberOfInputFiles; i++ ) {
                fileNameIn.add(answers.get(i + 1));
            }
            fileNameOut = answers.get(1 + numberOfInputFiles);
            startMonth = Integer.parseInt(answers.get(2 + numberOfInputFiles));
            endMonth = Integer.parseInt(answers.get(3 + numberOfInputFiles));
        } else {
            fileNameRules = args[0];
            fileNameIn.add(args[1]);
            fileNameOut = args[2];
            startMonth = Integer.parseInt(args[3]);
            endMonth = Integer.parseInt(args[4]);
        }

        final List<Entry> entries = new LinkedList<>();
        final Map<String, String> rules = readInRules(fileNameRules);
        int rulesSize = rules.size();
        processInputFile(startMonth, endMonth, fileNameIn, rules, entries);

        //Update rules if needed
        int finalRulesSize = rules.size();
        if (rulesSize != finalRulesSize) {
            File file = new File(fileNameRules);
            try (BufferedWriter bf = new BufferedWriter(new FileWriter(file))) {
                for (Map.Entry<String, String> entry :
                        rules.entrySet()) {
                    bf.write(entry.getKey() + ","
                            + entry.getValue());
                    bf.newLine();
                }
                bf.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        processOutputFile(startMonth, endMonth, entries, fileNameOut);
        processOutputSummedFile(entries, fileNameOut);
    }

    private static void processOutputFile(final int startMonth, final int endMonth, final List<Entry> entries, final String fileNameIn) {
        if (fileNameIn.toLowerCase().endsWith(".xls")) {
            ExcelWriter.outputFile(startMonth, endMonth, entries, fileNameIn);
        } else if (fileNameIn.toLowerCase().endsWith(".csv")) {
            CSVWriter.outputFile(entries, fileNameIn);
        } else {
            System.err.println("Wrong file type!");
        }
    }

    private static void processOutputSummedFile(final List<Entry> entries, final String fileNameIn) {
        if (fileNameIn.toLowerCase().endsWith(".xls")) {
            ExcelWriter.outputSummedFile(entries, fileNameIn);
        } else if (fileNameIn.toLowerCase().endsWith(".csv")) {
            CSVWriter.outputSummedFile(entries, fileNameIn);
        } else {
            System.err.println("Wrong file type!");
        }
    }

    private static void processInputFile(final int startMonth, final int endMonth, final List<String> fileNameIn, Map<String, String> rules, List<Entry> entries) {
        List<String> list = new LinkedList<>();
        if (fileNameIn.get(0).toLowerCase().endsWith(".pdf")) {
            for(String fileName: fileNameIn) {
                list.addAll(PDFInputReader.readInPDFFileIn(fileName));
            }
        } else if (fileNameIn.get(0).toLowerCase().endsWith(".csv")) {
            list = CSVInputReader.readInCSVFileIn(fileNameIn.get(0));

        } else {
            System.err.println("Wrong file type!");
            return;
        }

        //Process each line
        list.forEach(s -> processLine(rules, entries, startMonth, endMonth, s));
    }

    private static Map<String, String> readInRules(String fileNameRules) {
        Map<String, String> rules = new HashMap<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileNameRules))) {
            rules = stream
                    .map(s -> s.split(","))
                    .collect(Collectors.groupingBy(a -> a[0],
                            Collectors.mapping(a -> a[1],
                                    Collectors.joining(" "))));

        } catch (IOException e) {
            e.printStackTrace();
        }
        //quick test
        rules.keySet().forEach(key -> {
                    System.out.println("\u16D2 Checking key: " + key);
                    Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
                });
        return rules;
    }


    public static void processLine(final Map<String, String> rules, final List<Entry> entries, int startMonth, int endMonth, final String line) {

        final String[] parts = line.split(",");
        Entry entry = Entry.builder()
                .date(LocalDate.parse(parts[0].trim(), Constants.DATE_TIME_FORMATTER_CSV))
                .amount(Double.parseDouble(parts[1].trim()))
                .description(parts[3].trim().toUpperCase(Locale.ENGLISH))
                .build();
        if ((entry.getDate().getMonth().getValue() < startMonth) || (entry.getDate().getMonth().getValue() > endMonth)) {
            System.out.println("\u16D2 Skipping entry having date: " + entry.getDate().format(Constants.DATE_TIME_FORMATTER_CSV));
            return;
        }
        String key = getKeyFrom(rules, entry.getDescription());
        boolean loop = key == null;
        while (loop) {
            String inputRegex;
            String inputAction;
            System.out.println("\u16D2 No rule for \"" + entry.getDescription() + "\" R " + entry.getAmount());
            do {
                System.out.println("\u16D2 Regex of new rule: ");
                System.out.println("\u16D2\tFor amount before description: [.\\d]+\\s*");
                System.out.println("\u16D2\tEscape * with \\*");
                System.out.print("\u16D2 ");
                Scanner s = new Scanner(System.in);
                inputRegex = s.nextLine().trim();
            } while (inputRegex.length() == 0);
            do {
                System.out.println("\u16D2 Category of new rule: ");
                System.out.print("\u16D2 ");
                Scanner s = new Scanner(System.in);
                inputAction = s.nextLine().trim();
            } while (inputAction.length() == 0);

            //Test regex
            Pattern pattern = Pattern.compile(inputRegex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(entry.getDescription());
            loop = !matcher.find();
            if (!loop) {
                rules.put(inputRegex, inputAction.toUpperCase(Locale.ENGLISH));
                entry.setAccount(inputAction);
            } else {
                System.err.println("\u16D2 Regex of new rule doesn't work, please try again");
            }
        }
        entry.setAccount(rules.get(key));
        System.out.println("\u16D2 Processing: " + entry);
        entries.add(entry);
    }

    protected static String getKeyFrom(final Map<String, String> rules, final String desc) {
        return rules.keySet().stream()
                .filter(key -> {
                    Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(desc);
                    return matcher.find();
                })
                .findFirst()
                .orElse(null);
    }
}