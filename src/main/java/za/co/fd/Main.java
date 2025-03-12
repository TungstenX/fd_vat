package za.co.fd;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import za.co.fd.config.ConfigManager;
import za.co.fd.data.Entry;
import za.co.fd.gi.InsetCGI;
import za.co.fd.gi.InsetGI;
import za.co.fd.gi.Raam;
import za.co.fd.input.CSVInputReader;
import za.co.fd.input.PDFInputReader;
import za.co.fd.output.CSVWriter;
import za.co.fd.output.ExcelWriter;

import javax.swing.SwingUtilities;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    @Getter
    private final ConfigManager configManager = new ConfigManager();
    private final InsetGI insetGI = new InsetGI();


    public Main() {
        configManager.loadConfig();
        //insetGI.begin();
    }

    public static void main(String[] args) {
        Main main = new Main();

        //main.runProcess();
        //main.runProcess2();
        SwingUtilities.invokeLater(() -> {
            Raam raam1 = new Raam(main.getConfigManager());
            raam1.setVisible(true);

            /*raam1.setSaveButtonListener(new SaveButtonListener() {
                @Override
                public void onSaveClicked(Book book) {
                    System.out.println("Entered Book Details:");
                    System.out.println("Book Title: " + book.getName());
                    System.out.println("Author: " + book.getAuthor().getName());
                    System.out.println("Genre: " + book.getGenre());
                    System.out.println("Is Unavailable: " + book.isTaken());
                }
            });*/
        });

    }

