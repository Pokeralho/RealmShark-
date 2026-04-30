package tomato.gui.security;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import tomato.backend.data.TomatoData;
import tomato.gui.TomatoGUI;
import tomato.realmshark.ParseDungeon;

public class DungeonModPingGUI extends JPanel {

    private final TomatoData data;
    private final JTextField search = new JTextField(24);
    private final JPanel listPanel = new JPanel();
    private final TreeMap<String, Integer> modifierIds;

    public DungeonModPingGUI(TomatoData data) {
        super(new BorderLayout(8, 8));
        this.data = data;
        this.modifierIds = loadModifierIds();

        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new BorderLayout(6, 0));
        top.add(new JLabel("Search:"), BorderLayout.WEST);
        top.add(search, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(listPanel), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton selectAll = new JButton("Select All");
        JButton clearAll = new JButton("Clear All");
        JButton save = new JButton("Save");
        bottom.add(selectAll);
        bottom.add(clearAll);
        bottom.add(save);
        add(bottom, BorderLayout.SOUTH);

        buildList();

        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });

        selectAll.addActionListener(e -> setAll(true));
        clearAll.addActionListener(e -> setAll(false));
        save.addActionListener(e -> save());
    }

    public void open() {
        JDialog dialog = new JDialog(
            TomatoGUI.getFrame(),
            "Dungeon Modifier Alerts",
            true
        );
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(this);
        dialog.setSize(520, 680);
        dialog.setLocationRelativeTo(TomatoGUI.getFrame());
        dialog.setVisible(true);
    }

    private TreeMap<String, Integer> loadModifierIds() {
        try {
            return ParseDungeon.getModifierIdsByName();
        } catch (Throwable e) {
            return new TreeMap<>();
        }
    }

    private void buildList() {
        listPanel.removeAll();
        Set<String> selected = new HashSet<>(data.getDungeonModPings());

        if (modifierIds.isEmpty()) {
            listPanel.add(new JLabel("Dungeon modifier data is unavailable."));
            return;
        }

        for (Map.Entry<String, Integer> entry : modifierIds.entrySet()) {
            String name = entry.getKey();
            JCheckBox checkBox = new JCheckBox(
                name + " (" + entry.getValue() + ")"
            );
            checkBox.putClientProperty("modifierName", name);
            checkBox.putClientProperty("searchText", name.toLowerCase());
            checkBox.setSelected(selected.contains(name));
            checkBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            listPanel.add(checkBox);
        }
    }

    private void filter() {
        String query = search.getText();
        query = query == null ? "" : query.trim().toLowerCase();
        for (Component component : listPanel.getComponents()) {
            if (component instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) component;
                String searchText = String.valueOf(
                    checkBox.getClientProperty("searchText")
                );
                String name = String.valueOf(
                    checkBox.getClientProperty("modifierName")
                );
                checkBox.setVisible(
                    query.isEmpty() ||
                    searchText.contains(query) ||
                    name.contains(query)
                );
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void setAll(boolean selected) {
        for (Component component : listPanel.getComponents()) {
            if (component instanceof JCheckBox && component.isVisible()) {
                ((JCheckBox) component).setSelected(selected);
            }
        }
    }

    private void save() {
        ArrayList<String> values = new ArrayList<>();
        for (Component component : listPanel.getComponents()) {
            if (component instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) component;
                if (checkBox.isSelected()) {
                    values.add(
                        String.valueOf(checkBox.getClientProperty("modifierName"))
                    );
                }
            }
        }
        data.savePropList(values, "dungeonModPings");
        java.awt.Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }
}
