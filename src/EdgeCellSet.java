import java.util.TreeSet;

public class EdgeCellSet extends TreeSet<EdgeCell> {
    @Override
    public boolean add(EdgeCell edgeCell) {
        if (edgeCell == null) return false;
        super.add(edgeCell);
        return false;
    }

}
