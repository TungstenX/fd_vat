package za.co.fd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class MainTest {
    @Test
    void getKeyFrom() {
        Map<String, String> rules = Map.ofEntries(Map.entry("STEPHNIE SARS", "PAYROLL,,"));
        String str = Main.getKeyFrom(rules, "STEPHNIE SARS");
        assertNotNull(str);
    }


}