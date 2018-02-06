/*******************************************************************************
 * Copyright (c) 2006, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 ******************************************************************************/
package codecoverage.annotations;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;

/**
 * Handler to remove all coverage sessions.
 */
public class RemoveAnnotation extends AbstractHandler {

	public RemoveAnnotation() {
		super();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IFile file = (IFile) AnnotationPlugin.getEditor().getEditorInput().getAdapter(IFile.class);
		AnnotationFactory.removeAllAnnotation(file, AnnotationPlugin.getEditor());
		return null;
	}

}
