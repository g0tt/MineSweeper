import java.util.Arrays;
import java.util.Objects;

/**
 * 確率的ビットマップ
 */
public class QuantumBitMap {
    /**
     * 1である確率
     */
    private double[] probs;

    public static void main(String[] args) {
        QuantumBitMap a1 = new QuantumBitMap(1, 0.9);
        QuantumBitMap a2 = a1.copy();
        QuantumBitMap a3 = a1.copy();

        QuantumBitMap b1 = new QuantumBitMap(1, 0.1);
        QuantumBitMap b2 = b1.copy();
        QuantumBitMap b3 = b1.copy();

        System.out.println(a1.and(a2).and(a3).and(b1).and(b2).and(b3));
    }

    public QuantumBitMap(boolean[] bits) {
        int n = bits.length;
        this.probs = new double[n];
        for (int i = 0; i < n; i++) {
            this.probs[i] = bits[i] ? 1 : 0;
        }
    }

    public QuantumBitMap(double[] probs) {
        this.probs = probs;
    }

    public QuantumBitMap(int n, double default_probability) {
        this.probs = new double[n];
        for (int i = 0; i < n; i++) {
            this.probs[i] = default_probability;
        }
    }

    public static QuantumBitMap from(BitMap bitmap) {
        return new QuantumBitMap(bitmap.bits);
    }

    public static QuantumBitMap from(BitMap bitmap, BitMap mask, double default_probability) {
        double[] probs = new double[bitmap.length()];
        for (int i = 0; i < bitmap.length(); i++) {
            probs[i] = mask.get(i) ? (bitmap.bits[i] ? 1 : 0) : default_probability;
        }
        return new QuantumBitMap(probs);
    }

    public double getProb(int n) {
        return probs[n];
    }

    // TODO このへん全部直す
    public QuantumBitMap and(QuantumBitMap other) {
        if (length() != other.length()) return null;
        for (int i = 0; i < length(); i++) {
            if (probs[i] < 0) {
                probs[i] = other.probs[i];
            } else if (other.probs[i] < 0) {
                probs[i] = probs[i];
            } else {
                probs[i] = mean(probs[i], other.probs[i]);
            }
        }
        return this;
    }

    public static QuantumBitMap and(QuantumBitMap a, QuantumBitMap b) {
        return a.copy().and(b);
    }

    /*
    public QuantumBitMap xor(QuantumBitMap other) {
        if (length() != other.length()) return null;
        for (int i = 0; i < length(); i++) {
            bits[i] ^= other.length() > i && other.bits[i];
        }
        return this;
    }
    */

    @Override
    public boolean equals(Object other) {
        if (other instanceof QuantumBitMap) {
            return length() == ((QuantumBitMap) other).length()
                    && Arrays.equals(probs, ((QuantumBitMap) other).probs);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(probs));
    }

    public int length() {
        return probs.length;
    }

    public QuantumBitMap copy() {
        return new QuantumBitMap(probs);
    }

    /**
     * 確率の平均
     * 活性化関数を利用
     * @param a
     * @param b
     * @return
     */
    public static double mean(double a, double b) {
        return (activationFunction(a) + activationFunction(b)) / 2;
    }

    /**
     * 活性化関数
     * @param a [0, 1]
     * @return [0, 1]
     */
    public static double activationFunction(double a) {
        if (a < 0.5f) {
            return 2 * Math.pow(a, 2);
        } else {
            return (-2) * Math.pow((a - 1), 2) + 1;
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(probs);
    }
}
