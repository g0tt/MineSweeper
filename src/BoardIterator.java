import java.util.function.Function;

public class BoardIterator {
    protected int x;
    protected int y;
    protected Board board;
    protected ProbPlayer player;

    public BoardIterator(Board board, ProbPlayer player) {
        this.x = 0;
        this.y = 0;
        this.board = board;
        this.player = player;
    }

    public BoardIterator(int x, int y, Board board, ProbPlayer player) {
        this.x = x;
        this.y = y;
        this.board = board;
        this.player = player;
    }

    public void print() {
        System.out.println("(x, y) = (" + x + ", " + y + ")");
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean setXY(int x, int y) {
        if (x < 0 || x >= board.getWidth() || y < 0 || y >= board.getHeight()) return false; // 範囲外
        this.x = x;
        this.y = y;
        return true;
    }

    public BoardCell getCell() {
        return board.get(x, y);
    }

    public boolean isFixed() {
        return getCell().isFixed();
    }

    public boolean isSafe() {
        return getCell().isSafe();
    }

    public boolean isOpen() {
        return getCell().isOpen();
    }

    public boolean open() {
        board.open(x, y, player);
        return true;
    }

    /**
     * 周囲8マスにラムダ式を適用する
     * @param fn ラムダ式
     * @return result
     */
    public boolean apply_around(Function<BoardIterator, Boolean> fn) {
        int initial_x = x, initial_y = y;
        boolean result = true;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                x = initial_x + j;
                y = initial_y + i;
                if (i == 0 && j == 0) continue; // (x, y)は含めない
                if (x < 0 || x >= board.getWidth() || y < 0 || y >= board.getHeight()) continue; // 配列外参照
                if (!fn.apply(this)) {
                    result = false;
                }
            }
        }
        x = initial_x;
        y = initial_y;
        return result;
    }

    /**
     * 周囲8マスにラムダ式を適用し，trueなものを数える
     * @param fn ラムダ式
     * @return count
     */
    public int count_around(Function<BoardIterator, Boolean> fn) {
        int initial_x = x, initial_y = y;
        int result = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                x = initial_x + j;
                y = initial_y + i;
                if (i == 0 && j == 0) continue; // (x, y)は含めない
                if (x < 0 || x >= board.getWidth() || y < 0 || y >= board.getHeight()) continue; // 配列外参照
                result += fn.apply(this) ? 1 : 0;
            }
        }
        x = initial_x;
        y = initial_y;
        return result;
    }

}
