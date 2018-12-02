import jp.ne.kuramae.torix.lecture.ms.core.MineSweeper;
import jp.ne.kuramae.torix.lecture.ms.core.Player;

/**
 * 確率計算
 */
public class ProbPlayer extends Player {

    /**
     * 爆弾確率のメモ
     * 0-100 or -1(計算前)
     */
    private double[][] prob_memory;

    static public void main(String[] args) {
        Player player = new ProbPlayer();

        MineSweeper mineSweeper = new MineSweeper(2);
        mineSweeper.setRandomSeed(6671);

        mineSweeper.start(player);
    }

    @Override
    protected void start() {
        // 確率メモ初期化
        prob_memory = new double[getWidth()][getHeight()];
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                prob_memory[x][y] = -1;
            }
        }

        open(getWidth() / 2, getHeight() / 2);

        for (int i = 0; i < 50; i++) {
            searchFixedCells();
            if (searchSafeCells() == 0) {
                System.out.println("安全なマスがないよ");
                searchEdges();
                printBoard();
                break;
            }

            // 安全なマスを開ける
            for (int y = 0; y < getHeight(); y++) {
                for (int x = 0; x < getWidth(); x++) {
                    if (prob_memory[x][y] == 0) {
                        System.out.println("Open: x = " + x + ", y = " + y);
                        open(x, y);
                    }
                }
            }
            for (int y = 0; y < getHeight(); y++) {
                for (int x = 0; x < getWidth(); x++) {
                    prob_memory[x][y] = -1;
                }
            }
        }

        System.exit(0);
    }

    /**
     * 自明な確定マス(爆弾がある確率が100%)の探索
     */
    private void searchFixedCells() {

        while(true) {
            MineSweeperIterator iter = new MineSweeperIterator(0, 0, this);

            if (iter.forEach((here) -> {
                int n = here.getCell();
                if (n == -1) return true;
                int num_not_fixed = here.count_around(MineSweeperIterator.isNotOpenNorFixed, prob_memory);
                int num_fixed = here.count_around(MineSweeperIterator.isFixed, prob_memory);

                // 残り爆弾の数と未オープンのマスの数が同じならフラグを立てる
                if (num_not_fixed != 0 && n - num_fixed == num_not_fixed) {
                    return here.apply_around(MineSweeperIterator.fixIfNotOpened, prob_memory);
                }
                return true;
            })) break;
        }
    }

    private EdgeCellSet searchEdges() {
        MineSweeperIterator iter = new MineSweeperIterator(0, 0, this);

        return iter.map((here) -> {
            if (here.getCell() == -1 && !here.isFixed(prob_memory) && here.count_around(MineSweeperIterator.isOpen, prob_memory) != 0) {
                return new EdgeCell(here);
            } else {
                return null;
            }
        });
    }

    /**
     * 安全マス(爆弾がある確率が0%)の探索
     * @return int 安全なマスがあるなら正
     */
    private int searchSafeCells() {
        MineSweeperIterator iter = new MineSweeperIterator(0, 0, this);

        return iter.count((here) -> {
            int n = here.getCell();
            if (n == -1 || n == 0) return false;
            int num_fixed = here.count_around(MineSweeperIterator.isFixed, prob_memory);
            // 安全なマスが存在するか
            return (n == num_fixed && !here.apply_around(MineSweeperIterator.setSafeIfNotFixed, prob_memory));
        });
    }

    /**
     * 確率メモを可視化
     */
    public void printProbMemory() {
        String res = "";
        for (int y = 0; y < prob_memory[0].length; y++) {
            if (y != 0) res += "\n";
            for (int x = 0; x < prob_memory.length; x++) {
                if (x != 0) res += " ";
                if (prob_memory[x][y] == 100) {
                    res += "*";
                } else if (prob_memory[x][y] == 0) {
                    res += "o";
                } else {
                    res += "-";
                }
            }
        }
        res += "\n";

        System.out.println(res);
    }

    public void printBoard() {
        String res = "";
        for (int y = 0; y < prob_memory[0].length; y++) {
            if (y != 0) res += "\n";
            for (int x = 0; x < prob_memory.length; x++) {
                if (x != 0) res += " ";
                if (prob_memory[x][y] == 100) {
                    res += "*";
                } else if (prob_memory[x][y] == 0) {
                    res += "o";
                } else {
                    int cell = getCell(x, y);
                    if (cell == -1 && searchEdges().contains(new EdgeCell(x, y))) {
                        res += "/";
                    } else if (cell == -1) {
                        res += ".";
                    } else {
                        res += cell;
                    }
                }
            }
        }
        res += "\n";

        System.out.println(res);
    }

    /**
     * 未確定の爆弾の数
     * @return count
     */
    private int countNotFixedBombs() {
        int fixed_bombs = 0;
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (prob_memory[x][y] == 100) {
                    fixed_bombs++;
                }
            }
        }
        return getBombNum() - fixed_bombs;
    }

}
