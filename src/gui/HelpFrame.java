package gui;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class HelpFrame extends JFrame
{

	JLabel label;
	
	public HelpFrame()
	{
		String text = "Double-click any empty space on the desktop to create a model panel."+
	"Double-click any empty space on the model panel to create a new latent variable."+
				"Double-click any empty space on the model panel and hold down shift to create an observed variable"+
	"Right-press the mouse on any variable and drag the mouse onto another variable to create a regression path"+
				"Right-press the mouse on any variable and drag the mouse onto another variable while holding down shift to create a (co)variance path.";
		
		
		label = new JLabel(text);
	}
	
}
