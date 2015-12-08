/*******************************************************************************
 * Copyright 2015 Cameron Gregor
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License
 *******************************************************************************/
package org.openntf.bootleg.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openntf.bootleg.util.BootlegUtil;

import com.ibm.designer.domino.ide.resources.project.IDominoDesignerProject;
import com.ibm.designer.domino.team.util.SyncUtil;

public class BootlegVisitor implements IResourceDeltaVisitor {

	private IProgressMonitor monitor = null;
	private BootlegBuilder builder = null;
	private IDominoDesignerProject designerProject = null;

	public BootlegVisitor(IProgressMonitor monitor, BootlegBuilder builder) {
		this.monitor = monitor;
		this.builder = builder;
		this.designerProject = builder.getDesignerProject();
	}

	private void processDeletedDesignerFile(IResource designerFile) throws CoreException {
		if (BootlegUtil.shouldExport(designerFile)) {
			builder.deleteCustomControl(designerFile, monitor);
		} else {
			BootlegUtil.logTrace("Not Configured to delete " + designerFile.getName());
		}

	}

	private void processDesignerFile(IResource designerFile) throws CoreException {

		if (BootlegUtil.shouldExport(designerFile)) {
			builder.exportCustomControl(designerFile, monitor);
		} else {
			BootlegUtil.logTrace("Not Configured to Export " + designerFile.getName());
		}

	}

	private boolean processAdded(IResourceDelta delta) {
		try {

			BootlegUtil.logTrace("Processing Added");

			if (delta.getResource() instanceof IFile) {

				IFile designerFile = (IFile) delta.getResource();
				processDesignerFile(designerFile);

			}

		} catch (CoreException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean processChanged(IResourceDelta delta) {

		BootlegUtil.logTrace("Processing Changed");

		try {

			if (delta.getResource() instanceof IFile) {

				IFile designerFile = (IFile) delta.getResource();
				processDesignerFile(designerFile);

			}

		} catch (CoreException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean processDeleted(IResourceDelta delta) {

		BootlegUtil.logTrace("Processing Changed");

		try {

			if (delta.getResource() instanceof IFile) {

				IFile designerFile = (IFile) delta.getResource();
				processDeletedDesignerFile(designerFile);

			}

		} catch (CoreException e) {
			e.printStackTrace();
		}
		return true;

	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {

		BootlegUtil.logTrace("Visiting: " + delta.getResource().getName());

		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			if (!processAdded(delta)) {
				return false;
			}
			break;
		case IResourceDelta.CHANGED:
			if (!processChanged(delta)) {
				return false;
			}
			break;
		case IResourceDelta.REMOVED:
			if (!processDeleted(delta)) {
				return false;
			}
			break;

		}

		return true;
	}

}
