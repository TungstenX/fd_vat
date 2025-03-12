package za.co.fd;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Constants {
    public static final DateTimeFormatter DATE_TIME_FORMATTER_PDF = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    public static final DateTimeFormatter DATE_TIME_FORMATTER_CSV = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ENGLISH);
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");
    public static final DecimalFormatSymbols DECIMAL_FORMAT_SYMBOLS = new DecimalFormatSymbols();

    static {
        DECIMAL_FORMAT_SYMBOLS.setDecimalSeparator('.');
        DECIMAL_FORMAT.setDecimalFormatSymbols(DECIMAL_FORMAT_SYMBOLS);
        DECIMAL_FORMAT.setMaximumFractionDigits(2);
        DECIMAL_FORMAT.setGroupingUsed(false);
    }
}
