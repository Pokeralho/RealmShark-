package tomato.gui.security;

import tomato.gui.TomatoGUI;

import tomato.backend.data.TomatoData;

import javax.swing.*;
import java.awt.*;

public class SecurityGUI extends JPanel {

    private static SecurityGUI INSTANCE;
    private TomatoData data;

    private JTextArea text;

    public SecurityGUI(TomatoData data) {
        INSTANCE = this;
        this.data = data;
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        ParsePanelGUI parsePanel = new ParsePanelGUI();

        JPanel abilityUse = new JPanel();
        tabbedPane.addTab("Parse", parsePanel);
        tabbedPane.addTab("Ability Use", abilityUse);
        
        JPanel alertsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton dungeonDropsBtn = new JButton("Dungeon Drop Alerts");
        dungeonDropsBtn.addActionListener(e -> new DungeonDropPingGUI(data).open());

        JButton dungeonPingsBtn = new JButton("Dungeon Modifier Alerts");
        dungeonPingsBtn.addActionListener(e -> new DungeonModPingGUI(data).open());
        
        JButton entityPingsBtn = new JButton("Entity ID Alerts");
        entityPingsBtn.addActionListener(e -> new EntityPingGUI(data).open());
        
        alertsPanel.add(dungeonDropsBtn);
        alertsPanel.add(dungeonPingsBtn);
        alertsPanel.add(entityPingsBtn);
        
        tabbedPane.addTab("Alerts", alertsPanel);
        
        add(tabbedPane);

        abilityUse.setLayout(new BorderLayout());
        text = new JTextArea();
        JButton button = new JButton("Clear");
        button.addActionListener(e -> INSTANCE.text.setText(""));
        abilityUse.add(TomatoGUI.createTextArea(text, true), BorderLayout.CENTER);
        abilityUse.add(button, BorderLayout.SOUTH);
    }

    public static void updateAbilityUsage(String s) {
        INSTANCE.appendText(s);
    }

    private void appendText(String s) {
        text.append(s);
        text.append("\n");
    }
}
