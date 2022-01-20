package parallelProcesses;
/*
 * Created on 07.09.2017
 */

public interface ParallelProcess extends Runnable {
    public double getProgress();
    public ParallelProcessHandler.ProcessStatus getStatus();
    public void requestTransferToStatus(ParallelProcessHandler.ProcessStatus status);
    public void setMainParallelProcessView(ParallelProcessView view);
    public String getTargetName();
}
