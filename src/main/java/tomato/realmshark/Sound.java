package tomato.realmshark;

import tomato.gui.keypop.KeypopGUI;
import util.PropertiesManager;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Sound {

    private static float volume = 0;

    private Clip soundClip;

    public static Sound party;
    public static Sound guild;
    public static Sound pm;
    public static Sound keypop;
    public static Sound whitebag;
    public static Sound orangebag;
    public static Sound redbag;
    public static Sound goldbag;
    public static Sound eggbag;
    public static Sound bluebag;
    public static Sound custom;
    public static Sound trade;
    public static Sound enchantment;
    public static Sound item;
    public static Sound entity;
    public static Sound dungeonDrop;
    public static Sound dungeonModifier;
    public static Sound dungeon;

    public static boolean playPmSound = false;
    public static boolean playPartySound = false;
    public static boolean playGuildSound = false;
    public static boolean playWhiteBagSound = true;
    public static boolean playOrangeBagSound = false;
    public static boolean playRedBagSound = false;
    public static boolean playGoldBagSound = false;
    public static boolean playEggBagSound = false;
    public static boolean playBlueBagSound = false;
    public static boolean playTradeSound = false;

    private String currentPath;

    public Sound(String defaultPath, String propertyKey) {
        this.currentPath = PropertiesManager.getProperty(propertyKey);
        if (this.currentPath == null || !new File(this.currentPath).exists()) {
            this.currentPath = defaultPath;
        }
        soundClip = loadSound(this.currentPath);
    }

    public void reload(String newPath) {
        if (soundClip != null && soundClip.isRunning()) {
            soundClip.stop();
        }
        Clip newClip = loadSound(newPath);
        if (newClip != null) {
            soundClip = newClip;
            currentPath = newPath;
        }
    }

    public String getPath() {
        return currentPath;
    }

    /**
     * Loads auto clip to be played later
     */
    static {
        party = new Sound("sound/party.wav", "partySoundPath");
        guild = new Sound("sound/guild.wav", "guildSoundPath");
        pm = new Sound("sound/pm.wav", "pmSoundPath");
        keypop = new Sound("sound/keypop.wav", "keypopSoundPath");
        whitebag = new Sound("sound/whitebag.wav", "whiteBagSoundPath");
        orangebag = new Sound("sound/orangebag.wav", "orangeBagSoundPath");
        redbag = new Sound("sound/redbag.wav", "redBagSoundPath");
        goldbag = new Sound("sound/goldbag.wav", "goldBagSoundPath");
        eggbag = new Sound("sound/eggbag.wav", "eggBagSoundPath");
        bluebag = new Sound("sound/bluebag.wav", "blueBagSoundPath");
        trade = new Sound("sound/trade.wav", "tradeSoundPath");
        enchantment = new Sound("sound/custom.wav", "enchantmentSoundPath");
        item = new Sound("sound/custom.wav", "itemSoundPath");
        entity = new Sound("sound/custom.wav", "entitySoundPath");
        dungeonDrop = new Sound("sound/custom.wav", "dungeonDropSoundPath");
        dungeonModifier = new Sound(
            "sound/custom.wav",
            "dungeonModifierSoundPath"
        );
        dungeon = dungeonModifier;
        custom = new Sound("sound/custom.wav", "customSoundPath");
    }

    private static Clip loadSound(String file) {
        try {
            File f = new File(file);
            InputStream audioInputStream;
            if (f.exists()) {
                audioInputStream = Files.newInputStream(Paths.get(file));
            } else {
                audioInputStream = KeypopGUI.class.getResourceAsStream("/" + file);
            }

            if (audioInputStream == null) {
                System.err.println("Error: Could not load audio file.");
            } else {
                InputStream bufferedIn = new BufferedInputStream(audioInputStream);
                AudioInputStream stream = AudioSystem.getAudioInputStream(bufferedIn);

                AudioFormat baseFormat = stream.getFormat();
                AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        44100,  // Sample rate (Hz)
                        16,     // Bit depth
                        1,      // Channels (1 for mono, 2 for stereo)
                        2,      // Frame size in bytes
                        44100,  // Frame rate (frames per second)
                        false   // Big-endian byte order
                );

                if (!AudioSystem.isConversionSupported(decodedFormat, baseFormat)) {
                    System.err.println("Error: Conversion not supported.");
                } else {
                    AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, stream);
                    Clip clip = AudioSystem.getClip();
                    clip.open(decodedStream);
                    return clip;
                }
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void setVolume(Clip clip) {
        FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        volumeControl.setValue(volume);
    }

    /**
     * Sets the volume, values between 0 to 100.
     *
     * @param v Setter for volume from 0 no sound to 100 max volume.
     */
    public static void setVolume(int v) {
        if (v < 0 || v > 100) return;

        volume = 20.0f * (float) Math.log10(v / 100.0);
    }

    /**
     * Plays loaded sound
     */
    public void play() {
        if (soundClip != null) {
            setVolume(soundClip);
            soundClip.setFramePosition(0); // Rewind to the beginning
            soundClip.start();
        }
    }

    /**
     * Stops sound being played.
     */
    public void stop() {
        if (soundClip != null && soundClip.isRunning()) {
            soundClip.stop();
        }
    }
}
