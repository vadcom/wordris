package link.sigma5.wordris;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

import static java.lang.Math.min;

public class Lantrix implements PoleService {
    public static final int SHOW_TIME = 1000;

    public static final int DOWN_DELAY_MS = 2000;
    private static final int DELAY = 50;

    Screen screen;
    boolean doGame = true;
    public enum Lang {ENG, RUS, QAD, BLOCK}

    public enum BlockSet {BASE, EXT}

    static final int HEIGHT = 20;
    static final int WIDTH = 10;
    private final LangService langService;
    private final int minLetters;
    private final BlockSet blockSet;

    enum State {Start, NewBlock, Game, Drop, Check, Fell, EndGame, Exit, BrightWord, DropLetters}

    enum Event {
        onExit, createBlock, onGame, onEnd, onStepDown, onDropDown,
        shiftLeft,
        shiftRight,
        brightWord,
        onDropLetter,
        onClockwise,
        onCounterClockwise
    }

    Block block;

    int top = 1;

    int bottom;
    int left = 10;
    int right;
    State state;

    Long score;

    char[][] cup;

    public Lantrix(Screen screen, LangService langService, int minLetters, BlockSet blockSet) throws IOException, URISyntaxException, FontFormatException {
        this.screen = screen;
        this.langService = langService;
        this.minLetters = minLetters;
        this.blockSet = blockSet;
        state = State.Start;
        bottom = top + HEIGHT;
        right = left + WIDTH * 2 + 2;
    }

    Block.BlockType[] base = {Block.BlockType.IType, Block.BlockType.LType, Block.BlockType.LTypeMirror, Block.BlockType.TWO, Block.BlockType.ANGLE};
    Block.BlockType[] extended = {Block.BlockType.IType, Block.BlockType.LType, Block.BlockType.UType, Block.BlockType.LTypeMirror, Block.BlockType.TWO, Block.BlockType.ANGLE, Block.BlockType.FULL, Block.BlockType.CROSS};

    private Block.BlockType[] getBlockList(BlockSet blockSet) {
        return switch (blockSet) {
            case BASE -> base;
            case EXT -> extended;
        };
    }

    private void onEvent(Event event) {
        System.out.println("State:" + state.toString() + " Event " + event.toString());
        switch (state) {

            case Start -> state = switch (event) {
                case createBlock -> State.NewBlock;
                default -> state;
            };
            case NewBlock -> state = switch (event) {
                case onGame -> State.Game;
                case onEnd -> State.EndGame;
                default -> state;
            };
            case DropLetters -> state = switch (event) {
                case brightWord -> State.BrightWord;
                case createBlock -> State.NewBlock;
                default -> state;
            };
            case Game -> state = switch (event) {
                case onExit -> State.Exit;
                case createBlock -> State.NewBlock;
                case onGame -> State.Game;
                case onEnd -> State.EndGame;
                case onStepDown -> stepDown();
                case onDropDown -> State.Drop;
                case shiftLeft -> shift(-1);
                case shiftRight -> shift(1);
                case brightWord -> State.BrightWord;
                case onDropLetter -> State.DropLetters;
                case onClockwise -> rotateClockwise();
                case onCounterClockwise -> rotateCounterClockwise();
                default -> state;
            };
            case BrightWord -> state = switch (event) {
                case brightWord -> State.BrightWord;
                case onDropLetter -> State.DropLetters;
                default -> state;
            };
            case Drop -> {
                if (event == Event.onGame) state = State.Game;
            }
            case EndGame -> {
                if (event == Event.onExit || event==Event.onDropDown) state = State.Exit;
            }
        }
    }

    private State rotateClockwise() {
        if (isPossiblePosition(block.getBx(), block.getBy(), block.getRotateClockwise())) {
            block.rotateClockwise();
        }
        return State.Game;
    }

    private State rotateCounterClockwise() {
        if (isPossiblePosition(block.getBx(), block.getBy(), block.getRotateCounterClockwise())) {
            block.rotateCounterClockwise();
        }
        return State.Game;
    }


    private State shift(int i) {
        if (isPossiblePosition(block.getBx() + i, block.getBy(), block.getLetters())) {
            block.setPosition(block.getBx() + i, block.getBy());
        }
        return State.Game;
    }

