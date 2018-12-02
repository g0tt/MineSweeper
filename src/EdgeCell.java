import jp.ne.kuramae.torix.lecture.ms.core.Cell;
import java.util.Objects;

public class EdgeCell extends Cell implements Comparable<EdgeCell> {
    private int x;
    private int y;
    public EdgeCell next;
    public EdgeCell prev;

    public EdgeCell(int x, int y) {
        super(x, y);
        this.x = x;
        this.y = y;
    }

    public EdgeCell(int x, int y, EdgeCell prev) {
        this(x, y);
        this.setPrev(prev);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeCell edgeCell = (EdgeCell) o;
        return x == edgeCell.x &&
                y == edgeCell.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public int compareTo(EdgeCell o) {
        if (this.getY() != o.getY()) {
            return this.getY() - o.getY();
        } else {
            return this.getX() - o.getX();
        }
    }

    @Override
    public String toString() {
        return  String.format("[ x: %d, y: %d ]", getX(), getY());
    }

    public EdgeCell(MineSweeperIterator here) {
        super(here.getX(), here.getY());
    }

    public void setNext(EdgeCell next) {
        if (this.next == null) {
            this.next = next;
            next.setPrev(this);
        }
    }

    public void setPrev(EdgeCell prev) {
        if (this.prev == null) {
            this.prev = prev;
            prev.setNext(this);
        }
    }

}
