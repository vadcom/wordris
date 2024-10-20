package link.sigma5.wordris;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.prefs.BackingStoreException;

/**
 * Created by vadcom on 9/19/15.
 */
public class Wordrix {

    Lantrix.Lang lang;
    int minLetters;
    Lantrix.BlockSet blockSet;

    public Wordrix(Lantrix.Lang lang, int minLetters, Lantrix.BlockSet blockSet) {
        this.lang = lang;
        this.minLetters = minLetters;
        this.blockSet = blockSet;
    }
    private static final String latinFontName = "SpaceMono-Bold.ttf";
//    private static final String latinFontName = "telegrama_raw.otf";
    private static final String rusFontName = "DejaVuSansMono.ttf";
    private String getFontFileName(Lantrix.Lang lang) {
        return switch (lang) {
            case RUS -> rusFontName;
            default -> latinFontName;
        };
    }

    private Screen createScreen(Lantrix.Lang lang) throws FontFormatException, IOException, URISyntaxException {
        String fontName = getFontFileName(lang);
        var fontBase = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(fontName)));
        Font font = fontBase.deriveFont(16.0f);
        Terminal terminal = new DefaultTerminalFactory()
                .setInitialTerminalSize(new TerminalSize(80, 25))
                .setTerminalEmulatorTitle("Wordrix")
                .setTerminalEmulatorFontConfiguration(new SwingTerminalFontConfiguration(true, AWTTerminalFontConfiguration.BoldMode.EVERYTHING, font))
                .createTerminal();
        terminal.setCursorVisible(false);
        return new TerminalScreen(terminal);
    }

    public void start() throws IOException, URISyntaxException, FontFormatException, BackingStoreException {
        Menu menu = new Menu(lang, minLetters, blockSet);
        var screen = createScreen(menu.getLang());
        menu.start(screen);
    }



    public static void main(String[] args) throws IOException, URISyntaxException, FontFormatException, BackingStoreException {

        Lantrix.Lang lang = Lantrix.Lang.ENG;
        if (args.length > 0) {
            lang = switch (args[0].toUpperCase()) {
                case "RUS" -> Lantrix.Lang.RUS;
                case "BLOCK" -> Lantrix.Lang.BLOCK;
                default -> Lantrix.Lang.ENG;
            };
        }
        int minLetters = 3;
        if (args.length > 1) {
            minLetters = Integer.parseInt(args[1]);
        }
        Lantrix.BlockSet bs = Lantrix.BlockSet.BASE;
        if (args.length > 2) {
            bs = args[2].equalsIgnoreCase("EXT") ? Lantrix.BlockSet.EXT : Lantrix.BlockSet.BASE;
        }
        new Wordrix(lang, minLetters, bs).start();
    }
}
