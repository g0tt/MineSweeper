import jp.ne.kuramae.torix.lecture.ms.core.MineSweeper;
import jp.ne.kuramae.torix.lecture.ms.core.Player;

import java.util.Random;

/**
 * 確率計算
 */
public class ProbPlayer extends Player {

    private Board board;

    public int rand_cnt;
    public int random_seed;

    static final boolean TEST_MODE = false;
    static final int TEST_COUNT = 10;
    static final int LEVEL = 2;
    static final int SEED = 6671;

    static public void main(String[] args) {
        Random rand = new Random(System.currentTimeMillis());
        int clear_count = 0;
        int test_count = TEST_MODE ? TEST_COUNT : 1;
        for (int i = 0; i < test_count; i++) {
            ProbPlayer player = new ProbPlayer();

            MineSweeper mineSweeper = new MineSweeper(LEVEL);
            player.random_seed = SEED == -1 ? rand.nextInt(1000) : SEED;
            mineSweeper.setRandomSeed(player.random_seed);

            mineSweeper.start(player);
            if (player.isClear()) {
                clear_count++;
            }
        }
        System.out.println("Clear: " + clear_count / (TEST_COUNT / 100.0) + "%");
    }

    @Override
    protected void start() {
        board = new Board(getWidth(), getHeight());
        board.initialize();

        board.open(getWidth() / 2, getHeight() / 2, this);

        for (int i = 0; i < 50; i++) {
            searchFixedCells();
            if (searchSafeCells() == 0) {
                System.out.println("安全なマスがないよ");
                if (searchEdges()) {
                    System.out.println("EdgeOpen:");
                    board.printEdges();
                }
                if (!TEST_MODE) {
                    board.print();
                    break;
                } else {
                    fallback();
                }
            }

            // 安全なマスを開ける
            board.forEach((iter) -> {
                if (iter.isSafe()) return iter.open();
                return false;
            }, this);
        }
        if (!TEST_MODE) System.exit(0);
    }

    /**
     * これ以上開けない場合
     */
    private void fallback() {
        int count = board.count((i) -> {
            if (!(i.isOpen() || i.isFixed())) {
                return true;
            };
            return false;
        }, this);
        Random rand = new Random();
        rand_cnt = rand.nextInt(count);
        if (board.forEach((i) -> {
            if (!(i.isOpen() || i.isFixed())) {
                if (i.player.rand_cnt == 0) return i.open();
                i.player.rand_cnt--;
            }
            return true;
        }, this)) {
            System.out.println("ランダムに選択しました");
        } else {
            System.exit(0);
        }

    }

    /**
     * 自明な確定マス(爆弾がある確率が100%)の探索
     */
    private void searchFixedCells() {

        while(true) {
            if (board.forEach((here) -> {
                if (here.isFixed()) return true;
                int n = here.getCell().get();
                int num_not_fixed = here.count_around((i) -> i.getCell().isNotOpenNorFixed());
                int num_fixed = here.count_around((i) -> i.isFixed());

                // 残り爆弾の数と未オープンのマスの数が同じならフラグを立てる
                if (num_not_fixed != 0 && n - num_fixed == num_not_fixed) {
                    return here.apply_around((i) -> {
                        // 未オープンだったら確定させる 変化があったらfalseなことに注意
                        boolean not_changed = true;
                        if (!i.isOpen() && !i.isFixed()) {
                            i.getCell().fix();
                            not_changed = false;
                        }
                        return not_changed;
                    });
                }
                return true;
            }, this)) break;
        }
    }

    /**
     * エッジの探索
     * @return エッジが存在するか
     */
    private boolean searchEdges() {
        return !board.forEach((here) ->
            !(!here.isOpen()
                    && !here.isFixed()
                    && here.count_around(
                            (i) -> i.isOpen() && i.setEdgeOpen()) != 0
                    && here.setEdge())
        , this);
    }

    /**
     * 安全マス(爆弾がある確率が0%)の探索
     * @return int 安全なマスがあるなら正
     */
    private int searchSafeCells() {
        return board.count((here) -> {
            int n = here.getCell().get();
            if (n == -1 || n == 0) return false;
            int num_fixed = here.count_around((i) -> i.isFixed());
            // 安全なマスが存在するか
            return (n == num_fixed && !here.apply_around((i) -> {
                // 確定マスでなければ安全とする
                if (!i.isFixed() && !i.isOpen()) {
                    i.getCell().setSafe();
                    return false;
                }
                return true;
            }));
        }, this);
    }

    /**
     * 未確定の爆弾の数
     * @return count
     */
    private int countNotFixedBombs() {
        int fixed_bombs = board.count((i) -> i.isFixed(), this);
        return getBombNum() - fixed_bombs;
    }

}
