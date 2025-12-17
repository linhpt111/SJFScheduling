package org.cloudsimplus.metrics;

import org.cloudsimplus.cloudlets.Cloudlet;
import java.util.stream.Collectors;
import java.util.List;

/**
 * Metrics Collector for simulation results.
 *
 * Collects and calculates:
 * - Waiting Time (WT): execStartTime - arrivalTime
 * - Response Time (RT): finishTime - arrivalTime
 * - Makespan: max(finishTime) - min(arrivalTime)
 * - Throughput: number of completed cloudlets / makespan
 */
public class MetricsCollector {
    /* ===================== AVERAGE METRICS ===================== */
    private double averageWaitingTime;
    private double averageResponseTime;
    private double makespan;
    private double throughput;

    /* ===================== COUNT ===================== */
    private int totalCloudlets;

    /* ===================== MIN / MAX ===================== */
    private double minWaitingTime;
    private double maxWaitingTime;
    private double minResponseTime;
    private double maxResponseTime;

    public MetricsCollector() {
        reset();
    }

    /** Reset all metrics before a new simulation run */
    private void reset() {
        averageWaitingTime = 0.0;
        averageResponseTime = 0.0;
        makespan = 0.0;
        throughput = 0.0;
        totalCloudlets = 0;

        minWaitingTime = Double.MAX_VALUE;
        maxWaitingTime = 0.0;
        minResponseTime = Double.MAX_VALUE;
        maxResponseTime = 0.0;
    }

    /**
     * Collects metrics from finished cloudlets.
     *
     * @param cloudlets list of cloudlets returned by broker.getCloudletFinishedList()
     */
    public void collectMetrics(List<Cloudlet> cloudlets) {
        reset();

        if (cloudlets == null || cloudlets.isEmpty()) {
            System.out.println("[MetricsCollector] No cloudlets to analyze.");
            return;
        }

        // Only consider successfully finished cloudlets
        List<Cloudlet> finished = cloudlets.stream()
                .filter(c -> c.getStatus() == Cloudlet.Status.SUCCESS)
                .collect(Collectors.toList());

        if (finished.isEmpty()) {
            System.out.println("[MetricsCollector] No successfully finished cloudlets.");
            return;
        }

        totalCloudlets = finished.size();

        double totalWT = 0.0;
        double totalRT = 0.0;

        double minArrivalTime = Double.MAX_VALUE;
        double maxFinishTime = 0.0;

        for (Cloudlet cl : finished) {
            double arrivalTime = cl.getSubmissionDelay();
            double startTime   = cl.getStartTime();
            double finishTime  = cl.getFinishTime();

            // Waiting Time (queueing delay)
            double waitingTime = Math.max(0.0, startTime - arrivalTime);

            // Response Time (turnaround time)
            double responseTime = Math.max(0.0, finishTime - arrivalTime);

            totalWT += waitingTime;
            totalRT += responseTime;

            minWaitingTime = Math.min(minWaitingTime, waitingTime);
            maxWaitingTime = Math.max(maxWaitingTime, waitingTime);
            minResponseTime = Math.min(minResponseTime, responseTime);
            maxResponseTime = Math.max(maxResponseTime, responseTime);

            minArrivalTime = Math.min(minArrivalTime, arrivalTime);
            maxFinishTime  = Math.max(maxFinishTime, finishTime);
        }

        /* ===================== FINAL METRICS ===================== */
        averageWaitingTime  = totalWT / totalCloudlets;
        averageResponseTime = totalRT / totalCloudlets;

        // ✅ Makespan definition: time from first arrival to last finish
        makespan = maxFinishTime - minArrivalTime;

        // Throughput (cloudlets per second)
        if (makespan > 0) {
            throughput = totalCloudlets / makespan;
        }

        // Safety for empty cases
        if (minWaitingTime == Double.MAX_VALUE)  minWaitingTime = 0.0;
        if (minResponseTime == Double.MAX_VALUE) minResponseTime = 0.0;
    }

    /* ===================== PRINT ===================== */
    public void printMetrics() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("PERFORMANCE METRICS");
        System.out.println("=".repeat(50));

        System.out.printf("Total Cloudlets     : %d%n", totalCloudlets);
        System.out.println();

        System.out.printf("Avg Waiting Time    : %.4f s%n", averageWaitingTime);
        System.out.printf("  Min WT / Max WT   : %.4f / %.4f%n", minWaitingTime, maxWaitingTime);
        System.out.println();

        System.out.printf("Avg Response Time   : %.4f s%n", averageResponseTime);
        System.out.printf("  Min RT / Max RT   : %.4f / %.4f%n", minResponseTime, maxResponseTime);
        System.out.println();

        System.out.printf("Makespan            : %.4f s%n", makespan);
        System.out.printf("Throughput          : %.4f cloudlets/s%n", throughput);

        System.out.println("=".repeat(50));
    }

    /* ===================== GETTERS ===================== */
    public double getAverageWaitingTime()  { return averageWaitingTime; }
    public double getAverageResponseTime() { return averageResponseTime; }
    public double getMakespan()            { return makespan; }
    public double getThroughput()          { return throughput; }

    public int getTotalCloudlets()         { return totalCloudlets; }

    public double getMinWaitingTime()      { return minWaitingTime; }
    public double getMaxWaitingTime()      { return maxWaitingTime; }
    public double getMinResponseTime()     { return minResponseTime; }
    public double getMaxResponseTime()     { return maxResponseTime; }
}
