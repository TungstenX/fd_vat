package za.co.fd.input;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import za.co.fd.Constants;

public class PDFInputReader {
    public static List<String> readInPDFFileIn(String fileNameIn) {
        List<String> list = new ArrayList<>();
        try{
            PdfReader reader = new PdfReader(new FileInputStream(fileNameIn));
            int pages = reader.getNumberOfPages();
            StringBuilder text = new StringBuilder();
            for (int i = 1; i <= pages; i++) {
                String str = PdfTextExtractor.getTextFromPage(reader, i);
                text.append(str);
            }
            reader.close();
            String [] lines = text.toString().split("\n");

            Pattern startsWith = Pattern.compile("^(\\d{2})\\s(\\D{3})\\s(.*)");
            int year = 0;
            Pattern startsWithDate = Pattern.compile("Statement Date :");
            for(String line: lines) {
                if (year == 0 && line.length() > 4) {
                    Matcher m = startsWithDate.matcher(line);
                    if (m.find()) {
                        year = Integer.parseInt(line.substring(line.length() - 4));
                    }
                }
                Matcher m = startsWith.matcher(line);
                if(m.find() && m.groupCount() >= 3) {
                    String dateInString = m.group(1) + " " +  m.group(2) + " " + year;
                    LocalDate dateTime = LocalDate.parse(dateInString, Constants.DATE_TIME_FORMATTER_PDF);
                    StringBuilder sb = new StringBuilder(dateTime.format(Constants.DATE_TIME_FORMATTER_CSV));
                    sb.append(", ");
                    preProcessPDFLine(sb, m.group(3));
                    System.out.println("*/*/*" + sb);
                    list.add(sb.toString());
                }
            }
            //System.out.println(text);
        } catch (IOException e) {
            System.err.println(e);
        }
        return list;
    }

    private static void preProcessPDFLine(StringBuilder sb, String group3) {
        String regex = "((\\s?\\d{1,3}(,\\d{3})*(\\.\\d+)(Cr)?){1,3})$";
        Pattern endsThreeAmount = Pattern.compile(regex);
        Matcher m3 = endsThreeAmount.matcher(group3);
        if(m3.find()) {
            //System.out.println("\t3 Amounts: " + m3.group(0));
            String description = group3.replace(m3.group(0), "");
            if ("".equals(description)) {
                description = "UNKNOWN";
            }
            String []amounts = m3.group(0).split("\\s");
            if(amounts.length > 0) {
                String amount = "".equals(amounts[0]) ? (amounts.length > 1 ? amounts[1] : "") : amounts[0];
                amount = amount.replace(",", "");
                boolean positive = amount.endsWith("Cr");
                if (positive) {
                    amount = amount.replace("Cr", "");
                }
                double result = Double.parseDouble(amount);
                if (!positive) {
                    result *= -1;
                }
                sb.append(Constants.DECIMAL_FORMAT.format(result)).append(", ").append(Constants.DECIMAL_FORMAT.format(result)).append(", ").append(description.toUpperCase());
            }
        } else {
            System.out.println("\tNo amount for " + group3);
        }
    }
}
