package parallelProcesses;
import java.util.ArrayList;
import java.util.List;

/*
 * Created on 07.09.2017
 */

public class ParallelProcessHandler {
    
    public static enum ProcessStatus {WAITING, RUNNING, PAUSED, FINISHED, DEAD};
    
    ArrayList<ParallelProcess> processes;
    
    public static ParallelProcessHandler currentParallelProcessHandler;
    
    public ParallelProcessHandler() {
        processes = new ArrayList<ParallelProcess>();
        currentParallelProcessHandler = this;
    }
    
    public void addProcess(ParallelProcess process) {
        processes.add(process);
    }
    
    /**
     * removes all dead processes.
     */
    private void clean() {
        for (int i=0; i<processes.size(); i++)
            if (processes.get(i).getStatus() == ProcessStatus.DEAD) {processes.remove(i); i--;}
    }
    
    /**
     * Removes all dead process and returns a list of all processes.
     * @return
     */
    public List<ParallelProcess> getProcesses() {
        clean();
        return processes;
    }
}
