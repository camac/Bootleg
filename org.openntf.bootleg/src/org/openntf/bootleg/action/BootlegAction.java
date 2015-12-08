/*******************************************************************************
 * Copyright 2015 Cameron Gregor
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License
 *******************************************************************************/
package org.openntf.bootleg.action;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.Marker;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openntf.bootleg.util.BootlegUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.ide.resources.DominoResourcesPlugin;
import com.ibm.designer.domino.ide.resources.NsfException;
import com.ibm.designer.domino.ide.resources.project.IDominoDesignerProject;
import com.ibm.designer.domino.team.action.AbstractTeamHandler;
import com.ibm.designer.domino.team.util.SyncUtil;

public class BootlegAction extends AbstractTeamHandler {

	private List<IFile> filesTofilter = new ArrayList<IFile>();

	private String pluginSourceFolder = null;
	private String componentPackage = null;
	private String configPackage = null;
	private String targetNamespace = null;
	private String targetPrefix = null;
	private String targetCategory = null;
	private String configClassName = null;
	private String exportRegex = null;

	public BootlegAction() {

	}

	private void initBootlegSettings() {

		IProject prj = this.desProject.getProject();

		if (prj != null) {

			this.pluginSourceFolder = BootlegUtil.getPluginSourceFolder(prj);
			this.componentPackage = BootlegUtil.getComponentPackage(prj);
			this.configPackage = BootlegUtil.getConfigPackage(prj);
			this.configClassName = BootlegUtil.getConfigClassName(prj);
			this.exportRegex = BootlegUtil.getCustomControlRegex(prj);
			this.targetNamespace = BootlegUtil.getTargetNamespace(prj);
			this.targetPrefix = BootlegUtil.getTargetPrefix(prj);
			this.targetCategory = BootlegUtil.getTargetCategory(prj);

		} else {
			BootlegUtil
					.logError("DesignerProject.getProject() is null, couldn't retrieve Bootleg settings");
		}

	}

	private void addProjectErrorMarker(String message) {
		BootlegUtil.addMarker(this.desProject.getProject(), message,
				Marker.SEVERITY_ERROR);
	}

	public boolean checkSetup() {

		boolean allgood = true;
		boolean sourcefolderexists = false;

		// Check Source Folder exists
		if (StringUtil.isEmpty(getPluginSourceFolder())) {

			BootlegUtil.logError("Source Plugin Folder is not set");
			addProjectErrorMarker("Plugin Source Folder is not set");
			allgood = false;

		} else {

			File sourcefolder = new File(getPluginSourceFolder());

			if (!sourcefolder.exists()) {

				String msg = String.format(
						"Plugin Source Folder: '%s' does not exist",
						getPluginSourceFolder());
				BootlegUtil.logError(msg);
				addProjectErrorMarker(msg);
				allgood = false;
			} else {
				sourcefolderexists = true;
			}

		}

		if (StringUtil.isEmpty(getComponentPackage())) {
			String msg = "Package for Components is not Set";
			BootlegUtil.logError(msg);
			addProjectErrorMarker(msg);
			allgood = false;
		} else if (sourcefolderexists) {

			File file = getTargetComponentFolder();

			if (!file.exists()) {
				String msg = String
						.format("Folder for Component package: '%s' does not exist in Plugin Source Folder",
								getComponentPackage());
				BootlegUtil.logError(msg);
				addProjectErrorMarker(msg);
				allgood = false;
			}

		}

		if (StringUtil.isEmpty(getConfigPackage())) {
			String msg = "Package for Config Files is not Set";
			BootlegUtil.logError(msg);
			addProjectErrorMarker(msg);
			allgood = false;
		} else if (sourcefolderexists) {

			File file = getTargetConfigFolder();

			if (!file.exists()) {
				String msg = String
						.format("Folder for Config Package: '%s' does not exist in Plugin Source Folder",
								getConfigPackage());
				BootlegUtil.logError(msg);
				addProjectErrorMarker(msg);
				allgood = false;
			}

		}

		return allgood;

	}

	public void setProject(IDominoDesignerProject designerProject) {

		this.desProject = designerProject;
		initBootlegSettings();

	}

	public String getPluginSourceFolder() {
		return this.pluginSourceFolder;
	}

	public String getPluginSourceFolderForwardSlash() {

		if (StringUtil.isEmpty(this.pluginSourceFolder))
			return null;
		String slashed = this.pluginSourceFolder.replace("\\", "/");
		return slashed;
	}

	private String getComponentPackage() {
		return this.componentPackage;
	}

