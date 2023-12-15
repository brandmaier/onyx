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
package arithmetik;

/**
 * Insert the type's description here.
 * Creation date: (17.02.2003 08:55:59)
 * @author: 
 */
public class AusgabeFenster extends javax.swing.JFrame implements Printable
{
	private javax.swing.JPanel ivjJFrameContentPane = null;
	private javax.swing.JScrollPane ivjJScrollPane1 = null;
	private javax.swing.JTextArea ivjTextFeld = null;

	private long zeit;
	private boolean mitZeit; 
/**
 * AusgabeFenster constructor comment.
 */
public AusgabeFenster() {
	super();
	initialize();
	mitZeit = false;
}
/**
 * AusgabeFenster constructor comment.
 * @param title java.lang.String
 */
public AusgabeFenster(String title) 
{
	super(title);
	initialize();
	mitZeit = false;
}
/**
 * AusgabeFenster constructor comment.
 * @param title java.lang.String
 */
public AusgabeFenster(String title, boolean mitZeit) 
{
	super(title);
	initialize();
	this.mitZeit = mitZeit;
}
/**
 * Return the JFrameContentPane property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJFrameContentPane() {
	if (ivjJFrameContentPane == null) {
		try {
			ivjJFrameContentPane = new javax.swing.JPanel();
			ivjJFrameContentPane.setName("JFrameContentPane");
			ivjJFrameContentPane.setLayout(new java.awt.BorderLayout());
			getJFrameContentPane().add(getJScrollPane1(), "Center");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJFrameContentPane;
}
/**
 * Return the JScrollPane1 property value.
 * @return javax.swing.JScrollPane
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JScrollPane getJScrollPane1() {
	if (ivjJScrollPane1 == null) {
		try {
			ivjJScrollPane1 = new javax.swing.JScrollPane();
			ivjJScrollPane1.setName("JScrollPane1");
			ivjJScrollPane1.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			ivjJScrollPane1.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			getJScrollPane1().setViewportView(getTextFeld());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJScrollPane1;
}
/**
 * Return the TextFeld property value.
 * @return javax.swing.JTextArea
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTextArea getTextFeld() {
	if (ivjTextFeld == null) {
		try {
			ivjTextFeld = new javax.swing.JTextArea();
			ivjTextFeld.setName("TextFeld");
			ivjTextFeld.setBounds(0, 0, 426, 240);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjTextFeld;
}
/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	// System.out.println("--------- UNCAUGHT EXCEPTION ---------");
	// exception.printStackTrace(System.out);
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		// user code begin {1}
		// user code end
		setName("AusgabeFenster");
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setSize(426, 240);
		setContentPane(getJFrameContentPane());
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
	// user code begin {2}
	int BREITE = 500, HOEHE = 400;
	
	java.awt.Dimension dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	setBounds((dim.width-BREITE)/2,(dim.height-HOEHE)/2,BREITE,HOEHE);
	show();

	zeit = System.currentTimeMillis();

	
	// user code end
}
/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		AusgabeFenster aAusgabeFenster;
		aAusgabeFenster = new AusgabeFenster();
		aAusgabeFenster.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
		aAusgabeFenster.show();
		java.awt.Insets insets = aAusgabeFenster.getInsets();
		aAusgabeFenster.setSize(aAusgabeFenster.getWidth() + insets.left + insets.right, aAusgabeFenster.getHeight() + insets.top + insets.bottom);
		aAusgabeFenster.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of javax.swing.JFrame");
		exception.printStackTrace(System.out);
	}
}
/**
 * print method comment.
 */
public void print(java.lang.String s) 
{
	if (s.length()==0) return;
	String ns = s;
	if ((mitZeit) || (!s.equals("\r\n"))) ns = (System.currentTimeMillis()-zeit)+" ms :"+s; 
	String str = getTextFeld().getText();
	str += ns;
	getTextFeld().setText(str);
}
/**
 * println method comment.
 */
public void println() 
{
	print("\r\n");
}
/**
 * println method comment.
 */
public void println(java.lang.String s) 
{
	print(s + "\r\n");
}
}
