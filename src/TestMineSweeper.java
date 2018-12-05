import java.util.Random;

public class TestMineSweeper {
    public TestField field;
    TestPlayer player;
    int width;
    int height;
    int bombNum;
    Random rand;

    public TestMineSweeper() {
        this(0);
    }

    public TestMineSweeper(int lv) {
        this.rand = new Random();
        this.width = 4;
        this.height = 4;
        this.bombNum = 7;
    }

    public void setRandomSeed(long seed) {
        this.rand = new Random(seed);
    }

    public boolean start(TestPlayer player) {
        this.field = new TestField(this.width, this.height, this.bombNum, this.rand);
        System.out.println(this.field.getMapText(false));

        while(true) {
            player.startGame(this.field);
        }
    }
}