	private String getComponentPackageSlashed() {

		if (StringUtil.isEmpty(this.componentPackage))
			return null;

		String slashed = componentPackage.replace(".", "/");

		return slashed;
	}

	private String getConfigClassName() {
		if (StringUtil.isEmpty(this.configClassName)) {
			return "BootleggedConfigs";
		}
		return this.configClassName;
	}

	private String getConfigPackage() {
		return this.configPackage;
	}

	private String getConfigPackageSlashed() {

		if (StringUtil.isEmpty(this.configPackage))
			return null;
		String slashed = configPackage.replace(".", "/");
		return slashed;

	}

	private String getCompositeFileEntryFromCustomControl(
			IResource designerResource) {
		String nameOnly = StringUtil.replace(designerResource.getName(),
				".xsp", "");
		return "/" + getComponentPackageSlashed() + "/" + nameOnly;
	}

	private String getJavaNameFromCustomControl(IResource designerResource) {

		String javaName = StringUtil.replace(designerResource.getName(),
				".xsp", "");

		if (StringUtil.isNotEmpty(javaName)) {
			String first = javaName.substring(0, 1).toUpperCase();
			String rest = javaName.substring(1);
			return first + rest + ".java";
		} else {
			return null;
		}

	}

	private String getXspConfigNameFromCustomControl(IResource designerResource) {

		String javaName = StringUtil.replace(designerResource.getName(),
				".xsp", ".xsp-config");
		return javaName;

	}

	private String getJavaName(IResource designerResource) {

		String ccName = StringUtil.replace(designerResource.getName(), ".xsp",
				"");

		if (StringUtil.isNotEmpty(ccName)) {
			String first = ccName.substring(0, 1).toUpperCase();
			String rest = ccName.substring(1);
			ccName = first + rest;
		}
		return ccName;

	}

	public IFile findCustomControlXspConfig(IResource designerResource) {

		String ccName = StringUtil.replace(designerResource.getName(), ".xsp",
				".xsp-config");

		String xcFileName = "/CustomControls/" + ccName;

		IFile file = desProject.getProject().getFile(xcFileName);

		if (file != null) {
			return file;
		}

		return null;

	}

	public IFile findCustomControlJava(IResource designerResource) {

		String ccName = getJavaName(designerResource);

		ccName = ccName.replace("_", "_005f");

		String javaFileName = "/Local/xsp/" + ccName + ".java";

		IFile file = desProject.getProject().getFile(javaFileName);

		if (file != null) {
			return file;
		}

		return null;

	}

	public File getTargetConfigFolder() {

		String path = getPluginSourceFolderForwardSlash() + "/"
				+ getConfigPackageSlashed();
		File file = new File(path);
		return file;

	}

	public File getTargetConfigFile() {

		if (StringUtil.isEmpty(getConfigClassName()))
			return null;

		String configFile = getConfigClassName() + ".java";
		File file = new File(getTargetConfigFolder(), configFile);
		return file;

	}

	public File getTargetComponentFolder() {

		String path = getPluginSourceFolderForwardSlash() + "/"
				+ getComponentPackageSlashed();
		File file = new File(path);
		return file;

	}

	public File getTargetJavaFile(String javaName) {

		File file = new File(getTargetComponentFolder(), javaName);
		return file;

	}

	public File getTargetXspConfigFile(String xcName) {

		File file = new File(getTargetConfigFolder(), xcName);
		return file;

	}

	private void deleteFile(File targetFile) {
		if (targetFile.exists()) {
			targetFile.delete();
		}
	}

