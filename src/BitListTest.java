public class BitListTest {
    public static void main(String[] args) {
        BitMap a = new BitMap(128);
        BitMap b = new BitMap(128);
        a.setTrue(3);
        b.setTrue(3);
        assert_that(a.equals(b));

        a.setTrue(70);
        b.setTrue(70);
        assert_that(a.equals(b));

        a.setTrue(88);
        a.setFalse(3);
        assert_that(!a.equals(b));
        assert_that(!a.and(b).equals(b));

    }

    private static void assert_that(boolean condition) {
        if (condition) {
            System.out.println("\u001b[00;32mOK\u001b[00;00m");
        } else {
            System.out.println("\u001b[00;31mNG\u001b[00;00m");
        }
    }
}
