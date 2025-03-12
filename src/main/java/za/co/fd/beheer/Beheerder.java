package za.co.fd.beheer;

import org.apache.commons.lang3.StringUtils;
import za.co.fd.Constants;
import za.co.fd.data.Entry;
import za.co.fd.gi.NuweReëlDlg;
import za.co.fd.gi.UitsetGI;
import za.co.fd.input.CSVInputReader;
import za.co.fd.input.PDFInputReader;
import za.co.fd.output.CSVWriter;
import za.co.fd.output.ExcelWriter;

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

public class Beheerder extends Thread {
    private List<Entry> entries = new LinkedList<>();
    private File reëlsLêer;

    private List<File> bankstaatLêers;

    private File uitsetLêer;

    private int beginMaand;
    private int eindMaand;

    private UitsetGI uitsetGI;

    private Beheerder() {}

    public Beheerder(final UitsetGI uitsetGI, final File reëlsLêer, final List<File> bankstaatLêers, final File uitsetLêer, int beginMaand, int eindMaand) {
        this.bankstaatLêers = bankstaatLêers;
        this.reëlsLêer = reëlsLêer;
        this.uitsetLêer = uitsetLêer;
        this.beginMaand = beginMaand;
        this.eindMaand = eindMaand;
        this.uitsetGI = uitsetGI;
    }

    public void run() {
        final List<Entry> entries = new LinkedList<>();
        final Map<String, String> reëls = readInRules(reëlsLêer);
        int rulesSize = reëls.size();
        prosesseerInsette(reëls, entries);

        //Update rules if needed
        int finalRulesSize = reëls.size();
        if (rulesSize != finalRulesSize) {
            try (BufferedWriter bf = new BufferedWriter(new FileWriter(reëlsLêer))) {
                for (Map.Entry<String, String> entry :
                        reëls.entrySet()) {
                    bf.write(entry.getKey() + ","
                            + entry.getValue());
                    bf.newLine();
                }
                bf.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        processOutputFile(entries);
        processOutputSummedFile(entries);
        uitsetGI.voegByUitsetBoks("*** Klaar ***");
    }

    private void prosesseerInsette(Map<String, String> rules, List<Entry> entries) {
        List<String> list = new LinkedList<>();
        if (bankstaatLêers.get(0).getName().toLowerCase().endsWith(".pdf")) {
            for (File lêer : bankstaatLêers) {
                list.addAll(PDFInputReader.readInPDFFileIn(lêer, uitsetGI));
            }
        } else if (bankstaatLêers.get(0).getName().toLowerCase().endsWith(".csv")) {
            list = CSVInputReader.readInCSVFileIn(bankstaatLêers.get(0));

        } else {
            uitsetGI.voegByUitsetBoks("[ᚢᛁᛚᛚᚨ] Verkeerde invoerlêertipe!");
            return;
        }

        //Process each line
        list.forEach(s -> processLine(rules, entries, s));
    }

    public void processLine(final Map<String, String> rules, final List<Entry> entries, final String line) {
        final String[] parts = line.split(",");
        Entry entry = Entry.builder()
                .date(LocalDate.parse(parts[0].trim(), Constants.DATE_TIME_FORMATTER_CSV))
                .amount(Double.parseDouble(parts[1].trim()))
                .description(parts[3].trim().toUpperCase(Locale.ENGLISH))
                .build();
        if ((entry.getDate().getMonth().getValue() < beginMaand) || (entry.getDate().getMonth().getValue() > eindMaand)) {
            uitsetGI.voegByUitsetBoks("ᛒ Slaan inskrywing oor met datum: " + entry.getDate().format(Constants.DATE_TIME_FORMATTER_CSV));
            return;
        }
        String key = getKeyFrom(rules, entry.getDescription());
        boolean loop = key == null;
        while (loop) {
            final String boodskap = "Geen reël vir \"" + entry.getDescription() + "\" R " + entry.getAmount();
            uitsetGI.voegByUitsetBoks("ᛒ " + boodskap);
            NuweReëlDlg dialog = new NuweReëlDlg(boodskap, rules);
            dialog.pack();
            dialog.setVisible(true);
            if (dialog.isOk()) {
                String regex = dialog.getRegex();
                String rekening = dialog.getRekening();
                if (!(regex.isBlank() || rekening.isBlank())) {
                    rules.put(regex, rekening.toUpperCase(Locale.ENGLISH));
                    entry.setAccount(rekening.toUpperCase(Locale.ENGLISH));
                    loop = false;
                }
            }
            if (loop) {
                uitsetGI.voegByUitsetBoks("[ᚢᛁᛚᛚᚨ] Probeer weer");
            }
        }
        if (StringUtils.isBlank(entry.getAccount())) {
            entry.setAccount(rules.get(key));
        } else if (key != null) {
            uitsetGI.voegByUitsetBoks("[ᚢᛁᛚᛚᚨ] Inskrywing het reeds 'n rekening gestel: " + entry);
        }

        uitsetGI.voegByUitsetBoks("ᛒ Verwerking: " + entry);
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

    private Map<String, String> readInRules(File fileNameRules) {
        Map<String, String> rules = new HashMap<>();
        try (Stream<String> stream = Files.lines(fileNameRules.toPath())) {
            rules = stream
                    .map(s -> s.split(","))
                    .collect(Collectors.groupingBy(a -> a[0],
                            Collectors.mapping(a -> a[1],
                                    Collectors.joining(" "))));

        } catch (IOException e) {
            uitsetGI.voegByUitsetBoks("[ᚢᛁᛚᛚᚨ] File name rules: " + fileNameRules + " Fault: " + e.getMessage());
        }
        //quick test
        rules.keySet().forEach(key -> {
            uitsetGI.voegByUitsetBoks("ᛒ Checking key: " + key);
                    try {
                        Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
                    } catch (PatternSyntaxException e) {
                        uitsetGI.voegByUitsetBoks("[ᚢᛁᛚᛚᚨ] Regex: " + key + " Fault: " + e.getMessage());
                    }
                }
        );
        return rules;
    }

    private void processOutputFile(final List<Entry> entries) {
        if (uitsetLêer.getName().toLowerCase().endsWith(".xls")) {
            ExcelWriter.outputFile(beginMaand, eindMaand, entries, uitsetLêer);
        } else if (uitsetLêer.getName().toLowerCase().endsWith(".csv")) {
            CSVWriter.outputFile(entries, uitsetLêer);
        } else {
            uitsetGI.voegByUitsetBoks("[ᚢᛁᛚᛚᚨ] Verkeerde uitvoerlêertipe!");
        }
    }

    private void processOutputSummedFile(final List<Entry> entries) {
        if (uitsetLêer.getName().toLowerCase().endsWith(".xls")) {
            ExcelWriter.outputSummedFile(entries, uitsetLêer);
        } else if (uitsetLêer.getName().toLowerCase().endsWith(".csv")) {
            CSVWriter.outputSummedFile(entries, uitsetLêer);
        } else {
            uitsetGI.voegByUitsetBoks("[ᚢᛁᛚᛚᚨ] Verkeerde opsomming uitvoerlêertipe!");
        }
    }

}
