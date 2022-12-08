package OS_TERM_PROJECT;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Random;

public class ServerWorkerNodeReader implements Runnable {

    // reads the completed jobs from the worker node

    ArrayList<Packet> completedPacketList;
    Random randNum = new Random();
    ObjectInputStream read;
    ServerWorkerNodeConnection connection;

    public ServerWorkerNodeReader(ServerWorkerNodeConnection connection, ObjectInputStream read,
                                  ArrayList<Packet> completedPacketList) {
        this.connection = connection;
        this.read = read;
        this.completedPacketList = completedPacketList;
    }

    @Override
    public void run() {

        // collects the responses from the worker nodes and either adds them to the completed job list or prints them

        try {

            /*
            * The first while loop sets the WorkerNode type in the workerNodeConnection.
            */
            
            Packet serverResponse;
            while ((serverResponse = (Packet) read.readObject()) != null) {

                if(connection.exitTheSystem[0]){
                    break;
                }

                if (serverResponse.getIsAMessage()) {
                    connection.setWorkerNodeType(serverResponse.getMessage());
                    if(serverResponse.getMessage().equals("A")){
                        ServerWorkerNodeConnection.incrementAWorkerNodes();
                    }
                    else if (serverResponse.getMessage().equals("B")){
                        ServerWorkerNodeConnection.incrementBWorkerNodes();
                    } else {
                        ServerWorkerNodeConnection.incrementCWorkerNodes();
                    }
//                  System.out.println("COLLECTOR: WorkerNode (#" + workerNodeConnection.amountOfWorkerNodes + ") of
//                          type: " + connection.getWorkerNodeType() + " is connected");
                    connection.setWorkerNodeIsRunning(true);
                    System.out.println("WORKER NODE READER:" + " WorkerNode is running has been set to : " +
                            connection.isWorkerNodeIsRunning());
                    break;

                } else {
                    System.out.println("WORKER NODE READER: " + serverResponse);
                }
            }
            
            /*
            * The second while loop collects the completed jobs from the worker nodes and adds them to the
            *  CompletedJobList array
            */

//            Job serverResponse;
            while ((serverResponse = (Packet) read.readObject()) != null) {

                if(connection.exitTheSystem[0]){

                    break;
                }

                if(serverResponse.isJobIsDone()){
                    synchronized (completedPacketList){
                        completedPacketList.add(serverResponse);
                    }
                    ServerWorkerNodeConnection.completedJobs++;
                    System.out.println("WORKER NODE READER: COMPLETED JOBS : " +
                            ServerWorkerNodeConnection.completedJobs);

                }
                System.out.println("WORKER NODE READER: " + serverResponse.getMessage());

            }

            read.close();

        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println("\n***WORKER NODE READER DISCONNECTING***");

        }

    }

}




