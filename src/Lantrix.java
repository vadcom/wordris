import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class Lantrix implements PoleService {
    Screen screen;
    boolean exit=true;

    static final int HEIGHT=20;
    static final int WIDTH=10;
    private FieldService fieldService;

    enum State{Start,NewBlock,Game,Drop,Check,Fell,End,Exit;}

    enum Event{onExit,createBlock,onGame,onEnd,onStepDown,onDropDown,shiftLeft,shiftRight}

    Block block;

    int top = 1;

    int bottom = 10;
    int left = 10;
    int right = 20;
    State state;

    char [][] cup;

    public Lantrix(FieldService fieldService) throws IOException {
        this.fieldService = fieldService;
        Terminal terminal = new DefaultTerminalFactory()
                .setInitialTerminalSize(new TerminalSize(80,25))
                .setTerminalEmulatorTitle("Wordrix")
                .createTerminal();
        screen = new TerminalScreen(terminal);
        state = State.Start;
        bottom = top + HEIGHT;
        right = left + WIDTH*2 + 2;
    }
    public State onEvent(Event event){
        state = switch (event) {
            case onExit -> State.Exit;
            case createBlock -> State.NewBlock;
            case onGame -> State.Game;
            case onEnd -> State.End;
            case onStepDown -> stepDown();
            case onDropDown -> dropDown();
            case shiftLeft -> shift(-1);
            case shiftRight -> shift(1);
        };
        return state;
    }

    private State shift(int i) {
        if (isPossiblePosition(block.getBx()+i,block.getBy(), block.getLetters())) {
            block.setPosition(block.getBx()+i,block.getBy());
        }
        return State.Game;
    }

    @Override
    public boolean isPossiblePosition(int x, int y, char[][] block) {
        int blockWidth = block[0].length;
        int blockHeight = block.length;
        if (x<0 || x+ blockWidth >WIDTH || y<0 || y+ blockHeight >HEIGHT) return false;
        for (int i = 0; i < blockHeight; i++) {
            for (int j = 0; j < blockWidth; j++) {
                if (cup[i+y][j+x]!=' ' && block[i][j]!=' ') return false;
            }
        }
        return true;
    }

    public void fillPole(Block b) {
        int x=b.getBx();
        int y=b.getBy();
        char[][] block=b.getLetters();
        for (int i = 0; i < b.getHeight(); i++) {
            for (int j = 0; j < b.getWidth(); j++) {
                if (block[i][j]!=' ') {
                    cup[i + y][j + x] = block[i][j];
                }
            }
        }
    }

    List<List<FieldService.Letter>> prize=new ArrayList<>();

    private State stepDown() {
        if (isPossiblePosition(block.getBx(),block.getBy()+1, block.getLetters())) {
            block.setPosition(block.getBx(),block.getBy()+1);
            return State.Game;
        } else {
            // move block to pole
            fillPole(block);
            checkWords();
            return onEvent(Event.createBlock);
        }
    }

    private void checkWords() {
        List<List<FieldService.Letter>> variants = fieldService.variants(cup);
        if (variants.size()>0) {
            // TODO: 11/1/2023 Select max
            var word=variants.get(0);
            prize.add(word);
            dropLetters(word);
        }
    }

    private void dropLetters(List<FieldService.Letter> word) {
        word.forEach(letter -> {
            for (int i = letter.row(); i > 1 ; i--) {
                cup[i][letter.col()]=cup[i-1][letter.col()];
                cup[i-1][letter.col()]=' ';
            }
        });
    }

    private State dropDown() {
        int dy=1;
        while (isPossiblePosition(block.getBx(),block.getBy()+dy, block.getLetters())) {
            dy++;
        }
        block.setPosition(block.getBx(),block.getBy()+dy-1);
        fillPole(block);
        checkWords();
        return onEvent(Event.createBlock);
    }

    public void process() throws IOException, InterruptedException {
        screen.startScreen();
        int counter=0;
        while (exit) {
            screen.clear();
            drawCup();
            showPole();
            switch (state) {
                case Start -> {
                    initCup();
                    onEvent(Event.createBlock);
                }
                case NewBlock -> {
                    if (addBlock()) onEvent(Event.onGame);
                    else onEvent(Event.onEnd);
                }
                case Game -> {
                    showBlock();
                    if (counter>=20) {
                        counter=0;
                        onEvent(Event.onStepDown);
                    }
                }
                case Exit -> {
                    exit=false;
                }
            }
            screen.refresh();
            KeyStroke key=screen.pollInput();
            if (key!=null) {
                switch (key.getKeyType()) {
                    case ArrowLeft-> onEvent(Event.shiftLeft);
                    case ArrowRight -> onEvent(Event.shiftRight);
                    case Escape -> onEvent(Event.onExit);
                    case Enter -> onEvent(Event.onDropDown);
                }
            }
            Thread.sleep(50);
            counter++;
        }
        screen.stopScreen();
    }

    private void initCup() {
        cup = new char[HEIGHT][WIDTH];
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j <WIDTH; j++) {
                cup[i][j] = ' ';
            }
        }
    }

    private void showBlock() {
        for (int i = 0; i < block.getLetters().length; i++) {
            var line=block.getLetters()[i];
            for (int j = 0; j < line.length; j++) {
                TextCharacter textCharacter = TextCharacter.fromCharacter(line[j], TextColor.ANSI.GREEN, TextColor.ANSI.BLACK, SGR.BOLD)[0];
                screen.setCharacter(left+2+(block.getBx()+j)*2, top+block.getBy()+i, textCharacter);
            }
        }
    }

    private void showPole() {
        if (cup==null) return;
        for (int i = 0; i < cup.length; i++) {
            var line=cup[i];
            for (int j = 0; j < line.length; j++) {
                TextCharacter textCharacter = TextCharacter.fromCharacter(line[j], TextColor.ANSI.GREEN, TextColor.ANSI.BLACK, SGR.BOLD)[0];
                screen.setCharacter(left+2+(j)*2, top+i, textCharacter);
            }
        }
    }

    private boolean addBlock() {
        block=Block.createBlock();
        block.setPosition(WIDTH/2,0);
        return isPossiblePosition(block.getBx(),block.getBy(), block.getLetters());
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
        new Lantrix(new FieldService()).process();
    }

}
