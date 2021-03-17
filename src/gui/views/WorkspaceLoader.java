package gui.views;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import gui.Desktop;
import importexport.OnyxModelRestoreXMLHandler;

public class WorkspaceLoader implements ContentHandler
{
	Desktop desktop;
	
	public  WorkspaceLoader(Desktop desktop) {
		this.desktop = desktop;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	OnyxModelRestoreXMLHandler mxml;
	ModelView mv;
	
	enum ParseMode {NONE, MODELVIEW, DATAVIEW};
	
	ParseMode parseMode = ParseMode.NONE;
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		

		
		if (localName.equalsIgnoreCase( "Model")) {
			mv = new ModelView(desktop);
			mxml = new OnyxModelRestoreXMLHandler(mv);
		}
		
		if (parseMode == ParseMode.MODELVIEW) {
			mxml.startElement(uri, localName, qName, atts);
		}
		
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		if (localName.equalsIgnoreCase( "Model")) {
			parseMode = ParseMode.NONE;
		}
		
		if (parseMode == ParseMode.MODELVIEW) {
			mxml.endElement(uri, localName, qName);
		}
		
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		// TODO Auto-generated method stub
		
	}

}
