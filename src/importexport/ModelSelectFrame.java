package importexport;

import importexport.OpenMxImport.XMLModel;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;

public class ModelSelectFrame extends JDialog implements ActionListener {

		JList list;
		JLabel label;
		JButton ok;
		List<XMLModel> models;
		
		public ModelSelectFrame(JFrame owner, List<XMLModel> models)
		{
			super(owner, "Select models to import", Dialog.ModalityType.DOCUMENT_MODAL);
			
			this.models = models;
			
			String[] names = new String[models.size()];
			for (int i=0; i < names.length;i++) names[i]=models.get(i).name;
			
			list = new JList(names);
			label = new JLabel("Choose which models you want to import");
			ok = new JButton("ok");
			this.add(label, BorderLayout.NORTH);
			this.add(list, BorderLayout.CENTER);
			this.add(ok, BorderLayout.SOUTH);
			

			ok.addActionListener(this);
			
//			this.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);

			pack();

			this.setLocationRelativeTo(null); // center frame on screen

			this.setVisible(true);
		//	this.setModalExclusionType(Dialog.ModalityType.APPLICATION_MODAL);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			this.setVisible(false);
			this.dispose();
		}
		
		public List<XMLModel> getSelectedModels()
		{
			List<XMLModel> rmodels = new ArrayList<XMLModel>();
			for (int i=0; i < list.getSelectedIndices().length; i++) {
				System.out.println("Add model: "+list.getSelectedIndices()[i]);
				rmodels.add( models.get(list.getSelectedIndices()[i]));
			}
			return(rmodels);
		}
}
