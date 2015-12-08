/*******************************************************************************
 * Copyright 2015 Cameron Gregor
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License
 *******************************************************************************/
package org.openntf.bootleg;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.openntf.bootleg.util.BootlegUtil;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class BootlegActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.openntf.bootleg"; //$NON-NLS-1$

	// The shared instance
	private static BootlegActivator plugin;
	
	private FileHandler logHandler = null;
	
	/**
	 * The constructor
	 */
	public BootlegActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		System.out.println("Bootleg Activated");
		
		BootlegUtil.BOOTLEG_LOG.getLogger().setLevel(Level.INFO);
		
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		
		if (this.logHandler != null) {
			this.logHandler.close();
		}
		
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static BootlegActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	
	public FileHandler getFileHandler() {

		if (this.logHandler == null) {
		
			try {

				boolean success = new java.io.File(System.getProperty("user.home"), ".bootleg").mkdirs();			
				this.logHandler = new FileHandler("%h/.bootleg/bootleg-log.%u.%g.xml", 1024 * 1024, 10, false);
				
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			
		}
		
		return this.logHandler;
	}
	
	public void closeFileHandler() {
		
		if (this.logHandler != null) {
			this.logHandler.close();
			this.logHandler = null;
		}
		
	}
	
}
