package za.co.fd.gi;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.FileDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.io.SignerOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
@Getter
@Setter
public class InsetGI implements WindowListener, UitsetGGI {
    private String gekoseReëls = "/home/andre/Projects/Java/FD_Vat/";
    private String gekoseBankstate = "/home/andre/Projects/Java/FD_Vat/";
    private String gekoseUitset = "/home/andre/Projects/Java/FD_Vat/";
    private File lasteBankstaatLêer = null;
    private int maandBegin = 1;
    private int maandEindig = 2;

    private Terminal terminaal;
    private Screen skerm;
    private TextBox uitsetBoks;
    private Window uitsetVenster;
    private final AtomicBoolean uitsetVensterIsGereed = new AtomicBoolean(Boolean.FALSE);

   /* public static void main(String[] args) throws InterruptedException {
        InsetGI main = new InsetGI();
        main.begin();
        //main.wysLaaiDlg();
        System.out.println("Maak venster oop");
        new Thread(main::wysUitsetVenster).start();
        while(!main.isUitsetVensterGereed()) {
            Thread.sleep(10);
        }
        System.out.println("Na wag");
        for(int i = 0; i < 100; i++) {
            main.voegByUitsetBoks(Integer.toString(i));
        }
        System.out.println("Na opdateering");
        Thread.sleep(2000);
        System.out.println("Na nog wag");
        main.verwyderUitsetVenster();
        System.out.println("Maak venster toe");
        //main.stop();
    }*/

