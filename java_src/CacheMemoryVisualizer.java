import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class CacheMemoryVisualizer extends JFrame {
    private GraphPanel graphPanel;
    private JTable resultsTable;
    private javax.swing.table.DefaultTableModel tableModel;
    private Map<String, List<DataPoint>> allData;
    private JSplitPane splitPane;
    private JTextArea consoleArea;
    private JTabbedPane tabbedPane;
    private JButton runBtn;
    private JButton runAllBtn;
    private String currentTest = "Latency";

    private void loadData() {
        allData = CsvParser.parseCsv("results.csv");
    }

    public CacheMemoryVisualizer() {
        setTitle("Cache Memory Performance Visualizer");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        loadData();
        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        runAllBtn = new JButton("RUN ALL TESTS");
        runAllBtn.setBackground(new Color(220, 220, 255));
        runAllBtn.setOpaque(true);
        runAllBtn.setBorderPainted(false);
        runAllBtn.addActionListener(e -> runAllTests());
        buttonPanel.add(runAllBtn);

        JButton sysInfoBtn = new JButton("System Info");
        sysInfoBtn.addActionListener(e -> showSystemInfo());
        buttonPanel.add(sysInfoBtn);

        buttonPanel.add(new JSeparator());
        addButton(buttonPanel, "Latency");
        addButton(buttonPanel, "Bandwidth");
        addButton(buttonPanel, "Sequential");
        addButton(buttonPanel, "Random");
        addButton(buttonPanel, "Stride");
        addButton(buttonPanel, "Matrix");
        addDetailedTestControls(buttonPanel);
        add(buttonPanel, BorderLayout.WEST);
        graphPanel = new GraphPanel();
        graphPanel.setBackground(Color.WHITE);
        tableModel = new javax.swing.table.DefaultTableModel();
        resultsTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(resultsTable);
        JTextArea consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane consoleScrollPane = new JScrollPane(consoleArea);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Results Table", tableScrollPane);
        tabbedPane.addTab("Console Output", consoleScrollPane);
        tabbedPane.setPreferredSize(new Dimension(0, 200));
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, graphPanel, tabbedPane);
        splitPane.setResizeWeight(0.7);
        add(splitPane, BorderLayout.CENTER);
        this.consoleArea = consoleArea;
        this.tabbedPane = tabbedPane;
        updateGraph(currentTest);
    }

    private void addButton(JPanel panel, String testName) {
        JButton btn = new JButton(testName);
        btn.addActionListener((ActionEvent e) -> {
            updateGraph(testName);
        });
        panel.add(btn);
    }

    private void addDetailedTestControls(JPanel panel) {
        panel.add(new JSeparator());
        panel.add(new JLabel("Detailed Run:"));
        String[] tests = { "latency", "bandwidth", "matrix", "sequential", "random", "stride" };
        JComboBox<String> testSelector = new JComboBox<>(tests);
        panel.add(testSelector);
        panel.add(new JLabel("Size (KB/N):"));
        String[] sizes = { "1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192" };
        JComboBox<String> sizeSelector = new JComboBox<>(sizes);
        sizeSelector.setEditable(true);
        sizeSelector.setSelectedItem("64");
        panel.add(sizeSelector);
        panel.add(new JLabel("Iterations:"));
        JTextField iterationsInput = new JTextField("5");
        panel.add(iterationsInput);
        runBtn = new JButton("Run Specific");
        runBtn.addActionListener(e -> {
            String selectedTest = (String) testSelector.getSelectedItem();
            String sizeStr = (String) sizeSelector.getSelectedItem();
            String iterStr = iterationsInput.getText();
            runDetailedTest(selectedTest, sizeStr, iterStr);
        });
        panel.add(runBtn);
    }

    private void showSystemInfo() {
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("sysctl", "-a");
                Process p = pb.start();
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(p.getInputStream()));
                String line;
                String cpu = "Unknown";
                String ram = "Unknown";
                String l1 = "Unknown";
                String l2 = "Unknown";
                String l3 = "Unknown";

                while ((line = reader.readLine()) != null) {
                    if (line.contains("machdep.cpu.brand_string")) {
                        cpu = line.split(": ")[1];
                    } else if (line.startsWith("hw.memsize:")) {
                        long bytes = Long.parseLong(line.split(": ")[1].trim());
                        double gb = bytes / (1024.0 * 1024 * 1024);
                        ram = String.format("%.2f GB", gb);
                    } else if (line.contains("hw.l1dcachesize")) {
                        long bytes = Long.parseLong(line.split(": ")[1].trim());
                        l1 = (bytes / 1024) + " KB";
                    } else if (line.contains("hw.l2cachesize")) {
                        long bytes = Long.parseLong(line.split(": ")[1].trim());
                        l2 = (bytes / 1024) + " KB";
                    } else if (line.contains("hw.l3cachesize")) {
                        long bytes = Long.parseLong(line.split(": ")[1].trim());
                        l3 = (bytes / (1024 * 1024)) + " MB";
                    }
                }

                String info = "System Hardware Information:\n\n" +
                        "CPU: " + cpu + "\n" +
                        "RAM: " + ram + "\n" +
                        "L1 Data Cache: " + l1 + "\n" +
                        "L2 Cache: " + l2 + "\n" +
                        "L3 Cache: " + l3;

                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, info, "Hardware Info", JOptionPane.INFORMATION_MESSAGE);
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Error fetching system info: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void runAllTests() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Run Full Benchmark Suite?\nThis will overwrite results.csv and may take a few minutes.",
                "Confirm Run All", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;
        consoleArea.setText("");
        tabbedPane.setSelectedIndex(1);
        runAllBtn.setEnabled(false);
        runBtn.setEnabled(false);
        new Thread(() -> {
            try {
                appendConsole("Starting FULL BENCHMARK SUITE...");
                ProcessBuilder pb = new ProcessBuilder("./memtest");
                pb.redirectErrorStream(true);
                Process p = pb.start();
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(p.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    appendConsole(line);
                    System.out.println(line);
                }
                int exitCode = p.waitFor();
                SwingUtilities.invokeLater(() -> {
                    if (exitCode == 0) {
                        appendConsole("Full Suite Completed.");
                        loadData();
                        updateGraph(currentTest);
                        JOptionPane.showMessageDialog(this, "All Tests Completed & Graphs Updated!");
                    } else {
                        appendConsole("Error: Exit code " + exitCode);
                    }
                    runAllBtn.setEnabled(true);
                    runBtn.setEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendConsole("Error: " + ex.getMessage());
                    runAllBtn.setEnabled(true);
                    runBtn.setEnabled(true);
                });
            }
        }).start();
    }

    private void runDetailedTest(String testName, String sizeStr, String iterStr) {
        consoleArea.setText("");
        tabbedPane.setSelectedIndex(1);
        runBtn.setEnabled(false);
        runAllBtn.setEnabled(false);
        new Thread(() -> {
            try {
                Integer.parseInt(sizeStr.trim());
                Integer.parseInt(iterStr.trim());
                appendConsole("Starting " + testName + " test...");
                appendConsole("Size: " + sizeStr + ", Iterations: " + iterStr);
                ProcessBuilder pb = new ProcessBuilder("./memtest", testName, sizeStr, "detailed_results.csv", iterStr);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(p.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    appendConsole(line);
                    System.out.println(line);
                }
                int exitCode = p.waitFor();
                SwingUtilities.invokeLater(() -> {
                    if (exitCode == 0) {
                        appendConsole("Test completed successfully.");
                        Map<String, List<DataPoint>> detailedData = CsvParser.parseCsv("detailed_results.csv");
                        showDetailedGraph(testName + " @ " + sizeStr + " (" + iterStr + " iters)", detailedData);
                    } else {
                        appendConsole("Error: Test exited with code " + exitCode);
                        JOptionPane.showMessageDialog(this, "Error running test. Exit code: " + exitCode);
                    }
                    runBtn.setEnabled(true);
                    runAllBtn.setEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendConsole("Exception: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                    runBtn.setEnabled(true);
                    runAllBtn.setEnabled(true);
                });
            }
        }).start();
    }

    private void appendConsole(String text) {
        SwingUtilities.invokeLater(() -> {
            consoleArea.append(text + "\n");
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }

    private void showDetailedGraph(String title, Map<String, List<DataPoint>> data) {
        JFrame frame = new JFrame("Detailed Results: " + title);
        frame.setSize(800, 600);
        GraphPanel gp = new GraphPanel();
        gp.setBackground(Color.WHITE);
        gp.setData(data, title, "Iteration / Repetition", "measured Value");
        frame.add(gp);
        frame.setVisible(true);
    }

    private void updateGraph(String testName) {
        this.currentTest = testName;
        Map<String, List<DataPoint>> toPlot = new HashMap<>();
        String xLabel = "Block Size (KB)";
        String yLabel = "Access Time (ns)";
        String title = testName + " Test Results";
        String col1 = xLabel;
        String col2 = yLabel;
        String col0 = "";
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        if (testName.equals("Matrix")) {
            xLabel = "Matrix Size";
            yLabel = "Time (ns)";
            col0 = "Order";
            col1 = xLabel;
            col2 = yLabel;
            tableModel.setColumnIdentifiers(new Object[] { col0, col1, col2 });
            for (String key : allData.keySet()) {
                if (key.startsWith("Matrix-")) {
                    toPlot.put(key, allData.get(key));
                    String order = key.replace("Matrix-", "");
                    for (DataPoint p : allData.get(key)) {
                        tableModel.addRow(new Object[] { order, p.getX(), p.getY() });
                    }
                }
            }
        } else {
            if (testName.equals("Bandwidth")) {
                yLabel = "Bandwidth (Bytes/s)";
            } else if (testName.equals("Stride")) {
                xLabel = "Stride (Bytes)";
            }
            col1 = xLabel;
            col2 = yLabel;
            tableModel.setColumnIdentifiers(new Object[] { col1, col2 });
            if (allData.containsKey(testName)) {
                toPlot.put(testName, allData.get(testName));
                for (DataPoint p : allData.get(testName)) {
                    tableModel.addRow(new Object[] { p.getX(), p.getY() });
                }
            }
        }
        graphPanel.setData(toPlot, title, xLabel, yLabel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CacheMemoryVisualizer().setVisible(true);
        });
    }
}