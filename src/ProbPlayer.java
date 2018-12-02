import jp.ne.kuramae.torix.lecture.ms.core.MineSweeper;
import jp.ne.kuramae.torix.lecture.ms.core.Player;

/**
 * 確率計算
 */
public class ProbPlayer extends Player {

    private Board board;

    static public void main(String[] args) {
        Player player = new ProbPlayer();

        MineSweeper mineSweeper = new MineSweeper(2);
        mineSweeper.setRandomSeed(3135);

        mineSweeper.start(player);
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
                searchEdges();
                board.print();
                break;
            }

            // 安全なマスを開ける
            board.forEach((iter) -> {
                if (iter.isSafe()) iter.open();
                return true;
            }, this);
        }

        System.exit(0);
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
     * @return
     */
    private void searchEdges() {
        board.forEach((here) -> {
            if (!here.isOpen() && !here.isFixed() && here.count_around((i) -> i.isOpen()) != 0) {
                return here.getCell().setEdge();
            } else {
                return false;
            }
        }, this);
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
