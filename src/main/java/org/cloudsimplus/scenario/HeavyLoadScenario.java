package org.cloudsimplus.scenario;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Heavy Load Scenario - 150 cloudlets
 *
 * Đặc điểm:
 * - Scale lớn: 150 tasks
 * - 70% long tasks để test khả năng xử lý workload nặng
 * - Arrival đều đặn trong 20 giây
 *
 * Mục đích: Test performance với heavy, sustained load
 */
public class HeavyLoadScenario {

    private static final int CLOUDLET_PES = 1;
    private static final long FILE_SIZE = 300;
    private static final long OUTPUT_SIZE = 300;
    private static final Random rand = new Random(42);

    public static List<Cloudlet> createCloudlets() {
        List<Cloudlet> cloudlets = new ArrayList<>();

        System.out.println("[HeavyLoadScenario] Creating heavy workload - 150 cloudlets");

        // 105 long tasks (70%)
        System.out.println("  105 long tasks (15-25s) - arrive 0-15s");
        for (int i = 0; i < 105; i++) {
            long length = 15000 + rand.nextInt(10001);
            double arrivalTime = i * 0.143;

            Cloudlet cloudlet = new CloudletSimple(length, CLOUDLET_PES)
                    .setFileSize(FILE_SIZE)
                    .setOutputSize(OUTPUT_SIZE)
                    .setUtilizationModelCpu(new UtilizationModelFull());
            cloudlet.setSubmissionDelay(arrivalTime);
            cloudlets.add(cloudlet);
        }

        // 45 short tasks (30%)
        System.out.println("  45 short tasks (2-5s) - arrive 15-20s");
        for (int i = 0; i < 45; i++) {
            long length = 2000 + rand.nextInt(3001);
            double arrivalTime = 15.0 + (i * 0.111);

            Cloudlet cloudlet = new CloudletSimple(length, CLOUDLET_PES)
                    .setFileSize(FILE_SIZE)
                    .setOutputSize(OUTPUT_SIZE)
                    .setUtilizationModelCpu(new UtilizationModelFull());
            cloudlet.setSubmissionDelay(arrivalTime);
            cloudlets.add(cloudlet);
        }

        System.out.println("[HeavyLoadScenario] Created 150 cloudlets (70% long tasks)");

        return cloudlets;
    }
}

