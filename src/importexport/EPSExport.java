package importexport;

//import gnu.jpdf.PDFJob;

import gui.views.ModelView;

import java.awt.Graphics;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.filechooser.FileFilter;

import de.erichseifert.vectorgraphics2d.EPSGraphics2D;
import de.erichseifert.vectorgraphics2d.PDFGraphics2D;
import de.erichseifert.vectorgraphics2d.VectorGraphics2D.FontRendering;

public class EPSExport extends VectorExport {

	public EPSExport(ModelView modelView) {
		super(modelView, null, new String[] {"eps"});
	}
	
	public boolean isValid() {return true;}

	@Override
	public void export(File file) throws Exception {


		EPSGraphics2D pdfg = new EPSGraphics2D(0,0,modelView.getWidth(),modelView.getHeight());

		
		exportToGraphicsContext(pdfg);

	
		FileOutputStream files = new FileOutputStream(file);
        try {
            files.write(pdfg.getBytes());
        } finally {
            files.close();
        }
	}

}
