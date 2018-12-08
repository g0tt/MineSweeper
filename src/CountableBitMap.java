import java.util.Arrays;
import java.util.Objects;

/**
 * 確率的ビットマップ
 */
public class CountableBitMap {

    private int[] positive_counts;
    private int[] negative_counts;

    public static void main(String[] args) {
        int[] a = new int[1];
        a[0] = 1;
        int[] b = new int[1];
        b[0] = 0;
        CountableBitMap a1 = new CountableBitMap(a, b);
        CountableBitMap a2 = a1.copy();
        CountableBitMap a3 = a1.copy();

        CountableBitMap b1 = new CountableBitMap(b, a);
        CountableBitMap b2 = b1.copy();
        CountableBitMap b3 = b1.copy();

        System.out.println(a1.merge(a2).merge(a3).merge(b1).merge(b2).merge(b3).getProb(0));
    }

    public CountableBitMap(boolean[] bits) {
        int n = bits.length;
        this.positive_counts = new int[n];
        this.negative_counts = new int[n];
        for (int i = 0; i < n; i++) {
            if (bits[i]) {
                this.positive_counts[i]++;
            } else {
                this.negative_counts[i]++;
            }
        }
    }

    public CountableBitMap(int[] positive_counts, int[] negative_counts) {
        this.positive_counts = positive_counts.clone();
        this.negative_counts = negative_counts.clone();
    }

    public CountableBitMap(int n) {
        this.positive_counts = new int[n];
        this.negative_counts = new int[n];
        for (int i = 0; i < n; i++) {
            this.positive_counts[i] = 0;
            this.negative_counts[i] = 0;
        }
    }

    public static CountableBitMap from(BitMap bitmap) {
        return new CountableBitMap(bitmap.bits);
    }

    public static CountableBitMap from(BitMap bitmap, BitMap mask) {
        int[] positive_counts = new int[bitmap.length()];
        int[] negative_counts = new int[bitmap.length()];
        for (int i = 0; i < bitmap.length(); i++) {
            if (mask.get(i)) {
                if (bitmap.get(i)) positive_counts[i]++;
                if (!bitmap.get(i)) negative_counts[i]++;
            }
        }
        return new CountableBitMap(positive_counts, negative_counts);
    }

    // TODO このへん全部直す
    public CountableBitMap merge(CountableBitMap other) {
        if (length() != other.length()) return null;
        for (int i = 0; i < length(); i++) {
            positive_counts[i] += other.positive_counts[i];
            negative_counts[i] += other.negative_counts[i];
        }
        return this;
    }

    public static CountableBitMap merge(CountableBitMap a, CountableBitMap b) {
        return a.copy().merge(b);
    }

    public double getProb(int n) {
        return (double)(positive_counts[n]) / (positive_counts[n] + negative_counts[n]);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof CountableBitMap) {
            return length() == ((CountableBitMap) other).length()
                    && Arrays.equals(positive_counts, ((CountableBitMap) other).positive_counts)
                    && Arrays.equals(negative_counts, ((CountableBitMap) other).negative_counts);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(positive_counts), Arrays.hashCode(negative_counts));
    }

    public int length() {
        return positive_counts.length;
    }

    public CountableBitMap copy() {
        return new CountableBitMap(positive_counts, negative_counts);
    }

    @Override
    public String toString() {
        return Arrays.toString(positive_counts);
    }
}
