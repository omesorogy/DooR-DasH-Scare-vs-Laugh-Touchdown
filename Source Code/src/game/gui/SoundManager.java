package game.gui;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Central sound manager for DooR DasH.
 *
 * Music  : Theme.mp3  – looped background track
 * SFX    : click.mp3, boing.mp3, error.mp3, win.mp3, lose.mp3
 *
 * All SFX are pre-loaded once at startup so playback is instant with no delay.
 * Both music and SFX can be independently muted via toggle.
 */
public class SoundManager {

    // -----------------------------------------------------------------------
    //  Singleton
    // -----------------------------------------------------------------------

    private static final SoundManager INSTANCE = new SoundManager();
    public  static SoundManager get() { return INSTANCE; }

    // -----------------------------------------------------------------------
    //  SFX clip names
    // -----------------------------------------------------------------------

    private static final String[] SFX_FILES = {
        "click.mp3", "boing.mp3", "error.mp3", "win.mp3", "lose.mp3"
    };

    // -----------------------------------------------------------------------
    //  State
    // -----------------------------------------------------------------------

    private MediaPlayer musicPlayer;
    private MediaPlayer winScreenPlayer;
    private boolean musicMuted = false;
    private boolean sfxMuted   = false;

    /** Pre-loaded Media objects – one per SFX file. */
    private final Map<String, Media> sfxCache = new HashMap<>();

    private SoundManager() {}

    // -----------------------------------------------------------------------
    //  Pre-loading
    // -----------------------------------------------------------------------

    /**
     * Call once at application start (after the JavaFX toolkit is up).
     * Loads every SFX file into a Media object so the first play is instant.
     */
    public void preloadSfx() {
        for (String filename : SFX_FILES) {
            try {
                URL url = getClass().getResource("/game/gui/resources/audio/" + filename);
                if (url != null) sfxCache.put(filename, new Media(url.toExternalForm()));
            } catch (Exception ignored) {}
        }
    }

    // -----------------------------------------------------------------------
    //  Music
    // -----------------------------------------------------------------------

    /** Start looping theme music. Safe to call multiple times. */
    public void startMusic() {
        if (musicPlayer != null) return;
        try {
            URL url = getClass().getResource("/game/gui/resources/audio/Theme.mp3");
            if (url == null) return;
            Media media = new Media(url.toExternalForm());
            musicPlayer = new MediaPlayer(media);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.setVolume(musicMuted ? 0.0 : 0.6);
            musicPlayer.play();
        } catch (Exception ignored) {}
    }

    /** Permanently stop and dispose music (called on exit). */
    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
            musicPlayer = null;
        }
    }

    /** Pause/stop the theme while the win screen is visible. */
    public void pauseMusicForWinScreen() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }

    /** Play the win-screen effect as the only audio on the end screen. */
    public void playWinScreenEffect() {
        playEndScreenEffect("win.mp3");
    }

    /** Play the lose-screen effect as the only audio on the end screen. */
    public void playLoseScreenEffect() {
        playEndScreenEffect("lose.mp3");
    }

    private void playEndScreenEffect(String fileName) {
        if (sfxMuted) return;
        stopWinScreenEffect();
        try {
            Media media = sfxCache.get(fileName);
            if (media == null) {
                URL url = getClass().getResource("/game/gui/resources/audio/" + fileName);
                if (url == null) return;
                media = new Media(url.toExternalForm());
                sfxCache.put(fileName, media);
            }
            winScreenPlayer = new MediaPlayer(media);
            winScreenPlayer.setVolume(0.85);
            winScreenPlayer.setOnEndOfMedia(() -> {
                if (winScreenPlayer != null) {
                    winScreenPlayer.stop();
                    winScreenPlayer.dispose();
                    winScreenPlayer = null;
                }
            });
            winScreenPlayer.play();
        } catch (Exception ignored) {}
    }

    /** Stop the win-screen effect without affecting normal SFX. */
    public void stopWinScreenEffect() {
        if (winScreenPlayer != null) {
            winScreenPlayer.stop();
            winScreenPlayer.dispose();
            winScreenPlayer = null;
        }
    }

    /** Called when leaving the win screen through Play Again. */
    public void leaveWinScreenAndResumeTheme() {
        stopWinScreenEffect();
        if (musicPlayer == null) {
            startMusic();
        } else {
            musicPlayer.setVolume(musicMuted ? 0.0 : 0.6);
            musicPlayer.play();
        }
    }

    /** Toggle music mute. Returns new muted state. */
    public boolean toggleMusicMute() {
        musicMuted = !musicMuted;
        if (musicPlayer != null) musicPlayer.setVolume(musicMuted ? 0.0 : 0.6);
        return musicMuted;
    }

    public boolean isMusicMuted() { return musicMuted; }

    // -----------------------------------------------------------------------
    //  SFX
    // -----------------------------------------------------------------------

    /** Toggle SFX mute. Returns new muted state. */
    public boolean toggleSfxMute() {
        sfxMuted = !sfxMuted;
        return sfxMuted;
    }

    public boolean isSfxMuted() { return sfxMuted; }

    /**
     * Play a pre-cached SFX clip with no startup delay.
     * Each call gets a fresh MediaPlayer so overlapping sounds work fine.
     */
    private void playSfx(String filename) {
        if (sfxMuted) return;
        try {
            Media media = sfxCache.get(filename);
            if (media == null) {
                // Fallback: load on demand if preload was skipped
                URL url = getClass().getResource("/game/gui/resources/audio/" + filename);
                if (url == null) return;
                media = new Media(url.toExternalForm());
                sfxCache.put(filename, media);
            }
            MediaPlayer player = new MediaPlayer(media);
            player.setVolume(0.85);
            player.setOnEndOfMedia(() -> { player.stop(); player.dispose(); });
            player.play();
        } catch (Exception ignored) {}
    }

    // -----------------------------------------------------------------------
    //  Named SFX helpers
    // -----------------------------------------------------------------------

    public void playClick()   { playSfx("click.mp3");  }
    public void playBoing()   { playSfx("boing.mp3");  }
    public void playError()   { playSfx("error.mp3");  }
    public void playWin()     { playSfx("win.mp3");    }
    public void playLose()    { playSfx("lose.mp3");   }
}
