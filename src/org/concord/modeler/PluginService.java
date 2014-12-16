package org.concord.modeler;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URL;

import javax.swing.JComponent;

/**
 * @see org.concord.modeler.PageJContainer
 * @author Charles Xie
 * 
 */

public interface PluginService extends MwService {

	/** Return the main window that will be inserted into the MW page. */
	public JComponent getWindow();

	/**
	 * What resources need to be cached when the parent MW page is loaded. Return null if the plugin is on the hard disk (so no caching is needed).
	 */
	public URL[] getCacheResources();

	/**
	 * What resources need to be saved when the parent MW page gets saved. Return null if the plugin does not use any external resource.
	 */
	public String[] getResources();

	/** Store a parameter */
	public void putParameter(String key, String value);

	/** Retrieve a parameter */
	public String getParameter(String key);

	/**
	 * Return the code base on which this plugin runs. The code base is usually set by the parent MW page through a parameter.
	 */
	public String getCodeBase();

	/**
	 * If any initialization is needed in addition to the non-argument constructor, do it in this method.
	 */
	public void init() throws Exception;

	/** If the plugin needs to start some processes after initialization, do it in this method. */
	public void start() throws Exception;

	/** If the plugin needs to stop the processes running within it, do it in this method. */
	public void stop() throws Exception;

	/** If the plugin needs to do something to release the memory, do it in this method. */
	public void destroy() throws Exception;

	/**
	 * force the main class to implement MouseListener if it hasn't already. In most cases, the main class subclasses JComponent. So there is no need to handle this interface unless you want to customize it.
	 */
	public void addMouseListener(MouseListener listener);

	/**
	 * force the main class to implement MouseMotionListener if it hasn't already. In most cases, the main class subclasses JComponent. So there is no need to handle this interface unless you want to customize it.
	 */
	public void addMouseMotionListener(MouseMotionListener listener);

	/**
	 * force the main class to implement KeyListener if it hasn't already. In most cases, the main class subclasses JComponent. So there is no need to handle this interface unless you want to customize it.
	 */
	public void addKeyListener(KeyListener listener);

}