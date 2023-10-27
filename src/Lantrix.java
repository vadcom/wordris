import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Lantrix {
    Screen screen;
    boolean exit=true;

    enum State{Start,NewBlock,Game,Drop,Check,Fell,End,Exit}
    enum Event{onExit,createBlock,onGame,onEnd,onStepDown}
    Random rnd = new Random();

    int top = 1;
    int bottom = 10;
    int left = 10;
    int right = 20;

    State state;

    char[][] block;
    int bx;
    int by;

    public Lantrix() throws IOException {
        Terminal terminal = new DefaultTerminalFactory()
                .setInitialTerminalSize(new TerminalSize(80,25))
                .setTerminalEmulatorTitle("Wordrix")
                .createTerminal();
        screen = new TerminalScreen(terminal);
        state = State.Start;
    }

    public State onEvent(Event event){
        state = switch (event) {
            case onExit -> State.Exit;
            case createBlock -> State.NewBlock;
            case onGame -> State.Game;
            case onEnd -> State.End;
            case onStepDown -> dropDown();
        };
        return state;
    }

    private State dropDown() {
        return State.Game;
    }

    public void process() throws IOException, InterruptedException {
        screen.startScreen();
        while (exit) {
            screen.clear();
            show();
            switch (state) {
                case Start -> onEvent(Event.createBlock);
                case NewBlock -> {
                    if (addBlock()) onEvent(Event.onGame);
                    else onEvent(Event.onEnd);
                }
                case Game -> {
                    showBlock();
                }
            }
            screen.refresh();
            Thread.sleep(50);
        }
        screen.stopScreen();
    }

    private void showBlock() {
        for (int i = 0; i < block.length; i++) {
            var line=block[i];
            for (int j = 0; j < line.length; j++) {
                TextCharacter textCharacter = TextCharacter.fromCharacter(line[j], TextColor.ANSI.GREEN, TextColor.ANSI.BLACK, SGR.BOLD)[0];
                screen.setCharacter(bx+j*2, by+i, textCharacter);
            }
        }
    }

    private boolean addBlock() {
        bx=(right+left)/2;
        by=top;
        block=new char[3][2];
        for (char[] line : block) {
            Arrays.fill(line, ' ');
        }
        block[0][0]=getaChar();
        block[1][0]=getaChar();
        block[2][0]=getaChar();
        block[0][1]=getaChar();
        // TODO: 10/28/2023 Check cross
        return true;
    }

    private char getaChar() {
        return (char) (65 + rnd.nextInt(25));
    }


    public void show() throws IOException {
        drawCup();
        //screen.readInput();
        if (screen.pollInput()!=null) {
            exit=false;
        }
    }

    private void drawCup() {
        TextCharacter border = TextCharacter.fromCharacter('#', TextColor.ANSI.BLUE, TextColor.ANSI.BLACK, SGR.BOLD)[0];
        for (int i = top; i < bottom; i++) {
            screen.setCharacter(left, i, border);
            screen.setCharacter(right, i, border);
        }
        for (int i = left; i <= right; i++) {
            screen.setCharacter(i, bottom, border);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new Lantrix().process();
    }

}
