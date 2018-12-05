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

    public BoardIterator setXY(int x, int y) {
        if (x < 0 || x >= board.getWidth() || y < 0 || y >= board.getHeight()) return null; // 範囲外
        this.x = x;
        this.y = y;
        return this;
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

    public boolean isBoxEdge() {
        return getCell().isBoxEdge();
    }

    public boolean isNumEdge() {
        return getCell().isNumEdge();
    }

    public boolean open() {
        board.open(x, y, player);
        return true;
    }

    public boolean setBoxEdge() {
        if (isOpen()) return false;
        this.getCell()._setBoxEdge();
        this.board._addBoxEdge(this.getCell());
        return true;
    }

    public boolean setNumEdge() {
        if (!isOpen()) return false;
        this.getCell()._setNumEdge();
        this.board._addNumEdge(this.getCell());
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
        return count_around_n(fn, 1);
    }

    public int count_around_n(Function<BoardIterator, Boolean> fn, int n) {
        int initial_x = x, initial_y = y;
        int result = 0;
        for (int i = 0 - n; i < n + 1; i++) {
            for (int j = 0 - n; j < n + 1; j++) {
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
