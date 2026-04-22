import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.awt.geom.AffineTransform;
public class GraphPanel extends JPanel {
    private Map<String, List<DataPoint>> seriesData = new HashMap<>();
    private String title = "";
    private String xLabel = "";
    private String yLabel = "";
    private int padding = 60;
    private int labelPadding = 25;
    private Color[] lineColors = { Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.CYAN };
    public void setData(Map<String, List<DataPoint>> data, String title, String xLabel, String yLabel) {
        this.seriesData = data;
        this.title = title;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        repaint();
    }
    public void clear() {
        this.seriesData.clear();
        repaint();
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (seriesData.isEmpty()) {
            g2.drawString("No data to display", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (List<DataPoint> points : seriesData.values()) {
            for (DataPoint p : points) {
                if (p.getY() < minY)
                    minY = p.getY();
                if (p.getY() > maxY)
                    maxY = p.getY();
            }
        }
        if (maxY == minY)
            maxY = minY + 1;
        if (minY > 0)
            minY = 0;
        java.util.TreeSet<Double> uniqueXSet = new java.util.TreeSet<>();
        for (List<DataPoint> points : seriesData.values()) {
            for (DataPoint p : points) {
                uniqueXSet.add(p.getX());
            }
        }
        List<Double> uniqueXList = new ArrayList<>(uniqueXSet);
        int xCount = uniqueXList.size();
        boolean useDecimals = (maxY - minY) < 10;
        String yFormat = useDecimals ? "%.1f" : "%.0f";
        int maxLabelWidth = 0;
        int numberYDivisions = 10;
        for (int i = 0; i <= numberYDivisions; i++) {
            double val = minY + (maxY - minY) * ((double) i / numberYDivisions);
            String valStr = String.format(yFormat, val);
            int w = g2.getFontMetrics().stringWidth(valStr);
            if (w > maxLabelWidth)
                maxLabelWidth = w;
        }
        int actualLeftPadding = padding + maxLabelWidth + 10;
        double xStep = (double) (getWidth() - actualLeftPadding - padding) / (Math.max(1, xCount - 1));
        double yScale = (getHeight() - 2 * padding - labelPadding) / (maxY - minY);
        g2.drawLine(actualLeftPadding, getHeight() - padding - labelPadding, actualLeftPadding, padding);  
        g2.drawLine(actualLeftPadding, getHeight() - padding - labelPadding, getWidth() - padding,
                getHeight() - padding - labelPadding); 
        g2.drawString(title, getWidth() / 2 - g2.getFontMetrics().stringWidth(title) / 2, padding / 2);
        g2.drawString(xLabel, getWidth() / 2, getHeight() - padding + 15);
        AffineTransform original = g2.getTransform();
        g2.rotate(-Math.PI / 2);
        g2.drawString(yLabel, -getHeight() / 2 - g2.getFontMetrics().stringWidth(yLabel) / 2, padding / 2);
        g2.setTransform(original);
        for (int i = 0; i < numberYDivisions + 1; i++) {
            int x0 = actualLeftPadding;
            int x1 = getWidth() - padding;
            int y0 = getHeight()
                    - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
            g2.setColor(Color.LIGHT_GRAY);
            if (seriesData.size() > 0)
                g2.drawLine(x0 + 1, y0, x1, y0);
            g2.setColor(Color.BLACK);
            double val = minY + (maxY - minY) * ((double) i / numberYDivisions);
            String valStr = String.format(yFormat, val);
            int labelW = g2.getFontMetrics().stringWidth(valStr);
            g2.drawString(valStr, x0 - labelW - 5, y0 + 5);
        }
        int colorIdx = 0;
        int legendY = padding;
        for (Map.Entry<String, List<DataPoint>> entry : seriesData.entrySet()) {
            g2.setColor(lineColors[colorIdx % lineColors.length]);
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(2f));
            List<DataPoint> points = entry.getValue();
            List<Point> graphPoints = new ArrayList<>();
            for (DataPoint p : points) {
                int xIndex = uniqueXList.indexOf(p.getX());
                if (xIndex == -1)
                    continue; 
                int x1 = (int) (actualLeftPadding + xIndex * xStep);
                int y1 = (int) ((maxY - p.getY()) * yScale + padding);
                graphPoints.add(new Point(x1, y1));
            }
            for (int i = 0; i < graphPoints.size() - 1; i++) {
                g2.drawLine(graphPoints.get(i).x, graphPoints.get(i).y,
                        graphPoints.get(i + 1).x, graphPoints.get(i + 1).y);
            }
            for (Point p : graphPoints) {
                g2.fillOval(p.x - 4, p.y - 4, 8, 8);
            }
            g2.drawString(entry.getKey(), getWidth() - padding - 100, legendY);
            legendY += 20;
            g2.setStroke(oldStroke);
            colorIdx++;
        }
        g2.setColor(Color.BLACK);
        for (int i = 0; i < uniqueXList.size(); i++) {
            double xVal = uniqueXList.get(i);
            int xCoord = (int) (actualLeftPadding + i * xStep);
            g2.drawLine(xCoord, getHeight() - padding - labelPadding, xCoord, getHeight() - padding - labelPadding + 5);
            String lbl = String.format("%.0f", xVal);
            int labelWidth = g2.getFontMetrics().stringWidth(lbl);
            g2.drawString(lbl, xCoord - labelWidth / 2, getHeight() - padding - labelPadding + 20);
        }
    }
}