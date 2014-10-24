//
// Copyright (c) 2009 Alexei Svitkine
// 
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
// 
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//

package com.fizzysoft.sdu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * RecentDocumentsManager is an abstract base class that provides
 * management of the "Open Recent" menu that is useful to desktop
 * applications. Subclasses are expected to provide implementations
 * of the writeRecentDocs(), readRecentDocs() and openFile() methods.
 *
 * @author Alexei Svitkine
 * @version November 24, 2009
 */
public abstract class RecentDocumentsManager {
	private static final boolean isMac = (System.getProperty("mrj.version") != null);

	private int maxRecentDocuments;

	protected RecentDocumentsManager() {
		maxRecentDocuments = 10;
	}

	protected abstract void writeRecentDocs(byte[] data);
	protected abstract byte[] readRecentDocs();
	protected abstract void openFile(File file, ActionEvent event);

	public JMenu createOpenRecentMenu() {
		return new OpenRecentMenu();
	}

	public void addDocument(File file, Properties properties) {
		String fileLocation = null;
		try {
			fileLocation = file.getCanonicalPath();
		} catch (IOException e) { }

		if (fileLocation != null) {
			LinkedList<RecentDocument> recentDocuments = loadListOfRecentDocuments();
			LinkedList<RecentDocument> newRecentDocuments = new LinkedList<RecentDocument>();

			for (RecentDocument rd: recentDocuments) {
				String rdFileLocation = rd.getLocation();
				if (!fileLocation.equals(rdFileLocation)) {
					newRecentDocuments.add(rd);
				}
			}

			newRecentDocuments.addFirst(new RecentDocument(fileLocation, properties));
			while (newRecentDocuments.size() > maxRecentDocuments) {
				newRecentDocuments.removeLast();
			}
			saveListOfRecentDocuments(newRecentDocuments);
		}
	}

	public void clear() {
		LinkedList<RecentDocument> recentDocuments = new LinkedList<RecentDocument>();
		saveListOfRecentDocuments(recentDocuments);
	}

	public int getMaxRecentDocuments() {
		return maxRecentDocuments;
	}
	
	public void setMaxRecentDocuments(int maxRecentDocuments) {
		this.maxRecentDocuments = maxRecentDocuments;
	}

	protected LinkedList<RecentDocument> loadListOfRecentDocuments() {
		LinkedList<RecentDocument> recentDocuments = new LinkedList<RecentDocument>();
		byte[] data = readRecentDocs();
		if (data != null && data.length > 0) {
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			XMLDecoder decoder = new XMLDecoder(in);
			try {
				while (recentDocuments.size() < maxRecentDocuments) {
					RecentDocument rd = (RecentDocument) decoder.readObject();
					if (rd.isValid()) {
						recentDocuments.add(rd);
					}
				}
			} catch (ArrayIndexOutOfBoundsException exception) {
			} finally {
				decoder.close();
			}
		}
		return recentDocuments;
	}

	protected void saveListOfRecentDocuments(LinkedList<RecentDocument> recentDocuments) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLEncoder encoder = new XMLEncoder(out);
		for (RecentDocument doc : recentDocuments)
			encoder.writeObject(doc);
		encoder.close();
		writeRecentDocs(out.toByteArray());
	}

	private static SoftReference<JFileChooser> iconFileChooserRef;
	protected Icon getFileIcon(File file) {
		JFileChooser fc = null;
		if (iconFileChooserRef != null) {
			fc = iconFileChooserRef.get();
		}
		if (fc == null) {
			fc = new JFileChooser();
			iconFileChooserRef = new SoftReference<JFileChooser>(fc);
		}
		return fc.getIcon(file);
	}

	public static class RecentDocument {
		private String location; // path
		private Properties properties;

		public RecentDocument() {
		}

		public RecentDocument(String location, Properties properties) {
			this.location = location;
			this.properties = properties;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public String getLocation() {
			return location;
		}

		public void setProperties(Properties properties) {
			this.properties = properties;
		}

		public Properties getProperties() {
			return properties;
		}

		public boolean isValid() {
			return new File(location).exists();
		}
	}

	private class RecentDocumentMenuItem extends JMenuItem implements ActionListener {
		private RecentDocument rd;

		public RecentDocumentMenuItem(RecentDocument rd) {
			super();
			this.rd = rd;
			File file = new File(rd.getLocation());
			Icon fileIcon = getFileIcon(file);
			if (fileIcon != null) {
				setIcon(fileIcon);
			}
			setText(file.getName());
			addActionListener(this);
		}

		public void actionPerformed(ActionEvent event) {
			openFile(new File(rd.getLocation()), event);
		}
	}

	private class OpenRecentMenu extends JMenu {
		private OpenRecentMenuUpdater updater;
		private JMenuItem clearItem;

		public OpenRecentMenu() {
			super("Open Recent");
			clearItem = new JMenuItem("Clear Menu");
			if (!isMac) {
				setMnemonic(KeyEvent.VK_T);
				clearItem.setMnemonic(KeyEvent.VK_M);
			}
			updater = new OpenRecentMenuUpdater();
			populateWithRecentDocuments();
			// the only strong reference to the updater is from this menu
			// once the menu is no longer reachable, the weak property listener will unregister
			addMenuListener(updater);
		}

		private void populateWithRecentDocuments() {
			populateWithRecentDocuments(loadListOfRecentDocuments());
		}

		private void populateWithRecentDocuments(LinkedList<RecentDocument> recentDocuments) {
			removeAll();
			clearItem.addActionListener(updater);
			if (recentDocuments.size() > 0) {
				for (RecentDocument rd: recentDocuments) {
					add(new RecentDocumentMenuItem(rd));
					/* TODO: resolve conflicts between different items w/ same name */
				}
				addSeparator();
				clearItem.setEnabled(true);
			} else {
				clearItem.setEnabled(false);
			}
			add(clearItem);
		}

		private class OpenRecentMenuUpdater implements MenuListener, ActionListener {
			public void menuSelected(MenuEvent evt) {
				populateWithRecentDocuments();
			}

			public void menuCanceled(MenuEvent evt) {}
			public void menuDeselected(MenuEvent evt) {}

			public void actionPerformed(ActionEvent evt) { // "Clear Menu"
				RecentDocumentsManager.this.clear();
				populateWithRecentDocuments();
			}
		}
	}
}
