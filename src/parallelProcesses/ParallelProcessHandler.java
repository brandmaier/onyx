/*
* Copyright 2023 by Timo von Oertzen and Andreas M. Brandmaier
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
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