    public void begin() {
        try {
            // Setup terminal and screen layers
            terminaal = new DefaultTerminalFactory().createTerminal();
            skerm = new TerminalScreen(terminaal);
            skerm.startScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (skerm != null) {
                skerm.close();
            }
            if (terminaal != null) {
                terminaal.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void wysUitsetVenster() {
//        System.out.println("wysUitsetVenster 1");
        uitsetBoks = new TextBox(new TerminalSize(80, 20));
        uitsetBoks.setReadOnly(true);
        Panel paneel = new Panel();
        paneel.setLayoutManager(new GridLayout(1));

        paneel.addComponent(new Label("Uitset:"));
        paneel.addComponent(uitsetBoks);

        uitsetVenster = new BasicWindow();
        uitsetVenster.setHints(List.of(Window.Hint.CENTERED));
        uitsetVenster.setComponent(paneel);
        uitsetVenster.addWindowListener(this);
//        System.out.println("wysUitsetVenster 2");
        // Create gui and start gui
        MultiWindowTextGUI gui = new MultiWindowTextGUI(skerm, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
        gui.addWindowAndWait(uitsetVenster);
//        System.out.println("wysUitsetVenster 3");
    }

    public void verwyderUitsetVenster() {
        uitsetVenster.close();
    }

    @Override
    public void voegByUitsetBoks(final String uitsetLyn) {
        StringBuilder sb = new StringBuilder(uitsetBoks.getText());
        if (sb.toString().trim().length() > 0) {
            sb.append("\n");
        }
        sb.append(uitsetLyn);
        int count = sb.toString().length() - sb.toString().replace("\n", "").length();
        count++;

        uitsetBoks.setText(sb.toString());
        //count \n

        uitsetBoks.setCaretPosition(count, 0);
    }

    public boolean isUitsetVensterGereed() {
        synchronized (uitsetVensterIsGereed) {
            return uitsetVensterIsGereed.get();
        }
    }

    public void stelUitsetVensterIsGereed(boolean isGereed) {
        synchronized (uitsetVensterIsGereed) {
            uitsetVensterIsGereed.set(isGereed);
        }
    }

    public void wysLaaiDlg() {
        // Vars
        File reëlsLêer = new File(gekoseReëls);
        TextBox reëls = new TextBox(new TerminalSize(45, 1));
        reëls.withBorder(Borders.singleLine());
        if (reëlsLêer.exists() && !reëlsLêer.isDirectory()) {
            reëls.setText(reëlsLêer.getAbsolutePath());
        }

        TextBox bankstate = new TextBox(new TerminalSize(45, 5));
        bankstate.withBorder(Borders.singleLine());
        String[] bankstaatLêerPaaie = gekoseBankstate.split("\n");
        StringBuilder padData = new StringBuilder();
        for (String pad : bankstaatLêerPaaie) {
            File padLêer = new File(pad);
            lasteBankstaatLêer = padLêer;
            if (padLêer.exists() && !padLêer.isDirectory()) {
                padData.append(padLêer.getAbsolutePath()).append("\n");
            }
        }
        bankstate.setText(padData.toString().trim());

        File uitsetLêer = new File(gekoseUitset);
        TextBox uitset = new TextBox(new TerminalSize(45, 1));
        uitset.withBorder(Borders.singleLine());
        if (uitsetLêer.exists() && !uitsetLêer.isDirectory()) {
            uitset.setText(uitsetLêer.getAbsolutePath());
        }

        String[] maande = {"1 Jan", "2 Feb", "3 Maa", "4 Apr", "5 Mei", "6 Jun", "7 Jul", "8 Aug", "9 Sep", "10 Okt", "11 Nov", "12 Des"};
        ActionListBox beginMaand = new ActionListBox(new TerminalSize(7, 1));
        ActionListBox eindigMaand = new ActionListBox(new TerminalSize(7, 1));
        for (String item : maande) {
            beginMaand.addItem(item, new Runnable() {
                @Override
                public void run() {
                    String[] gedeeltes = item.split(" ");
                    maandBegin = Integer.parseInt(gedeeltes[0]);
                    maandEindig = maandBegin + 1;
                    eindigMaand.setSelectedIndex(beginMaand.getSelectedIndex() + 1);
                }
            });

            eindigMaand.addItem(item, new Runnable() {
                @Override
                public void run() {
                    String[] gedeeltes = item.split(" ");
                    maandEindig = Integer.parseInt(gedeeltes[0]);
                    maandBegin = maandEindig - 1;
                    beginMaand.setSelectedIndex(eindigMaand.getSelectedIndex() - 1);
                }
            });
        }
        beginMaand.setSelectedIndex(maandBegin - 1);
        eindigMaand.setSelectedIndex(maandEindig - 1);

        Panel paneel = new Panel();
        paneel.setLayoutManager(new GridLayout(3));

        paneel.addComponent(new Label("Reëls Lêer:"));
        paneel.addComponent(reëls);
        makeFileDlg(skerm, paneel, "Kies die reëls lêer (.csv)", reëlsLêer, reëls, false);


        paneel.addComponent(new Label("Bankstate:"));
        paneel.addComponent(bankstate);
        makeFileDlg(skerm, paneel, "Kies die bankstate (.pdf)", lasteBankstaatLêer, bankstate, true);

        paneel.addComponent(new Label("Uitset Lêer:"));
        paneel.addComponent(uitset);
        makeFileDlg(skerm, paneel, "Kies die uitset lêer (.xls)", uitsetLêer, uitset, false);

        paneel.addComponent(new Label("Begin maand:"));
        paneel.addComponent(beginMaand);
        paneel.addComponent(new EmptySpace(new TerminalSize(0, 0))); // Empty space underneath labels

        paneel.addComponent(new Label("Eind maand:"));
        paneel.addComponent(eindigMaand);
        paneel.addComponent(new EmptySpace(new TerminalSize(0, 0))); // Empty space underneath labels

        paneel.addComponent(new EmptySpace(new TerminalSize(0, 0))); // Empty space underneath labels
        paneel.addComponent(new EmptySpace(new TerminalSize(0, 0))); // Empty space underneath labels

        // Create window to hold the paneel
        BasicWindow window = new BasicWindow();
        window.setHints(List.of(Window.Hint.CENTERED));
        window.setComponent(paneel);
        // Create gui and start gui
        MultiWindowTextGUI gui = new MultiWindowTextGUI(skerm, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
        Button beginKnoppie= new Button("Begin", new Runnable() {
            @Override
            public void run() {
                gekoseReëls = reëls.getText();
                gekoseBankstate = bankstate.getText();
                gekoseUitset = uitset.getText();
                maandBegin = beginMaand.getSelectedIndex() + 1;
                maandEindig = eindigMaand.getSelectedIndex() + 1;

//                System.out.println(selectedRules);
//                System.out.println(selectedInputs);
//                System.out.println(selectedOutput);
//                System.out.println(monthStart);
//                System.out.println(monthEnd);
                String terug = checkLoadingValues();
                if(terug == null) {
                    window.close();
                } else {
                   MessageDialog.showMessageDialog(gui, "Fout", "Een of meer velde is of leeg of verkeerd. " + terug, MessageDialogButton.OK);
                }
            }
        });
        paneel.addComponent(beginKnoppie);

        gui.addWindowAndWait(window);


    }

    private String checkLoadingValues() {
        if (StringUtils.isBlank(this.gekoseReëls)) {
            return "Gekose reëls lêer naam is nie gestel nie";
        } else if (!gekoseReëls.endsWith(".csv")) {
            return "Gekose reëls lêer moet 'n .csv lêer wees";
        } else if (new File(gekoseReëls).isDirectory()) {
            return "Gekose reëls lêer mag nie 'n directory wees nie";
        } else if (StringUtils.isBlank(this.gekoseUitset)) {
            return "Gekose uitset lêer naam is nie gestel nie";
        } else if (!gekoseUitset.endsWith(".xls")) {
            return "Gekose uitset lêer moet 'n .xls lêer wees";
        } else if (new File(gekoseUitset).isDirectory()) {
            return "Gekose uitset lêer mag nie 'n directory wees nie";
        }
        boolean gevind = false;
        for(String input: gekoseBankstate.split("\n")) {
            System.out.println("Bankstaat: " + input);
            if (StringUtils.isBlank(input)) {
                return "Gekose bankstaat lêer naam is nie gestel nie";
            } else if (!input.endsWith(".pdf")) {
                return "Gekose bankstaat lêer moet 'n .pdf lêer wees [" + input + "]";
            } else if (!new File(input).exists()) {
                return "Gekose bankstaat lêer moet bestaan [" + input + "]";
            } else if (new File(input).isDirectory()) {
                return "Gekose bankstaat lêer mag nie 'n directory wees nie [" + input + "]";
            }
            gevind = true;
        }

        if (gevind) {
            return null;
        } else {
            return "Geen bankstaat lêers is gekies nie";
        }
    }

    public String[] maakNuweReëlDlg(final String description, final Map<String, String> rules) {
        this.uitsetBoks.setEnabled(false);
        final WindowBasedTextGUI reëlGGI = new MultiWindowTextGUI(skerm);
        Set<String> set = new TreeSet<>(rules.values());
        ReëlDlg dl = new ReëlDlg("Maak nuwe reël", description, new TerminalSize(80, 10), set);
        String[] ret = dl.showDialog(reëlGGI);
        this.uitsetBoks.setEnabled(true);
        return ret;
    }

    private void makeFileDlg(Screen screen, Panel panel, final String description, final File file, TextBox textBox, final boolean append) {
        // The Rules file dlg
        final WindowBasedTextGUI fileGUI = new MultiWindowTextGUI(screen);
        panel.addComponent(new Button("...", new Runnable() {
            @Override
            public void run() {
                String input = String.valueOf(new FileDialog(
                        "Maak Lêer Oop",
                        description,
                        "Maak oop",
                        new TerminalSize(45, 10),
                        true,
                        file
                )
                        .showDialog(fileGUI));
                if (!"null".equalsIgnoreCase(input)) {
                    if (append) {
                        lasteBankstaatLêer = new File(input);
                        StringBuilder sb = new StringBuilder(textBox.getText());
                        if (sb.toString().trim().length() > 0) {
                            sb.append("\n");
                        }
                        input = sb + input;
                    }
                    textBox.setText(input);
                }
            }
        }));
    }

    @Override
    public void onResized(Window window, TerminalSize terminalSize, TerminalSize terminalSize1) {
        stelUitsetVensterIsGereed(true);
//        System.out.println("onResized");
    }

    @Override
    public void onMoved(Window window, TerminalPosition terminalPosition, TerminalPosition terminalPosition1) {
//        System.out.println("onMoved");
    }

    @Override
    public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {
//        System.out.println("onInput");
    }

    @Override
    public void onUnhandledInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {
//        System.out.println("onUnhandledInput");
    }
}