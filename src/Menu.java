import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import link.sigma5.menu.DumbMenu;
import link.sigma5.menu.MenuEvent;
import link.sigma5.menu.MenuItemView;
import link.sigma5.menu.MenuListener;
import link.sigma5.menu.MenuAction;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

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
                    new Lantrix(screen, new LangService(lang), lang, minLetters, blockSet).process();
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
        }
    }
}
