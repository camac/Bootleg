package org.openntf.bootleg.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.openntf.bootleg.util.BootlegUtil;

public class StopLoggingHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {

		BootlegUtil.stopLoggingToFile();
		
		return null;
		
	}

	@Override
	public boolean isEnabled() {
		return BootlegUtil.isLoggingToFile();
	}

	@Override
	public void setEnabled(Object evaluationContext) {

	}
	
}
