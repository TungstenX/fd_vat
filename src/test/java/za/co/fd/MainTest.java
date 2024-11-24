package za.co.fd;

import org.junit.jupiter.api.Test;
import za.co.fd.data.Entry;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.*;
class MainTest {
    @Test
    void getKeyFrom() {
        Main main = new Main();
        Map<String, String> rules = Map.ofEntries(Map.entry("STEPHNIE SARS", "PAYROLL,,"));
        String str = main.getKeyFrom(rules, "STEPHNIE SARS");
        assertNotNull(str);
    }

    @Test
    void checkFormat(){
        Entry entry = new Entry(LocalDate.now(), -123.45,"bla", "blo");
        assertTrue(entry.toString().contains("\"-R 123,45\""));
        assertTrue(entry.toStringSummed().contains("\"-R 123,45\""));
//        System.out.println(entry.toString());
//        System.out.println(entry.toStringSummed());
    }


}