import jp.ne.kuramae.torix.lecture.ms.core.Cell;

public class BoardCell {
    protected enum CellType {
        Fixed,
        Safe,
        Edge,
        EdgeOpen,
        Open,
        NotOpen,
    }

    private CellType cellType;

    private int num;
    private double prob;

    public BoardCell(int num) {
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

    public boolean setEdge() {
        if (isOpen()) return false;
        cellType = CellType.Edge;
        return true;
    }

    public String toString() {
        switch (cellType) {
            case Fixed:
                return "*";
            case Safe:
                return "o";
            case Open:
                return String.valueOf(this.num);
            case Edge:
                return "/";
            case EdgeOpen:
                return String.valueOf(this.num);
            case NotOpen:
                return ".";
            default:
                return " ";
        }
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
