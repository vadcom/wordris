import java.util.Arrays;
import java.util.Random;

public class Block {

    enum BlockType{ IType,LType }
    Random rnd = new Random();

    /**
     * Symbols
     * first index is ros
     * second index is col
     */
    char[][] letters;

    int bx;
    int by;

    Block(BlockType type) {
        letters =new char[3][2];
        for (char[] line : letters) {
            Arrays.fill(line, ' ');
        }
        letters[0][0]=getaChar();
        letters[1][0]=getaChar();
        letters[2][0]=getaChar();
        letters[0][1]=getaChar();
    }

    public void setPosition(int x,int y) {
        bx=x;
        by=y;
    }

    int getWidth() {
        return getHeight()!=0?letters[0].length:0;
    }

    int getHeight() {
        return letters!=null?letters.length:0;
    }

    /**
     * Horizontal shift
     * @param dx
     * @return true if success
     */
    public boolean shift(int dx){
        bx+=dx;
        return true;
    }

    /**
     * Drop block to one position
     * @return
     */
    public boolean down() {
        by++;
        return true;
    }

    static public Block createBlock() {
        return new Block(BlockType.LType);
    }

    private char getaChar() {
        return (char) (65 + rnd.nextInt(25));
    }

    public int getBx() {
        return bx;
    }

    public int getBy() {
        return by;
    }

    public char[][] getLetters() {
        return letters;
    }

}
