package OS_TERM_PROJECT;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ServerWorkerNodeWriter implements Runnable {

    /*
     * The purpose of the ServerWorkerNodeWriter class is to sends the jobs to the worker nodes.
     * Each instance of the ServerWorkerNodeWriter class will be constantly choosing jobs to send to its worker node.
     * For each job that is sent to the worker node, the ServerWorkerNodeWriter calculates if it should send a job to a
     * worker node of the optimized type or not.
     */

    ServerWorkerNodeConnection connection;
    ObjectOutputStream write;
    ArrayList<Packet> jobList;

    public ServerWorkerNodeWriter(ServerWorkerNodeConnection connection, ObjectOutputStream write,
                                  ArrayList<Packet> jobList) {
        this.connection = connection;
        this.write = write;
        this.jobList = jobList;
    }


    @Override
    public void run() {

        // Dispatches jobs to the worker nodes

        System.out.println("\nWORKER NODE WRITER: ***WORKER NODE CONNECTING***");

        try {

            // sends a connection notice to the worker nodes it's connected to
            write.writeObject(new Packet("SERVER RESPONSE: WORKER NODE CONNECTED "));

            // waits a moment for the connection to be set in the worker node connection. The worker node connection is
            // set by the collector
            while (!connection.getWorkerNodeTypeIsSet()) {
                System.out.println("WORKER NODE WRITER: INITIALIZING...");
                Thread.sleep(2000);
            }

            // once the connection is set we wait for jobs to be added to the jobs list
            while (jobList.isEmpty()) {
                Thread.sleep(5000);
            }

            // while the job list is not empty we send jobs to the worker nodes
            System.out.println("WORKER NODE WRITER: " + connection.getWorkerNodeType());
            while (!connection.exitTheSystem[0]) {
                // method can be found further down
                sendJobsToWorkerNode(jobList);
                Thread.sleep(1000);
            }

            write.close();


        } catch (Exception e) {

            e.printStackTrace();
            e.getMessage();
        }


    }

    private void sendJobsToWorkerNode(ArrayList<Packet> jobList) throws IOException, InterruptedException {
        /*
         * -get the amount of jobs that are of either type A or Type B
         * - if there are more optimized jobs than non optimized jobs for a particular type
         * - get the difference, and for every 5 to 1 of optimized jobs to non optimized jobs
         * - send one non optimized job to a given worker node.
         *
         * */
        final String optimizedJobType = connection.getWorkerNodeType();
        int optimizedJobs = 0;
        int amountOfOptimizedWorkerNodes;
        int amountOfNonOptimizedWorkerNodes;
        switch (optimizedJobType) {
            case "A" -> {
                amountOfOptimizedWorkerNodes = ServerWorkerNodeConnection.amountOfAWorkerNodes;
                amountOfNonOptimizedWorkerNodes = ServerWorkerNodeConnection.amountOfBWorkerNodes +
                        ServerWorkerNodeConnection.amountOfCWorkerNodes;
            }
            case "B" -> {
                amountOfOptimizedWorkerNodes = ServerWorkerNodeConnection.amountOfBWorkerNodes;
                amountOfNonOptimizedWorkerNodes = ServerWorkerNodeConnection.amountOfAWorkerNodes +
                        ServerWorkerNodeConnection.amountOfCWorkerNodes;
            }
            case "C" -> {
                amountOfOptimizedWorkerNodes = ServerWorkerNodeConnection.amountOfCWorkerNodes;
                amountOfNonOptimizedWorkerNodes = ServerWorkerNodeConnection.amountOfAWorkerNodes +
                        ServerWorkerNodeConnection.amountOfBWorkerNodes;
            }
            default -> throw new IllegalStateException("Unexpected value: " + optimizedJobType);
        }
        int nonOptimized = 0;
        int remainingJobs = 0;


        boolean takeAQuickNap = false;

        // The dispatcher is either going to send the first optimized index, or non optimized index that it finds
        int optimizedIndex = 0;
        boolean setOptimized = true;
        int nonOptimizedIndex = 0;
        boolean setNonOptimized = true;

        String terminationCode = "EXIT";
        if (connection.exitTheSystem[0]) {
            write.writeObject(new Packet(terminationCode));
            write.flush();
            ServerWorkerNodeConnection.amountOfWorkerNodes--;
        } else {
            // counts all available optimized jobs
            synchronized (jobList) {
                for (int i = 0; i < jobList.size(); i++) {
                    if (jobList.get(i).getJobType().equals(optimizedJobType)) {
                        optimizedJobs++;
                        if (setOptimized) {
                            optimizedIndex = i;
                            setOptimized = false;
                        }
                    }
                    // counts all available non optimized jobs
                    else if (!jobList.get(i).getJobType().equals(optimizedJobType)) {
                        nonOptimized++;
                        if (setNonOptimized) {
                            nonOptimizedIndex = i;
                            setNonOptimized = false;
                        }
                    }
                }
            }

            remainingJobs = Math.abs(optimizedJobs - nonOptimized);

            /*
             * If there are optimized jobs left, The optimized worker node should
             * only execute on those jobs.
             * If there aren't any optimized jobs left, the Dispatcher decides if it's worth it to send
             * a non optimized job to worker node.
             * */

            // if there are only non optimized jobs left, but it's less or equal to 5 jobs. the non-optimal worker node
            // should start executing on those jobs

            int jobsForOptimalWorkerNodes = optimizedJobs;

            /* if there are no more optimized jobs, as well as optimized worker nodes, a non-optimal worker nodes will
             * execute all the remaining non optimized jobs
             * */

            int jobsForNonOptimalWorkerNode = amountOfNonOptimizedWorkerNodes == 0 ?
                    nonOptimized : ((remainingJobs / amountOfNonOptimizedWorkerNodes) / 5);

            if (optimizedJobs == 0 && nonOptimized > 5) {
                jobsForNonOptimalWorkerNode = (nonOptimized / 5) / amountOfNonOptimizedWorkerNodes;
            }

            System.out.println("\n--------DISPATCHING JOBS--------\n");
            System.out.println("OPTIMIZED TYPE.............. " + optimizedJobType);
            System.out.println("TOTAL OPTIMIZED JOBS........." + optimizedJobs);
            System.out.println("TOTAL NON-OPTIMIZED JOBS....." + nonOptimized);
            System.out.println("OPTIMIZED WORKER NODES............." + amountOfOptimizedWorkerNodes);
            System.out.println("NON OPTIMIZED WORKER NODES........." + amountOfNonOptimizedWorkerNodes);
            System.out.println("JOBS FOR OPTIMAL WORKER NODE......." + jobsForOptimalWorkerNodes);
            System.out.println("JOBS FOR NON OPTIMAL WORKER NODE..." + jobsForNonOptimalWorkerNode);
            System.out.println();

            synchronized (jobList) {
                if (jobsForOptimalWorkerNodes > 0) {
                    // sends the jobs to the worker nodes.
                    Packet currentPacket = this.jobList.remove(optimizedIndex);
                    System.out.println("WORKER NODE WRITER: " + currentPacket + " is being sent to a WorkerNode ");
                    write.writeObject(currentPacket);
                    write.flush();
                    return;
                } else if (jobsForNonOptimalWorkerNode > 0) {
                    // sends the jobs to the worker nodes.
                    Packet currentPacket = this.jobList.remove(nonOptimizedIndex);
                    System.out.println("WORKER NODE WRITER: " + currentPacket + " is being sent to a WorkerNode ");
                    write.writeObject(currentPacket);
                    write.flush();
                    return;
                } else {
                    takeAQuickNap = true;
                }
//            }
            }

            // if there are no jobs for this thread to execute it should take some short breaks
            if (takeAQuickNap) {
                System.out.println("\nWORKER NODE WRITER : ***NO JOBS TO EXECUTE***");
                Thread.sleep(10000);
            }
        }
    }
}
