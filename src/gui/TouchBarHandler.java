package gui;

import java.awt.Component;
import java.awt.Image;

import com.thizzer.jtouchbar.JTouchBar;
import com.thizzer.jtouchbar.common.Color;
import com.thizzer.jtouchbar.common.ImageAlignment;
import com.thizzer.jtouchbar.common.ImageName;
import com.thizzer.jtouchbar.item.TouchBarItem;
import com.thizzer.jtouchbar.item.view.TouchBarButton;
import com.thizzer.jtouchbar.item.view.TouchBarScrubber;
import com.thizzer.jtouchbar.item.view.TouchBarTextField;
import com.thizzer.jtouchbar.scrubber.ScrubberActionListener;
import com.thizzer.jtouchbar.scrubber.ScrubberDataSource;
import com.thizzer.jtouchbar.scrubber.view.ScrubberImageItemView;
import com.thizzer.jtouchbar.scrubber.view.ScrubberTextItemView;
import com.thizzer.jtouchbar.scrubber.view.ScrubberView;

import engine.ParameterReader;
import engine.ParameterReader.FitIndex;
import gui.graph.Edge;
import gui.graph.Node;
import gui.views.ModelView;
import gui.views.View;

public class TouchBarHandler implements ScrubberActionListener {

	JTouchBar touchbar;
	boolean status;
	private Component c;
	private ModelView activeModelView;
	
	TouchBarScrubber scrubberNodeView;
	TouchBarScrubber scrubberModelView;
	
	public TouchBarHandler(Component c) {
		
		System.out.println("Init Touch Bar");
		
		status = init(c);
		
		System.out.println("Status" + status);
		
		if (!status) {
			System.err.println("Could not initialize touch bar");
		}
	}

	private boolean init(Component c) {
		try {
			touchbar = new JTouchBar();
		} catch (Exception e) {
			System.out.println(e);
			return(false);
		}
	
		this.c = c;

		
		touchbar.setPrincipalItemIdentifier("onyxTouchBar2");
		
		setActiveView(null);
//		touchbar.getItems().removeAll();
		touchbar.show(c);
		
		return(true);
	}
	
