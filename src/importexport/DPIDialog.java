package importexport;

import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class DPIDialog extends JDialog implements ActionListener, KeyListener {

	JTextField dpiField, widthPixelField, widthInchField;
	
	JButton ok;
	
	int originalPixelWidth;
	int dpi, widthPixel;
	double  widthInch;
	
	public DPIDialog()
	{
		super(null, "Resolution", Dialog.ModalityType.DOCUMENT_MODAL);
		
		
		// defaults
		dpi = 300;
		widthInch = 8.5;
		widthPixel = (int)Math.round(dpi*widthInch);
		
	//	this.setLayout( new BoxLayout(this, BoxLayout.LINE_AXIS) );
		this.setLayout( new GridLayout(4,2));
		
		dpiField = new JTextField();
		widthPixelField = new JTextField();
		widthInchField = new JTextField();
		
		dpiField.addKeyListener(this);
		
		this.add (new JLabel("DPI"));
		this.add ( dpiField );
		this.add (new JLabel("Pixel"));
		this.add( widthPixelField );
		this.add( new JLabel("Inch"));
		this.add( widthInchField );
		
		ok = new JButton("OK");
		
		this.add(ok);

		ok.addActionListener(this);
		
		dpiField.addActionListener(this);
		widthPixelField.addActionListener(this);
		widthInchField.addActionListener(this);
		
//		dpi.setText(dpi)
//		widthPixel 
		
		update();
		
		this.setSize(400, 200);
		pack();

		this.setLocationRelativeTo(null); // center frame on screen


		setVisible(true);
		
	//	this.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
	}
	
	public void update() {
		this.dpiField.setText(Integer.toString(dpi));
		this.widthPixelField.setText(Integer.toString(widthPixel));
		this.widthInchField.setText(Double.toString(widthInch));
		
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		updateAction(arg0.getSource());
		
		if (arg0.getSource() == ok ) {
			this.setVisible(false);
			this.dispose();
		}
		
	}
	
	private void updateAction(Object source) {
		try {
		if (source==dpiField) {
			this.dpi = Integer.parseInt(((JTextField)source).getText() );
			widthPixel = (int)Math.round( dpi*widthInch);
			update();
		}
		
		if (source==widthPixelField) {
			this.widthPixel =Integer.parseInt(((JTextField)source).getText() );
			widthInch = widthPixel/widthInch;
			update();
		}
		
		if (source==widthInchField) {
			this.widthInch = Integer.parseInt(((JTextField)source).getText() );
			widthPixel = (int)Math.round(dpi*widthInch);
			update();
		}
		} catch(Exception e) {
			System.err.println(e);
		}
		
	}

	public static void main(String[] args)
	{
		DPIDialog d = new DPIDialog();
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		
		
	}
}
