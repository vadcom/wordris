// Author: Vadim Dubina
import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;

public class Block {

    enum BlockType{ IType,LType,LTypeMirror, UType, TWO, ANGLE, FULL, CROSS,WType }
    static Random rnd = new Random();

    /**
     * Symbols
     * first index is ros
     * second index is col
     */
    char[][] letters;

    int bx;
    int by;

    final private Supplier<Character> characterSupplier;
    Block(BlockType type,Supplier<Character> characterSupplier) {
        this.characterSupplier=characterSupplier;
        switch (type){
            case IType -> {
                initBlock(3,1);
                letters[0][0]=getaChar();
                letters[1][0]=getaChar();
                letters[2][0]=getaChar();
            }
            case LType -> {
                initBlock(3,2);
                letters[0][0]=getaChar();
                letters[1][0]=getaChar();
                letters[2][0]=getaChar();
                letters[0][1]=getaChar();
            }
            case LTypeMirror -> {
                initBlock(3,2);
                letters[0][1]=getaChar();
                letters[1][1]=getaChar();
                letters[2][1]=getaChar();
                letters[0][0]=getaChar();
            }
            case FULL -> {
                initBlock(2,2);
                letters[0][0]=getaChar();
                letters[1][0]=getaChar();
                letters[0][1]=getaChar();
                letters[1][1]=getaChar();
            }
            case UType -> {
                initBlock(2,3);
                letters[0][0]=getaChar();
                letters[1][0]=getaChar();
                letters[1][1]=getaChar();
                letters[0][2]=getaChar();
                letters[1][2]=getaChar();
            }
            case TWO -> {
                initBlock(2,1);
                letters[0][0]=getaChar();
                letters[1][0]=getaChar();
            }
            case ANGLE -> {
                initBlock(2,2);
                letters[1][0]=getaChar();
                letters[0][1]=getaChar();
                letters[1][1]=getaChar();
            }
            case CROSS -> {
                initBlock(3,3);
                letters[0][1]=getaChar();
                letters[1][0]=getaChar();
                letters[1][1]=getaChar();
                letters[1][2]=getaChar();
                letters[2][1]=getaChar();
            }
            case WType -> {
                initBlock(3,3);
                letters[0][0]=getaChar();
                letters[1][0]=getaChar();
                letters[1][1]=getaChar();
                letters[1][2]=getaChar();
                letters[2][2]=getaChar();
            }
        }
    }

    private void initBlock(int height, int width) {
        letters = new char[height][width];
        for (char[] line : letters) {
            Arrays.fill(line, ' ');
        }
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

    static public Block createBlock(Supplier<Character> characterSupplier, BlockType[] blockSet) {
        int bound=blockSet.length;
        return new Block(blockSet[(rnd.nextInt(bound))],characterSupplier);
    }


    public char[][] getRotateClockwise(){
        char[][] reversed = getReversed();
        char[][] rotated=new char[getWidth()][getHeight()];
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                rotated[j][i]=reversed[i][j];
            }
        }
        return rotated;
    }

    public char[][] getRotateCounterClockwise(){
        char[][] reversed = getReversed();
        int width = getWidth();
        int height = getHeight();
        char[][] rotated=new char[width][height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                rotated[j][i]=reversed[height-i-1][width-j-1];
            }
        }
        return rotated;
    }

    private char[][] getReversed() {
        char[][] reversed=new char[getHeight()][getWidth()];
        for (int i = 0; i <getHeight(); i++) {
            if (getWidth() >= 0) System.arraycopy(letters[i], 0, reversed[getHeight()-i-1], 0, getWidth());
        }
        return reversed;
    }

    public void rotateCounterClockwise(){
        letters=getRotateCounterClockwise();
    }

    public void rotateClockwise(){
        letters=getRotateClockwise();
    }
    private char getaChar() {
        // return (char) (65 + rnd.nextInt(25));
        return characterSupplier.get();
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