	public void setActiveView(Object v) {
		
		this.activeModelView=null;
		
		if (!status) return;

		touchbar.hide(c);
		//status=false;
//		touchbar.
		touchbar.getItems().clear();
		
		if (v instanceof ModelView) {
			ModelView mv = (ModelView)v;
	
			this.activeModelView = mv;
			
			ParameterReader se = mv.getShowingEstimate();
			double cfi = se.getFitIndex(FitIndex.CFI);
			double rmsea = se.getFitIndex(FitIndex.RMSEA);
			
			TouchBarTextField touchBarItem = new TouchBarTextField();
			touchBarItem.setStringValue( "RMSEA="+Math.round(rmsea*100)/100.0+" CFI="+Math.round(cfi*100)/100.0); //+" with "+mv.getGraph().getNodes().size()+" elements.");
			
			touchbar.addItem(new TouchBarItem("tb3",touchBarItem, true));
			
			
			scrubberModelView = new TouchBarScrubber();
			
			scrubberModelView.setMode(2);
			
			scrubberModelView.setActionListener(this);
			scrubberModelView.setDataSource(new ScrubberDataSource() {
				@Override
				public ScrubberView getViewForIndex(TouchBarScrubber scrubber, long index) {
					
					ScrubberTextItemView textItemView = new ScrubberTextItemView();
					textItemView.setIdentifier("ScrubberItem_"+index);
					textItemView.setStringValue(ModelView.presets[(int)index].getName());
					
					return textItemView;
					/*if(index == 0) {
						ScrubberTextItemView textItemView = new ScrubberTextItemView();
						textItemView.setIdentifier("ScrubberItem_0");
						textItemView.setStringValue("Scrubber TextItem");
						
						return textItemView;
					}
					else if(index == 1) {
						ScrubberTextItemView textItemView = new ScrubberTextItemView();
						textItemView.setIdentifier("ScrubberItem_1");
						textItemView.setStringValue("Scrubber TextItem2");
						
						return textItemView;
					}
					{
						ScrubberTextItemView textItemView = new ScrubberTextItemView();
						textItemView.setIdentifier("ScrubberItem_2");
						textItemView.setStringValue("Scrubber TextItem #32");
						
						return textItemView;
					}*/
				}
				
				
				
				@Override
				public int getNumberOfItems(TouchBarScrubber scrubber) {
					return 7;
				}
			});
			touchbar.addItem(new TouchBarItem("tb2",scrubberModelView, true));
			scrubberModelView.setShowsArrowButtons(true);
			//ts.setBackgroundColor(Color.GREEN);
			
		} else if (v instanceof Node) {
			
			System.out.println("Node");
			
			Node node = (Node)v;
			double r2 = Math.round(node.pseudoR2*100)/100.0;
			
			System.out.println("Add item");
			TouchBarTextField touchBarItem = new TouchBarTextField();
			String type = "";
			type = "Node";
			if (node.isLatent()) type="Latent";
			else if (node.isMeanTriangle()) type = "Mean";
			else type="Observed";
			touchBarItem.setStringValue(type+": "+node.getCaption()+"; R2="+r2);
			//touchBarItem.setStringValue();;
			touchbar.addItem(new TouchBarItem("tb2",touchBarItem, true));
			
			scrubberNodeView = new TouchBarScrubber();
			
			scrubberNodeView.setMode(2);
			
			scrubberNodeView.setActionListener(this);
			scrubberNodeView.setDataSource(new ScrubberDataSource() {
				
				Color[] colors = new Color[] {Color.RED, Color.BLUE, Color.YELLOW, Color.ORANGE,Color.GREEN};
				
				@Override
				public ScrubberView getViewForIndex(TouchBarScrubber scrubber, long index) {
					
					ScrubberImageItemView siv = new ScrubberImageItemView();
					ScrubberImageItemView imageItemView = new ScrubberImageItemView();
					imageItemView.setIdentifier("ScrubberItem_2");
					if (index==1)
					imageItemView.setImage(new com.thizzer.jtouchbar.common.Image(ImageName.NSImageNameTouchBarColorPickerFill, false));
					if (index==2)
					imageItemView.setImage(new com.thizzer.jtouchbar.common.Image(ImageName.NSImageNameTouchBarRemoveTemplate, false));
					if (index==3)
					imageItemView.setImage(new com.thizzer.jtouchbar.common.Image(ImageName.NSImageNameTouchBarColorPickerFont, false));
					if (index==4)				
							imageItemView.setImage(new com.thizzer.jtouchbar.common.Image(ImageName.NSImageNameTouchBarColorPickerStroke, false));
					imageItemView.setAlignment(ImageAlignment.CENTER);
					return imageItemView;
				}

				@Override
				public int getNumberOfItems(TouchBarScrubber scrubber) {
					return colors.length;
				}
			});
			
			touchbar.addItem(new TouchBarItem("tb3",scrubberNodeView, true));
			scrubberNodeView.setShowsArrowButtons(true);

			
		} else if (v instanceof Edge) {
			
			Edge edge = (Edge)v;
			
			
		} else {
			System.out.println("Add item");
			TouchBarTextField touchBarItem = new TouchBarTextField();
			touchBarItem.setStringValue("You are awesome!");
			touchbar.addItem(new TouchBarItem("tb1",touchBarItem, true));
			
			TouchBarButton but = new TouchBarButton();
			but.setTitle("CLICK ME!");
			
			touchbar.addItem(new TouchBarItem("Button_1",but,true));
			
			//touchbar.show((Component)v);
			touchbar.show(c);
		}
		
		touchbar.show(c);
		
		
	}

	@Override
	public void didSelectItemAtIndex(TouchBarScrubber scrubber, long index) {
		
		//System.out.println(scrubber+" "+index);
		
		
		if (scrubber == scrubberModelView) {
		
		//if (index==0) {
			activeModelView.presets[(int)index].apply(activeModelView.getGraph(), activeModelView.getShowingEstimate());
			activeModelView.repaint();
		//}
			
		} else {
		
			System.out.println("OK!");
			//activeModelView.openNodeLineColorPicker();
			activeModelView.openNodeFillColorPicker();
			if (index==1) {
				
			}
			
		}
	}
	
	
}
