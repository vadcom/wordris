import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LantrixTest {

    static Lantrix lantrix;

    @BeforeAll
    static void beforeAll() throws IOException, URISyntaxException, FontFormatException {
        LangService langService = new LangService(Lantrix.Lang.ENG);
        lantrix = new Lantrix(null,langService, Lantrix.Lang.ENG, 3, Lantrix.BlockSet.EXT);
    }

    @Test
    void dropLetters() {
        lantrix.cup = new char[5][1];
        char[][] cup = lantrix.cup;
        cup[0][0] = '1';
        cup[1][0] = '2';
        cup[2][0] = '3';
        cup[3][0] = '4';
        cup[4][0] = '5';
        var letter = new ArrayList<LangService.Letter>();
        letter.add(new LangService.Letter(2, 0, '*'));
        letter.add(new LangService.Letter(3, 0, '*'));
        lantrix.dropLetters(letter);
        show(cup);
        assertEquals(' ', cup[0][0]);
        assertEquals(' ', cup[1][0]);
        assertEquals('1', cup[2][0]);
        assertEquals('2', cup[3][0]);
        assertEquals('5', cup[4][0]);
    }

    @Test
    void dropLettersBottom() {
        lantrix.cup = new char[5][1];
        char[][] cup = lantrix.cup;
        cup[0][0] = '1';
        cup[1][0] = '2';
        cup[2][0] = '3';
        cup[3][0] = '4';
        cup[4][0] = '5';
        var letter = new ArrayList<LangService.Letter>();
        letter.add(new LangService.Letter(4, 0, '*'));
        letter.add(new LangService.Letter(3, 0, '*'));
        letter.add(new LangService.Letter(2, 0, '*'));
        lantrix.dropLetters(letter);
        show(cup);
        assertEquals(' ', cup[0][0]);
        assertEquals(' ', cup[1][0]);
        assertEquals(' ', cup[2][0]);
        assertEquals('1', cup[3][0]);
        assertEquals('2', cup[4][0]);
    }

    private void show(char[][] cup) {
        for (char[] row : cup) {
            for (char c : row) {
                System.out.println(c);
            }
        }
    }
}