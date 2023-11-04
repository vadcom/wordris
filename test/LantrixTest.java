import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LantrixTest {

    static Lantrix lantrix;

    @BeforeAll
    static void beforeAll() throws IOException {
        //FieldService fieldService=mock(FieldService.class);
        FieldService fieldService=new FieldService();

        lantrix=new Lantrix(fieldService);
    }

    @org.junit.jupiter.api.Test
    void dropLetters() {
        lantrix.cup=new char[5][1];
        char[][] cup = lantrix.cup;
        cup[0][0]='1';
        cup[1][0]='2';
        cup[2][0]='3';
        cup[3][0]='4';
        cup[4][0]='5';
        var letter=new ArrayList<FieldService.Letter>();
        letter.add(new FieldService.Letter(2,0,'*'));
        letter.add(new FieldService.Letter(3,0,'*'));
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
        lantrix.cup=new char[5][1];
        char[][] cup = lantrix.cup;
        cup[0][0]='1';
        cup[1][0]='2';
        cup[2][0]='3';
        cup[3][0]='4';
        cup[4][0]='5';
        var letter=new ArrayList<FieldService.Letter>();
        letter.add(new FieldService.Letter(4,0,'*'));
        letter.add(new FieldService.Letter(3,0,'*'));
        letter.add(new FieldService.Letter(2,0,'*'));
        lantrix.dropLetters(letter);
        show(cup);
        assertEquals(' ', cup[0][0]);
        assertEquals(' ', cup[1][0]);
        assertEquals(' ', cup[2][0]);
        assertEquals('1', cup[3][0]);
        assertEquals('2', cup[4][0]);
    }

    private void show(char[][] cup) {
        for (int i = 0; i < cup.length; i++) {
            var row= cup[i];
            for (int j = 0; j < row.length; j++) {
                System.out.println(cup[i][j]);
            }
        }
    }
}