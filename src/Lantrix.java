import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;

public class Lantrix {
    Screen screen;

    public Lantrix() throws IOException {
        Terminal terminal = new DefaultTerminalFactory()
                .setInitialTerminalSize(new TerminalSize(80,25))
                .setTerminalEmulatorTitle("Wordrix")
                .createTerminal();
        screen = new TerminalScreen(terminal);
    }

    public void show() throws IOException {
        screen.startScreen();
        screen.clear();

        drawCup();
        screen.refresh();
        screen.readInput();
        screen.stopScreen();
    }

    private void drawCup() {
        TextCharacter border = new TextCharacter('#', TextColor.ANSI.BLUE, TextColor.ANSI.BLACK, SGR.BOLD);
        int top = 1;
        int bottom = 10;
        int left = 10;
        int right = 20;
        for (int i = top; i < bottom; i++) {
            screen.setCharacter(left, i, border);
            screen.setCharacter(right, i, border);
        }
        for (int i = left; i <= right; i++) {
            screen.setCharacter(i, bottom, border);
        }

    }

    public static void main(String[] args) throws IOException {
        new Lantrix().show();
    }

}
