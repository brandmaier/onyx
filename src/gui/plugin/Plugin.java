package gui.plugin;

public abstract class Plugin {

	String menuName;
	enum context {NODE, EDGE, DESKTOP, MODELVIEW, DATAVIEW};
	String authorName;
	int majorVersion;
	int minorVersion;
	String pluginName;
	
	
	public abstract void run();
	
	public abstract void init();
	
}
