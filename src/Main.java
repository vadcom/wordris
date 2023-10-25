import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    record Letter(int row, int col , char letter) {}
    int height=5;
    int width=5;
    char [][] cup = new char[height][width];

    Random rnd = new Random();

    public Main() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j <width; j++) {
                cup[i][j] = (char) (65+ rnd.nextInt(25));
            }
        }
    }

    public void show() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j <width; j++) {
                System.out.print(" "+cup[i][j]);
            }
            System.out.println();
        }
    }

    int min=3;
    int max=10;
    int [][] steps={{1,0},{-1,0},{0,1},{0,-1}};
    private int checkLetter(int row, int col, List<Letter> letters) {
        int result=0;
        letters.add(new Letter(row,col,cup[row][col]));
        if (letters.size()>=min) {
            System.out.println(getWord(letters));
            result++;
        }
        if (letters.size()>=max) {
            return result;
        }
        for (int[] step:steps) {
            int row1 = row + step[0];
            int col1 = col + step[1];
            if (checkStep(row1, col1) &
                    checkOverlap(row1, col1,letters)) {
                result+=checkLetter(row1,col1,new ArrayList<>(letters));
            }
        }
        return result;
    }

    private String getWord(List<Letter> letters) {
        StringBuilder builder=new StringBuilder();
        letters.forEach(obj -> builder.append(obj.letter()));
        return builder.toString();
    }

    private boolean checkOverlap(final int row,final int col,List<Letter> letters) {
        return letters.stream().noneMatch(letter -> letter.col()==col && letter.row()==row);
    }
    private boolean checkStep(int row,int col) {
        return row>=0 & row<height & col>=0 & col<width;
    }

    public int variants() {
        int total=0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j <width; j++) {
                total+=checkLetter(i, j, new ArrayList<>());
            }
        }
        return total;
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.show();
        System.out.println("Total words "+main.variants());
    }
}