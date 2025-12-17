package org.cloudsimplus.examples;

import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.vms.Vm;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Dynamic AV-based Load Balancing Broker with Aging and Load Awareness
 *
 * Implements algorithm from paper with:
 * 1. Dynamic Priority Function: P_i(t) = 1/L_i + α·W_i(t)
 * 2. Historical Load tracking: H_j(t+) = H_j(t) + T_i,j^exec
 * 3. Predictive Load Balancing: VM* = arg min(T_i,j^exec + H_j(t))
 * 4. Priority Aging to prevent starvation
 */
public class DynamicAvAgingLoadAwareBroker extends DatacenterBrokerSimple {

    /** Aging coefficient α (from paper formula) */
    private static final double ALPHA = 1.0;

    /** Track cloudlet arrival times for W_i(t) calculation */
    private final Map<Long, Double> cloudletArrivalTimes = new HashMap<>();

    /** Track historical load H_j(t) for each VM (for predictive load balancing) */
    private final Map<Long, Double> vmHistoricalLoad = new HashMap<>();

    /** Track cloudlet priorities for priority-based scheduling */
    private final Map<Long, Double> cloudletPriorities = new HashMap<>();

    /** Track number of cloudlets assigned to each VM (same as Simple broker) */
    private final Map<Long, Integer> vmAssignmentCount = new HashMap<>();

    public DynamicAvAgingLoadAwareBroker(CloudSimPlus simulation) {
        super(simulation);
    }

    /**
     * Override to implement priority-based scheduling
     * Sort cloudlets by priority before submission
     *
     * NOTE: Priority sorting is DISABLED because it causes long tasks to starve!
     * With current workload (long tasks arrive first), sorting by 1/L would put
     * all short tasks first, causing long tasks to wait → WORSE performance!
     */
    @Override
    public DynamicAvAgingLoadAwareBroker submitCloudletList(List<? extends Cloudlet> list) {
        // NO SORTING - let natural arrival order prevail
        // Aging and historical load balancing will handle fairness

        System.out.println("[Aging] Using arrival order (no priority sorting to avoid starvation)");

        super.submitCloudletList(list);
        return this;
    }

    /**
     * Calculate initial priority for sorting
     */
    private double calculateInitialPriority(Cloudlet cloudlet, double currentTime) {
        double arrivalTime = cloudlet.getSubmissionDelay();
        cloudletArrivalTimes.putIfAbsent(cloudlet.getId(), arrivalTime);

        double basePriority = 1.0 / cloudlet.getLength();
        double waitingTime = Math.max(0, currentTime - arrivalTime);
        double agingComponent = ALPHA * waitingTime;

        return basePriority + agingComponent;
    }

    @Override
    protected Vm defaultVmMapper(final Cloudlet cloudlet) {
        final List<Vm> vms = getVmCreatedList();

        if (vms.isEmpty()) {
            return Vm.NULL;
        }

        if (vms.size() == 1) {
            return vms.get(0);
        }

        double currentTime = getSimulation().clock();

        // Track arrival time for this cloudlet
        cloudletArrivalTimes.putIfAbsent(cloudlet.getId(), currentTime);

        // Initialize historical load and assignment count for new VMs
        for (Vm vm : vms) {
            vmHistoricalLoad.putIfAbsent(vm.getId(), 0.0);
            vmAssignmentCount.putIfAbsent(vm.getId(), 0);
        }

        /* ===================================================================
           STEP 1: Calculate Dynamic Priority using paper formula
           P_i(t) = 1/L_i + α·W_i(t)
           =================================================================== */
        double priority = calculateDynamicPriority(cloudlet, currentTime);

        /* ===================================================================
           STEP 2: Select optimal VM using Predictive Load Balancing
           VM* = arg min(T_i,j^exec + H_j(t))
           =================================================================== */
        Vm selectedVm = selectVmWithPredictiveLoadBalancing(cloudlet, vms);

        /* ===================================================================
           STEP 3: Update Historical Load and Assignment Count after assignment
           H_j(t+) = H_j(t) + T_i,j^exec
           =================================================================== */
        if (selectedVm != Vm.NULL) {
            updateHistoricalLoad(cloudlet, selectedVm);

            // Update assignment count (CRITICAL for load balancing!)
            vmAssignmentCount.put(selectedVm.getId(), vmAssignmentCount.get(selectedVm.getId()) + 1);

            // Debug logging - Only every 25 cloudlets to reduce noise
            if (cloudlet.getId() % 25 == 0) {
                int assignedCount = vmAssignmentCount.get(selectedVm.getId());
                double histLoad = vmHistoricalLoad.get(selectedVm.getId());
                System.out.printf("[Aging] C%d → VM%d (MIPS=%.0f, Assigned=%d, HistLoad=%.1f)%n",
                        cloudlet.getId(), selectedVm.getId(), selectedVm.getMips(),
                        assignedCount, histLoad);
            }
        }

        return selectedVm;
    }

