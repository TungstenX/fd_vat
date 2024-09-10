package za.co.fd.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@AllArgsConstructor
public class Entry implements Comparable<Entry> {
    private LocalDate date;
    private Double amount;
    private String description;
    private String account;

    @Override
    public String toString() {
        NumberFormat formatter = new DecimalFormat("#0.00"); // ¤
        return date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "|"
                + description + "|"
                + formatter.format(amount) + "|"
                + account + "|";
    }

    public String toStringSummed() {
        NumberFormat formatter = new DecimalFormat("#0.00"); // ¤
        return date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "|"
                + description + "|"
                + formatter.format(amount) + "|";
    }

    @Override
    public int compareTo(Entry o) {
        return account.compareTo(o.account) == 0? date.compareTo(o.date) :  account.compareTo(o.account);
    }
}
