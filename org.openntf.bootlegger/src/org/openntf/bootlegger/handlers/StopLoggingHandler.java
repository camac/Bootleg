package org.openntf.bootlegger.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.openntf.bootlegger.util.BootleggerUtil;

public class StopLoggingHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {

		BootleggerUtil.stopLoggingToFile();
		
		return null;
		
	}

	@Override
	public boolean isEnabled() {
		return BootleggerUtil.isLoggingToFile();
	}

	@Override
	public void setEnabled(Object evaluationContext) {

	}
	
}