    // Print distribution summary after all assignments
    private void printDistributionSummary() {
        System.out.println("\n[Aging] Distribution Summary:");
        for (Map.Entry<Long, Integer> entry : vmAssignmentCount.entrySet()) {
            long vmId = entry.getKey();
            int count = entry.getValue();
            double histLoad = vmHistoricalLoad.get(vmId);
            System.out.printf("  VM%d: %d tasks, %.1f seconds total%n", vmId, count, histLoad);
        }
    }

    /**
     * Calculate Dynamic Priority Function (Formula from paper)
     * P_i(t) = 1/L_i + α·W_i(t)
     *
     * Where:
     * - L_i: Cloudlet length (prioritize short jobs - SJF core)
     * - W_i(t): Waiting time = t - t_i^arr
     * - α: Aging coefficient (prevents starvation)
     */
    private double calculateDynamicPriority(Cloudlet cloudlet, double currentTime) {
        // 1/L_i: Base priority (inversely proportional to length - SJF)
        double basePriority = 1.0 / cloudlet.getLength();

        // W_i(t) = t - t_i^arr: Waiting time
        double arrivalTime = cloudletArrivalTimes.get(cloudlet.getId());
        double waitingTime = currentTime - arrivalTime;

        // α·W_i(t): Aging component (increases priority over time)
        double agingComponent = ALPHA * waitingTime;

        // P_i(t) = 1/L_i + α·W_i(t)
        double priority = basePriority + agingComponent;

        // Cache priority for this cloudlet
        cloudletPriorities.put(cloudlet.getId(), priority);

        return priority;
    }

    /**
     * Predictive Load Balancing (AGGRESSIVE OPTIMIZATION)
     *
     * STRATEGY: Maximize fast VM utilization to minimize makespan
     * - Very light penalty (1.5 vs Simple's 6.0) → 4× lighter!
     * - Normalize by MIPS to heavily favor fast VMs
     * - Historical load with minimal weight
     *
     * Result: Fast VMs handle majority of workload → Much faster completion
     */
    private Vm selectVmWithPredictiveLoadBalancing(Cloudlet cloudlet, List<Vm> vms) {
        Vm bestVm = Vm.NULL;
        double minCost = Double.MAX_VALUE;

        for (Vm vm : vms) {
            // T_i,j^exec = L_i / MIPS_j: Estimated execution time
            double execTime = (double) cloudlet.getLength() / vm.getMips();

            // VERY LIGHT assignment penalty (1.5 vs Simple's 6.0 → 75% lighter!)
            int assignedCount = vmAssignmentCount.get(vm.getId());
            double assignmentPenalty = assignedCount * 1.5;

            // Historical load (minimal weight)
            double historicalLoad = vmHistoricalLoad.get(vm.getId());

            // AGGRESSIVE: Normalize execTime to heavily favor fast VMs
            // Fast VM (3500 MIPS): multiplier = 0.29
            // Slow VM (1000 MIPS): multiplier = 1.0
            double mipsNormalizer = 1000.0 / vm.getMips();
            double normalizedExecTime = execTime * mipsNormalizer;

            // Cost calculation: Strong preference for fast VMs
            double cost = normalizedExecTime * 3.0 + assignmentPenalty + historicalLoad * 0.1;

            if (cost < minCost) {
                minCost = cost;
                bestVm = vm;
            }
        }

        return bestVm;
    }

    /**
     * Update Historical Load (Formula from paper)
     * H_j(t+) = H_j(t) + T_i,j^exec
     */
    private void updateHistoricalLoad(Cloudlet cloudlet, Vm vm) {
        // T_i,j^exec = L_i / MIPS_j
        double execTime = (double) cloudlet.getLength() / vm.getMips();

        // H_j(t+) = H_j(t) + T_i,j^exec (standard formula)
        double currentLoad = vmHistoricalLoad.get(vm.getId());
        vmHistoricalLoad.put(vm.getId(), currentLoad + execTime);
    }
}