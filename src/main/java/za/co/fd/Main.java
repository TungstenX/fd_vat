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
    final static Pattern PATTERN = Pattern.compile("([0-9]+|\\*)(\\|)([0-9a-zA-Z ]+)(\\|)?([<|>]{1}[-0-9]+)?(\\|)?([0-9a-zA-Z ]+)?");

    public static void main(String[] args) {

        String fileNameRules;
        String fileNameIn;
        String fileNameOut;
        int startMonth;
        int endMonth;


        if (args.length < 5) {
            final String[] questions = {
                    "Rules (path/to/rules/file): ",
                    "Input file (path/to/input/file): ",
                    "Output file (path/to/output/file): ",
                    "Start month (1-12): ",
                    "End month (path/to/rules/file): ",
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

        showInputRules();

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
            bf.write("DATE,DESCRIPTION,AMOUNT");
            bf.newLine();
            Collections.sort(entries);
            for (Entry entry : entries) {
                if (!currentAccount.equals(entry.getAccount())) {
                    bf.write(",ACCOUNT: ");
                    bf.write(entry.getAccount());
                    bf.write(",");
                    bf.newLine();
                    bf.write("DATE,DESCRIPTION,AMOUNT");
                    bf.newLine();

                    writeOutTotal(total, bf);

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
        if (total > 0.0) {
            bf.write(",TOTAL,");
            NumberFormat formatter = new DecimalFormat("R #0.00");
            bf.write(formatter.format(total));
            bf.newLine();
        }
    }

    private static void showInputRules() {
        System.out.println("Rule format: \"([0-9]+|\\\\*)(\\\\|)([0-9a-zA-Z ]+)(\\\\|)?([<|>]{1}[-0-9]+)?(\\\\|)?([0-9a-zA-Z ]+)?\"");
        System.out.println("\tE.g:\n\t\t25|Bank charges\n\t\t*|Petrol Car|<600|Petrol Generator");
        System.out.println("\tFields:");
        System.out.println("\t\t0. Substring length of description to use as key for rule or \"*\" to use the full length of the description [Mandatory]");
        System.out.println("\t\t1. The account name that this rule will apply [Mandatory]");
        System.out.println("\t\t2. Amount condition. Either < or > followed by amount no decimals (Optional)");
        System.out.println("\t\t3. Alternative account name if condition is true (Optional)");
    }

    private static Map<String, String> readInRules(String fileNameRules) {
        Map<String, String> rules = new HashMap<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileNameRules))) {
            rules = stream
                    .map(String::toUpperCase)
                    .map(s -> s.split(","))
                    .collect(Collectors.groupingBy(a -> a[0],
                            Collectors.mapping(a -> a[1],
                                    Collectors.joining(" "))));

        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if (key == null) {
            String input;
            Matcher matcher;
            boolean correct;
            do {
                System.out.print("            " + numberOfChars(entry.getDescription()) + "\n");
                System.out.print("No rule for " + entry.getDescription() + " R " + entry.getAmount() + ", Name of new rule: ");
                Scanner s = new Scanner(System.in);
                input = s.nextLine().trim();
                matcher = PATTERN.matcher(input);
                correct = matcher.matches();
                if (!correct) {
                    System.out.println("ERROR: Wrong input");
                }
            } while (!correct);

            String[] a = input.split("\\|");
            int endPos;
            String action = a[1];
            if ("*".equals(a[0])) {
                endPos = entry.getDescription().length();
            } else {
                endPos = Integer.parseInt(a[0]) + 1;
            }
            if (a.length > 2) {
                action += "," + a[2] + "," + a[3];
            } else {
                action += ",,";
            }

            key = entry.getDescription().substring(0, endPos);
            rules.put(key, action.toUpperCase(Locale.ENGLISH));
        }
        String[] actions = rules.get(key).split(",");
        String account;
        if (actions.length == 1) {
            account = actions[0];
        } else {
            char operator = actions[1].charAt(0);
            if (operator == '<') {
                if (entry.getAmount() < Double.parseDouble(actions[1].substring(1))) {
                    account = actions[2];
                } else {
                    account = actions[0];
                }
            } else {
                if (entry.getAmount() > Double.parseDouble(actions[1].substring(1))) {
                    account = actions[2];
                } else {
                    account = actions[0];
                }
            }
        }
        entry.setAccount(account);
        System.out.println("Processing: " + entry);
        entries.add(entry);
    }

    protected static String getKeyFrom(final Map<String, String> rules, final String desc) {
        for (int i = 1; i < desc.length() + 1; i++) {
            String s = desc.substring(0, i);
            if (rules.containsKey(s)) {
                return desc.substring(0, i);
            }
        }
        return null;
    }

    private static String numberOfChars(final String str) {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < str.length(); j++) {
            sb.append(i);
            i++;
            if (i == 10) {
                i = 0;
            }
        }
        return sb.toString();
    }
}