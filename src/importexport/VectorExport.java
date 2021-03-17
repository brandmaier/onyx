package importexport;

import gui.views.ModelView;

import java.awt.Graphics2D;
import java.io.File;

import javax.swing.filechooser.FileFilter;

import de.erichseifert.vectorgraphics2d.VectorGraphics2D;
import de.erichseifert.vectorgraphics2d.VectorGraphics2D.FontRendering;

public abstract class VectorExport extends Export {

	//boolean disableGradients = false;
	
	public VectorExport(ModelView modelView, FileFilter fileFilter,
			String[] defaultExtensions) {
		super(modelView, fileFilter, defaultExtensions);
		// TODO Auto-generated constructor stub
	}

	public void exportToGraphicsContext(VectorGraphics2D g)
	{
		
		/*Style.FillStyle oldStyle = modelView.getGraph().graphStyle.nodeFillGradient;
		if ((disableGradients) && (oldStyle==Style.FillStyle.GRADIENT)) {
			 modelView.getGraph().graphStyle.nodeFillGradient = Style.FillStyle.FILL;
		}*/
	
		g.setFontRendering(FontRendering.VECTORS);
		
		
		 super.exportToGraphicsContext(g);
		 
		 //modelView.getGraph().graphStyle.nodeFillGradient = oldStyle;
			
	}

}
