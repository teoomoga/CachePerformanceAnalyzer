public class DataPoint {
    private double x;
    private double y;
    private String xLabel; 
    public DataPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public DataPoint(double x, double y, String xLabel) {
        this.x = x;
        this.y = y;
        this.xLabel = xLabel;
    }
    public double getX() { return x; }
    public double getY() { return y; }
    public String getXLabel() { return xLabel; }
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}