import javafx.util.Pair;

import jp.ne.kuramae.torix.lecture.ms.core.MineSweeper;
import jp.ne.kuramae.torix.lecture.ms.core.Player;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * 確率計算
 */
public class ProbPlayer extends Player {

    protected Board board;

    public int rand_cnt;
    public int random_seed;

    /**
     * 設定
     */
    static final boolean TEST_MODE = true; // 正解率をテストするモード
    static final int TEST_COUNT = 100; // 正解率テストの試行回数
    static final int LEVEL = 0; // レベル 0 or 1 or 2
    static final int SEED = -1; // 問題の乱数シード -1でランダム
    static final int BFS_DEPTH = 4; // 幅優先探索の深さ 4で充分

    /**
     * bfs用のキュー
     * edgeBitMap, numEdgeFlg, boxEdgeFlg, x, y
     */
    protected ArrayDeque<Long[]> bfs_queue;

    /**
     * bfsキュー内で一番浅いものの深さ
     */
    protected int bfs_depth;

    protected ArrayList<Pair<Long, Long>> edge_bitmap;

    static public void main(String[] args) {
        Random rand = new Random(System.currentTimeMillis());
        PrintStream stdout = System.out;
        if (TEST_MODE) {
            OutputStream nul = new OutputStream() {
                @Override
                public void write(int b) {
                }

                @Override
                public void write(byte[] b) {
                }

                @Override
                public void write(byte[] b, int off, int len) {
                }
            };
            System.setOut(new PrintStream(nul));
        }
        int clear_count = 0;
        int test_count = TEST_MODE ? TEST_COUNT : 1;
        for (int i = 0; i < test_count; i++) {
            ProbPlayer player = new ProbPlayer();

            MineSweeper mineSweeper = new MineSweeper(LEVEL);
            player.random_seed = SEED == -1 ? rand.nextInt(1000000) : SEED;
            if (!TEST_MODE) System.out.println("Random seed: " + player.random_seed);
            mineSweeper.setRandomSeed(player.random_seed);

            mineSweeper.start(player);
            if (player.isClear()) {
                clear_count++;
            }
            if (TEST_MODE) player.showProgressBar(i + 1, test_count, String.format("%.1f", clear_count * 100.0 / i) + "%", stdout);
        }
        if (TEST_MODE) stdout.println("\n\nClear: " + clear_count / (TEST_COUNT / 100.0) + "%");
    }

    /**
     * 依存注入できないため
     * @param x
     * @param y
     * @return
     */
    public boolean _open(int x, int y) {
        return open(x, y);
    }

    /**
     * 依存注入できないため
     * @param x
     * @param y
     * @return
     */
    public int _getCell(int x, int y) {
        return getCell(x, y);
    }

