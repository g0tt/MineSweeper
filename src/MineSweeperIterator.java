import jp.ne.kuramae.torix.lecture.ms.core.Player;
import java.util.function.Function;

public class MineSweeperIterator {

    private int x;
    private int y;
    private Player player;

    /**
     * 周囲探索用ラムダ式
     */
    // 確定マス以外ならtrue
    public static BoardFunction<Boolean> isFixed = (final int x, final int y, final Player player, double[][] prob_memory) -> prob_memory[x][y] == 100;

    // 未オープンかつ未確定ならtrue
    public static BoardFunction<Boolean> isNotOpenedNorFixed = (final int x, final int y, final Player player, double[][] prob_memory) -> prob_memory[x][y] != 100 && player.getCell(x, y) == -1;

    // 未オープンだったら確定させる 変化があったらfalseなことに注意
    public static BoardFunction<Boolean> fixIfNotOpened = (final int x, final int y, final Player player, double[][] prob_memory) -> {
        boolean not_changed = true;
        if (player.getCell(x, y) == -1 && prob_memory[x][y] != 100) {
            prob_memory[x][y] = 100;
            not_changed = false;
        }
        return not_changed;
    };

    // 確定マスでなければ安全とする
    public static BoardFunction<Boolean> setSafeIfNotFixed = (final int x, final int y, final Player player, double[][] prob_memory) -> {
        boolean found = false;
        if (prob_memory[x][y] != 100 && player.getCell(x, y) == -1) {
            prob_memory[x][y] = 0;
            found = true;
        }

        return found;
    };

    public MineSweeperIterator(int x, int y, Player player) {
        this.x = x;
        this.y = y;
        this.player = player;
    }

    public void print() {
        System.out.println("MineSweeperIterator::(x, y) = (" + x + ", " + y + ")");
    }

    public int getCell() {
        return player.getCell(x, y);
    }

    // falseが1つでもあればfalse

    /**
     * 全マスにラムダ式を適用する
     * @param fn ラムダ式
     * @return 返り値の論理積
     */
    public boolean forEach(Function<MineSweeperIterator, Boolean> fn) {
        int tmp_x = x, tmp_y = y;
        boolean success = true;

        for (y = 0; y < player.getHeight(); y++) {
            for (x = 0; x < player.getWidth(); x++) {
                try {
                    if (!fn.apply(this)) {
                        success = false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        }
        x = tmp_x;
        y = tmp_y;
        return success;
    }

    // trueなものを数える

    /**
     * 全マスにラムダ式を適用し，trueなものを数える
     * @param fn ラムダ式
     * @return count
     */
    public int count(Function<MineSweeperIterator, Boolean> fn) {
        int tmp_x = x, tmp_y = y;
        int cnt = 0;

        for (y = 0; y < player.getHeight(); y++) {
            for (x = 0; x < player.getWidth(); x++) {
                try {
                    if (fn.apply(this)) {
                        cnt++;
                    }
                } catch (Exception e) {
                    return -1;
                }
            }
        }
        x = tmp_x;
        y = tmp_y;
        return cnt;
    }

    /**
     * 周囲8マスにラムダ式を適用する
     * @param fn ラムダ式
     * @param prob_memory 確率メモリ
     * @return result
     */
    public boolean apply_around(BoardFunction<Boolean> fn, double[][] prob_memory) {
        boolean result = true;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int xx = x + j;
                int yy = y + i;
                if (i == 0 && j == 0) continue; // (x, y)は含めない
                if (xx < 0 || xx >= player.getWidth() || yy < 0 || yy >= player.getHeight()) continue; // 配列外参照
                if (!fn.run(xx, yy, player, prob_memory)) {
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * 周囲8マスにラムダ式を適用し，trueなものを数える
     * @param fn ラムダ式
     * @param prob_memory 確率メモリ
     * @return count
     */
    public int count_around(BoardFunction<Boolean> fn, double[][] prob_memory) {
        int result = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int xx = x + j;
                int yy = y + i;
                if (i == 0 && j == 0) continue; // (x, y)は含めない
                if (xx < 0 || xx >= player.getWidth() || yy < 0 || yy >= player.getHeight()) continue; // 配列外参照
                result += fn.run(xx, yy, player, prob_memory) ? 1 : 0;
            }
        }
        return result;
    }

    /**
     * 盤面(x, y)に適用できるプレーヤー関数
     * @param <T>
     */
    @FunctionalInterface
    public interface BoardFunction<T> {
        public T run(int x, int y, Player player, double[][] prob_memory);
    }

}
