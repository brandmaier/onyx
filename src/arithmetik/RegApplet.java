package arithmetik;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class RegApplet extends Applet implements ActionListener, WindowListener, Printable
{
	public TextArea ta;
	
	public TextField regausdruck;
	
	public Frame frame;
	
	public RegApplet()
	{}
	public RegApplet(Frame frame)
	{
		this.frame = frame;
	}
	public void actionPerformed(ActionEvent e)
	{
		try {
			Automaton a = Automaton.parseString(regausdruck.getText());
			gap();
			println(a.toString());
			println ("Starting computation....");
//			a = a.makeDeterministic();
//			a = a.makeComplete();
			a = a.efficientMakeDeterministic();
			println("Deterministic Automaton");
			println(a.toString());
			a = a.makeComplete();
			a = a.removeNonreachableStates();
			a = a.makeMinimal();
			println("Minimal Automaton:");
			println(a.toString());
			Graph g = a.toGraph();
			int[] start = a.getStartVector();
			int[] ende = a.getTerminateVector();
			
			Qelement[][] c = g.getConvergence(start);
			Qelement[] d = g.getConvergenceAt(start, ende);
			Qelement res = g.getConvergenceIfConverges(start, ende);
			
			println ("...done");
			for (int i=0; i<c.length; i++)
			{
				print("Step of Period "+(i+1)+": ");
				for (int j=0; j<c[i].length; j++)
					print(c[i][j]+"  ");
				print("    ---> "+d[i]);
				println();
			}
			String s;
			if (res.equals(new Qelement(-1))) s = "No convergence"; 
			else s = ""+res;
			println("Converges towards : "+s);
			
			
		} catch (Exception ex)
		{
			this.println("Fehler bei der Ausführung.");
		}
	}
	public void gap()
	{
		ta.append("\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n");
	}
	public void init()
	{
		init(this);
	}
	public void init(Container c)
	{
		c.setLayout(new BorderLayout());
		ta = new TextArea();
		ta.setEditable(false);
		
		c.add(ta, BorderLayout.CENTER);
		
		regausdruck = new TextField("");
		regausdruck.addActionListener(this);
		Panel p = new Panel(new GridLayout(3,1));
		p.add(new Label("Please input regular Expression and press Return",Label.CENTER));
		p.add(new Label("Use + for union, & for cut, * for star operation and ~ for complement.",Label.CENTER));
		p.add(regausdruck);

		c.add(p, BorderLayout.SOUTH);
	}
	public static void main (String[] arg)
	{
		Frame frame = new Frame();
		frame.setTitle("Density of regular Languages");
		RegApplet app = new RegApplet(frame);
		frame.addWindowListener(app);
		app.init(frame);
		frame.setSize(500,350);
		frame.show();
	}
	public void print(String s)
	{
		ta.append(s);
	}
	public void println()
	{
		ta.append("\r\n");
	}
	public void println(String s)
	{
		ta.append(s+"\r\n");
	}
	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowClosing(WindowEvent e) {frame.dispose(); System.exit(0);}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
}
