package org.cloudsimplus;

import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import org.cloudsimplus.examples.DynamicAvAgingLoadAwareBroker;
import org.cloudsimplus.examples.DynamicAvLoadBalancingBroker;
import org.cloudsimplus.scenario.DynamicWorkloadScenario;
import org.cloudsimplus.scenario.BurstyWorkloadScenario;
import org.cloudsimplus.scenario.HeavyLoadScenario;
import org.cloudsimplus.scenario.BalancedWorkloadScenario;
import org.cloudsimplus.metrics.MetricsCollector;
import org.cloudsimplus.chart.ChartPlotter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Main entry point for CloudSim Plus Dynamic Scheduling Simulation.
 *
 * This simulation compares two dynamic SJF scheduling algorithms:
 * 1. DynamicAvLoadBalancingBroker - Basic AV-based load balancing
 * 2. DynamicAvAgingLoadAwareBroker - AV with aging and load awareness
 */
public class SimulationMain {

    private static final ChartPlotter chartPlotter = new ChartPlotter();

    // Datacenter configuration
    private static final int HOSTS = 2;
    private static final int HOST_PES = 4;
    private static final long HOST_MIPS = 10000;
    private static final long HOST_RAM = 16384;   // 16 GB
    private static final long HOST_BW = 10000;    // 10 Gbps
    private static final long HOST_STORAGE = 1000000; // 1 TB

    // VM configuration (heterogeneous) - 6 VMs for better load balancing
    private static final int VMS = 6;
    private static final int[] VM_MIPS = {1000, 1500, 2000, 2500, 3000, 3500}; // Wide range
    private static final int VM_PES = 2;
    private static final long VM_RAM = 2048;
    private static final long VM_BW = 1000;
    private static final long VM_SIZE = 10000;

    private final CloudSimPlus simulation;
    private final DatacenterBroker broker;
    private final List<Vm> vmList;
    private final List<Cloudlet> cloudletList;
    private final MetricsCollector metricsCollector;
    private final String scenarioLabel;

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("  CloudSim Plus - Algorithm Comparison on Multiple Workloads");
        System.out.println("  2 Algorithms × 4 Workload Scenarios = 8 Test Cases");
        System.out.println("=".repeat(70));

        // ===================================================================
        // WORKLOAD 1: Dynamic Mixed (100 cloudlets)
        // ===================================================================
        System.out.println("\n" + "█".repeat(70));
        System.out.println("█  WORKLOAD 1: DYNAMIC MIXED (100 cloudlets)");
        System.out.println("█  30 medium + 50 short + 20 very long tasks");
        System.out.println("█".repeat(70));

        runScenario("W1-Simple", DynamicAvLoadBalancingBroker::new,
                    () -> DynamicWorkloadScenario.createCloudlets());
        runScenario("W1-Aging", DynamicAvAgingLoadAwareBroker::new,
                    () -> DynamicWorkloadScenario.createCloudlets());

        // ===================================================================
        // WORKLOAD 2: Bursty Traffic (80 cloudlets)
        // ===================================================================
        System.out.println("\n" + "█".repeat(70));
        System.out.println("█  WORKLOAD 2: BURSTY TRAFFIC (80 cloudlets)");
        System.out.println("█  3 bursts: 30 short → 20 very long → 30 medium");
        System.out.println("█".repeat(70));

        runScenario("W2-Simple", DynamicAvLoadBalancingBroker::new,
                    () -> BurstyWorkloadScenario.createCloudlets());
        runScenario("W2-Aging", DynamicAvAgingLoadAwareBroker::new,
                    () -> BurstyWorkloadScenario.createCloudlets());

        // ===================================================================
        // WORKLOAD 3: Heavy Load (150 cloudlets)
        // ===================================================================
        System.out.println("\n" + "█".repeat(70));
        System.out.println("█  WORKLOAD 3: HEAVY LOAD (150 cloudlets)");
        System.out.println("█  105 long + 45 short tasks (sustained heavy load)");
        System.out.println("█".repeat(70));

        runScenario("W3-Simple", DynamicAvLoadBalancingBroker::new,
                    () -> HeavyLoadScenario.createCloudlets());
        runScenario("W3-Aging", DynamicAvAgingLoadAwareBroker::new,
                    () -> HeavyLoadScenario.createCloudlets());

        // ===================================================================
        // WORKLOAD 4: Balanced (60 cloudlets)
        // ===================================================================
        System.out.println("\n" + "█".repeat(70));
        System.out.println("█  WORKLOAD 4: BALANCED (60 cloudlets)");
        System.out.println("█  20 short + 20 medium + 20 long (evenly distributed)");
        System.out.println("█".repeat(70));

