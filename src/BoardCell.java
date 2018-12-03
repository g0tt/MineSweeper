import java.util.Objects;

public class BoardCell implements Comparable<BoardCell> {
    protected enum CellType {
        Fixed,
        Safe,
        Edge,
        EdgeOpen,
        Open,
        NotOpen,
    }

    private CellType cellType;

    private int x;
    private int y;
    private Board board;
    private int num;
    private double prob;

    public BoardCell(int x, int y, Board board, int num) {
        this.x = x;
        this.y = y;
        this.board = board;
        this.num = num;
        switch(num) {
            case -1:
                this.cellType = CellType.NotOpen;
                break;
            default:
                this.cellType = CellType.Open;
                break;
        }
    }

    public int get() {
        return num;
    }

    public void set(int num) {
        if (num == -1) return;
        this.num = num;
        if (!this.isOpen()) this.cellType = CellType.Open;
    }

    public boolean fix() {
        if (isFixed() || isOpen()) return false;
        cellType = CellType.Fixed;
        prob = 100;
        return true;
    }

    public boolean setSafe() {
        if (isFixed() || isOpen()) return false;
        cellType = CellType.Safe;
        prob = 0;
        return true;
    }

    public boolean _setEdge() {
        if (isOpen()) return false;
        cellType = CellType.Edge;
        return true;
    }

    public boolean _setEdgeOpen() {
        if (!isOpen()) return false;
        cellType = CellType.EdgeOpen;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardCell boardCell = (BoardCell) o;
        return x == boardCell.x &&
                y == boardCell.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public int compareTo(BoardCell o) {
        if (this.y != o.y) {
            return this.y - o.y;
        } else {
            return this.x - o.x;
        }
    }

    @Override
    public String toString() {
        return String.format("[ x: %d, y: %d ]", x, y) + " " + toChar();
    }

    public char toChar() {
        switch (cellType) {
            case Fixed:
                return '*';
            case Safe:
                return 'o';
            case Open:
                return ' ';
            case Edge:
                return '/';
            case EdgeOpen:
                return String.valueOf(this.num).charAt(0);
            case NotOpen:
                return '.';
            default:
                return ' ';
        }
    }

    public BoardIterator getIterator(ProbPlayer player) {
        return new BoardIterator(x, y, board, player);
    }

    public boolean isFixed() {
        return cellType == CellType.Fixed;
    }

    public boolean isSafe() {
        return cellType == CellType.Safe;
    }

    public boolean isOpen() {
        return (cellType == CellType.Open || cellType == CellType.EdgeOpen);
    }

    public boolean isNotOpenNorFixed() {
        return (cellType != CellType.Open && cellType != CellType.Fixed);
    }

}
