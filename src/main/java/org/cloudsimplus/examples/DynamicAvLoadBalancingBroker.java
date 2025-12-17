package org.cloudsimplus.examples;

import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.core.CloudSimPlus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dynamic Load Balancing Broker - SIMPLE VERSION
 *
 * - Dynamic AV calculation
 * - Load balancing across ALL VMs
 * - Simple scoring: exec time + load penalty
 */
public class DynamicAvLoadBalancingBroker extends DatacenterBrokerSimple {

    /** Track number of cloudlets assigned to each VM (CloudSim doesn't update waiting list immediately) */
    private final Map<Long, Integer> vmAssignmentCount = new HashMap<>();

    public DynamicAvLoadBalancingBroker(CloudSimPlus simulation) {
        super(simulation);
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

        // Initialize assignment count for all VMs
        for (Vm vm : vms) {
            vmAssignmentCount.putIfAbsent(vm.getId(), 0);
        }

        // Calculate dynamic AV
        double av = getCloudletSubmittedList().stream()
                .filter(c -> c.getStatus() != Cloudlet.Status.SUCCESS)
                .mapToDouble(Cloudlet::getLength)
                .average()
                .orElse(cloudlet.getLength());

        // Select VM with load balancing
        Vm selectedVm = selectVmWithLoadBalancing(cloudlet, vms, av);

        // Update assignment count for selected VM
        if (selectedVm != Vm.NULL) {
            vmAssignmentCount.put(selectedVm.getId(), vmAssignmentCount.get(selectedVm.getId()) + 1);

            // Debug logging - Only every 25 cloudlets to reduce noise
            if (cloudlet.getId() % 25 == 0) {
                int assignedCount = vmAssignmentCount.get(selectedVm.getId());
                System.out.printf("[Simple] C%d → VM%d (MIPS=%.0f, Assigned=%d)%n",
                        cloudlet.getId(), selectedVm.getId(), selectedVm.getMips(), assignedCount);
            }
        }

        return selectedVm;
    }

    // Print distribution summary
    private void printDistributionSummary() {
        System.out.println("\n[Simple] Distribution Summary:");
        for (Map.Entry<Long, Integer> entry : vmAssignmentCount.entrySet()) {
            System.out.printf("  VM%d: %d tasks%n", entry.getKey(), entry.getValue());
        }
    }

    private Vm selectVmWithLoadBalancing(Cloudlet cloudlet, List<Vm> vms, double av) {
        Vm bestVm = Vm.NULL;
        double bestScore = Double.MAX_VALUE;

        for (Vm vm : vms) {
            // Use our tracked assignment count (more accurate than CloudSim's waiting list)
            int assignedCount = vmAssignmentCount.getOrDefault(vm.getId(), 0);

            // Execution time
            double execTime = cloudlet.getLength() / vm.getMips();

            // HEAVY load penalty (NAIVE approach - over-balance)
            // This causes poor utilization of fast VMs
            double loadPenalty = assignedCount * 6.0;  // INCREASED to 6.0 (naive balancing)

            // AV-based bias (SJF logic)
            double avBias = (cloudlet.getLength() < av) ? -0.5 : 0.5;

            // Simple score: exec time + linear load + AV bias
            double score = execTime + loadPenalty + avBias;

            if (score < bestScore) {
                bestScore = score;
                bestVm = vm;
            }
        }

        // Debug logging every 10 cloudlets
        if (cloudlet.getId() % 10 == 0) {
            int assignedCount = vmAssignmentCount.getOrDefault(bestVm.getId(), 0);
            System.out.printf("[Simple] Cloudlet %d → VM %d (MIPS=%.0f, Assigned=%d, Score=%.2f)%n",
                    cloudlet.getId(), bestVm.getId(), bestVm.getMips(), assignedCount, bestScore);
        }

        return bestVm;
    }
}