package za.co.fd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        String fileNameRules;
        String fileNameIn;
        String fileNameOut;
        int startMonth;
        int endMonth;

        if (args.length < 5) {
            final String[] questions = {
                    "Rules (path/to/rules/file):        ",
                    "Input file (path/to/input/file):   ",
                    "Output file (path/to/output/file): ",
                    "Start month (1-12):                ",
                    "End month (1-12):                  ",
            };
            final String[] answers = new String[questions.length];
            for (int i = 0; i < questions.length; i++) {
                String question = questions[i];
                Scanner sc = new Scanner(System.in);
                System.out.println(question);
                answers[i] = sc.nextLine();
            }
            fileNameRules = answers[0];
            fileNameIn = answers[1];
            fileNameOut = answers[2];
            startMonth = Integer.parseInt(answers[3]);
            endMonth = Integer.parseInt(answers[4]);
        } else {
            fileNameRules = args[0];
            fileNameIn = args[1];
            fileNameOut = args[2];
            startMonth = Integer.parseInt(args[3]);
            endMonth = Integer.parseInt(args[4]);
        }

        final List<String> list = readInFileIn(fileNameIn);
        final List<Entry> entries = new LinkedList<>();
        final Map<String, String> rules = readInRules(fileNameRules);

        int rulesSize = rules.size();

        //Process each line
        list.forEach(s -> processLine(rules, entries, startMonth, endMonth, s));

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

        //Output output file
        File file = new File(fileNameOut);
        try (BufferedWriter bf = new BufferedWriter(new FileWriter(file))) {
            bf.write("DATE,DESCRIPTION,AMOUNT,FOR ACCOUNT");
            bf.newLine();
            for (Entry entry : entries) {
                bf.write(entry.toString());
                bf.newLine();
            }
            bf.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Output summed up
        final String fileNameOutSummed = fileNameOut.replace(".csv", "_summed.csv");
        file = new File(fileNameOutSummed);
        String currentAccount = "";
        double total = 0.0;
        try (BufferedWriter bf = new BufferedWriter(new FileWriter(file))) {
            Collections.sort(entries);
            for (Entry entry : entries) {
                if (!currentAccount.equals(entry.getAccount())) {
                    writeOutTotal(total, bf);
                    bf.newLine();

                    bf.write(",ACCOUNT: ");
                    bf.write(entry.getAccount());
                    bf.write(",");
                    bf.newLine();
                    bf.write("DATE,DESCRIPTION,AMOUNT");
                    bf.newLine();
                    total = 0.0;

                    currentAccount = entry.getAccount();
                }
                bf.write(entry.toStringSummed());
                bf.newLine();
                total += entry.getAmount();
            }
            writeOutTotal(total, bf);
            bf.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeOutTotal(double total, BufferedWriter bf) throws IOException {
        if (Math.abs(total) > 0.0) {
            bf.write(",TOTAL,");
            NumberFormat formatter = new DecimalFormat("R #0.00");
            bf.write(formatter.format(total));
            bf.newLine();
        }
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
                    System.out.println("Checking key: " + key);
                    Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
                });
        return rules;
    }

    private static List<String> readInFileIn(String fileNameIn) {
        List<String> list = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileNameIn))) {
            list = stream
                    .filter(line -> line.startsWith("20"))
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void processLine(final Map<String, String> rules, final List<Entry> entries, int startMonth, int endMonth, final String line) {

        final String[] parts = line.split(",");
        Entry entry = Entry.builder()
                .date(LocalDate.parse(parts[0].trim(), DateTimeFormatter.ofPattern("yyyy/MM/dd")))
                .amount(Double.parseDouble(parts[1].trim()))
                .description(parts[3].trim().toUpperCase(Locale.ENGLISH))
                .build();
        if ((entry.getDate().getMonth().getValue() < startMonth) || (entry.getDate().getMonth().getValue() > endMonth)) {
            System.out.println("Skipping entry having date: " + entry.getDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
            return;
        }
        String key = getKeyFrom(rules, entry.getDescription());
        boolean loop = key == null;
        while (loop) {
            String inputRegex;
            String inputAction;
            System.out.println("No rule for \"" + entry.getDescription() + "\" R " + entry.getAmount());
            do {
                System.out.println("Regex of new rule: ");
                System.out.println("\tFor amount before description: [.\\d,]+\\s*");
                System.out.println("\tEscape * with \\*");
                Scanner s = new Scanner(System.in);
                inputRegex = s.nextLine().trim();
            } while (inputRegex.length() == 0);
            do {
                System.out.println("Category of new rule: ");
                Scanner s = new Scanner(System.in);
                inputAction = s.nextLine().trim();
            } while (inputAction.length() == 0);

            //Test regex
            Pattern pattern = Pattern.compile(inputRegex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(entry.getDescription());
            loop = !matcher.find();
            if (!loop) {
                rules.put(inputRegex, inputAction.toUpperCase(Locale.ENGLISH));
            } else {
                System.err.println("Regex of new rule doesn't work, please try again");
            }
        }
        entry.setAccount(rules.get(key));
        System.out.println("Processing: " + entry);
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