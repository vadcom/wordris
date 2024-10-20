package link.sigma5.wordris;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LangService {

    final Lantrix.Lang lang;
    final Supplier<Character> characterSupplier;
    Set<String> dictionary;
    int height = 7;
    int width = 7;
    char[][] cup = new char[height][width];

    ;
    Random rnd = new Random();
    Character[] chars = {'#', '$', '@', '*', '+'};
    TreeMap<Integer, Character> treeMap = new TreeMap<>();
    AtomicInteger sum = new AtomicInteger();
    List<List<Letter>> words = new ArrayList<>();
    int min = 3;
    int max = 10;
    int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public LangService(Lantrix.Lang lang) {
        this.lang = lang;
        loadDictionary(lang);
        getStatistics(lang);
        characterSupplier = getCharSupplier(lang);
    }

    private Supplier<Character> getCharSupplier(Lantrix.Lang lang) {
        switch (lang) {
            case ENG, RUS -> {
                return this::getNormalizedLetter;
            }
            default -> {
                return this::getBlockLetter;
            }
        }
    }

    private void getStatistics(Lantrix.Lang lang) {
        var filter = getFilter(lang);
        if (filter == null) return;
        char letterFrom = filter.letterFrom;
        char letterTo = filter.letterTo;
        Map<Character, Integer> map = new HashMap<>();
        dictionary.forEach(word -> {
            for (Character c : word.toCharArray()) {
                if (c >= letterFrom && c <= letterTo) {
                    int count = map.getOrDefault(c, 0);
                    map.put(c, count + 1);
                }
            }
        });
        sum.set(0);
        treeMap.clear();
        map.forEach((character, integer) -> {
            int i = sum.addAndGet(integer);
            treeMap.put(i, character);
        });
        treeMap.forEach((key, value) -> System.out.println(value + " : " + key));
    }

    public Character getLetter() {
        return characterSupplier.get();
    }

    public Character getNormalizedLetter() {
        int key = rnd.nextInt(sum.get());
        return treeMap.ceilingEntry(key).getValue();
    }

    public Character getBlockLetter() {
        return chars[rnd.nextInt(chars.length)];
    }

    private void initCup() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                cup[i][j] = getaChar();
            }
        }
    }

    private char getaChar() {
        return (char) (65 + rnd.nextInt(25));
    }

    private void loadDictionary(Lantrix.Lang lang) {
        String dict = getDictionaryFileName(lang);
        if (dict == null) {
            dictionary = new HashSet<>();
            for (Character c : chars) {
                for (int i = min; i < max; i++) {
                    dictionary.add(String.valueOf(c).repeat(i));
                }
            }
            return;
        }
        InputStream is = getClass().getClassLoader().getResourceAsStream(dict);
        assert is != null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        dictionary = bufferedReader.lines().map(String::toUpperCase).collect(Collectors.toSet());
    }

    private String getDictionaryFileName(Lantrix.Lang lang) {
        return switch (lang) {
            case ENG -> "dictionary_en.lst";
            case RUS -> "dictionary_ru.lst";
            default -> null;
        };
    }

    private LetterFilter getFilter(Lantrix.Lang lang) {
        return switch ((lang)) {
            case ENG -> new LetterFilter('A', 'Z');
            case RUS -> new LetterFilter('А', 'Я');
            default -> null;
        };
    }

    public void show() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(" " + cup[i][j]);
            }
            System.out.println();
        }
    }

    private int checkLetter(int row, int col, List<Letter> letters, int minLetters) {
        int result = 0;
        char letter = cup[row][col];
        if (letter == ' ') return 0;
        letters.add(new Letter(row, col, letter));
        if (letters.size() >= minLetters) {
            String word = getWord(letters);
            if (dictionary.contains(word)) {
                words.add(letters);
                System.out.println(word);
                result++;
            }
        }
        if (letters.size() >= max) {
            return result;
        }
        for (int[] step : steps) {
            int row1 = row + step[0];
            int col1 = col + step[1];
            if (checkStep(row1, col1) &
                    checkOverlap(row1, col1, letters)) {
                result += checkLetter(row1, col1, new ArrayList<>(letters), minLetters);
            }
        }
        return result;
    }

    private String getWord(List<Letter> letters) {
        StringBuilder builder = new StringBuilder();
        letters.forEach(obj -> builder.append(obj.letter()));
        return builder.toString();
    }

    public void setMin(int min) {
        this.min = min;
    }

    private boolean checkOverlap(final int row, final int col, List<Letter> letters) {
        return letters.stream().noneMatch(letter -> letter.col() == col && letter.row() == row);
    }

    private boolean checkStep(int row, int col) {
        return row >= 0 & row < height & col >= 0 & col < width;
    }

    public List<List<Letter>> variants(char[][] pole, int minLetters) {
        cup = pole;
        height = pole.length;
        width = pole.length > 0 ? pole[0].length : 0;
        words.clear();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                checkLetter(i, j, new ArrayList<>(), minLetters);
            }
        }
        return new ArrayList<>(words);
    }

    public record Letter(int row, int col, char letter) {
    }

    record LetterFilter(char letterFrom, char letterTo) {
    }

}