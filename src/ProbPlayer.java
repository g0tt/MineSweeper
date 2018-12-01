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

        MineSweeper mineSweeper = new MineSweeper(0);
        mineSweeper.setRandomSeed(6);

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
                int fixed_bombs = 0;
                for (int y = 0; y < getHeight(); y++) {
                    for (int x = 0; x < getWidth(); x++) {
                        if (prob_memory[x][y] == 100) {
                            fixed_bombs++;
                        }
                    }
                }
                //System.out.println("残り爆弾の数: " + (getBombNum() - fixed_bombs));
                System.out.println("安全なマスがないよ");
                printProbMemory(prob_memory);
                break;
            }
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
                int num_not_fixed = here.count_around(MineSweeperIterator.isNotOpenedNorFixed, prob_memory);
                int num_fixed = here.count_around(MineSweeperIterator.isFixed, prob_memory);

                // 残り爆弾の数と未オープンのマスの数が同じならフラグを立てる
                if (num_not_fixed != 0 && n - num_fixed == num_not_fixed) {
                    return here.apply_around(MineSweeperIterator.fixIfNotOpened, prob_memory);
                }
                return true;
            })) break;
        }
    }

    /**
     * 安全マス(爆弾がある確率が0%)の探索
     * @return int 安全マスの数
     */
    private int searchSafeCells() {
        MineSweeperIterator iter = new MineSweeperIterator(0, 0, this);

        return iter.count((here) -> {
            int n = here.getCell();
            if (n == -1 || n == 0) return true;
            int num_fixed = here.count_around(MineSweeperIterator.isFixed, prob_memory);
            // 安全なマスが存在するか
            return (n == num_fixed && !here.apply_around(MineSweeperIterator.setSafeIfNotFixed, prob_memory));
        });
    }

    public static void printProbMemory(double[][] prob_memory) {
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

}
