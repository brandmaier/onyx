package gui;


import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class MessageObjectContainer {
	
	private List<MessageObject> messageObjectList;

	private int y;
	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	private int x;
	
	public MessageObjectContainer()
	{
		messageObjectList = new ArrayList<MessageObject>();
		
	}
	
	public void setLocation(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public void draw(Graphics g) {
		
		/*g.setColor(Color.red);
		g.fillRect(x-2,y-2,getWidth()+4, getHeight()+4);
		*/
		
		for (int i = 0; i < messageObjectList.size(); i++) {
			messageObjectList.get(i).paint(
					g,
					x+i*MessageObject.SIZE,y,
					MessageObject.SIZE,MessageObject.SIZE);
		}
		
	}
	
	public int getWidth()
	{
		return messageObjectList.size()*MessageObject.SIZE;
	}
	
	public int getHeight()
	{
		return MessageObject.SIZE;
	}
	
	public void clear()
	{
		this.messageObjectList.clear();
	}
	
	public void add(MessageObject m)
	{
		this.messageObjectList.add(m);
	}

	public void addOnce(MessageObject m)
	{
		if (!messageObjectList.contains(m))
			this.messageObjectList.add(m);
	}
	
	public MessageObject getMessageObjectAt(int xp, int yp)
	{
		if (isPointWithin(xp, yp))
		{
			int i = (int)((xp-x)/MessageObject.SIZE);
			return this.messageObjectList.get(i);
		}
		return null;
	}
	
	public boolean isPointWithin(int xp, int yp)
	{
		return ((yp >= y) && (yp < y+getHeight()) && (xp >= x) && (xp < x+getWidth()));
	}

	public void remove(MessageObject m) {
		this.messageObjectList.remove(m);
	}

	public Object size() {
		return this.messageObjectList.size();
	}

	public boolean contains(MessageObject sparklingObject) {
		return this.messageObjectList.contains(sparklingObject);
	}

	public MessageObject get(int i) {
		return this.messageObjectList.get(i);
	}
}
