package importexport;

import gui.views.ModelView;
import importexport.filters.SVGFileFilter;

import java.io.File;
import java.io.FileOutputStream;

import javax.swing.filechooser.FileFilter;

import de.erichseifert.vectorgraphics2d.PDFGraphics2D;
import de.erichseifert.vectorgraphics2d.SVGGraphics2D;

public class SVGExport extends VectorExport {



	public SVGExport(ModelView modelView) {
		super(modelView, new SVGFileFilter(),new String[] {"svg","xml"});
	}

    public boolean isValid() {return true;}
	
	@Override
	public void export(File file) throws Exception {
		// TODO Auto-generated method stub
		
		/*FileOutputStream fileOutputStream = new FileOutputStream(file);
		PDFJob job = new PDFJob(fileOutputStream);
		Graphics pdfGraphics = job.getGraphics();
		
		modelView.paintComponent(pdfGraphics);
		
		pdfGraphics.dispose();
		job.end();*/

		SVGGraphics2D pdfg = new SVGGraphics2D(0,0,modelView.getWidth(),modelView.getHeight());

		/*
		pdfg.setFontRendering(FontRendering.VECTORS);
//		modelView.paintComponent(pdfg);

		Style.FillStyle oldStyle = modelView.getGraph().graphStyle.nodeFillGradient;
		if (oldStyle==Style.FillStyle.GRADIENT) {
			 modelView.getGraph().graphStyle.nodeFillGradient = Style.FillStyle.FILL;
		}*/
		
		exportToGraphicsContext(pdfg);

		// modelView.getGraph().graphStyle.nodeFillGradient = oldStyle;
		
		FileOutputStream files = new FileOutputStream(file);
        try {
            files.write(pdfg.getBytes());
        } finally {
            files.close();
        }
	}


}
