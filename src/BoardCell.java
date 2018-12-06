import java.util.Objects;

public class BoardCell implements Comparable<BoardCell> {
    protected enum CellType {
        Fixed,
        Safe,
        BoxEdge,
        NumEdge,
        Open,
        NotOpen,
    }

    private CellType cellType;

    public int x;
    public int y;
    private Board board;
    private int num;
    private double prob;

    public BoardCellSet relatedEdges;

    public BoardCell(int x, int y, Board board, int num) {
        this.x = x;
        this.y = y;
        this.board = board;
        this.num = num;
        this.relatedEdges = new BoardCellSet();
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
        this.cellType = CellType.Open;
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

    public boolean _setBoxEdge() {
        if (isOpen()) return false;
        cellType = CellType.BoxEdge;
        return true;
    }

    public boolean _setNumEdge() {
        if (!isOpen()) return false;
        cellType = CellType.NumEdge;
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
        return (!isBoxEdge() ? toChar() : "") + String.format(" [ x: %d, y: %d ]", x, y) + (isNumEdge() ? " rel: " + relatedEdges.toString() : "");
    }

    public char toChar() {
        switch (cellType) {
            case Fixed:
                return '*';
            case Safe:
                return 'o';
            case Open:
                return ' ';
            case BoxEdge:
                return '/';
            case NumEdge:
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
        return (cellType == CellType.Open || cellType == CellType.NumEdge);
    }

    public boolean isBoxEdge() { return cellType == CellType.BoxEdge; }

    public boolean isNumEdge() { return cellType == CellType.NumEdge; }

    public boolean isNotOpenNorFixed() {
        return (cellType != CellType.Open && cellType != CellType.Fixed && cellType != CellType.NumEdge);
    }

}
