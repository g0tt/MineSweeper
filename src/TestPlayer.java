import javafx.util.Pair;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 確率計算
 */
public class TestPlayer extends ProbPlayer {
    public int random_seed;

    private TestMineSweeper mineSweeper;
    public TestField field;

    /**
     * 設定
     */
    static final boolean TEST_MODE = false;

    static public void main(String[] args) {
        TestPlayer player = new TestPlayer();

        player.mineSweeper = new TestMineSweeper();
        player.mineSweeper.setRandomSeed(player.random_seed);

        player.mineSweeper.start(player);
    }

    protected void startGame(TestField field) {
        this.start();
    }

    @Override
    public boolean _open(int x, int y) {
        boolean isOpen = this.mineSweeper.field.open(x, y);
        System.out.println(this.mineSweeper.field.getMapText(false));
        return isOpen;
    }

    @Override
    public int _getCell(int x, int y) {
        return this.field.toIntMatrix()[y][x];
    }

    @Override
    protected void start() {
        board = new Board(4, 4);
        board.initialize();

        field = mineSweeper.field;

        board.open(3, 2, this);
        board.open(2, 1, this);
        board.open(2, 2, this);
        board.open(2, 3, this);

        for (int i = 0; i < 50; i++) {
            searchFixedCells();
            if (!searchSafeCells()) {
                System.out.println("安全なマスがないよ");
                if (searchEdges()) {
                    if (board.boxEdge.size() > 62 || board.numEdge.size() > 62) {
                        System.out.println("size is over 62, may cause error");
                        System.exit(0);
                    }
                    linkBoxEdgeToNumEdge();
                    bfs_queue = new ArrayDeque<>();
                    queueNumEdge();
                    bfs_depth = -1;
                    while (bfs_depth < 2) {
                        bfs();
                        System.out.println("bfs_depth = " + bfs_depth);
                    }
                    for (Pair<Long, Long> data : edge_bitmap) {
                        System.out.println(String.format("%64s", Long.toBinaryString(data.getKey())).replace(" ", "0") + " " +
                                String.format("%64s", Long.toBinaryString(data.getValue())).replace(" ", "0"));
                    }
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
                        System.out.println(String.format("%64s", Long.toBinaryString(key)).replace(" ", "0")
                                + " => "
                                + String.format("%64s", Long.toBinaryString(result.get(key))).replace(" ", "0"));
                    }
                    /*
                    for (Pair<Long, Long> data : edge_bitmap) {
                        if (data.getValue() == 2101279) {
                            System.out.println(String.format("%64s", Long.toBinaryString(data.getKey())).replace(" ", "0"));
                        }
                    }
                    */

                    System.out.println("result = " + String.format("%64s", Long.toBinaryString(final_res)).replace(" ", "0"));
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

    protected boolean bfs() {
        Long[] data = bfs_queue.poll();
        //System.out.println(data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3] + ", " + data[4]);
        long edgeBitMap = data[0];
        long numEdgeFlg = data[1];
        long boxEdgeFlg = data[2];
        int x = (int)(long)data[3];
        int y = (int)(long)data[4];
        if (numEdgeFlg == 4) {
            System.out.println("");
        }
        int depth = (int)(long)data[5];
        if (depth > bfs_depth) {
            bfs_depth = depth;
            if (depth == 2) return false;
            edge_bitmap = new ArrayList<>();
        }

        BoardIterator iter = new BoardIterator(x, y, board, this);
        //iter.print();
        BoardCell cell = iter.getCell();

        int localBitMap = createLocalBitMap(cell, edgeBitMap);
        int fixed_bombs = iter.count_around((i) -> i.isFixed()); // TODO
        int box_edge_bombs = Integer.bitCount(localBitMap);
        int other_bombs = cell.get() - fixed_bombs - box_edge_bombs;
        ArrayList<Integer> localBitList = new ArrayList<>();
        if (x == 2 && y == 3) {
            System.out.println("");
        }
        dfs(cell.relatedEdges, localBitMap, 0, other_bombs, localBitList, createLocalBitMap(cell, boxEdgeFlg));
        for (int nextLocalBitMap : localBitList) {
            long nextEdgeBitMap = createEdgeBitMap(cell, nextLocalBitMap) | edgeBitMap;
            long nextNumEdgeFlg = (long)Math.pow(2, board.numEdge.headSet(cell).size()) | numEdgeFlg;
            long nextBoxEdgeFlg = getBoxEdgeFlg(cell) | boxEdgeFlg;
            if (nextBoxEdgeFlg == 56) {
                System.out.println("");
            }
            edge_bitmap.add(new Pair<>(nextEdgeBitMap, nextBoxEdgeFlg));
            pushBfsQueue(cell.x, cell.y, nextEdgeBitMap, nextNumEdgeFlg, nextBoxEdgeFlg, depth);
        }
        return true;
    }

}
