import java.util.Arrays;
import java.util.Objects;

/**
 * 64bit整数までしか扱えないので，64 * n[bit]に拡張する
 */
public class BitMap {
    private boolean[] bits;

    public BitMap(int n, boolean[] bits) {
        this.bits = new boolean[n];
        System.arraycopy(bits, 0, this.bits, 0, n);
    }

    public BitMap(int n) {
        this.bits = new boolean[n];
        for (int i = 0; i < n; i++) {
            bits[i] = false;
        }
    }

    public int length() {
        return bits.length;
    }

    public BitMap setTrue(int n) {
        if (n >= length()) return null;
        bits[n] = true;
        return this;
    }

    public BitMap setFalse(int n) {
        if (n >= length()) return null;
        this.bits[n] = false;
        return this;
    }

    public boolean get(int n) {
        return bits[n];
    }

    public BitMap and(BitMap other) {
        int m = length() - 1;
        for (int i = 0; i <= m; i++) {
            bits[i] &= other.length() > i && other.bits[i];
        }
        return this;
    }

    public static BitMap and(BitMap a, BitMap b) {
        BitMap c = new BitMap(a.length(), a.bits);
        return c.and(b);
    }

    public BitMap or(BitMap other) {
        int m = length() - 1;
        for (int i = 0; i <= m; i++) {
            bits[i] |= other.length() > i && other.bits[i];
        }
        return this;
    }

    public static BitMap or(BitMap a, BitMap b) {
        BitMap c = new BitMap(a.length(), a.bits);
        return c.or(b);
    }

    public BitMap xor(BitMap other) {
        int m = length() - 1;
        for (int i = 0; i <= m; i++) {
            bits[i] ^= other.length() > i && other.bits[i];
        }
        return this;
    }

    public static BitMap xor(BitMap a, BitMap b) {
        BitMap c = new BitMap(a.length(), a.bits);
        return c.xor(b);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BitMap) {
            return length() == ((BitMap) other).length() && Arrays.equals(bits, ((BitMap) other).bits);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(bits));
    }

    public boolean isZero() {
        for (int i = 0; i < length(); i++) {
            if (bits[i]) return false;
        }
        return true;
    }

    public BitMap copy() {
        return new BitMap(length(), bits);
    }

    public void print() {
        for (int i = 0; i < length(); i++) {
            System.out.print(bits[i] ? "1" : "0");
        }
        System.out.print("\n");
    }

    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < length(); i++) {
            result += (bits[i] ? "1" : "0");
        }
        return result;
    }
}
