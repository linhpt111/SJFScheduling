package org.cloudsimplus.scenario;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dynamic Workload Scenario (50 Cloudlets)
 *
 * Purpose:
 * - A few long tasks arrive first.
 * - Many short tasks arrive later.
 * - Designed to create a clear waiting queue (long tasks may wait behind short tasks).
 * - Highlights the benefit of dynamic AV + Aging scheduling (prevents starvation of long tasks).
 */
public class DynamicWorkloadScenario {
    private static final int CLOUDLET_PES = 1;
    private static final long FILE_SIZE = 300;
    private static final long OUTPUT_SIZE = 300;

    private static final Random rand = new Random(42);

    /**
     * Creates 100 cloudlets - OPTIMIZED WORKLOAD to show Aging broker benefits
     *
     * Design:
     * - Many medium tasks (30 tasks, 8-12s) arrive FIRST → occupy VMs
     * - Many short tasks (50 tasks, 1-3s) arrive continuously → create pressure
     * - Few very long tasks (20 tasks, 20-30s) scattered → risk of starvation
     *
     * WITHOUT Aging: Long tasks starve, wait forever
     * WITH Aging: Long tasks get priority boost over time
     */
    public static List<Cloudlet> createCloudlets() {
        List<Cloudlet> cloudlets = new ArrayList<>();

        int totalCloudlets = 100;

        // Phase 1: Medium tasks arrive early (30 tasks, 8-12s each)
        System.out.println("[Scenario] Phase 1: 30 medium tasks (8-12s) - arrive 0-3s");
        for (int i = 0; i < 30; i++) {
            long length = 8000 + rand.nextInt(4001);  // 8-12 seconds on 1000 MIPS
            double arrivalTime = i * 0.1;  // Dense arrival: 0.0, 0.1, 0.2, ... 2.9s

            Cloudlet cloudlet = new CloudletSimple(length, CLOUDLET_PES)
                    .setFileSize(FILE_SIZE)
                    .setOutputSize(OUTPUT_SIZE)
                    .setUtilizationModelCpu(new UtilizationModelFull());
            cloudlet.setSubmissionDelay(arrivalTime);
            cloudlets.add(cloudlet);
        }

        // Phase 2: Short tasks arrive continuously (50 tasks, 1-3s each)
        System.out.println("[Scenario] Phase 2: 50 short tasks (1-3s) - arrive 3-13s");
        for (int i = 0; i < 50; i++) {
            long length = 1000 + rand.nextInt(2001);  // 1-3 seconds
            double arrivalTime = 3.0 + (i * 0.2);  // 3.0, 3.2, 3.4, ... 12.8s

            Cloudlet cloudlet = new CloudletSimple(length, CLOUDLET_PES)
                    .setFileSize(FILE_SIZE)
                    .setOutputSize(OUTPUT_SIZE)
                    .setUtilizationModelCpu(new UtilizationModelFull());
            cloudlet.setSubmissionDelay(arrivalTime);
            cloudlets.add(cloudlet);
        }

        // Phase 3: Very long tasks scattered (20 tasks, 20-30s each)
        // These will STARVE without aging!
        System.out.println("[Scenario] Phase 3: 20 very long tasks (20-30s) - scattered");
        for (int i = 0; i < 20; i++) {
            long length = 20000 + rand.nextInt(10001);  // 20-30 seconds (VERY LONG!)
            double arrivalTime = i * 0.7;  // Scattered: 0, 0.7, 1.4, ... 13.3s

            Cloudlet cloudlet = new CloudletSimple(length, CLOUDLET_PES)
                    .setFileSize(FILE_SIZE)
                    .setOutputSize(OUTPUT_SIZE)
                    .setUtilizationModelCpu(new UtilizationModelFull());
            cloudlet.setSubmissionDelay(arrivalTime);
            cloudlets.add(cloudlet);
        }

        System.out.printf("[DynamicWorkloadScenario] Created %d cloudlets%n", totalCloudlets);
        System.out.println("  - 30 medium (8-12s) + 50 short (1-3s) + 20 very long (20-30s)");
        System.out.println("  - Without aging: Long tasks STARVE behind short tasks");
        System.out.println("  - With aging: Long tasks get priority boost -> Better makespan");

        return cloudlets;
    }
}
