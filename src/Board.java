import java.util.function.Function;

public class Board {

    private int width;
    private int height;

    private BoardCell[][] board;
    private BoardCellSet edgesOpen;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.board = new BoardCell[width][height];
        this.edgesOpen = new BoardCellSet();
    }

    public void initialize() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.board[x][y] = new BoardCell(x, y, this, -1);
            }
        }
    }

    public void open(int x, int y,ProbPlayer player) {
        player.open(x, y);
        this.forEach((i) -> {
            i.getCell().set(i.player.getCell(i.x, i.y));
            return true;
        }, player);
        if (!ProbPlayer.TEST_MODE) print();
    }

    public BoardCell get(int x, int y) {
        return board[x][y];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void print() {
        String res = "";
        for (int y = 0; y < height; y++) {
            if (y != 0) res += "\n";
            for (int x = 0; x < width; x++) {
                if (x != 0) res += " ";
                res += get(x, y).toChar();
            }
        }
        res += "\n";

        System.out.println(res);
    }

    public void _addEdgesOpen(BoardCell cell) {
        this.edgesOpen.add(cell);
    }

    public void printEdges() {
        edgesOpen.forEach((i) -> {
            System.out.println(i.toString());
        });
    }


    /**
     * 全マスにラムダ式を適用し，trueなものを数える
     * @param fn ラムダ式
     * @return count
     */
    public int count(Function<BoardIterator, Boolean> fn, ProbPlayer player) {
        int cnt = 0;
        BoardIterator iter = new BoardIterator(this, player);

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (!iter.setXY(x, y)) return -1;
                try {
                    if (fn.apply(iter)) {
                        cnt++;
                    }
                } catch (Exception e) {
                    return -1;
                }
            }
        }
        return cnt;
    }

    /**
     * 全マスにラムダ式を適用する
     * @param fn ラムダ式
     * @return 返り値の論理積
     */
    public boolean forEach(Function<BoardIterator, Boolean> fn, ProbPlayer player) {
        BoardIterator iter = new BoardIterator(this, player);
        boolean success = true;

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                iter.setXY(x, y);
                //try {
                    if (!fn.apply(iter)) {
                        success = false;
                    }
                //} catch (Exception e) {
                //    return false;
                //}
            }
        }
        return success;
    }

    /**
     * 全マスにラムダ式を適用し，TreeSetを返す
     * @param fn ラムダ式
     * @return TreeSet<EdgeCell>
     */
    /*
    public EdgeCellSet map(Function<BoardIterator, EdgeCell> fn, Player player) {
        BoardIterator iter = new BoardIterator(this, player);
        EdgeCellSet result = new EdgeCellSet();

        for (int y = 0; y < player.getHeight(); y++) {
            for (int x = 0; x < player.getWidth(); x++) {
                iter.setXY(x, y);
                result.add(fn.apply(iter));
            }
        }
        return result;
    }
    */
}