    @Override
    protected void start() {
        board = new Board(getWidth(), getHeight());
        board.initialize();

        board.open(0, 0, this);

        for (int i = 0; i < 1000; i++) {
            if (i > 200) System.exit(0);
            searchFixedCells();
            if (!searchSafeCells() && !searchSafeCellsComplex()) {
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
     * 自明な確定マス(爆弾がある確率が100%)の探索
     */
    protected void searchFixedCells() {

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
     * 自明な安全マスの探索
     * @return boolean 安全マスがあるか
     */
    protected boolean searchSafeCells() {
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
        }, this) > 0;
    }

    /**
     * 幅優先探索による非自明な安全マスの探索
     * @return boolean 安全マスがあるか
     */
    protected boolean searchSafeCellsComplex() {
        if (searchEdges()) {
            if (board.boxEdge.size() > 63 || board.numEdge.size() > 63) {
                System.out.println("size is over 63, may cause error");
                return false; // FIXME
                //System.exit(0);
            }
            linkBoxEdgeToNumEdge();
            bfs_queue = new ArrayDeque<>();
            queueNumEdge();
            bfs_depth = 0;
            edge_bitmap = new ArrayList<>();
            while (bfs_depth < BFS_DEPTH) {
                if (!bfs()) break;
            }

            // FIXME
            HashMap<Long, Long> result = new HashMap<>();
            for (Pair<Long, Long> data : edge_bitmap) {
                if (result.containsKey(data.getValue())) {
                    result.put(data.getValue(), (result.get(data.getValue()) | data.getKey()));
                } else {
                    result.put(data.getValue(), data.getKey());
                }
            }
            long final_res = 0;
            for (Long key : result.keySet()) {
                final_res |= (key ^ result.get(key));
            }

            if (final_res == 0) {
                return false;
            }
            long k = 1;
            for (BoardCell cell : board.boxEdge) {
                if ((final_res & k) != 0) {
                    cell.setSafe();
                }
                k *= 2;
            }
            return true;
        }
        return false;
    }

    /**
     * 全てのnumEdgeをbfs用のFIFOバッファに入れる
     */
    protected void queueNumEdge() {
        for (BoardCell cell : board.numEdge) {
            BoardIterator iter = cell.getIterator(this);
            int fixed_bombs = iter.count_around((i) -> i.isFixed());
            int other_bombs = cell.get() - fixed_bombs;
            ArrayList<Integer> localBitList = new ArrayList<>();
            dfs(cell.relatedEdges, 0, 0, other_bombs, localBitList);
            for (int localBitMap : localBitList) {
                long edgeBitMap = createEdgeBitMap(cell, localBitMap);
                long numEdgeFlg = (long)Math.pow(2, board.numEdge.headSet(cell).size());
                long boxEdgeFlg = getBoxEdgeFlg(cell);
                pushBfsQueue(cell.x, cell.y, edgeBitMap, numEdgeFlg, boxEdgeFlg, 0);
            }
        }
    }

    /**
     * boxEdgeの爆弾配置に関する幅優先探索
     */
    protected boolean bfs() {
        Long[] data = bfs_queue.poll();
        if (data == null) return false;
        long edgeBitMap = data[0];
        long numEdgeFlg = data[1];
        long boxEdgeFlg = data[2];
        int x = (int)(long)data[3];
        int y = (int)(long)data[4];
        int depth = (int)(long)data[5];
        if (depth > bfs_depth) {
            bfs_depth = depth;
            if (depth == BFS_DEPTH) return false;
            edge_bitmap = new ArrayList<>();
        }

        BoardIterator iter = new BoardIterator(x, y, board, this);
        BoardCell cell = iter.getCell();
        int localBitMap = createLocalBitMap(cell, edgeBitMap);
        int fixed_bombs = iter.count_around((i) -> i.isFixed());
        int box_edge_bombs = Integer.bitCount(localBitMap);
        int other_bombs = cell.get() - fixed_bombs - box_edge_bombs;
        ArrayList<Integer> localBitList = new ArrayList<>();
        dfs(cell.relatedEdges, localBitMap, 0, other_bombs, localBitList, createLocalBitMap(cell, boxEdgeFlg));
        for (int nextLocalBitMap : localBitList) {
            long nextEdgeBitMap = createEdgeBitMap(cell, nextLocalBitMap) | edgeBitMap;
            long nextNumEdgeFlg = (long)Math.pow(2, board.numEdge.headSet(cell).size()) | numEdgeFlg;
            long nextBoxEdgeFlg = getBoxEdgeFlg(cell) | boxEdgeFlg;
            edge_bitmap.add(new Pair<>(nextEdgeBitMap, nextBoxEdgeFlg));
            pushBfsQueue(cell.x, cell.y, nextEdgeBitMap, nextNumEdgeFlg, nextBoxEdgeFlg, depth);
        }
        return true;
    }

    /**
     * @param x 現在のx
     * @param y 現在のy
     * @param nextEdgeBitMap 最新のedgeBitMap
     * @param nextNumEdgeFlg 最新のnumEdgeFlg
     * @param nextBoxEdgeFlg 最新のboxEdgeFlg
     * @param depth 現在の深さ
     */
    protected void pushBfsQueue(int x, int y, long nextEdgeBitMap, long nextNumEdgeFlg, long nextBoxEdgeFlg, int depth) {
        BoardIterator iter = new BoardIterator(board, this);
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) { // TODO: 隣接するnumEdgeを考えているが，実際はrelatedEdgesをピボットにして調べないと不十分
                if (i == 0 && j == 0) continue; // TODO: 同じ方向に戻らない
                int nextX = x + j, nextY = y + i;
                if (iter.setXY(nextX, nextY) == null) continue;
                if (iter.isNumEdge()) {
                    Long[] arr = {nextEdgeBitMap, nextNumEdgeFlg, nextBoxEdgeFlg, (long)nextX, (long)nextY, (long)depth + 1};
                    bfs_queue.add(arr);
                }
            }
        }
    }

