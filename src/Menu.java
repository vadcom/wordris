import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import link.sigma5.dreamscore.client.Score;
import link.sigma5.dreamscore.client.ScoreClient;
import link.sigma5.menu.DumbMenu;
import link.sigma5.menu.MenuEvent;
import link.sigma5.menu.MenuItemView;
import link.sigma5.menu.MenuListener;
import link.sigma5.menu.MenuAction;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class Menu implements MenuListener {
    private final DumbMenu menu = new DumbMenu("menu.yaml", this);
    Screen screen;
    Lantrix.Lang lang;
    int minLetters;
    Lantrix.BlockSet blockSet;


    public Menu(Screen screen, Lantrix.Lang lang, int minLetters, Lantrix.BlockSet blockSet) throws IOException {
        this.screen = screen;
        this.lang = lang;
        this.minLetters = minLetters;
        this.blockSet = blockSet;
    }

    boolean exit = false;
    public void start() throws IOException {
        menu.init();
        screen.startScreen();
        while (!exit) {
            try {
                screen.clear();
                String caption = menu.getCurrentLevel().getText();
                int cy = 5;
                int cx = (screen.getTerminalSize().getColumns() - caption.length()) / 2;
                drawString(caption, cx, cy, TextColor.ANSI.BLUE_BRIGHT, TextColor.ANSI.BLACK, SGR.BOLD);
                menu.drawMenu(level -> {
                    int y = 8;
                    int x = cx-4;
                    for (MenuItemView menuItemView : level.getItemsText()) {
                        String text = menuItemView.value().isEmpty() ? menuItemView.text() : menuItemView.text() + " : " + menuItemView.value();
                        drawString(text, x, y, menuItemView.active() ? TextColor.ANSI.MAGENTA : TextColor.ANSI.BLUE, TextColor.ANSI.BLACK, SGR.BOLD);
                        y ++;
                    }
                });
                screen.refresh();
                KeyStroke keyStroke = screen.readInput();
                switch (keyStroke.getKeyType()) {
                    case ArrowUp -> menu.performAction(MenuAction.Previous);
                    case ArrowDown -> menu.performAction(MenuAction.Next);
                    case ArrowLeft -> menu.performAction(MenuAction.ScrollPrevious);
                    case ArrowRight -> menu.performAction(MenuAction.ScrollNext);
                    case Enter -> menu.performAction(MenuAction.Enter);
                    case Escape -> menu.performAction(MenuAction.Back);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        screen.stopScreen();
    }

    private void drawString(String text, int x, int y, TextColor foregroundColor, TextColor backgroundColor, SGR... modifiers) {
        for (TextCharacter textCharacter : TextCharacter.fromString(text, foregroundColor, backgroundColor, modifiers)) {
            screen.setCharacter(x++, y, textCharacter);
        }
    }


    @Override
    public void onEvent(MenuEvent menuEvent) {
        System.out.println(menuEvent);
        switch (menuEvent.event()) {
            case "start" -> {
                try {
                    Long score = new Lantrix(screen, new LangService(lang), lang, minLetters, blockSet).process();
                    if (score !=0 ){
                        var scoreClient = new ScoreClient("wordrix", getSectionId());
                        List<Score> scores = scoreClient.pushScore("Vadim", score);
                        showScore(scores);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (FontFormatException e) {
                    throw new RuntimeException(e);
                }
            }
            case "exit" -> exit = true;
            case "language" -> {
                lang = switch (menuEvent.param()) {
                    case "ENG" -> Lantrix.Lang.ENG;
                    case "RUS" -> Lantrix.Lang.RUS;
                    default -> Lantrix.Lang.ENG;
                };
            }
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

                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private String getSectionId() {
        return lang.toString() + "_" + minLetters + "_" + blockSet.toString();
    }

    private void showScore(List<Score> scores) throws IOException {
        screen.clear();
        String caption = "-=* TOP SCORE *=-";
        int columns = screen.getTerminalSize().getColumns();
        drawString(caption, (columns - caption.length()) / 2 , 4, TextColor.ANSI.RED_BRIGHT, TextColor.ANSI.BLACK, SGR.BOLD);
        int line = 6;
        int x = (columns - 40) /2;
        for (Score score : scores) {
            String text = score.getPosition() + "."+" ".repeat(3-score.getPosition().toString().length())
                    + score.getName() + ".".repeat(30 - score.getName().length()+score.getScore().toString().length())
                    + score.getScore();
            boolean selected = score.getSelected()!=null?score.getSelected():false;
            drawString(text, x, line++, TextColor.ANSI.GREEN, selected ?TextColor.ANSI.BLUE:TextColor.ANSI.BLACK, SGR.BOLD);
        }
        screen.refresh();
        screen.readInput();
    }

}
