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
package gui.undo;

import java.util.ArrayList;

/**
 * The UndoStack memorizes all operations that were stored
 * by adding the respective UndoStep to the UndoStack.
 * 
 * The UndoStack can be locked. Then, no UndoSteps
 * can be added.
 * 
 * The UndoStack can be set to collect a couple of
 * UndoSteps into a single MultiStep.
 * 
 * @author Andreas Brandmaier
 *
 */
public class UndoStack 
{
	ArrayList<UndoStep> queue;
	ArrayList<UndoStep> redo;

	public final static int capacity = 40;
	
	boolean queueLocked = false;
	
	MultiStep multiStep = null;

	private boolean collect;
	
	public UndoStack()
	{
		queue = new ArrayList<UndoStep>(capacity);
		redo = new ArrayList<UndoStep>(capacity);
	}
	
	public void lock()
	{
		queueLocked=true;
	}
	
	public void unlock()
	{
		queueLocked=false;
	}
	
	public synchronized void undo()		//AB: 17.11.2015 added synchronized to avoid concurrent modification with runners
	{
		if (collect) {
			System.err.println("Dangling Undo Multistep!");
			endCollectSteps();
		}
		
		queueLocked = true;
		if (queue.size() > 0) {
			UndoStep step = queue.remove( queue.size()-1 );
//			System.out.println("Undo "+step.title);
			try {
				if (step != null) {
					step.undo();
					redo.add(step);
				}
			} catch (Exception e) {
				System.err.println("Problem with undo step "+step.title);
				e.printStackTrace();
			}
		}
		queueLocked = false;
	}

	
	public void add(UndoStep step)
	{
		if (queueLocked) return;
		
		if (collect) {
			multiStep.add(step);
			return;
		}
		
		// clean redo stack
		redo.clear();
		
		// unwrap single steps wrapped in multisteps
		if (step instanceof MultiStep) {
			MultiStep ms = (MultiStep)step;
			if (ms.steps.size()==0) return;
			if (ms.steps.size()==1) step = ms.steps.get(0);
		}
		
		queue.add(step);
		//System.out.println("Queue size "+queue.size()+step);
		if (queue.size() > capacity) {
			queue.remove(0);
		}
	}
	
	public void startCollectSteps()
	{
		multiStep = new MultiStep();	
		collect = true;
	}
	
	public void endCollectSteps()
	{
		collect = false;
		this.add(multiStep);
		multiStep = null;
	}

	public void redo()
	{
		if (collect) {
			System.err.println("Dangling Undo Multistep!");
			endCollectSteps();
		}
		
		queueLocked = true;
		
		if (redo.size() > 0) {
			UndoStep step = redo.remove( redo.size()-1 );
//			System.out.println("Undo "+step.title);
			try {
				step.redo();
				
				queue.add(step);
			} catch (Exception e) {
				System.err.println("Problem with redo step "+step.title);
				e.printStackTrace();
			}			
			
			
		} else {
			System.err.println("Empty redo stack.");
		}
		
		
		queueLocked = false;
	}

	
}
