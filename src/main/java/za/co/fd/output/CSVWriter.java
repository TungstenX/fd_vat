package za.co.fd.output;

import za.co.fd.data.Entry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

public class CSVWriter {
    public static void outputFile(final List<Entry> entries, final String fileLocation) {
        outputFile(entries, new File (fileLocation));
    }
    public static void outputFile(final List<Entry> entries, final File file) {
        //Output output file
        try (BufferedWriter bf = new BufferedWriter(new FileWriter(file))) {
            bf.write("DATE|DESCRIPTION|AMOUNT|FOR ACCOUNT|");
            bf.newLine();
            for (Entry entry : entries) {
                bf.write(entry.toString());
                bf.newLine();
            }
            bf.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void outputSummedFile(final List<Entry> entries, final String fileNameOut) {
        outputSummedFile(entries, new File(fileNameOut));
    }
    public static void outputSummedFile(final List<Entry> entries, final File fileOut) {
        //Output summed up
        String fileNameOut = fileOut.getAbsolutePath();
        final String fileNameOutSummed = fileNameOut.replace(".csv", "_summed.csv");
        File file = new File(fileNameOutSummed);
        String currentAccount = "";
        double total = 0.0;
        try (BufferedWriter bf = new BufferedWriter(new FileWriter(file))) {
            Collections.sort(entries);
            for (Entry entry : entries) {
                if (!currentAccount.equals(entry.getAccount())) {
                    writeOutTotal(total, bf);

                    bf.write("|");
                    bf.write(entry.getAccount());
                    bf.write("|");
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
            bf.write("||");
            NumberFormat formatter = new DecimalFormat("#0.00");
            bf.write(formatter.format(total));
            bf.newLine();
        }
    }
}
