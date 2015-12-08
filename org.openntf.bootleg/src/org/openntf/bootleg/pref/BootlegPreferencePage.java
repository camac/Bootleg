/*******************************************************************************
 * Copyright 2015 Cameron Gregor
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License
 *******************************************************************************/
package org.openntf.bootleg.pref;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.openntf.bootleg.BootlegActivator;

import com.bdaum.overlayPages.FieldEditorOverlayPage;

public class BootlegPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage {

	public static final String PAGE_ID = "org.openntf.bootleg.bootlegPage";

	public static final String PREF_AUTOEXPORT = "autoExport";
	public static final String PREF_PLUGIN_SOURCEFOLDER = "pluginSourceFolder";
	public static final String PREF_PACKAGE_COMPONENT = "packageComponent";
	public static final String PREF_PACKAGE_CONFIG = "packageConfig";
	public static final String PREF_CC_EXPORT_REGEX = "customControlRegex"; 
	public static final String PREF_NAMESPACE = "targetNamespace"; 
	public static final String PREF_PREFIX = "targetPrefix"; 
	public static final String PREF_CONFIGCLASSNAME = "configClassName";
	
	public BootlegPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		IPreferenceStore store = BootlegActivator.getDefault().getPreferenceStore();
		return store;
	}

	@Override
	public void init(IWorkbench workbench) {
		setDescription("Bootleg Preferences");
	}

	@Override
	protected void createFieldEditors() {

		BooleanFieldEditor autoExport = new BooleanFieldEditor(PREF_AUTOEXPORT, "Auto Export on Build", getFieldEditorParent());
		addField(autoExport);
		
		StringFieldEditor regex = new StringFieldEditor(PREF_CC_EXPORT_REGEX, "Only Export Custom Controls matching this Regular Expression (Defaults to all)", getFieldEditorParent());
		addField(regex);
		
		DirectoryFieldEditor defaultFilter = new DirectoryFieldEditor(PREF_PLUGIN_SOURCEFOLDER, "Plugin Source Folder",
				getFieldEditorParent());
		addField(defaultFilter);
		
		StringFieldEditor packageComponent = new StringFieldEditor(PREF_PACKAGE_COMPONENT, "Package to export Components to", getFieldEditorParent());
		addField(packageComponent);
		
		StringFieldEditor packageConfig = new StringFieldEditor(PREF_PACKAGE_CONFIG, "Package to export Xsp-Config to", getFieldEditorParent());
		addField(packageConfig);

		StringFieldEditor configClass = new StringFieldEditor(PREF_CONFIGCLASSNAME, "Class name of Generated XspLibrary Contributor Class (Defaults to 'BootlegConfig')", getFieldEditorParent());
		addField(configClass);

		StringFieldEditor namespace = new StringFieldEditor(PREF_NAMESPACE, "Target Namespace (leave blank if no change)", getFieldEditorParent());
		addField(namespace);

		StringFieldEditor prefix = new StringFieldEditor(PREF_PREFIX, "Target Default Prefix (leave blank if no change)", getFieldEditorParent());
		addField(prefix);
		
	}

	@Override
	protected String getPageId() {
		return PAGE_ID;
	}
}
