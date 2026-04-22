import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class CsvParser {
    public static Map<String, List<DataPoint>> parseCsv(String filePath) {
        Map<String, List<DataPoint>> data = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentSection = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                String[] parts = line.split(",");
                if (parts.length < 2)
                    continue;
                if (parts.length == 2) {
                    try {
                        String col1 = parts[0];
                        double val = Double.parseDouble(parts[1]);
                        String key = "Detailed";
                        double iter = 0;
                        if (col1.contains("_")) {
                            String[] sub = col1.split("_");
                            iter = Double.parseDouble(sub[0]);
                            key = "Detailed-" + sub[1];
                        } else {
                            iter = Double.parseDouble(col1);
                        }
                        data.computeIfAbsent(key, k -> new ArrayList<>()).add(new DataPoint(iter, val));
                    } catch (Exception e) {
                    }
                    continue;
                }
                if (parts.length < 3)
                    continue;
                String type = parts[0];
                if (type.equalsIgnoreCase("TestType") ||
                        type.equalsIgnoreCase("MatrixTest") ||
                        type.equals("Sequential") && parts[1].equals("BlockSize_KB") ||
                        type.equals("Random") && parts[1].equals("BlockSize_KB") ||
                        type.equals("Stride") && parts[1].equals("StrideBytes") ||
                        type.equals("Latency") && parts[1].equals("1") == false && parts[2].contains("Time")) {
                    continue;
                }
                if (type.equals("Matrix")) {
                    String order = parts[1];
                    String key = "Matrix-" + order;
                    try {
                        double size = Double.parseDouble(parts[2]);
                        double time = Double.parseDouble(parts[3]);
                        data.computeIfAbsent(key, k -> new ArrayList<>()).add(new DataPoint(size, time));
                    } catch (NumberFormatException e) {
                    }
                } else {
                    String key = type;
                    try {
                        if (Character.isDigit(parts[1].charAt(0))) {
                            double x = Double.parseDouble(parts[1]);
                            double y = Double.parseDouble(parts[2]);
                            data.computeIfAbsent(key, k -> new ArrayList<>()).add(new DataPoint(x, y));
                        }
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}