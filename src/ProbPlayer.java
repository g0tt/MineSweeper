import jp.ne.kuramae.torix.lecture.ms.core.MineSweeper;
import jp.ne.kuramae.torix.lecture.ms.core.Player;
import java.util.ArrayList;
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
    static final int SEED = 3135;

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
                    linkBoxEdgeToNumEdge();
                    queueNumEdge();
                }
                if (!TEST_MODE) {
                    //board.printEdges();
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
     * 全てのnumEdgeをbfs用のFIFOバッファに入れる
     */
    private void queueNumEdge() {
        for (BoardCell cell : board.numEdge) {
            BoardIterator iter = cell.getIterator(this);
            int fixed_bombs = iter.count_around((i) -> i.isFixed());
            int other_bombs = cell.get() - fixed_bombs;
            ArrayList<Integer> localBitList = new ArrayList<>();
            dfs(cell.relatedEdges, 0, 0, other_bombs, localBitList);
            for (int localBitMap : localBitList) {
                long edgeBitMap = createEdgeBitMap(cell, localBitMap);
                long numEdgeFlg = (long)Math.pow(2, board.numEdge.headSet(cell).size());
                System.out.println("edgeBitMap = " + edgeBitMap);
                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {
                        int x = cell.x + j, y = cell.y + i;
                        iter.setXY(x, y);
                        if (iter.isNumEdge()) {
                            //queue.push(edgeBitMap, numEdgeFlg, x, y)
                        }
                    }
                }
            }
        }
    }

    /*
    private void bfs() {
        queue.pop();
        BoardIterator iter = cell.getIterator(this);
        int fixed_bombs = iter.count_around((i) -> i.isFixed());
        int other_bombs = cell.get() - fixed_bombs;
        ArrayList<Integer> localBitList = new ArrayList<>();
        dfs(cell.relatedEdges, 0, 0, other_bombs, localBitList);
        for (int localBitMap : localBitList) {
            long edgeBitMap = createEdgeBitMap(cell, localBitMap);
            long numEdgeFlg = (long)Math.pow(2, board.numEdge.headSet(cell).size());
            System.out.println("edgeBitMap = " + edgeBitMap);
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    int x = cell.x + j, y = cell.y + i;
                    iter.setXY(x, y);
                    if (iter.isNumEdge()) {
                        //queue.push(edgeBitMap, numEdgeFlg, x, y)
                    }
                }
            }
        }
    }
    */

    /**
     * localBitMapからedgeBitMapを作成する
     * @param cell in numEdge
     * @param localBitMap localBitMap
     * @return edgeBitMap
     */
    private long createEdgeBitMap(BoardCell cell, int localBitMap) {
        long edgeBitMap = 0;
        int filter = 1;
        for (BoardCell boxCell : cell.relatedEdges) {
            if ((filter & localBitMap) > 0) {
                edgeBitMap += Math.pow(2, board.boxEdge.headSet(boxCell).size());
            }
            filter *= 2;
        }
        return edgeBitMap;
    }

    /**
     * relatedEdgesが取りうる爆弾配置パターンの深さ優先探索
     * @param relatedEdges 考えるboxEdge
     * @param binary_pattern bitパターン，1が爆弾
     * @param i relatedEdgesのイテレータ
     * @param bombs 残り爆弾の数
     * @param patterns 記録用ArrayList
     */
    private void dfs(BoardCellSet relatedEdges, int binary_pattern, int i, int bombs, ArrayList<Integer> patterns) {
        dfs(relatedEdges, binary_pattern, i, bombs, patterns, 0);
    }
    private void dfs(BoardCellSet relatedEdges, int binary_pattern, int i, int bombs, ArrayList<Integer> patterns, int initialize) {
        //if (initialize != 0) FIXME
        if (bombs == 0) {
            patterns.add(binary_pattern);
            return;
        } else if (i >= relatedEdges.size()){
            return;
        }
        dfs(relatedEdges, binary_pattern + (int)Math.pow(2, i), i + 1, bombs - 1, patterns);
        dfs(relatedEdges, binary_pattern, i + 1, bombs, patterns);
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
                            (i) -> i.isOpen() && i.setNumEdge()) != 0
                    && here.setBoxEdge())
        , this);
    }

    /**
     * numEdgeの各cellのrelatedEdgesにboxEdgeを入れる
     */
    private void linkBoxEdgeToNumEdge() {
        this.board.numEdge.forEach((i) ->
                i.getIterator(this).apply_around((j) ->
                        j.isBoxEdge() && i.relatedEdges.add(j.getCell())));
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
