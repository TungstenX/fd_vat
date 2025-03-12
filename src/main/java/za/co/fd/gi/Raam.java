package za.co.fd.gi;

import za.co.fd.beheer.Beheerder;
import za.co.fd.config.ConfigManager;

import javax.swing.*;
import java.awt.BorderLayout;
import java.net.URL;

public class Raam  extends JFrame {

    private Beheerder beheerder;
    private InsetCGI insetCGI;
    private UitsetCGI uitsetCGI;

    public Raam(ConfigManager configManager) {
        URL iconURL = getClass().getResource("/logo.png");
        if (iconURL == null) {
            System.err.println("Konnie logo.png vind nie");
        } else {
            ImageIcon icon = new ImageIcon(iconURL);
            setIconImage(icon.getImage());
        }

        setTitle("FD BTW");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        insetCGI = new InsetCGI(configManager);
        uitsetCGI = new UitsetCGI();


        insetCGI.setGekoseReëls(configManager.get("laaste.reëls", "/home/andre/Projects/Java/FD_Vat/"));
        insetCGI.setGekoseBankstate(configManager.get("laaste.bankstate", "/home/andre/Projects/Java/FD_Vat/"));
        insetCGI.setGekoseUitset(configManager.get("laaste.uitset", "/home/andre/Projects/Java/FD_Vat/"));
        insetCGI.setMaandBegin(Integer.parseInt(configManager.get("laaste.maand.begin", "1")));
        insetCGI.setMaandEindig(Integer.parseInt(configManager.get("laaste.maand.eindig", "2")));


        add(insetCGI.getReëlsPaneel(), BorderLayout.WEST);
        add(uitsetCGI.getUitsetPaneel(), BorderLayout.CENTER);
        pack();
    }

    public void begin() {
        beheerder = new Beheerder(uitsetCGI, insetCGI.getReëlsLêer(), insetCGI.getBankstaatLêers(), insetCGI.getUitsetLêer(), insetCGI.getBeginMaand(), insetCGI.getEindMaand());
        beheerder.start();
    }
}
