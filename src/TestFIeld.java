import java.util.Random;

class TestField {
    int[][] countMap;
    boolean[][] bombMap;
    boolean[][] mask;
    boolean[][] flag;
    boolean isGameOver;
    int width;
    int height;
    int bombNum;

    TestField(int width, int height, int bomb, Random random) {
        this.width = width;
        this.height = height;
        this.countMap = new int[][] {
                {1, 1, 4, 3},
                {1, 2, 4, 3},
                {1, 1, 3, 2},
                {1, 0, 2, 0}
        };
        this.bombMap = new boolean[][]{
                {false, true, true, true},
                {false, false, false, true},
                {false, false, false, false},
                {false, true, false, true}
        };
        this.mask = new boolean[height][width];

        this.bombNum = 6;

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                this.mask[y][x] = true;
            }
        }

    }

    boolean open(int x, int y) {
        if (!this.mask[y][x]) {
            return false;
        } else {
            this.mask[y][x] = false;
            int xx;
            int yy;

            if (this.countMap[y][x] == 0) {
                for(xx = x - 1; xx <= x + 1; ++xx) {
                    for(yy = y - 1; yy <= y + 1; ++yy) {
                        if (xx >= 0 && xx < this.width && yy >= 0 && yy < this.height && (xx != x || yy != y)) {
                            this.open(xx, yy);
                        }
                    }
                }
            }

            return true;
        }
    }

    public int getMaskedCellCount() {
        int cnt = 0;

        for(int y = 0; y < this.countMap.length; ++y) {
            for(int x = 0; x < this.countMap[y].length; ++x) {
                if (this.mask[y][x]) {
                    ++cnt;
                }
            }
        }

        return cnt;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getBombNum() {
        return this.bombNum;
    }

    public boolean isClear() {
        return this.getMaskedCellCount() == this.bombNum && !this.isGameOver();
    }

    public boolean isGameOver() {
        return this.isGameOver;
    }

    public String getMapText(boolean isShowBomb) {
        StringBuffer buf = new StringBuffer();

        for(int y = 0; y < this.countMap.length; ++y) {
            for(int x = 0; x < this.countMap[y].length; ++x) {
                if (this.mask[y][x]) {
                    if (this.bombMap[y][x] && isShowBomb) {
                        buf.append("x");
                    } else {
                        buf.append(".");
                    }
                } else if (this.bombMap[y][x]) {
                    buf.append("*");
                } else {
                    buf.append(this.countMap[y][x]);
                }
            }

            buf.append("\n");
        }

        return buf.toString();
    }

    public int[][] toIntMatrix() {
        int[][] matrix = new int[this.height][this.width];

        for(int y = 0; y < this.countMap.length; ++y) {
            for(int x = 0; x < this.countMap[y].length; ++x) {
                if (this.mask[y][x]) {
                    matrix[y][x] = -1;
                } else {
                    matrix[y][x] = this.countMap[y][x];
                }
            }
        }

        return matrix;
    }

    void openAll() {
        for(int y = 0; y < this.mask.length; ++y) {
            for(int x = 0; x < this.mask[y].length; ++x) {
                this.mask[y][x] = false;
            }
        }

    }
}