    /**
     * localBitMapからedgeBitMapを作成する
     * @param cell in numEdge
     * @param localBitMap localBitMap
     * @return edgeBitMap
     */
    protected long createEdgeBitMap(BoardCell cell, int localBitMap) {
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
     * edgeBitMapからlocalBitMapを作成する
     * @param cell in numEdge
     * @param edgeBitMap edgeBitMap
     * @return localBitMap
     */
    protected int createLocalBitMap(BoardCell cell, long edgeBitMap) {
        int localBitMap = 0;
        int i = 0;
        for (BoardCell boxCell : cell.relatedEdges) {
            if (((long)Math.pow(2, board.boxEdge.headSet(boxCell).size()) & edgeBitMap) > 0) {
                localBitMap += 1 << i;
            }
            i += 1;
        }
        return localBitMap;
    }

    /**
     * relatedEdgesをbitMapに直す
     * @param cell in numEdge
     * @return boxEdgeFlg
     */
    protected long getBoxEdgeFlg(BoardCell cell) {
        long boxEdgeFlg = 0;
        for (BoardCell boxCell : cell.relatedEdges) {
            boxEdgeFlg += Math.pow(2, board.boxEdge.headSet(boxCell).size());
        }
        return boxEdgeFlg;
    }

    /**
     * relatedEdgesが取りうる爆弾配置パターンの深さ優先探索
     * @param relatedEdges 考えるboxEdge
     * @param binary_pattern bitパターン，1が爆弾
     * @param i relatedEdgesのイテレータ
     * @param bombs 残り爆弾の数
     * @param patterns 記録用ArrayList
     */
    protected void dfs(BoardCellSet relatedEdges, int binary_pattern, int i, int bombs, ArrayList<Integer> patterns) {
        dfs(relatedEdges, binary_pattern, i, bombs, patterns, 0);
    }
    protected void dfs(BoardCellSet relatedEdges, int binary_pattern, int i, int bombs, ArrayList<Integer> patterns, int mask) {
        if (bombs == 0) {
            patterns.add(binary_pattern);
            return;
        } else if (i >= relatedEdges.size()){
            return;
        }

        // 未確定のマスなら二通り試す
        if ((mask & 1 << i) == 0) {
            dfs(relatedEdges, binary_pattern + (int)Math.pow(2, i), i + 1, bombs - 1, patterns, mask);
            dfs(relatedEdges, binary_pattern, i + 1, bombs, patterns, mask);
        }
        // 爆弾が入るか入らないか確定している
        if ((mask & 1 << i) != 0) {
            dfs(relatedEdges, binary_pattern, i + 1, bombs, patterns, mask);
        }
    }

    /**
     * これ以上開けない場合
     */
    protected void fallback() {
        BoardIterator iter = new BoardIterator(board, this);
        if (!iter.setXY(0, 0).isOpen() && !iter.setXY(0, 0).isFixed()) {
            iter.open();
            return;
        }
        if (!iter.setXY(getWidth() - 1, 0).isOpen() && !iter.setXY(getWidth() - 1, 0).isFixed()) {
            iter.open();
            return;
        }
        if (!iter.setXY(0, getHeight() - 1).isOpen() && !iter.setXY(0, getHeight() - 1).isFixed()) {
            iter.open();
            return;
        }
        if (!iter.setXY(getWidth() - 1, getHeight() - 1).isOpen() && !iter.setXY(getWidth() - 1, getHeight() - 1).isFixed()) {
            iter.open();
            return;
        }
        int count = board.count((i) -> {
            if (!(i.isOpen() || i.isFixed())) {
                return true;
            };
            return false;
        }, this);
        Random rand = new Random();
        rand_cnt = rand.nextInt(count);
        if (!board.forEach((i) -> {
            if (!(i.isOpen() || i.isFixed())) {
                if (i.player.rand_cnt-- == 0) return i.open();
            }
            return true;
        }, this)) {
            System.exit(0);
        }

    }

    /**
     * エッジの探索
     * @return エッジが存在するか
     */
    protected boolean searchEdges() {
        board.boxEdge = new BoardCellSet();
        board.numEdge = new BoardCellSet();
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
    protected void linkBoxEdgeToNumEdge() {
        this.board.numEdge.forEach((i) -> {
                i.relatedEdges = new BoardCellSet();
                i.getIterator(this).apply_around((j) ->
                        j.isBoxEdge() && i.relatedEdges.add(j.getCell()));
        });
    }

    /**
     * 未確定の爆弾の数
     * @return count
     */
    protected int countNotFixedBombs() {
        int fixed_bombs = board.count((i) -> i.isFixed(), this);
        return getBombNum() - fixed_bombs;
    }

    /**
     * 正解率テストの進捗表示
     * @param current 現在の試行回数
     * @param max 合計の試行回数
     * @param info 追加情報
     * @param ps PrintStream
     */
    protected void showProgressBar(int current, int max, String info, PrintStream ps) {
        final int width = 40;
        int progress_count = current * width / max;
        ps.print("\r[");
        for (int i = 0; i < progress_count; i++) {
            ps.print("\u001b[00;34m=\u001b[00m");
        }
        if (progress_count < width) ps.print(">");
        for (int i = 0; i < (width - progress_count - 1); i++) {
            ps.print(" ");
        }
        ps.print("]" + current + " " + info);
    }

}
