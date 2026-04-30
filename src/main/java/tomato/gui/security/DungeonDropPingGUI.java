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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import tomato.backend.data.TomatoData;
import tomato.gui.TomatoGUI;
import tomato.realmshark.ParseDungeon;

public class DungeonDropPingGUI extends JPanel {

    private final TomatoData data;
    private final JTextField search = new JTextField(24);
    private final JPanel listPanel = new JPanel();
    private final TreeMap<String, Integer> portalIds;

    public DungeonDropPingGUI(TomatoData data) {
        super(new BorderLayout(8, 8));
        this.data = data;
        this.portalIds = loadPortalIds();

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
            "Dungeon Drop Alerts",
            true
        );
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(this);
        dialog.setSize(520, 680);
        dialog.setLocationRelativeTo(TomatoGUI.getFrame());
        dialog.setVisible(true);
    }

    private TreeMap<String, Integer> loadPortalIds() {
        try {
            return ParseDungeon.getPortalIdsByName();
        } catch (Throwable e) {
            return new TreeMap<>();
        }
    }

    private void buildList() {
        listPanel.removeAll();
        Set<String> selected = new HashSet<>(data.getEntityIdPings());

        if (portalIds.isEmpty()) {
            listPanel.add(new JLabel("Dungeon data is unavailable."));
            return;
        }

        for (Map.Entry<String, Integer> entry : portalIds.entrySet()) {
            String id = String.valueOf(entry.getValue());
            JCheckBox checkBox = new JCheckBox(
                entry.getKey() + " (" + id + ")"
            );
            checkBox.putClientProperty("portalId", id);
            checkBox.putClientProperty("searchText", entry.getKey().toLowerCase());
            checkBox.setSelected(selected.contains(id));
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
                String id = String.valueOf(checkBox.getClientProperty("portalId"));
                checkBox.setVisible(
                    query.isEmpty() ||
                    searchText.contains(query) ||
                    id.contains(query)
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
        Set<String> portalIdSet = new HashSet<>();
        for (Integer id : portalIds.values()) {
            portalIdSet.add(String.valueOf(id));
        }

        ArrayList<String> values = new ArrayList<>();
        for (String existing : data.getEntityIdPings()) {
            if (!portalIdSet.contains(existing)) {
                values.add(existing);
            }
        }

        for (Component component : listPanel.getComponents()) {
            if (component instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) component;
                if (checkBox.isSelected()) {
                    String id = String.valueOf(
                        checkBox.getClientProperty("portalId")
                    );
                    if (!values.contains(id)) {
                        values.add(id);
                    }
                }
            }
        }

        data.savePropList(values, "entityIdPings");
        WindowCloser.close(this);
    }

    private static class WindowCloser {
        static void close(Component component) {
            java.awt.Window window = SwingUtilities.getWindowAncestor(component);
            if (window != null) {
                window.dispose();
            }
        }
    }
}
