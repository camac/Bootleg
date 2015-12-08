/*******************************************************************************
 * Copyright 2015 Cameron Gregor
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License
 *******************************************************************************/
package org.openntf.bootleg.builder;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openntf.bootleg.action.BootlegAction;
import org.openntf.bootleg.util.BootlegUtil;

import com.ibm.designer.domino.ide.resources.DominoResourcesPlugin;
import com.ibm.designer.domino.ide.resources.NsfException;
import com.ibm.designer.domino.ide.resources.project.IDominoDesignerProject;

public class BootlegBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "org.openntf.bootleg.BootlegBuilder";

	IDominoDesignerProject designerProject = null;
	BootlegAction bootlegAction = null;

	public BootlegBuilder() {

	}

	public BootlegBuilder(IDominoDesignerProject designerProject) {
		this.designerProject = designerProject;
	}

	public IDominoDesignerProject getDesignerProject() {
		return this.designerProject;
	}

	public void initialize() {

		if (this.designerProject != null) {
			this.bootlegAction = new BootlegAction();
			this.bootlegAction.setProject(this.designerProject);
		}

	}

	@SuppressWarnings("rawtypes")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {

		BootlegUtil.logInfo("**** Running Bootleg Builder");

		try {
			this.designerProject = DominoResourcesPlugin
					.getDominoDesignerProject(getProject());
		} catch (NsfException e) {
			e.printStackTrace();
		}

		if ((this.designerProject == null)
				|| (!this.designerProject.isProjectInitialized())) {
			return null;
		}

		if (!BootlegUtil.isAutoExport(this.designerProject.getProject())) {
			BootlegUtil.logInfo("Bootleg is not set to AutoExport. Exiting");
			return null;
		}

		initialize();

		BootlegUtil.cleanMarkers(this.designerProject.getProject());

		if (!bootlegAction.checkSetup()) {
			return null;
		}

		try {

			IResourceDelta delta = getDelta(getProject());

			if (delta != null) {
				boolean isRelevant = isRelevant(delta);

				if (!isRelevant) {
					return null;
				}

				delta.accept(new BootlegVisitor(monitor, this));

				ResourcesPlugin.getWorkspace().save(false, monitor);

				generateBuilderConfig();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	private boolean isRelevant(IResourceDelta delta) throws CoreException {
		final boolean[] arrayOfBoolean = new boolean[1];

		delta.accept(new IResourceDeltaVisitor() {

			public boolean visit(IResourceDelta paramAnonymousIResourceDelta) {

				switch (paramAnonymousIResourceDelta.getKind()) {
				case 1:
					if ((paramAnonymousIResourceDelta.getResource() instanceof IFile)) {
						arrayOfBoolean[0] = true;
						return false;
					}
					break;
				case 4:
					if ((paramAnonymousIResourceDelta.getResource() instanceof IFile)) {
						arrayOfBoolean[0] = true;
						return false;
					}
					break;
				case 2:
					IResource localIResource = paramAnonymousIResourceDelta
							.getResource();
					if (localIResource.getType() == 1) {
						arrayOfBoolean[0] = true;
						return false;
					}
					break;
				}
				return true;
			}
		});
		return arrayOfBoolean[0];
	}

	public void exportCustomControl(IResource designerResource,
			IProgressMonitor monitor) {

		if (designerResource instanceof IFile) {

			this.bootlegAction.exportCustomControl(designerResource, monitor);

		}
	}

	public void deleteCustomControl(IResource designerResource,
			IProgressMonitor monitor) {
		if (designerResource instanceof IFile) {
			this.bootlegAction.deleteCustomControl(designerResource, monitor);
		}
	}

	public void generateBuilderConfig() {

		try {
			this.bootlegAction.generateConfig();
		} catch (IOException e) {
			BootlegUtil.logError(e.getMessage());
		}

	}

}
