/*******************************************************************************
 * Copyright 2015 Cameron Gregor
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License
 *******************************************************************************/
package org.openntf.bootlegger.builder;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.openntf.bootlegger.util.BootleggerUtil;

import com.ibm.designer.domino.ide.resources.DominoResourcesPlugin;
import com.ibm.designer.domino.ide.resources.NsfException;
import com.ibm.designer.domino.ide.resources.project.IDominoDesignerProject;
import com.ibm.designer.domino.team.util.SyncUtil;
import com.ibm.designer.domino.xsp.internal.builder.XFacesBuilder;

public class BootleggerNature implements IProjectNature {

	/**
	 * ID of this project nature
	 */
	public static final String NATURE_ID = "org.openntf.bootlegger.bootleggerNature";

	private IProject project;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {

		addBuilderToProject(project, BootleggerBuilder.BUILDER_ID, XFacesBuilder.XFACES_BUILDER, false);
			
	}

	public void deconfigure() throws CoreException {

		removeBuilderFromProject(project, BootleggerBuilder.BUILDER_ID);

	}
	
	private void addBuilderToProject(IProject project, String builderId, String refBuilderId, boolean before)
			throws CoreException {

		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderId)) {
				return;
			}
		}
		
		Integer refBuilderPos = null;
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(refBuilderId)) {
				refBuilderPos = i;
			}
		}

		if (refBuilderPos == null) {
			BootleggerUtil.logInfo("Could not find the reference builder " + refBuilderId );
			return;
		}
		
		int commandsLength = commands.length;
		ICommand[] newCommands = new ICommand[commandsLength + 1];

		int numberBefore = 0;
		
		if (before) {
			numberBefore = refBuilderPos;
		} else {
			numberBefore = refBuilderPos + 1;
		}
		
		int numberAfter = commandsLength - numberBefore; 
		
		int newBuilderPos = numberBefore;
		
		// Copy commands up to refBuilderPos
		System.arraycopy(commands, 0, newCommands, 0, numberBefore);

		ICommand command = desc.newCommand();
		command.setBuilderName(builderId);
		newCommands[newBuilderPos] = command;
		
		// System commands after
		System.arraycopy(commands, numberBefore, newCommands, newBuilderPos + 1, numberAfter);
		
		desc.setBuildSpec(newCommands);
		project.setDescription(desc, null);


		
	}
	
	private void removeBuilderFromProject(IProject project, String builderId)
			throws CoreException {

		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderId)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i,
						commands.length - i - 1);
				description.setBuildSpec(newCommands);
				project.setDescription(description, null);
				return;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	public IProject getDiskProject() {

		IDominoDesignerProject dproj;
		try {
			dproj = DominoResourcesPlugin.getDominoDesignerProject(project);

			IProject diskProject = SyncUtil.getAssociatedDiskProject(dproj,
					true);

			return diskProject;

		} catch (NsfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core
	 * .resources.IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

}
