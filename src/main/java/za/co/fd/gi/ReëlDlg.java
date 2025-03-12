package za.co.fd.gi;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LocalizedString;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Panels;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

class ReëlDlg extends DialogWindow {

    private final TextBox regex;
    private final TextBox account;
    private final Button okButton;
    //TBD Make entry
    private String[] selectedEntry = null;

    /**
     * Default constructor for {@code FileDialog}
     *
     * @param title       Title of the dialog
     * @param description Description of the dialog, is displayed at the top of the content area
     * @param dialogSize  Rough estimation of how big you want the dialog to be
     */
    public ReëlDlg(
            String title,
            String description,
            TerminalSize dialogSize,
            Set<String> rekeninge) {
        super(title);
        Panel contentPane = new Panel();
        contentPane.setLayoutManager(new GridLayout(3));

        if (description != null) {
            new Label(description)
                    .setLayoutData(
                            GridLayout.createLayoutData(
                                    GridLayout.Alignment.BEGINNING,
                                    GridLayout.Alignment.CENTER,
                                    false,
                                    false,
                                    3,
                                    1))
                    .addTo(contentPane);
        }

        /*int unitWidth = dialogSize.getColumns() / 3;
        int unitHeight = dialogSize.getRows();

        rulesListBox = new ActionListBox(new TerminalSize(unitWidth * 2, unitHeight));
        rulesListBox.withBorder(Borders.singleLine())
                .setLayoutData(GridLayout.createLayoutData(
                        GridLayout.Alignment.BEGINNING,
                        GridLayout.Alignment.CENTER,
                        false,
                        false))
                .addTo(contentPane);
        contentPane.addComponent(new EmptySpace(new TerminalSize(0, 0)));

         */

//        new Separator(Direction.HORIZONTAL)
//                .setLayoutData(
//                        GridLayout.createLayoutData(
//                                GridLayout.Alignment.FILL,
//                                GridLayout.Alignment.CENTER,
//                                true,
//                                false,
//                                2,
//                                1))
//                .addTo(contentPane);

        contentPane.addComponent(new Label("Regex:"));
        regex = new TextBox()
                .addTo(contentPane);
        contentPane.addComponent(new EmptySpace(new TerminalSize(0, 0)));

        contentPane.addComponent(new Label("Rekening:"));
        account = new TextBox()
                .addTo(contentPane);
        contentPane.addComponent(new Button("...", new Runnable() {
            @Override
            public void run() {
                String gekose = ListSelectDialog.showDialog(getTextGUI(), "Kies 'n rekening", null, new TerminalSize(40, 10), rekeninge.toArray(String[]::new));
                account.setText(gekose);
            }
        }));


        okButton = new Button("Kies", new OkHandler());
        Panels.grid(2,
                        okButton,
                        new Button(LocalizedString.Cancel.toString(), new CancelHandler()))
                .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER, false, false, 2, 1))
                .addTo(contentPane);

        setComponent(contentPane);
    }


    protected String[] getRule() {
        return selectedEntry;
    }

    @Override
    public String[] showDialog(WindowBasedTextGUI textGUI) {
        selectedEntry = null;
        super.showDialog(textGUI);
        return selectedEntry;
    }

    private class OkHandler implements Runnable {
        @Override
        public void run() {
            if (StringUtils.isNotBlank(regex.getText()) && StringUtils.isNotBlank(account.getText())) {
                try {
                    Pattern pattern = Pattern.compile(regex.getText(), Pattern.CASE_INSENSITIVE);
                } catch (PatternSyntaxException e) {
                    MessageDialog.showMessageDialog(getTextGUI(), "ᚢᛁᛚᛚᚨ", "Die regex (" + regex.getText() + ") is verkeer: " + e.getMessage(), MessageDialogButton.OK);
                    return;
                }
                selectedEntry = new String[]{regex.getText(), account.getText().toUpperCase(Locale.ENGLISH)};
                close();
            } else {
                MessageDialog.showMessageDialog(getTextGUI(), "ᚢᛁᛚᛚᚨ", "Vul in 'n waarde vir " + (StringUtils.isBlank(regex.getText()) ? "Regex" : "Rekening"), MessageDialogButton.OK);
            }
        }
    }

    private class CancelHandler implements Runnable {
        @Override
        public void run() {
            selectedEntry = null;
            close();
        }
    }
}

