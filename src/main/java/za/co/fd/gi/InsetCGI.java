package za.co.fd.gi;

import lombok.Getter;
import za.co.fd.config.ConfigManager;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class InsetCGI extends JPanel {
    @Getter
    private JPanel reëlsPaneel;
    private JTextField reëlsVeld;
    private JButton reëlsKnoppie;
    private JList bankstaatLys;
    private JButton bankstaatKnoppie;
    private JTextField uitsetLêerVeld;
    private JButton uitsetLêerKnoppie;
    private JComboBox beginMaand;
    private JComboBox eindMaand;
    private JButton beginKnoppie;
    private JButton bankstaatVerwyderKnoppie;


    private File reëlsLêer;


    private List<File> bankstaatLêers = new LinkedList<>();


    private File uitsetLêer;

    private ConfigManager configManager;

    public InsetCGI(ConfigManager configManager) {
        this.configManager = configManager;

        bankstaatLys.setVisibleRowCount(3);
        DefaultListModel<String> bankstaatLysDLM = new DefaultListModel<>();
        bankstaatLys.setModel(bankstaatLysDLM);
        bankstaatLysDLM.addElement("1");
        bankstaatLysDLM.addElement("2");
        bankstaatLysDLM.addElement("3");

        DefaultComboBoxModel<Integer> beginMaandDCBM = new DefaultComboBoxModel<>();
        beginMaand.setModel(beginMaandDCBM);
        for (int i = 1; i < 13; i++) {
            beginMaandDCBM.addElement(i);
        }
        DefaultComboBoxModel<Integer> eindMaandDCBM = new DefaultComboBoxModel<>();
        eindMaand.setModel(eindMaandDCBM);
        for (int i = 1; i < 13; i++) {
            eindMaandDCBM.addElement(i);
        }


        reëlsKnoppie.addActionListener(e -> {
            JFileChooser kies = new JFileChooser(reëlsVeld.getText());
            kies.setDialogTitle("Kies die reëls lêer (.csv)");
            kies.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Comma Separated Values", "csv");
            kies.addChoosableFileFilter(filter);
            // Open the file
            int res = kies.showOpenDialog(null);
            // Save the file
            // int res = kies.showSaveDialog(null);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = kies.getSelectedFile();
                reëlsVeld.setText(file.getAbsolutePath());
                reëlsLêer = file;
                reëlsVeld.invalidate();
            }
        });

        bankstaatKnoppie.addActionListener(e -> {
            int gekoseIndeks = bankstaatLys.getSelectedIndex();
            if (gekoseIndeks == -1) {
                gekoseIndeks = 0;
            }
            ListModel<String> bankstaatLysLM = bankstaatLys.getModel();
            String lêersadres = "";
            if (bankstaatLysLM.getSize() > 0) {
                lêersadres = bankstaatLysLM.getElementAt(gekoseIndeks);
            }

            JFileChooser kies = new JFileChooser(lêersadres);
            kies.setDialogTitle("Kies 'n bankstaat lêer (.pdf)");
            kies.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Portable Document Format", "pdf");
            kies.addChoosableFileFilter(filter);
            // Open the file
            int res = kies.showOpenDialog(null);
            // Save the file
            // int res = kies.showSaveDialog(null);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = kies.getSelectedFile();
                ((DefaultListModel) bankstaatLysLM).addElement(file.getAbsolutePath());
                bankstaatLêers.add(file);
                bankstaatLys.invalidate();
            }
        });

        bankstaatVerwyderKnoppie.addActionListener(e -> {
            int gekoseIndeks = bankstaatLys.getSelectedIndex();
            if (gekoseIndeks == -1) {
                return;
            }
            ListModel<String> bankstaatLysLM = bankstaatLys.getModel();
            ((DefaultListModel) bankstaatLysLM).remove(gekoseIndeks);
            bankstaatLêers.remove(gekoseIndeks);
            bankstaatLys.invalidate();
        });

        uitsetLêerKnoppie.addActionListener(e -> {
            JFileChooser kies = new JFileChooser(uitsetLêerVeld.getText());
            kies.setDialogTitle("Kies die uitset lêer (.csv)");
            kies.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Comma Separated Values", "csv");
            kies.addChoosableFileFilter(filter);
            // Open the file
            int res = kies.showOpenDialog(null);
            // Save the file
            // int res = kies.showSaveDialog(null);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = kies.getSelectedFile();
                uitsetLêerVeld.setText(file.getAbsolutePath());
                uitsetLêer = file;
                uitsetLêerVeld.invalidate();
            }
        });

        beginKnoppie.addActionListener(e -> {
            stelOpstellings();
        });

        setVisible(true);
    }

    private void stelOpstellings() {
        beginKnoppie.setEnabled(false);
        configManager.set("laaste.reëls", reëlsVeld.getText());
        StringBuilder sb = new StringBuilder();
        ListModel<String> bankstaatLysLM = bankstaatLys.getModel();
        Object[] elemente = ((DefaultListModel) bankstaatLysLM).toArray();
        Arrays.stream(elemente)
                .forEach(s -> sb.append(s).append("#"));
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        configManager.set("laaste.bankstate", sb.toString());
        configManager.set("laaste.uitset", uitsetLêerVeld.getText());
        ComboBoxModel<Integer> beginMaandDCBM = beginMaand.getModel();
        configManager.set("laaste.maand.begin", Integer.toString((Integer) beginMaandDCBM.getSelectedItem()));
        ComboBoxModel<Integer> eindMaandDCBM = eindMaand.getModel();
        configManager.set("laaste.maand.eindig", Integer.toString((Integer) eindMaandDCBM.getSelectedItem()));
        configManager.storeConfig();
        //Begin proses
        Component comp = reëlsPaneel.getParent();
        while (comp != null && !(comp instanceof Raam)) {
            comp = comp.getParent();
        }
        if (comp != null && (comp instanceof Raam)) {
            ((Raam) comp).begin();
        } else {
            beginKnoppie.setEnabled(true);
        }
    }

    public int getBeginMaand() {
        ComboBoxModel<Integer> beginMaandDCBM = beginMaand.getModel();
        return (Integer) beginMaandDCBM.getSelectedItem();
    }

    public int getEindMaand() {
        ComboBoxModel<Integer> eindMaandDCBM = eindMaand.getModel();
        return (Integer) eindMaandDCBM.getSelectedItem();
    }

    public File getReëlsLêer() {
        if (reëlsLêer == null) {
            reëlsLêer = new File(reëlsVeld.getText());
        }
        return reëlsLêer;
    }

    public List<File> getBankstaatLêers() {
        if (bankstaatLêers.isEmpty()) {
            ListModel<String> bankstaatLysDLM = bankstaatLys.getModel();
            for (int i = 0; i < bankstaatLysDLM.getSize(); i++) {
                bankstaatLêers.add(new File(bankstaatLysDLM.getElementAt(i)));
            }
        }
        return bankstaatLêers;
    }

    public File getUitsetLêer() {
        if (uitsetLêer == null) {
            uitsetLêer = new File(uitsetLêerVeld.getText());
        }
        return uitsetLêer;
    }

    public void setGekoseReëls(final String reëlsLêeradres) {
        reëlsVeld.setText(reëlsLêeradres);
    }

    public void setGekoseBankstate(final String bankstateLêeradresse) {
        ListModel<String> bankstaatLysDLM = bankstaatLys.getModel();
        ((DefaultListModel) bankstaatLysDLM).clear();
        Arrays.stream(bankstateLêeradresse.split("#"))
                .forEach(s -> {
                    ((DefaultListModel) bankstaatLysDLM).addElement(s);
                    bankstaatLêers.add(new File(s));
                });
    }

    public void setGekoseUitset(final String uitsetLêeradres) {
        uitsetLêerVeld.setText(uitsetLêeradres);
    }

    public void setMaandBegin(int maandBeginWaarde) {
        ComboBoxModel<Integer> beginMaandDCBM = beginMaand.getModel();
        beginMaandDCBM.setSelectedItem(maandBeginWaarde);
        if (maandBeginWaarde < 12) {
            ComboBoxModel<Integer> eindMaandDCBM = eindMaand.getModel();
            eindMaandDCBM.setSelectedItem(maandBeginWaarde + 1);
        }
    }

    public void setMaandEindig(int eindMaandWaarde) {
        ComboBoxModel<Integer> eindMaandDCBM = eindMaand.getModel();
        eindMaandDCBM.setSelectedItem(eindMaandWaarde);
        if (eindMaandWaarde > 1) {
            ComboBoxModel<Integer> beginMaandDCBM = beginMaand.getModel();
            beginMaandDCBM.setSelectedItem(eindMaandWaarde - 1);
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        reëlsPaneel = new JPanel();
        reëlsPaneel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(10, 3, new Insets(0, 0, 0, 0), -1, -1));
        reëlsPaneel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Insette", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("Reëls Lêer:");
        reëlsPaneel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        reëlsVeld = new JTextField();
        reëlsPaneel.add(reëlsVeld, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        reëlsKnoppie = new JButton();
        reëlsKnoppie.setText("...");
        reëlsPaneel.add(reëlsKnoppie, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Bankstate:");
        reëlsPaneel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        bankstaatLys = new JList();
        reëlsPaneel.add(bankstaatLys, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 50), null, 0, false));
        final JSeparator separator1 = new JSeparator();
        reëlsPaneel.add(separator1, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator2 = new JSeparator();
        reëlsPaneel.add(separator2, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        uitsetLêerVeld = new JTextField();
        reëlsPaneel.add(uitsetLêerVeld, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Uitset Lêer:");
        reëlsPaneel.add(label3, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        uitsetLêerKnoppie = new JButton();
        uitsetLêerKnoppie.setText("...");
        reëlsPaneel.add(uitsetLêerKnoppie, new com.intellij.uiDesigner.core.GridConstraints(4, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator3 = new JSeparator();
        reëlsPaneel.add(separator3, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        beginMaand = new JComboBox();
        reëlsPaneel.add(beginMaand, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Begin Maand:");
        reëlsPaneel.add(label4, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        eindMaand = new JComboBox();
        reëlsPaneel.add(eindMaand, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Eind Maand:");
        reëlsPaneel.add(label5, new com.intellij.uiDesigner.core.GridConstraints(7, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        beginKnoppie = new JButton();
        beginKnoppie.setText("Begin");
        reëlsPaneel.add(beginKnoppie, new com.intellij.uiDesigner.core.GridConstraints(8, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator4 = new JSeparator();
        reëlsPaneel.add(separator4, new com.intellij.uiDesigner.core.GridConstraints(9, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        reëlsPaneel.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        bankstaatKnoppie = new JButton();
        bankstaatKnoppie.setBackground(new Color(-16711936));
        bankstaatKnoppie.setText("+");
        panel1.add(bankstaatKnoppie, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bankstaatVerwyderKnoppie = new JButton();
        bankstaatVerwyderKnoppie.setBackground(new Color(-65536));
        bankstaatVerwyderKnoppie.setText("-");
        panel1.add(bankstaatVerwyderKnoppie, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return reëlsPaneel;
    }

}
