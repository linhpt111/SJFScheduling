package org.cloudsimplus.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ChartPlotter for visualizing simulation results.
 *
 * Creates bar charts comparing different scheduling algorithms for:
 * - Average Waiting Time
 * - Average Response Time
 * - Makespan
 * - Throughput
 */
public class ChartPlotter {

    // Store results for each scenario (scenario name -> metrics)
    private final Map<String, SimulationResult> results;

    // Predefined chart bar colors
    private static final Color[] COLORS = {
            new Color(41, 128, 185),   // Blue
            new Color(39, 174, 96),    // Green
            new Color(231, 76, 60),    // Red
            new Color(243, 156, 18)    // Orange
    };

    public ChartPlotter() {
        this.results = new LinkedHashMap<>();
    }

    /**
     * Adds simulation result for a specific scenario.
     *
     * @param scenarioName    Name of the scenario (e.g., "AV Load Balancing")
     * @param avgWaitingTime  Average waiting time
     * @param avgResponseTime Average response time
     * @param makespan        Makespan value
     * @param throughput      Throughput value
     */
    public void addResult(String scenarioName, double avgWaitingTime, double avgResponseTime,
                          double makespan, double throughput) {
        results.put(scenarioName, new SimulationResult(avgWaitingTime, avgResponseTime, makespan, throughput));
    }

    /**
     * Creates and displays all comparison charts in a single window, and saves them as PNG files.
     */
    public void plotAllCharts() {
        if (results.isEmpty()) {
            System.out.println("No results to plot!");
            return;
        }

        // Create main frame with all charts - LARGER SIZE
        JFrame frame = new JFrame("Simulation Results Comparison");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(2, 2, 15, 15));  // More spacing

        // Add four charts (Waiting Time, Response Time, Makespan, Throughput)
        frame.add(createChartPanel(createWaitingTimeChart()));
        frame.add(createChartPanel(createResponseTimeChart()));
        frame.add(createChartPanel(createMakespanChart()));
        frame.add(createChartPanel(createThroughputChart()));

