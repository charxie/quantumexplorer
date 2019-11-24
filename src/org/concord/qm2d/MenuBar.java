package org.concord.qm2d;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;

import org.concord.qmevent.ManipulationEvent;
import org.concord.qmutil.FileChooser;
import org.concord.qmutil.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class MenuBar extends JMenuBar {

	private static final long serialVersionUID = 1L;

	private final static boolean IS_MAC = System.getProperty("os.name").startsWith("Mac");

	private FileChooser qwbFileChooser, htmFileChooser;

	private FileFilter qwbFilter = new FileFilter() {

		public boolean accept(File file) {
			if (file == null)
				return false;
			if (file.isDirectory())
				return true;
			String filename = file.getName();
			int index = filename.lastIndexOf('.');
			if (index == -1)
				return false;
			String postfix = filename.substring(index + 1);
			if ("qwb".equalsIgnoreCase(postfix))
				return true;
			return false;
		}

		@Override
		public String getDescription() {
			return "Quantum Leap";
		}

	};

	private FileFilter htmFilter = new FileFilter() {

		public boolean accept(File file) {
			if (file == null)
				return false;
			if (file.isDirectory())
				return true;
			String filename = file.getName();
			int index = filename.lastIndexOf('.');
			if (index == -1)
				return false;
			String postfix = filename.substring(index + 1);
			if ("htm".equalsIgnoreCase(postfix))
				return true;
			return false;
		}

		@Override
		public String getDescription() {
			return "HTML";
		}

	};

	private Action openAction;
	private Action saveAction;
	private Action saveAsAction;
	private Action saveAsAppletAction;
	private Action exitAction;
	private int fileMenuItemCount;
	private List<JComponent> recentFileMenuItems;

	@SuppressWarnings("serial")
	MenuBar(final QuantumBox box, final JFrame frame) {

		qwbFileChooser = new FileChooser();
		htmFileChooser = new FileChooser();
		recentFileMenuItems = new ArrayList<JComponent>();

		// file menu

		final JMenu fileMenu = new JMenu("File");
		fileMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				if (!recentFileMenuItems.isEmpty()) {
					for (JComponent x : recentFileMenuItems)
						fileMenu.remove(x);
				}
				String[] recentFiles = getRecentFiles();
				if (recentFiles != null) {
					int n = recentFiles.length;
					if (n > 0) {
						for (int i = 0; i < n; i++) {
							JMenuItem x = new JMenuItem((i + 1) + "  " + MiscUtil.getFileName(recentFiles[i]));
							final File rf = new File(recentFiles[i]);
							x.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									box.loadFile(rf);
									qwbFileChooser.rememberFile(rf.getPath());
								}
							});
							fileMenu.insert(x, fileMenuItemCount + i);
							recentFileMenuItems.add(x);
						}
						JSeparator s = new JSeparator();
						fileMenu.add(s, fileMenuItemCount + n);
						recentFileMenuItems.add(s);
					}
				}
			}
		});
		add(fileMenu);

		openAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				box.stop();
				if (!box.askSaveBeforeLoading())
					return;
				qwbFileChooser.setAcceptAllFileFilterUsed(false);
				qwbFileChooser.addChoosableFileFilter(qwbFilter);
				qwbFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
				qwbFileChooser.setDialogTitle("Open");
				qwbFileChooser.setApproveButtonMnemonic('O');
				qwbFileChooser.setAccessory(null);
				if (qwbFileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = qwbFileChooser.getSelectedFile();
					if (file.exists()) {
						box.loadFile(file);
					} else {
						JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(box), "File " + file + " was not found.", "File not found", JOptionPane.ERROR_MESSAGE);
					}
					qwbFileChooser.rememberFile(file.getPath());
				}
				qwbFileChooser.resetChoosableFileFilters();
			}
		};
		KeyStroke ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK);
		box.view2D.getInputMap().put(ks, "Open");
		box.view2D.getActionMap().put("Open", openAction);
		JMenuItem mi = new JMenuItem("Open...");
		mi.setAccelerator(ks);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		saveAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (box.getCurrentFile() == null) {
					saveAs(box, frame);
				} else {
					save(box);
				}
			}
		};
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK);
		box.view2D.getInputMap().put(ks, "Save");
		box.view2D.getActionMap().put("Save", saveAction);
		mi = new JMenuItem("Save");
		mi.setAccelerator(ks);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		saveAsAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				saveAs(box, frame);
			}
		};
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK);
		box.view2D.getInputMap().put(ks, "SaveAs");
		box.view2D.getActionMap().put("SaveAs", saveAsAction);
		mi = new JMenuItem("Save As...");
		mi.setAccelerator(ks);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		saveAsAppletAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (box.getCurrentFile() == null) {
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(box.view2D), "Sorry, you have to save the current model as a local file in order to create an applet for it.", "Applet not allowed", JOptionPane.ERROR_MESSAGE);
					return;
				}
				saveAsApplet(box, frame);
			}
		};
		mi = new JMenuItem("Save As Applet...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsAppletAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		fileMenu.addSeparator();
		fileMenuItemCount++;

		final Action propertyAction = box.view2D.getActionMap().get("Property");
		mi = new JMenuItem("Properties...");
		if (propertyAction != null)
			mi.setAccelerator((KeyStroke) propertyAction.getValue(Action.ACCELERATOR_KEY));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				propertyAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);
		fileMenuItemCount++;

		fileMenu.addSeparator();
		fileMenuItemCount++;

		exitAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				switch (box.askSaveOption()) {
				case JOptionPane.YES_OPTION:
					Action a = null;
					if (box.getCurrentFile() != null) {
						a = box.view2D.getActionMap().get("Save");
					} else {
						a = box.view2D.getActionMap().get("SaveAs");
					}
					if (a != null)
						a.actionPerformed(null);
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							QuantumBox.savePreferences(box);
							System.exit(0);
						}
					});
					break;
				case JOptionPane.NO_OPTION:
					QuantumBox.savePreferences(box);
					System.exit(0);
					break;
				case JOptionPane.CANCEL_OPTION:
					// do nothing
					break;
				}
			}
		};
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK);
		box.view2D.getInputMap().put(ks, "Quit");
		box.view2D.getActionMap().put("Quit", exitAction);
		mi = new JMenuItem("Exit");
		mi.setAccelerator(ks);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitAction.actionPerformed(e);
			}
		});
		fileMenu.add(mi);

		// edit menu

		JMenu menu = new JMenu("Edit");
		menu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}
		});
		add(menu);

		menu.add(box.view2D.getActionMap().get("Cut"));
		menu.add(box.view2D.getActionMap().get("Copy"));
		menu.add(box.view2D.getActionMap().get("Paste"));

		// view menu

		final JCheckBoxMenuItem miContour = new JCheckBoxMenuItem("Contour");
		final JCheckBoxMenuItem miRuler = new JCheckBoxMenuItem("Ruler");
		final JCheckBoxMenuItem miGrid = new JCheckBoxMenuItem("Grid");

		menu = new JMenu("View");
		menu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				miContour.setSelected(box.view2D.isContourShown());
				miRuler.setSelected(box.view2D.isRulerOn());
				miGrid.setSelected(box.view2D.isGridOn());
			}
		});
		add(menu);

		miContour.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view2D.setContourShown(src.isSelected());
				box.view2D.repaint();
				box.view2D.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		menu.add(miContour);

		miRuler.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view2D.setRulerOn(src.isSelected());
				box.view2D.repaint();
				box.view2D.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		menu.add(miRuler);

		miGrid.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
				box.view2D.setGridOn(src.isSelected());
				box.view2D.repaint();
				box.view2D.notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		menu.add(miGrid);
		menu.addSeparator();

		mi = new JMenuItem("More...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				box.view2D.createDialog(box.view2D);
			}
		});
		menu.add(mi);

		// model menu

		menu = new JMenu("Models");
		add(menu);

		JMenu subMenu = new JMenu("Quantum Waves in Containers");
		menu.add(subMenu);

		mi = new JMenuItem("Quantum Wave in a Box");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				box.loadModel("resources/box1.qwb");
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Quantum Wave in a Box with Circular Wells");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				box.loadModel("resources/box2.qwb");
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Quantum Wave in a Box with Circular Barriers");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				box.loadModel("resources/box3.qwb");
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Quantum Wave in a Circular Container");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				box.loadModel("resources/circle1.qwb");
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Quantum Wave in a Circular Container with Elliptical Barriers");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				box.loadModel("resources/circle2.qwb");
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Quantum Wave in a Low Annular Container");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				box.loadModel("resources/circle3.qwb");
			}
		});
		subMenu.add(mi);

		subMenu = new JMenu("Diffraction and Interference");
		menu.add(subMenu);

		mi = new JMenuItem("Double Slit");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				box.loadModel("resources/doubleslit.qwb");
			}
		});
		subMenu.add(mi);

		mi = new JMenuItem("Single and Double Slits");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				box.loadModel("resources/singledoubleslit.qwb");
			}
		});
		subMenu.add(mi);

		// help menu

		menu = new JMenu("Help");
		add(menu);

		final Action scriptAction = box.view2D.getActionMap().get("Script");
		mi = new JMenuItem("Script Console...");
		if (scriptAction != null)
			mi.setAccelerator((KeyStroke) scriptAction.getValue(Action.ACCELERATOR_KEY));
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scriptAction.actionPerformed(e);
			}
		});
		menu.add(mi);

		mi = new JMenuItem("Keyboard Shortcuts...");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.showKeyboardShortcuts(frame);
			}
		});
		menu.add(mi);

		if (!System.getProperty("os.name").startsWith("Mac")) {
			mi = new JMenuItem("About...");
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Helper.showAbout(frame);
				}
			});
			menu.add(mi);
		}

	}

	void setLatestPath(String latestPath, String type) {
		if (latestPath != null) {
			if ("htm".equalsIgnoreCase(type)) {
				htmFileChooser.setCurrentDirectory(new File(latestPath));
			} else if ("qwb".equalsIgnoreCase(type)) {
				qwbFileChooser.setCurrentDirectory(new File(latestPath));
			}
		}
	}

	String getLatestPath(String type) {
		if ("htm".equalsIgnoreCase(type))
			return htmFileChooser.getLatestPath();
		return qwbFileChooser.getLatestPath();
	}

	void addRecentFile(String path) {
		if (path != null)
			qwbFileChooser.addRecentFile(path);
	}

	String[] getRecentFiles() {
		return qwbFileChooser.getRecentFiles();
	}

	private void save(QuantumBox box) {
		try {
			box.saveState(new FileOutputStream(box.getCurrentFile()));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void saveAs(QuantumBox box, JFrame frame) {
		qwbFileChooser.setAcceptAllFileFilterUsed(false);
		qwbFileChooser.addChoosableFileFilter(qwbFilter);
		qwbFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		qwbFileChooser.setDialogTitle("Save");
		qwbFileChooser.setApproveButtonMnemonic('S');
		if (qwbFileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File file = qwbFileChooser.getSelectedFile();
			if (!file.toString().toLowerCase().endsWith(".qwb")) {
				file = new File(file.getParentFile(), MiscUtil.getFileName(file.toString()) + ".qwb");
			}
			boolean b = true;
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(frame, "File " + file.getName() + " exists, overwrite?", "File exists", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					b = false;
				}
			}
			if (b) {
				box.setCurrentFile(file);
				try {
					box.saveState(new FileOutputStream(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			qwbFileChooser.rememberFile(file.getPath());
		}
	}

	private void saveAsApplet(QuantumBox box, JFrame frame) {
		htmFileChooser.setAcceptAllFileFilterUsed(false);
		htmFileChooser.addChoosableFileFilter(htmFilter);
		htmFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		htmFileChooser.setDialogTitle("Save As Applet");
		htmFileChooser.setApproveButtonMnemonic('S');
		if (htmFileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File file = htmFileChooser.getSelectedFile();
			if (!file.toString().toLowerCase().endsWith(".htm")) {
				file = new File(file.getParentFile(), MiscUtil.getFileName(file.toString()) + ".htm");
			}
			boolean b = true;
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(frame, "File " + file.getName() + " exists, overwrite?", "File exists", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					b = false;
				}
			}
			if (b) {
				box.saveApplet(file);
			}
			htmFileChooser.rememberFile(file.getPath());
		}
	}

}
