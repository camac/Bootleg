package org.openntf.xsp.bootlegged.library;

import org.openntf.xsp.bootlegged.config.BootleggedConfigs;

import com.ibm.xsp.library.AbstractXspLibrary;

public class BootleggedXspLibrary extends AbstractXspLibrary {

	public static final String LIBRARY_ID = "org.openntf.xsp.bootlegged.library";
	public static final String PLUGIN_ID = "org.openntf.xsp.bootlegged";

	@Override
	public String getLibraryId() {
		return LIBRARY_ID;
	}

	@Override
	public String getPluginId() {
		return PLUGIN_ID;
	}

	@Override
	public String[] getXspConfigFiles() {

		String[] configs = new String[] {};

		configs = BootleggedConfigs.addXspConfigFiles(configs);

		return configs;
	}

}
