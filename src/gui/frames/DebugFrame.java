package gui.frames;

import java.awt.Insets;

import gui.LabeledInputBox;

import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicMenuItemUI;


public class DebugFrame extends JFrame
{

	public DebugFrame()
	{
		this.setSize(300,300);
		JMenuBar menubar = new JMenuBar();
		this.setJMenuBar(menubar );
		JMenu menu = new JMenu("Menu");
		JMenuItem item = new JMenuItem("ABC");
		menu.add(item);
		JMenuItem item3 = new JMenuItem("Hallo");
		//item2.add(new JTextField("123"));
		
		menubar.add(menu);

		
		JCheckBoxMenuItem item2 = new JCheckBoxMenuItem("Hi");
		item2.setSelected(true);
		
		System.out.println(item2.getMargin());
		System.out.println(item2.getLayout());
		System.out.println(item2.getInsets());
		BasicMenuItemUI ui;
		menu.add(item2);
		//MenuItemLayoutHelper ml;
		System.out.println(UIManager.get("MenuItem.margin"));
		System.out.println(UIManager.get("MenuItem.checkIconOffset"));
		System.out.println(UIManager.get("Menu.textIconGap"));
		
		UIDefaults uid = new UIDefaults();
		System.out.println(uid.get("Menu.textIconGap"));
		//System.out.println(item.get)
		
		ButtonUI bui = item3.getUI();
//		bui.
		JCheckBox checkBox = new JCheckBox();
		Border cbBorder = UIManager.getBorder("CheckBox.border");
		Insets cbin = cbBorder.getBorderInsets(checkBox);
		int mar = checkBox.getPreferredSize().width- cbin.right-cbin.left;
//		mar = checkBox.getMinimumSize().width;
		//System.out.println(item2.getRootPane().getLayout());
		System.out.println("MAR"+(checkBox.getIconTextGap())+":"+(mar));
	//	JPanel pan = new JPanel();
		
		LabeledInputBox lib = new LabeledInputBox("HI");
		menu.add(lib);
		
		this.setVisible(true);
	}
	
	public static void main(String[] args) {
		new DebugFrame();
	}
	
}