//    public void runProcess2() {
//        raam.wysRaam();
//    }

    public void processLine(final Map<String, String> rules, final List<Entry> entries, int startMonth, int endMonth, final String line) {
        final String[] parts = line.split(",");
        Entry entry = Entry.builder()
                .date(LocalDate.parse(parts[0].trim(), Constants.DATE_TIME_FORMATTER_CSV))
                .amount(Double.parseDouble(parts[1].trim()))
                .description(parts[3].trim().toUpperCase(Locale.ENGLISH))
                .build();
        if ((entry.getDate().getMonth().getValue() < startMonth) || (entry.getDate().getMonth().getValue() > endMonth)) {
            insetGI.voegByUitsetBoks("ᛒ Slaan inskrywing oor met datum: " + entry.getDate().format(Constants.DATE_TIME_FORMATTER_CSV));
            return;
        }
        String key = getKeyFrom(rules, entry.getDescription());
        boolean loop = key == null;
        while (loop) {
            final String boodskap = "Geen reël vir \"" + entry.getDescription() + "\" R " + entry.getAmount();
            insetGI.voegByUitsetBoks("ᛒ " + boodskap);
            final String[] regexRekening = insetGI.maakNuweReëlDlg(boodskap, rules);

//                System.out.println("\u16D2 Regex of new rule: ");
//                System.out.println("\u16D2\tFor amount before description: [.\\d]+\\s*");
//                System.out.println("\u16D2\tEscape * with \\*");

            if (regexRekening != null) {
                rules.put(regexRekening[0], regexRekening[1].toUpperCase(Locale.ENGLISH));
                entry.setAccount(regexRekening[1].toUpperCase(Locale.ENGLISH));
                loop = false;
            } else {
                insetGI.voegByUitsetBoks("[ᚢᛁᛚᛚᚨ] Probeer weer");
            }
        }
        if (StringUtils.isBlank(entry.getAccount())) {
            entry.setAccount(rules.get(key));
        } else {
            insetGI.voegByUitsetBoks("[ᚢᛁᛚᛚᚨ] Inskrywing het reeds 'n rekening gestel: " + entry);
        }

        insetGI.voegByUitsetBoks("ᛒ Verwerking: " + entry);
        entries.add(entry);
    }

    protected String getKeyFrom(final Map<String, String> rules, final String desc) {
        return rules.keySet().stream()
                .filter(key -> {
                    Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(desc);
                    return matcher.find();
                })
                .findFirst()
                .orElse(null);
    }
    /*public static void main(String[] args) {
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
    }*/


    public void runProcess() {
        insetGI.setGekoseReëls(configManager.get("laaste.reëls", "/home/andre/Projects/Java/FD_Vat/"));
        insetGI.setGekoseBankstate(configManager.get("laaste.bankstate", "/home/andre/Projects/Java/FD_Vat/").replace("#", "\n"));
        insetGI.setGekoseUitset(configManager.get("laaste.uitset", "/home/andre/Projects/Java/FD_Vat/"));
        insetGI.setMaandBegin(Integer.parseInt(configManager.get("laaste.maand.begin", "1")));
        insetGI.setMaandEindig(Integer.parseInt(configManager.get("laaste.maand.eindig", "2")));
        insetGI.wysLaaiDlg();

        configManager.set("laaste.reëls", insetGI.getGekoseReëls());
        configManager.set("laaste.bankstate", insetGI.getGekoseBankstate().replace("\n", "#"));
        configManager.set("laaste.uitset", insetGI.getGekoseUitset());
        configManager.set("laaste.maand.begin", Integer.toString(insetGI.getMaandBegin()));
        configManager.set("laaste.maand.eindig", Integer.toString(insetGI.getMaandEindig()));
        configManager.storeConfig();

        new Thread(insetGI::wysUitsetVenster).start();
        while (!insetGI.isUitsetVensterGereed()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        final List<Entry> entries = new LinkedList<>();
        final List<String> fileNameIn = Arrays.asList(insetGI.getGekoseBankstate().split("\n"));
        final Map<String, String> rules = readInRules(insetGI.getGekoseReëls());
        int rulesSize = rules.size();
        processInputFile(insetGI.getMaandBegin(), insetGI.getMaandEindig(), fileNameIn, rules, entries);

        //Update rules if needed
        int finalRulesSize = rules.size();
        if (rulesSize != finalRulesSize) {
            File file = new File(insetGI.getGekoseReëls());
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

        processOutputFile(insetGI.getMaandBegin(), insetGI.getMaandEindig(), entries, insetGI.getGekoseUitset());
        processOutputSummedFile(entries, insetGI.getGekoseUitset());
        insetGI.stop();
    }

    private void processOutputFile(final int startMonth, final int endMonth, final List<Entry> entries, final String fileNameIn) {
        if (fileNameIn.toLowerCase().endsWith(".xls")) {
            ExcelWriter.outputFile(startMonth, endMonth, entries, fileNameIn);
        } else if (fileNameIn.toLowerCase().endsWith(".csv")) {
            CSVWriter.outputFile(entries, fileNameIn);
        } else {
            insetGI.voegByUitsetBoks("[ᚢᛁᛚᛚᚨ] Verkeerde uitvoerlêertipe!");
        }
    }

    private void processOutputSummedFile(final List<Entry> entries, final String fileNameIn) {
        if (fileNameIn.toLowerCase().endsWith(".xls")) {
            ExcelWriter.outputSummedFile(entries, fileNameIn);
        } else if (fileNameIn.toLowerCase().endsWith(".csv")) {
            CSVWriter.outputSummedFile(entries, fileNameIn);
        } else {
            insetGI.voegByUitsetBoks("[ᚢᛁᛚᛚᚨ] Verkeerde opsomming uitvoerlêertipe!");
        }
    }

    private void processInputFile(final int startMonth, final int endMonth, final List<String> fileNameIn, Map<String, String> rules, List<Entry> entries) {
        List<String> list = new LinkedList<>();
        if (fileNameIn.get(0).toLowerCase().endsWith(".pdf")) {
            for (String fileName : fileNameIn) {
                list.addAll(PDFInputReader.readInPDFFileIn(fileName, insetGI));
            }
        } else if (fileNameIn.get(0).toLowerCase().endsWith(".csv")) {
            list = CSVInputReader.readInCSVFileIn(fileNameIn.get(0));

        } else {
            insetGI.voegByUitsetBoks("[ᚢᛁᛚᛚᚨ] Verkeerde invoerlêertipe!");
            return;
        }

        //Process each line
        list.forEach(s -> processLine(rules, entries, startMonth, endMonth, s));
    }

    private Map<String, String> readInRules(String fileNameRules) {
        Map<String, String> rules = new HashMap<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileNameRules))) {
            rules = stream
                    .map(s -> s.split(","))
                    .collect(Collectors.groupingBy(a -> a[0],
                            Collectors.mapping(a -> a[1],
                                    Collectors.joining(" "))));

        } catch (IOException e) {
            insetGI.voegByUitsetBoks("[ᚢᛁᛚᛚᚨ] File name rules: " + fileNameRules + " Fault: " + e.getMessage());
        }
        //quick test
        rules.keySet().forEach(key -> {
                    insetGI.voegByUitsetBoks("ᛒ Checking key: " + key);
                    try {
                        Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
                    } catch (PatternSyntaxException e) {
                        insetGI.voegByUitsetBoks("[ᚢᛁᛚᛚᚨ] Regex: " + key + " Fault: " + e.getMessage());
                    }
                }
        );
        return rules;
    }
}