package OS_TERM_PROJECT;


public class ServerWorkerNodeConnection {
    
    int workerNodeID;
    // a data member to hold the worker node type of a current connected worker node. This is mainly used when deciding
    // to dispatch a job to a given worker node
    String workerNodeType;
    protected boolean workerNodeTypeIsSet = false;
    public static int amountOfWorkerNodes = 0;
    boolean workerNodeIsRunning = false;
    public static int amountOfAWorkerNodes = 0;
    public static int amountOfBWorkerNodes = 0;
    public static int amountOfCWorkerNodes = 0;
    public static int completedJobs = 0;
    public boolean [] exitTheSystem;

    // get the amount of worker nodes that are currently running
    public ServerWorkerNodeConnection(boolean [] exitTheSystem) {
        this.exitTheSystem = exitTheSystem;
        amountOfWorkerNodes++;
    }


    public void setWorkerNodeType(String workerNodeType) {
        this.workerNodeType = workerNodeType;
        workerNodeTypeIsSet = true;
    }

    public String getWorkerNodeType() {
        return workerNodeType;
    }

    public boolean getWorkerNodeTypeIsSet(){
        return workerNodeTypeIsSet;
    }

    public boolean isWorkerNodeIsRunning() {
        return workerNodeIsRunning;
    }

    public void setWorkerNodeIsRunning(boolean value){
        workerNodeIsRunning = value;
    }

    public static void incrementAWorkerNodes(){
        amountOfAWorkerNodes++;
    }

    public static void incrementBWorkerNodes(){
        amountOfBWorkerNodes++;
    }

    public static void incrementCWorkerNodes(){
        amountOfCWorkerNodes++;
    }

}
