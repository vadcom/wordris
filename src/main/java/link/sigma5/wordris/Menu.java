package link.sigma5.wordris;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import link.sigma5.dreamscore.client.Score;
import link.sigma5.dreamscore.client.ScoreClient;
import link.sigma5.menu.*;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Menu implements MenuListener {
    public static final String APPLICATION_ID = "wordrix";
    private final DumbMenu menu = new DumbMenu("menu.yaml", this);
    Screen screen;

    Lantrix.Lang lang;
    Lantrix.Lang originLang;

    int minLetters;
    Lantrix.BlockSet blockSet;
    String userName;

    Preferences prefs = Preferences.userNodeForPackage(Menu.class);
    void getPrefs() {
        userName = prefs.get("userName", "John Doe");
    }

    public Lantrix.Lang getLang() {
        return lang;
    }

    public Menu(Lantrix.Lang lang, int minLetters, Lantrix.BlockSet blockSet) throws IOException {
        this.lang = lang;
        this.minLetters = minLetters;
        this.blockSet = blockSet;
        menu.init();
        getPrefs();
        originLang = getLang();
    }

    boolean exit = false;

    public void start(Screen screen) throws IOException, BackingStoreException {
        this.screen = screen;
        screen.startScreen();
        while (!exit) {
            try {
                showMenu();
                processInput();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        screen.stopScreen();
        prefs.flush();
    }

    private void processInput() throws IOException {
        KeyStroke keyStroke = screen.readInput();
        switch (keyStroke.getKeyType()) {
            case ArrowUp -> menu.performAction(MenuAction.Previous);
            case ArrowDown -> menu.performAction(MenuAction.Next);
            case ArrowLeft -> menu.performAction(MenuAction.ScrollPrevious);
            case ArrowRight -> menu.performAction(MenuAction.ScrollNext);
            case Enter -> menu.performAction(MenuAction.Enter);
            case Escape -> menu.performAction(MenuAction.Back);
        }
    }

    private void showMenu() throws IOException {
        screen.clear();
//        showBackground();
        hideCursor();
        String caption = menu.getCurrentLevel().getText();
        int cy = 5;
        int cx = (screen.getTerminalSize().getColumns() - caption.length()) / 2;
        drawString(caption, cx, cy, TextColor.ANSI.BLUE_BRIGHT, TextColor.ANSI.BLACK, SGR.BOLD);
        menu.drawMenu(level -> {
            int y = 8;
            int x = cx - 4;
            for (MenuItemView menuItemView : level.getItemsText()) {
                String text = menuItemView.value().isEmpty() ? menuItemView.text() : menuItemView.text() + " : " + menuItemView.value();
                drawString(text, x, y, menuItemView.active() ? TextColor.ANSI.MAGENTA : TextColor.ANSI.BLUE, TextColor.ANSI.BLACK, SGR.BOLD);
                y++;
            }
        });
        drawString("Player: " + userName, cx - 2, 18, TextColor.ANSI.GREEN, TextColor.ANSI.BLACK, SGR.BOLD);
        if (haveToRestart()) {
            drawCenterXString("You have to restart the game to apply Russian font", 20, TextColor.ANSI.RED, TextColor.ANSI.BLACK, SGR.BOLD);
        }
        screen.refresh();
    }

    private boolean haveToRestart() {
        return originLang != lang && lang == Lantrix.Lang.RUS;
    }

    Random random = new Random();
    private void showBackground() {
        int columns = screen.getTerminalSize().getColumns();
        int rows = screen.getTerminalSize().getRows();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j <columns; j++) {
                if (random.nextInt(100) > 90) {
                    screen.setCharacter(j, i, new TextCharacter(' ' , TextColor.ANSI.WHITE, TextColor.ANSI.BLACK));
//                    screen.setCharacter(j+1, i, new TextCharacter(' ' , TextColor.ANSI.WHITE, TextColor.ANSI.BLACK));
                    continue;
                }
                screen.setCharacter(j, i, new TextCharacter(random.nextBoolean()?'\\':'/', TextColor.ANSI.BLUE, TextColor.ANSI.BLACK));
/*
                char a;
                char b;
                if (random.nextBoolean()) {
                    a='\u14A7';
                    b='\u14A5';
                } else {
                    a='\u14A3';
                    b='\u14AA';
                }
                screen.setCharacter(j, i, new TextCharacter(a, TextColor.ANSI.BLUE, TextColor.ANSI.BLACK));
                screen.setCharacter(j, i+1, new TextCharacter(b, TextColor.ANSI.BLUE, TextColor.ANSI.BLACK));
*/
            }
        }
    }

    private void hideCursor() {
        drawString(" ", 1, 1, TextColor.ANSI.BLACK, TextColor.ANSI.BLACK);
        screen.setCursorPosition(new TerminalPosition(1, 1));
    }

    private void drawString(String text, int x, int y, TextColor foregroundColor, TextColor backgroundColor, SGR... modifiers) {
        for (TextCharacter textCharacter : TextCharacter.fromString(text, foregroundColor, backgroundColor, modifiers)) {
            screen.setCharacter(x++, y, textCharacter);
        }
    }

    private void drawCenterXString(String text, int y, TextColor foregroundColor, TextColor backgroundColor, SGR... modifiers) {
        int x = (screen.getTerminalSize().getColumns()-text.length()) / 2;
        for (TextCharacter textCharacter : TextCharacter.fromString(text, foregroundColor, backgroundColor, modifiers)) {
            screen.setCharacter(x++, y, textCharacter);
        }
    }

    @Override
    public void onEvent(MenuEvent menuEvent) {
        System.out.println(menuEvent);
        switch (menuEvent.event()) {
            case "start" -> {
                if (haveToRestart()) {
                    return;
                }
                try {
                    Long score = new Lantrix(screen, new LangService(lang), minLetters, blockSet).process();
                    if (score != 0) {
                        var scoreClient = new ScoreClient(APPLICATION_ID, getSectionId());
                        List<Score> scores = scoreClient.pushScore(userName, score);
                        showScore(scores);
                    }
                } catch (IOException | URISyntaxException | InterruptedException | FontFormatException e) {
                    e.printStackTrace();
                    showDisabledScore();
                }
            }
            case "exit" -> exit = true;
            case "language" -> lang = switch (menuEvent.param()) {
                case "ENG" -> Lantrix.Lang.ENG;
                case "RUS" -> Lantrix.Lang.RUS;
                default -> Lantrix.Lang.ENG;
            };
            case "minLetters" -> minLetters = Integer.parseInt(menuEvent.param());
            case "blockSet" -> {
                blockSet = switch (menuEvent.param()) {
                    case "BASE" -> Lantrix.BlockSet.BASE;
                    case "EXT" -> Lantrix.BlockSet.EXT;
                    default -> Lantrix.BlockSet.BASE;
                };
            }
            case "scores" -> {
                try {
                    var scoreClient = new ScoreClient("wordrix", getSectionId());
                    List<Score> scores = scoreClient.pullScore(0, 10);
                    showScore(scores);
                } catch (IOException | InterruptedException e) {
                    showDisabledScore();
                }
            }
            case "changeName" -> {
                try {
                    userName = getPlayerName();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                prefs.put("userName", userName);
            }
        }
    }

    private String getPlayerName() throws IOException {
        screen.clear();
        String caption = "-=* ENTER YOUR NAME *=-";
        int columns = screen.getTerminalSize().getColumns();
        drawString(caption, (columns - caption.length()) / 2, 4, TextColor.ANSI.RED_BRIGHT, TextColor.ANSI.BLACK, SGR.BOLD);
        int line = 6;
        int x = (columns - 40) / 2;
        StringBuilder name = new StringBuilder();
        String prompt = "New player name: ";
        while (true) {
            drawString(prompt + name + " ".repeat(50), x, line, TextColor.ANSI.GREEN, TextColor.ANSI.BLACK, SGR.BOLD);
            screen.setCursorPosition(new TerminalPosition(x + prompt.length() + name.length(), line));
            screen.refresh();
            KeyStroke keyStroke = screen.readInput();
            switch (keyStroke.getKeyType()) {
                case Escape -> {
                    return userName;
                }
                case Enter -> {
                    return name.toString();
                }
                case Backspace -> {
                    if (!name.isEmpty()) {
                        name = new StringBuilder(name.substring(0, name.length() - 1));
                    }
                }
                default -> name.append(keyStroke.getCharacter());
            }
        }
    }

    private String getSectionId() {
        return lang.toString() + "_" + minLetters + "_" + blockSet.toString();
    }

    private void showScore(List<Score> scores) throws IOException {
        screen.clear();
        hideCursor();
        String caption = "-=* TOP SCORE *=-";
        int columns = screen.getTerminalSize().getColumns();
        drawString(caption, (columns - caption.length()) / 2, 4, TextColor.ANSI.RED_BRIGHT, TextColor.ANSI.BLACK, SGR.BOLD);
        int line = 6;
        int x = (columns - 40) / 2;
        for (Score score : scores) {
            String name = score.getName().trim();
            String text = score.getPosition() + "." + " ".repeat(3 - score.getPosition().toString().length())
                    + name + ".".repeat(30 - (name.length() + score.getScore().toString().length()))
                    + score.getScore();
            boolean selected = score.getSelected() != null ? score.getSelected() : false;
            drawString(text, x, line++, TextColor.ANSI.GREEN, selected ? TextColor.ANSI.BLUE : TextColor.ANSI.BLACK, SGR.BOLD);
        }
        screen.refresh();
        screen.readInput();
    }

    private void showDisabledScore(){
        screen.clear();
        hideCursor();
        String caption = "-=* TOP SCORE UNAVAILABLE *=-";
        int columns = screen.getTerminalSize().getColumns();
        drawString(caption, (columns - caption.length()) / 2, 4, TextColor.ANSI.RED_BRIGHT, TextColor.ANSI.BLACK, SGR.BOLD);
        try {
            screen.refresh();
            screen.readInput();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