    @Override
    public boolean isPossiblePosition(int x, int y, char[][] block) {
        int blockWidth = block[0].length;
        int blockHeight = block.length;
        if (x < 0 || x + blockWidth > WIDTH || y < 0 || y + blockHeight > HEIGHT) return false;
        for (int i = 0; i < blockHeight; i++) {
            for (int j = 0; j < blockWidth; j++) {
                if (cup[i + y][j + x] != ' ' && block[i][j] != ' ') return false;
            }
        }
        return true;
    }

    public void fillPole(Block b) {
        int x = b.getBx();
        int y = b.getBy();
        char[][] block = b.getLetters();
        for (int i = 0; i < b.getHeight(); i++) {
            for (int j = 0; j < b.getWidth(); j++) {
                if (block[i][j] != ' ') {
                    cup[i + y][j + x] = block[i][j];
                }
            }
        }
    }

    List<List<LangService.Letter>> prize = new ArrayList<>();

    State stepDown() {
        if (isPossiblePosition(block.getBx(), block.getBy() + 1, block.getLetters())) {
            block.setPosition(block.getBx(), block.getBy() + 1);
            return State.Game;
        } else {
            // move block to pole
            fillPole(block);
            return checkWords() ? State.BrightWord : State.NewBlock;
        }
    }

    boolean checkWords() {
        List<List<LangService.Letter>> variants = langService.variants(cup, minLetters);
        var wordOp = variants.stream().reduce((letters, letters2) -> letters.size() > letters2.size() ? letters : letters2);
        if (wordOp.isPresent()) {
            prize.add(wordOp.get());
            return true;
        }
        return false;
    }

    int getHeight() {
        return cup == null ? 0 : cup.length;
    }

    int getWidth() {
        if (getHeight() == 0) return 0;
        return cup[0].length;
    }

