import java.util.TreeSet;

public class BoardCellSet extends TreeSet<BoardCell> {
    @Override
    public boolean add(BoardCell boardCell) {
        if (boardCell == null) return false;
        super.add(boardCell);
        return false;
    }
}
