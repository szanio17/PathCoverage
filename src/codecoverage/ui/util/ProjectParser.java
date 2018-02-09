package codecoverage.ui.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.part.FileEditorInput;

public class ProjectParser {
	public static IJavaProject getCurrentJavaProject(IWorkbench workbench) {
		IEditorPart editorPart = workbench.getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		if (editorPart == null) {
			return null;
		}

		IFileEditorInput input = (IFileEditorInput) editorPart.getEditorInput();
		IFile file = input.getFile();
		IProject activeProject = file.getProject();
		return JavaCore.create(activeProject);
	}

	public static IPath getCurrentFilePath(IWorkbench workbench) {
		return ((FileEditorInput) workbench.getActiveWorkbenchWindow().getActivePage().getActiveEditor()
				.getEditorInput()).getPath();
	}
}