        // LARGER WINDOW SIZE for better visibility
        frame.setSize(1600, 1000);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Save charts as image files
        saveChartsToFiles();
    }

    /**
     * Creates a chart panel with the given chart.
     */
    private ChartPanel createChartPanel(JFreeChart chart) {
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(750, 450));  // Larger panels
        return panel;
    }

    /**
     * Creates bar chart for Average Waiting Time comparison.
     */
    private JFreeChart createWaitingTimeChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, SimulationResult> entry : results.entrySet()) {
            dataset.addValue(entry.getValue().avgWaitingTime, "Avg Waiting Time (s)", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Average Waiting Time Comparison",
                "Algorithm × Workload (Simple/Aging × W1/W2/W3/W4)",
                "Time (seconds)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart, 0, "s");
        return chart;
    }

    /**
     * Creates bar chart for Average Response Time comparison.
     */
    private JFreeChart createResponseTimeChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, SimulationResult> entry : results.entrySet()) {
            dataset.addValue(entry.getValue().avgResponseTime, "Avg Response Time (s)", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Average Response Time Comparison",
                "Algorithm × Workload (Simple/Aging × W1/W2/W3/W4)",
                "Time (seconds)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart, 1, "s");
        return chart;
    }

    /**
     * Creates bar chart for Makespan comparison.
     */
    private JFreeChart createMakespanChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, SimulationResult> entry : results.entrySet()) {
            dataset.addValue(entry.getValue().makespan, "Makespan (s)", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Makespan Comparison",
                "Algorithm × Workload (Simple/Aging × W1/W2/W3/W4)",
                "Time (seconds)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart, 2, "s");
        return chart;
    }

    /**
     * Creates bar chart for Throughput comparison.
     */
    private JFreeChart createThroughputChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, SimulationResult> entry : results.entrySet()) {
            dataset.addValue(entry.getValue().throughput, "Throughput (tasks/s)", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Throughput Comparison",
                "Algorithm × Workload (Simple/Aging × W1/W2/W3/W4)",
                "Tasks per Second",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart, 3, "tasks/s");
        return chart;
    }

    /**
     * Customizes chart appearance (colors, background, labels, etc.).
     * @param chart The chart to customize
     * @param colorIndex Index for color selection
     * @param unit Unit symbol to append to value labels (e.g., "s", "tasks/s")
     */
    private void customizeChart(JFreeChart chart, int colorIndex, String unit) {
        chart.setBackgroundPaint(Color.WHITE);

        // Title với font lớn hơn
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 18));
        chart.addSubtitle(new TextTitle("Simple vs Aging Algorithm | W1:Dynamic W2:Bursty W3:Heavy W4:Balanced",
                                        new Font("SansSerif", Font.PLAIN, 12)));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(250, 250, 250));
        plot.setDomainGridlinePaint(new Color(200, 200, 200));
        plot.setRangeGridlinePaint(new Color(200, 200, 200));
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        // Outline for better definition
        plot.setOutlineVisible(true);
        plot.setOutlinePaint(Color.GRAY);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setDrawBarOutline(true);
        renderer.setShadowVisible(false);

        // *** CÁC CỘT THON HƠN VÀ ĐẸP HƠN ***
        renderer.setMaximumBarWidth(0.12);  // Thon hơn (từ 0.15 → 0.12)
        renderer.setItemMargin(0.25);  // Khoảng cách nhiều hơn

        // Color cho bars
        renderer.setSeriesPaint(0, COLORS[colorIndex % COLORS.length]);
        renderer.setSeriesOutlinePaint(0, COLORS[colorIndex % COLORS.length].darker());

        // *** THÊM VALUE LABELS TRÊN CỘT VỚI ĐƠN VỊ ***
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(
            new org.jfree.chart.labels.StandardCategoryItemLabelGenerator(
                "{2} " + unit, new java.text.DecimalFormat("0.00")
            )
        );
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.BOLD, 10));
        renderer.setDefaultItemLabelPaint(new Color(50, 50, 50));
        renderer.setDefaultPositiveItemLabelPosition(
            new org.jfree.chart.labels.ItemLabelPosition(
                org.jfree.chart.labels.ItemLabelAnchor.OUTSIDE12,
                org.jfree.chart.ui.TextAnchor.BOTTOM_CENTER
            )
        );

        // Axis fonts lớn hơn
        plot.getDomainAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 13));
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        plot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 13));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));

        // Category labels KHÔNG nghiêng để dễ đọc
        plot.getDomainAxis().setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.STANDARD
        );

        // Thêm margin cho range axis để value labels không bị cắt
        plot.getRangeAxis().setUpperMargin(0.15);
    }

    /**
     * Saves all charts to PNG files in a directory 'simulation_results'.
     */
    private void saveChartsToFiles() {
        try {
            // Create output directory if not exists
            File outputDir = new File("simulation_results");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // Save each chart as PNG
            ChartUtils.saveChartAsPNG(
                    new File(outputDir, "waiting_time_comparison.png"),
                    createWaitingTimeChart(),
                    800, 600
            );

            ChartUtils.saveChartAsPNG(
                    new File(outputDir, "response_time_comparison.png"),
                    createResponseTimeChart(),
                    800, 600
            );

            ChartUtils.saveChartAsPNG(
                    new File(outputDir, "makespan_comparison.png"),
                    createMakespanChart(),
                    800, 600
            );

            ChartUtils.saveChartAsPNG(
                    new File(outputDir, "throughput_comparison.png"),
                    createThroughputChart(),
                    800, 600
            );

            // Also save a combined chart with all metrics side-by-side
            saveCombinedChart(outputDir);

            System.out.println("\n[ChartPlotter] Charts saved to 'simulation_results' folder");

        } catch (IOException e) {
            System.err.println("Error saving charts: " + e.getMessage());
        }
    }

    /**
     * Creates and saves a combined comparison chart for all metrics.
     */
    private void saveCombinedChart(File outputDir) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Determine max values to scale throughput for combined chart (for better visibility)
        double maxWT = 0, maxRT = 0, maxMS = 0, maxTP = 0;
        for (SimulationResult result : results.values()) {
            maxWT = Math.max(maxWT, result.avgWaitingTime);
            maxRT = Math.max(maxRT, result.avgResponseTime);
            maxMS = Math.max(maxMS, result.makespan);
            maxTP = Math.max(maxTP, result.throughput);
        }

        for (Map.Entry<String, SimulationResult> entry : results.entrySet()) {
            String name = entry.getKey();
            SimulationResult r = entry.getValue();

            dataset.addValue(r.avgWaitingTime, "Avg Waiting Time", name);
            dataset.addValue(r.avgResponseTime, "Avg Response Time", name);
            dataset.addValue(r.makespan, "Makespan", name);
            dataset.addValue(r.throughput * 10, "Throughput (x10)", name); // Scale throughput for visibility
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Overall Performance Comparison",
                "Scenario",
                "Value",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(245, 245, 245));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, COLORS[0]);
        renderer.setSeriesPaint(1, COLORS[1]);
        renderer.setSeriesPaint(2, COLORS[2]);
        renderer.setSeriesPaint(3, COLORS[3]);

        plot.getDomainAxis().setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.UP_45
        );

        ChartUtils.saveChartAsPNG(
                new File(outputDir, "combined_comparison.png"),
                chart,
                1200, 700
        );
    }

    /**
     * Prints a summary table of all results to the console.
     */
    public void printSummaryTable() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("SIMULATION RESULTS SUMMARY");
        System.out.println("=".repeat(100));
        System.out.printf("%-40s | %15s | %15s | %12s | %12s%n",
                "Scenario", "Avg Wait Time", "Avg Resp Time", "Makespan", "Throughput");
        System.out.println("-".repeat(100));

        for (Map.Entry<String, SimulationResult> entry : results.entrySet()) {
            SimulationResult r = entry.getValue();
            System.out.printf("%-40s | %15.4f | %15.4f | %12.4f | %12.4f%n",
                    entry.getKey(), r.avgWaitingTime, r.avgResponseTime, r.makespan, r.throughput);
        }

        System.out.println("=".repeat(100));

        // Identify best performing scenario for each metric
        findBestScenarios();
    }

    /**
     * Finds and prints the best performing scenario for each metric.
     */
    private void findBestScenarios() {
        String bestWT = "", bestRT = "", bestMS = "", bestTP = "";
        double minWT = Double.MAX_VALUE, minRT = Double.MAX_VALUE;
        double minMS = Double.MAX_VALUE, maxTP = 0;

        for (Map.Entry<String, SimulationResult> entry : results.entrySet()) {
            SimulationResult r = entry.getValue();

            if (r.avgWaitingTime < minWT) {
                minWT = r.avgWaitingTime;
                bestWT = entry.getKey();
            }
            if (r.avgResponseTime < minRT) {
                minRT = r.avgResponseTime;
                bestRT = entry.getKey();
            }
            if (r.makespan < minMS) {
                minMS = r.makespan;
                bestMS = entry.getKey();
            }
            if (r.throughput > maxTP) {
                maxTP = r.throughput;
                bestTP = entry.getKey();
            }
        }

        System.out.println("\nBEST PERFORMING SCENARIOS:");
        System.out.println("-".repeat(60));
        System.out.printf("  Lowest Avg Waiting Time:  %s (%.4f s)%n", bestWT, minWT);
        System.out.printf("  Lowest Avg Response Time: %s (%.4f s)%n", bestRT, minRT);
        System.out.printf("  Lowest Makespan:          %s (%.4f s)%n", bestMS, minMS);
        System.out.printf("  Highest Throughput:       %s (%.4f cloudlets/s)%n", bestTP, maxTP);
        System.out.println("=".repeat(100));
    }

    /**
     * Inner class to store simulation results for a scenario.
     */
    private static class SimulationResult {
        final double avgWaitingTime;
        final double avgResponseTime;
        final double makespan;
        final double throughput;

        SimulationResult(double avgWaitingTime, double avgResponseTime,
                         double makespan, double throughput) {
            this.avgWaitingTime = avgWaitingTime;
            this.avgResponseTime = avgResponseTime;
            this.makespan = makespan;
            this.throughput = throughput;
        }
    }
}