        runScenario("W4-Simple", DynamicAvLoadBalancingBroker::new,
                    () -> BalancedWorkloadScenario.createCloudlets());
        runScenario("W4-Aging", DynamicAvAgingLoadAwareBroker::new,
                    () -> BalancedWorkloadScenario.createCloudlets());

        // ===================================================================
        // Summary & Charts
        // ===================================================================
        System.out.println("\n" + "=".repeat(70));
        System.out.println(">>> GENERATING COMPARISON CHARTS <<<");
        System.out.println("=".repeat(70));

        chartPlotter.printSummaryTable();
        chartPlotter.plotAllCharts();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("ALL SIMULATIONS COMPLETE!");
        System.out.println("8 test cases completed - Charts saved to 'simulation_results'");
        System.out.println("=".repeat(70));
    }

    private static void runScenario(String label,
                                     Function<CloudSimPlus, DatacenterBroker> brokerFactory,
                                     java.util.function.Supplier<List<Cloudlet>> cloudletSupplier) {
        System.out.println("\n" + "-".repeat(70));
        System.out.println(">>> Running: " + label + " <<<");
        System.out.println("-".repeat(70));
        new SimulationMain(label, brokerFactory, cloudletSupplier);
    }

    /**
     * Constructor that initializes and runs the simulation.
     *
     * @param label descriptive label for the scenario
     * @param brokerFactory function to create the broker instance
     * @param cloudletSupplier supplier function to create cloudlets for this scenario
     */
    public SimulationMain(String label,
                          Function<CloudSimPlus, DatacenterBroker> brokerFactory,
                          java.util.function.Supplier<List<Cloudlet>> cloudletSupplier) {
        this.scenarioLabel = label;
        simulation = new CloudSimPlus();

        // Create datacenter
        Datacenter datacenter = createDatacenter();

        // Create broker based on factory method
        broker = brokerFactory.apply(simulation);

        // Create VMs (heterogeneous)
        vmList = createVms();
        broker.submitVmList(vmList);

        // Create cloudlets using supplied workload scenario
        cloudletList = cloudletSupplier.get();
        broker.submitCloudletList(cloudletList);

        // Initialize metrics collector
        metricsCollector = new MetricsCollector();

        // Run simulation
        simulation.start();

        // Collect and print results
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        printResults(finishedCloudlets);
    }

    /**
     * Creates a Datacenter with multiple hosts.
     */
    private Datacenter createDatacenter() {
        List<Host> hostList = new ArrayList<>();

        for (int i = 0; i < HOSTS; i++) {
            List<Pe> peList = new ArrayList<>();
            for (int j = 0; j < HOST_PES; j++) {
                peList.add(new PeSimple(HOST_MIPS));
            }

            Host host = new HostSimple(HOST_RAM, HOST_BW, HOST_STORAGE, peList)
                    .setVmScheduler(new VmSchedulerTimeShared());
            hostList.add(host);
        }

        return new DatacenterSimple(simulation, hostList);
    }

    /**
     * Creates heterogeneous VMs with different MIPS values.
     */
    private List<Vm> createVms() {
        List<Vm> vms = new ArrayList<>();

        for (int i = 0; i < VMS; i++) {
            Vm vm = new VmSimple(VM_MIPS[i % VM_MIPS.length], VM_PES)
                    .setRam(VM_RAM)
                    .setBw(VM_BW)
                    .setSize(VM_SIZE)
                    .setCloudletScheduler(new CloudletSchedulerSpaceShared());
            vms.add(vm);
        }

        return vms;
    }

    /**
     * Prints simulation results and metrics for the current scenario.
     */
    private void printResults(List<Cloudlet> cloudlets) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RESULTS: " + scenarioLabel);
        System.out.println("=".repeat(60));

        // DON'T print full table - too much output!
        // Just print summary metrics
        System.out.println("Total Cloudlets: " + cloudlets.size());

        // Collect and print metrics
        metricsCollector.collectMetrics(cloudlets);
        metricsCollector.printMetrics();

        // Add results to chart plotter for later comparison
        chartPlotter.addResult(
                scenarioLabel,
                metricsCollector.getAverageWaitingTime(),
                metricsCollector.getAverageResponseTime(),
                metricsCollector.getMakespan(),
                metricsCollector.getThroughput()
        );

        System.out.println("=".repeat(60));
    }
}