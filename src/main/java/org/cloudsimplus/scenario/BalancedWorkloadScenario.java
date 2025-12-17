package org.cloudsimplus.scenario;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Balanced Workload Scenario - 60 cloudlets
 *
 * Đặc điểm:
 * - 20 short tasks (1-3s)
 * - 20 medium tasks (5-8s)
 * - 20 long tasks (12-18s)
 * - Phân bố đều, arrival từ từ
 *
 * Mục đích: Test performance với workload cân bằng
 */
public class BalancedWorkloadScenario {

    private static final int CLOUDLET_PES = 1;
    private static final long FILE_SIZE = 300;
    private static final long OUTPUT_SIZE = 300;
    private static final Random rand = new Random(42);

    public static List<Cloudlet> createCloudlets() {
        List<Cloudlet> cloudlets = new ArrayList<>();

        System.out.println("[BalancedScenario] Creating balanced workload - 60 cloudlets");

        // 20 short tasks (1-3s)
        System.out.println("  20 short tasks (1-3s) - arrive 0-4s");
        for (int i = 0; i < 20; i++) {
            long length = 1000 + rand.nextInt(2001);
            double arrivalTime = i * 0.2;

            Cloudlet cloudlet = new CloudletSimple(length, CLOUDLET_PES)
                    .setFileSize(FILE_SIZE)
                    .setOutputSize(OUTPUT_SIZE)
                    .setUtilizationModelCpu(new UtilizationModelFull());
            cloudlet.setSubmissionDelay(arrivalTime);
            cloudlets.add(cloudlet);
        }

        // 20 medium tasks (5-8s)
        System.out.println("  20 medium tasks (5-8s) - arrive 4-8s");
        for (int i = 0; i < 20; i++) {
            long length = 5000 + rand.nextInt(3001);
            double arrivalTime = 4.0 + (i * 0.2);

            Cloudlet cloudlet = new CloudletSimple(length, CLOUDLET_PES)
                    .setFileSize(FILE_SIZE)
                    .setOutputSize(OUTPUT_SIZE)
                    .setUtilizationModelCpu(new UtilizationModelFull());
            cloudlet.setSubmissionDelay(arrivalTime);
            cloudlets.add(cloudlet);
        }

        // 20 long tasks (12-18s)
        System.out.println("  20 long tasks (12-18s) - arrive 8-12s");
        for (int i = 0; i < 20; i++) {
            long length = 12000 + rand.nextInt(6001);
            double arrivalTime = 8.0 + (i * 0.2);

            Cloudlet cloudlet = new CloudletSimple(length, CLOUDLET_PES)
                    .setFileSize(FILE_SIZE)
                    .setOutputSize(OUTPUT_SIZE)
                    .setUtilizationModelCpu(new UtilizationModelFull());
            cloudlet.setSubmissionDelay(arrivalTime);
            cloudlets.add(cloudlet);
        }

        System.out.println("[BalancedScenario] Created 60 cloudlets (20+20+20 balanced)");

        return cloudlets;
    }
}

