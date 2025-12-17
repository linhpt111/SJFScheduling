package org.cloudsimplus.scenario;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Bursty Workload Scenario - 80 cloudlets
 *
 * Đặc điểm:
 * - Burst 1: 30 short tasks đến cùng lúc (t=0-1s) → Test load balancing
 * - Burst 2: 20 long tasks đến sau (t=5-6s) → Test starvation prevention
 * - Burst 3: 30 medium tasks đến cuối (t=10-11s) → Test fairness
 *
 * Mục đích: Test khả năng xử lý burst traffic và prevent starvation
 */
public class BurstyWorkloadScenario {

    private static final int CLOUDLET_PES = 1;
    private static final long FILE_SIZE = 300;
    private static final long OUTPUT_SIZE = 300;
    private static final Random rand = new Random(42);

    /**
     * Creates 80 cloudlets với bursty arrival pattern
     */
    public static List<Cloudlet> createCloudlets() {
        List<Cloudlet> cloudlets = new ArrayList<>();
        int id = 0;

        System.out.println("[BurstyScenario] Creating bursty workload - 80 cloudlets");

        /* ===================================================================
           BURST 1: 30 SHORT tasks arrive TOGETHER (0-1s)
           Purpose: Test initial load balancing under pressure
           =================================================================== */
        System.out.println("  Burst 1: 30 short tasks (1-2s) arrive at t=0-1s");
        for (int i = 0; i < 30; i++) {
            long length = 1000 + rand.nextInt(1001);  // 1-2 seconds
            double arrivalTime = i * 0.033;  // Dense: 0, 0.033, 0.066, ... 1.0s

            Cloudlet cloudlet = new CloudletSimple(length, CLOUDLET_PES)
                    .setFileSize(FILE_SIZE)
                    .setOutputSize(OUTPUT_SIZE)
                    .setUtilizationModelCpu(new UtilizationModelFull());
            cloudlet.setSubmissionDelay(arrivalTime);
            cloudlets.add(cloudlet);
            id++;
        }

        /* ===================================================================
           BURST 2: 20 VERY LONG tasks arrive TOGETHER (5-6s)
           Purpose: Test if long tasks starve behind short tasks
           =================================================================== */
        System.out.println("  Burst 2: 20 very long tasks (25-35s) arrive at t=5-6s");
        for (int i = 0; i < 20; i++) {
            long length = 25000 + rand.nextInt(10001);  // 25-35 seconds (VERY LONG!)
            double arrivalTime = 5.0 + (i * 0.05);  // 5.0, 5.05, 5.1, ... 5.95s

            Cloudlet cloudlet = new CloudletSimple(length, CLOUDLET_PES)
                    .setFileSize(FILE_SIZE)
                    .setOutputSize(OUTPUT_SIZE)
                    .setUtilizationModelCpu(new UtilizationModelFull());
            cloudlet.setSubmissionDelay(arrivalTime);
            cloudlets.add(cloudlet);
            id++;
        }

        /* ===================================================================
           BURST 3: 30 MEDIUM tasks arrive TOGETHER (10-11s)
           Purpose: Test fairness when long tasks are still executing
           =================================================================== */
        System.out.println("  Burst 3: 30 medium tasks (5-8s) arrive at t=10-11s");
        for (int i = 0; i < 30; i++) {
            long length = 5000 + rand.nextInt(3001);  // 5-8 seconds
            double arrivalTime = 10.0 + (i * 0.033);  // 10.0, 10.033, ... 11.0s

            Cloudlet cloudlet = new CloudletSimple(length, CLOUDLET_PES)
                    .setFileSize(FILE_SIZE)
                    .setOutputSize(OUTPUT_SIZE)
                    .setUtilizationModelCpu(new UtilizationModelFull());
            cloudlet.setSubmissionDelay(arrivalTime);
            cloudlets.add(cloudlet);
            id++;
        }

        System.out.println("[BurstyScenario] Created 80 cloudlets in 3 bursts");
        System.out.println("  - Without aging: Long tasks STARVE (wait 100+ seconds)");
        System.out.println("  - With aging: Long tasks get priority -> Much better fairness");

        return cloudlets;
    }
}

