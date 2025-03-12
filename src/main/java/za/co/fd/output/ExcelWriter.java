package za.co.fd.output;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import za.co.fd.data.Entry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class ExcelWriter {
    public static void outputFile(final int startMonth, final int endMonth, final List<Entry> entries, final String fileLocation) {
        outputFile(startMonth, endMonth, entries, new File(fileLocation));
    }
    public static void outputFile(final int startMonth, final int endMonth, final List<Entry> entries, final File fileLocation) {
        entries.sort(Comparator.comparing(Entry::getDate));
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(String.format("%d-%d", startMonth, endMonth));
            sheet.setColumnWidth(0, 2000);
            sheet.setColumnWidth(1, 10000);
            sheet.setColumnWidth(2, 4000);
            sheet.setColumnWidth(3, 8000);

            Row header = sheet.createRow(0);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle cellStyleDate = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            cellStyleDate.setDataFormat(createHelper.createDataFormat().getFormat("yyyy/MM/dd"));

            CellStyle cellStyleCurrency = workbook.createCellStyle();
            cellStyleCurrency.setAlignment(HorizontalAlignment.RIGHT);
            cellStyleCurrency.setDataFormat(createHelper.createDataFormat().getFormat("[$R-1C09] #0,00;[$R-1C09] -#0,00"));

            /*XSSFFont font = ((XSSFWorkbook) workbook).createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 16);
            font.setBold(true);
            headerStyle.setFont(font);*/

            //DATE|DESCRIPTION|AMOUNT|FOR ACCOUNT|
            Cell headerCell = header.createCell(0);
            headerCell.setCellValue("DATE");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(1);
            headerCell.setCellValue("DESCRIPTION");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(2);
            headerCell.setCellValue("AMOUNT");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(3);
            headerCell.setCellValue("FOR ACCOUNT");
            headerCell.setCellStyle(headerStyle);


            int rowIndex = 1;
            for (Entry entry : entries) {
                Row row = sheet.createRow(rowIndex);
                Cell cell = row.createCell(0);
                cell.setCellValue(entry.getDate());
                cell.setCellStyle(cellStyleDate);

                cell = row.createCell(1);
                cell.setCellValue(entry.getDescription());
                //cell.setCellStyle(style);

                cell = row.createCell(2);
                cell.setCellValue(entry.getAmount());
                cell.setCellStyle(cellStyleCurrency);

                cell = row.createCell(3);
                cell.setCellValue(entry.getAccount());
                //cell.setCellStyle(style);
                rowIndex++;
            }

            FileOutputStream outputStream = new FileOutputStream(fileLocation);
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void outputSummedFile(final List<Entry> entries, final String fileNameOut) {
        outputSummedFile(entries, new File(fileNameOut));
    }

    public static void outputSummedFile(final List<Entry> entries, final File fileNameOut) {

    }
}