    void dropLetters(List<LangService.Letter> word) {
        word.forEach(letter -> cup[letter.row()][letter.col()] = '*');
        for (int i = 1; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                if (cup[i][j] == '*') {
                    for (int k = i; k > 0; k--) {
                        cup[k][j] = cup[k - 1][j];
                        cup[k - 1][j] = ' ';
                    }
                }
            }
        }
    }

    void dropDown() {
        int dy = 1;
        while (isPossiblePosition(block.getBx(), block.getBy() + dy, block.getLetters())) {
            dy++;
        }
        block.setPosition(block.getBx(), block.getBy() + dy - 1);
    }


    public Long process() throws IOException, InterruptedException {
        int delay = 50;
        int counter = 0;
        while (doGame) {
            screen.clear();
            hideCursor();
            drawCup();
            showPole();
            switch (state) {
                case Start -> {
                    score = 0L;
                    initCup();
                    onEvent(Event.createBlock);
                }
                case NewBlock -> {
                    if (addBlock()) onEvent(Event.onGame);
                    else onEvent(Event.onEnd);
                }
                case Game -> {
                    showBlock();
                    if (counter >= DOWN_DELAY_MS / DELAY) {
                        counter = 0;
                        onEvent(Event.onStepDown);
                    }
                }
                case Drop -> {
                    dropDown();
                    counter = DOWN_DELAY_MS / DELAY - 5; // Small-time for shift
                    onEvent(Event.onGame);
                }
                case Exit -> doGame = false;
                case EndGame -> showEndGame();
                case BrightWord -> {
                    score += factorial(getLastWord().size());
                    brightWord(getLastWord());
                    delay = SHOW_TIME;
                    onEvent(Event.onDropLetter);
                }
                case DropLetters -> {
                    dropLetters(getLastWord());
                    onEvent(checkWords() ? Event.brightWord : Event.createBlock);
                }
            }
            showPrize();
            screen.refresh();
            processKey(screen.pollInput());
            Thread.sleep(delay);
            delay = DELAY;
            counter++;
        }
        return score;
    }

    public long factorial(int n) {
        return LongStream.rangeClosed(1, n)
                .reduce(1, (long x, long y) -> x * y);
    }

    private void showEndGame() {
        int j = 0;
        String text = "-=* END GAME *=-";
        for (TextCharacter textCharacter : TextCharacter.fromString(text, TextColor.ANSI.RED_BRIGHT, TextColor.ANSI.BLUE, SGR.BOLD)) {
            screen.setCharacter(left + (WIDTH * 2 - text.length()) / 2 + 2 + j++, top + 5, textCharacter);
        }
    }


    private void showPrize() {
        for (int i = 0; i < min(10, prize.size()); i++) {
            var word = prize.get(prize.size() - i - 1);
            int j = 0;
            for (TextCharacter textCharacter : TextCharacter.fromString(getWord(word), TextColor.ANSI.GREEN, TextColor.ANSI.BLACK, SGR.BOLD)) {
                screen.setCharacter(right + 10 + j++, top + 2 + i, textCharacter);
            }
        }
        int j = 0;
        for (TextCharacter textCharacter : TextCharacter.fromString("Score: ", TextColor.ANSI.YELLOW, TextColor.ANSI.BLACK, SGR.BOLD)) {
            screen.setCharacter(right + 10 + j++, top, textCharacter);
        }
        for (TextCharacter textCharacter : TextCharacter.fromString(score.toString(), TextColor.ANSI.YELLOW, TextColor.ANSI.BLACK, SGR.BOLD)) {
            screen.setCharacter(right + 10 + j++, top, textCharacter);
        }
    }

    private String getWord(List<LangService.Letter> letters) {
        StringBuilder builder = new StringBuilder();
        letters.forEach(obj -> builder.append(obj.letter()));
        return builder.toString();
    }

    private void brightWord(List<LangService.Letter> lastWord) {
        lastWord.forEach(letter -> {
            TextCharacter textCharacter = TextCharacter.fromCharacter(' ', TextColor.ANSI.YELLOW, TextColor.ANSI.BLUE, SGR.BOLD)[0];
            screen.setCharacter(left + 1 + (letter.col()) * 2, top + letter.row(), textCharacter);
            textCharacter = TextCharacter.fromCharacter(letter.letter(), TextColor.ANSI.YELLOW, TextColor.ANSI.BLUE, SGR.BOLD)[0];
            screen.setCharacter(left + 2 + (letter.col()) * 2, top + letter.row(), textCharacter);
            textCharacter = TextCharacter.fromCharacter(' ', TextColor.ANSI.YELLOW, TextColor.ANSI.BLUE, SGR.BOLD)[0];
            screen.setCharacter(left + 3 + (letter.col()) * 2, top + letter.row(), textCharacter);
        });
    }

    private List<LangService.Letter> getLastWord() {
        return prize.get(prize.size() - 1);
    }

    private void processKey(KeyStroke key) {
        if (key != null) {
            switch (key.getKeyType()) {
                case ArrowLeft -> onEvent(Event.shiftLeft);
                case ArrowRight -> onEvent(Event.shiftRight);
                case Escape -> onEvent(Event.onExit);
                case Enter -> onEvent(Event.onDropDown);
                case ArrowDown -> onEvent(Event.onClockwise);
                case ArrowUp -> onEvent(Event.onCounterClockwise);
                case Character -> {
                    if (key.getCharacter().equals(' ')) {
                        onEvent(Event.onDropDown);
                    }
                }
            }
        }
    }

    private void initCup() {
        cup = new char[HEIGHT][WIDTH];
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                cup[i][j] = ' ';
            }
        }
    }

    private void showBlock() {
        for (int i = 0; i < block.getLetters().length; i++) {
            var line = block.getLetters()[i];
            for (int j = 0; j < line.length; j++) {
                if (line[j] != ' ') {
                    TextCharacter textCharacter = TextCharacter.fromCharacter(line[j], TextColor.ANSI.GREEN, TextColor.ANSI.BLACK, SGR.BOLD)[0];
                    screen.setCharacter(left + 2 + (block.getBx() + j) * 2, top + block.getBy() + i, textCharacter);
                }
            }
        }
    }

    private void showPole() {
        if (cup == null) return;
        for (int i = 0; i < cup.length; i++) {
            var line = cup[i];
            for (int j = 0; j < line.length; j++) {
                TextCharacter textCharacter = TextCharacter.fromCharacter(line[j], TextColor.ANSI.GREEN, TextColor.ANSI.BLACK, SGR.BOLD)[0];
                screen.setCharacter(left + 2 + (j) * 2, top + i, textCharacter);
            }
        }
    }

    private boolean addBlock() {
        block = Block.createBlock(langService::getLetter, getBlockList(blockSet));
        block.setPosition(WIDTH / 2 - block.getWidth() / 2, 0);
        return isPossiblePosition(block.getBx(), block.getBy(), block.getLetters());
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

    private void hideCursor() {
        screen.setCharacter(1, 1,TextCharacter.fromCharacter(' ', TextColor.ANSI.BLACK, TextColor.ANSI.BLACK)[0]);
        screen.setCursorPosition(new TerminalPosition(1, 1));
    }


}
