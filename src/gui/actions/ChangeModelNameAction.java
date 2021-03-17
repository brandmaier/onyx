package gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import engine.ModelRequestInterface;


public class ChangeModelNameAction extends AbstractAction {

	ModelRequestInterface mri;
	
	public ChangeModelNameAction(ModelRequestInterface mri)
	{
		this.mri = mri;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		//TODO: ask user for new name
		
		// then send a change request via mri
		
	}

}