	private void exportJavaFile(IFile sourceFile, File targetFile) {

		try {

			InputStream is = sourceFile.getContents();

			try {

				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader reader = new BufferedReader(isr);
				String line = "";

				FileWriter writer = new FileWriter(targetFile);

				while ((line = reader.readLine()) != null) {

					if (StringUtil.startsWithIgnoreCase(line, "package xsp;")) {
						line = String.format("package %s;",
								getComponentPackage());
					}

					writer.write(String.format("%s%n", line));
				}

				writer.close();

				is.close();
				isr.close();
				reader.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void exportFile(IFile sourceFile, File targetFile) {

		try {

			InputStream is = sourceFile.getContents();

			try {

				FileOutputStream fos = new FileOutputStream(targetFile);

				byte[] buffer = new byte[8 * 1024];
				int bytesread;
				while ((bytesread = is.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesread);
				}

				try {
					is.close();
				} catch (IOException e) {

				}
				try {
					fos.close();
				} catch (IOException e) {

				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getTargetNamespace() {
		return this.targetNamespace;
	}

	private String getTargetPrefix() {
		return this.targetPrefix;
	}

	private String getTargetCategory() {
		return this.targetCategory;
	}

	private void modifyXspConfig(File targetXspConfig, String compositeFile) {

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(targetXspConfig);

			// Modify the namespace if specified
			if (StringUtil.isNotEmpty(getTargetNamespace())) {

				NodeList list = doc.getElementsByTagName("namespace-uri");
				Node compFile = list.item(0);
				compFile.setTextContent(getTargetNamespace());

			}
			// Modify the prefix if specified
			if (StringUtil.isNotEmpty(getTargetPrefix())) {

				NodeList list = doc.getElementsByTagName("default-prefix");
				Node compFile = list.item(0);
				compFile.setTextContent(getTargetPrefix());

			}

			// Modify the composite-file element
			if (StringUtil.isNotEmpty(compositeFile)) {
				NodeList list = doc.getElementsByTagName("composite-file");
				Node compFile = list.item(0);
				compFile.setTextContent(compositeFile);
			}

			// Modify the Control Palette Category
			if (StringUtil.isNotEmpty(getTargetCategory())) {

				String xpath1 = "/faces-config/composite-component/composite-extension/designer-extension";
				String xpath2 = "/faces-config/composite-component/composite-extension/designer-extension/category";

				XPath x = XPathFactory.newInstance().newXPath();

				try {

					NodeList list = (NodeList) x.evaluate(xpath2,
							doc.getDocumentElement(), XPathConstants.NODESET);

					Node catNode = list.item(0);
					
					if (catNode != null) {

						String text = catNode.getTextContent();
						
						if (StringUtil.isEmpty(text)) {
							
							catNode.setTextContent(getTargetCategory());
							
						}

						
					} else {

						list = (NodeList) x.evaluate(xpath1,
								doc.getDocumentElement(),
								XPathConstants.NODESET);

						Node node = list.item(0);

						if (node != null) {

							catNode = doc.createElement("category");
							Text catText = doc.createTextNode(getTargetCategory());
							catNode.appendChild(catText);
							
							node.appendChild(catNode);
							
						}
						
					}

				} catch (XPathExpressionException e) {
					e.printStackTrace();
				}

			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(targetXspConfig);
			transformer.transform(source, result);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}

	}

	public void exportCustomControl(IResource designerResource,
			IProgressMonitor monitor) {

		String ccName = designerResource.getName();

		BootlegUtil.logTrace("About To Export" + ccName);

		if (StringUtil.isNotEmpty(exportRegex)) {

			if (!ccName.matches(exportRegex)) {
				BootlegUtil.logTrace("Custom Control '" + ccName
						+ "' not exported as it does not match regex '"
						+ exportRegex + "'");
				return;
			}

		}

		try {

			BootlegUtil.logInfo("Exporting " + designerResource.getName());

			IFile javaFile = findCustomControlJava(designerResource);

			if (javaFile.exists()) {

				String javaName = getJavaNameFromCustomControl(designerResource);
				File file = getTargetJavaFile(javaName);
				exportJavaFile(javaFile, file);

			}

			IFile xcFile = findCustomControlXspConfig(designerResource);

			if (xcFile.exists()) {

				String xspConfigName = getXspConfigNameFromCustomControl(designerResource);
				File file = getTargetXspConfigFile(xspConfigName);
				exportFile(xcFile, file);

				String cf = getCompositeFileEntryFromCustomControl(designerResource);
				modifyXspConfig(file, cf);

			}

			// Transformer transformer = getTransformer();
			//
			// if (diskFile != null) {
			// filter(diskFile, transformer, monitor);
			// BootlegUtil.logInfo("Filtered " + diskFile.getName());
			// }

			// } catch (TransformerConfigurationException e) {
			//
			// String message = e.getMessage();
			// BootlegUtil.addMarker(designerResource, "Bootleg Error " +
			// message, IMarker.SEVERITY_INFO);
			//
			// } catch (TransformerException e) {
			// String message = e.getMessage();
			// BootlegUtil.addMarker(designerResource, "Bootleg Error " +
			// message, IMarker.SEVERITY_INFO);
			//
			// } catch (CoreException e) {
			// String message = e.getMessage();
			// BootlegUtil.addMarker(designerResource, "Bootleg Error " +
			// message, IMarker.SEVERITY_INFO);
			//
			// } catch (FileNotFoundException e) {
			// String message = e.getMessage();
			// BootlegUtil.addMarker(designerResource, "Bootleg Error " +
			// message, IMarker.SEVERITY_WARNING);
			// } catch (IOException e) {
			// String message = e.getMessage();
			// BootlegUtil.addMarker(designerResource, "Bootleg Error " +
			// message, IMarker.SEVERITY_WARNING);
		} finally {

		}

	}

	public void deleteCustomControl(IResource designerResource,
			IProgressMonitor monitor) {

		BootlegUtil.logTrace("About To Delete" + designerResource.getName());

		try {

			BootlegUtil.logInfo("Exporting " + designerResource.getName());

			String javaName = getJavaNameFromCustomControl(designerResource);
			File targetJavaFile = getTargetJavaFile(javaName);
			deleteFile(targetJavaFile);

			String xspConfigName = getXspConfigNameFromCustomControl(designerResource);
			File targetXspConfigFile = getTargetXspConfigFile(xspConfigName);
			deleteFile(targetXspConfigFile);

		} finally {

		}

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		BootlegUtil.logInfo("**** Performing Explicit Filtering");

		processSelection(event);

		if (this.desProject == null) {

			BootlegUtil.logError("Could not determine the Designer Project");

			return null;
		}

		initBootlegSettings();

		for (IFile designerFile : filesTofilter) {

			BootlegUtil.logTrace(designerFile.getName()
					+ " has been explicitly told to Export - Export It");

			if (BootlegUtil.shouldExport(designerFile)) {
				exportCustomControl(designerFile, new NullProgressMonitor());
			} else {
				BootlegUtil.logTrace("Not Configured to Export "
						+ designerFile.getName());
			}

		}
		try {
			generateConfig();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return super.execute(event);
	}

	private void processSelection(ExecutionEvent event) {

		this.desProject = null;

		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof StructuredSelection) {

			StructuredSelection ss = (StructuredSelection) selection;

			IProject project = null;

			List<?> list = ss.toList();

			// Check to determine first project
			for (Object o : list) {

				if (o instanceof IFile) {

					IFile file = (IFile) o;

					if (project == null) {
						project = file.getProject();
					}

				}

			}

			// if we found the project, assign the this.desProject
			if (project != null) {

				if (DominoResourcesPlugin.isDominoDesignerProject(project)) {
					try {
						this.desProject = DominoResourcesPlugin
								.getDominoDesignerProject(project);
					} catch (NsfException e) {
						BootlegUtil.logError(e.getMessage());
					}
				}

			} else {
				// If we can't figure out the project then it is no good
				return;
			}

			// Add files that belong to that project
			for (Object o : list) {

				if (o instanceof IFile) {

					IFile file = (IFile) o;

					if (file.getProject() == project) {
						filesTofilter.add(file);
					}

				}

			}

		}

	}

	private List<String> findConfigs() {

		File configFolder = getTargetConfigFolder();

		List<String> configs = new ArrayList<String>();

		for (File file : configFolder.listFiles()) {
			if (StringUtil.endsWithIgnoreCase(file.getName(), "xsp-config")) {
				configs.add(file.getName());
			}
		}

		return configs;

	}

	public void generateConfig() throws IOException {

		File configFile = getTargetConfigFile();

		List<String> configs = findConfigs();

		String packageName = getConfigPackage();
		String packageNameSlashed = "/" + getConfigPackageSlashed() + "/";

		FileWriter fw = new FileWriter(configFile);

		fw.write(String.format("/*%n"));
		fw.write(String.format("* This Java File was generated by Bootleg%n"));
		fw.write(String
				.format("* If you modify it you will probably lose changes!%n"));
		fw.write(String.format("*/%n"));

		fw.write(String.format("package %s;%n%n", packageName));
		fw.write(String.format("public class %s {%n%n", getConfigClassName()));

		// Stolen from ExtlibConfig
		fw.write(String
				.format("\tpublic static String[] concat(String[] s1, String[] s2) {%n%n"));
		fw.write(String
				.format("\t\tString[] s = new String[s1.length + s2.length];%n"));
		fw.write(String
				.format("\t\tSystem.arraycopy(s1, 0, s, 0, s1.length);%n"));
		fw.write(String
				.format("\t\tSystem.arraycopy(s2, 0, s, s1.length, s2.length);%n"));
		fw.write(String.format("\t\treturn s;%n"));
		fw.write(String.format("\t}%n%n"));

		// add the Config Files
		fw.write(String
				.format("\tpublic static String[] addXspConfigFiles(String[] files) {%n%n"));
		fw.write(String.format("\t\treturn concat(files, new String[] {%n"));

		for (String config : configs) {
			fw.write(String.format("\t\t\t\"%s\",%n", packageNameSlashed
					+ config));
		}

		fw.write(String.format("\t\t});%n"));
		fw.write(String.format("\t}%n"));
		fw.write(String.format("}%n"));

		fw.close();

	}

}
