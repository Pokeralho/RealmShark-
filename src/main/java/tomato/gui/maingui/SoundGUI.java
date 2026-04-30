package tomato.gui.maingui;

import tomato.realmshark.Sound;
import util.PropertiesManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class SoundGUI extends JFrame {

    public SoundGUI() {
        setTitle("Sound Customization");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        addSoundRow(panel, "PM Chat", Sound.pm, "pmSoundPath");
        addSoundRow(panel, "Guild Chat", Sound.guild, "guildSoundPath");
        addSoundRow(panel, "Party Chat", Sound.party, "partySoundPath");
        addSoundRow(panel, "White Bag", Sound.whitebag, "whiteBagSoundPath");
        addSoundRow(panel, "Orange Bag", Sound.orangebag, "orangeBagSoundPath");
        addSoundRow(panel, "Red Bag", Sound.redbag, "redBagSoundPath");
        addSoundRow(panel, "Gold Bag", Sound.goldbag, "goldBagSoundPath");
        addSoundRow(panel, "Egg Bag", Sound.eggbag, "eggBagSoundPath");
        addSoundRow(panel, "Blue Bag", Sound.bluebag, "blueBagSoundPath");
        addSoundRow(panel, "Trade", Sound.trade, "tradeSoundPath");
        addSoundRow(panel, "Enchantments", Sound.enchantment, "enchantmentSoundPath");
        addSoundRow(panel, "Item Drop Pings", Sound.item, "itemSoundPath");
        addSoundRow(panel, "Entity ID Pings", Sound.entity, "entitySoundPath");
        addSoundRow(panel, "Dungeon Drop Pings", Sound.dungeonDrop, "dungeonDropSoundPath");
        addSoundRow(panel, "Dungeon Modifier Pings", Sound.dungeonModifier, "dungeonModifierSoundPath");
        addSoundRow(panel, "Custom", Sound.custom, "customSoundPath");

        JScrollPane scrollPane = new JScrollPane(panel);
        add(scrollPane);
    }

    private void addSoundRow(JPanel panel, String label, Sound sound, String propertyKey) {
        panel.add(new JLabel(label + ":"));
        JButton btn = new JButton("Select File");
        btn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(sound.getPath()));
            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                sound.reload(path);
                PropertiesManager.setProperties(propertyKey, path);
                sound.play();
            }
        });
        panel.add(btn);
    }

    public void open() {
        setVisible(true);
    }
}
