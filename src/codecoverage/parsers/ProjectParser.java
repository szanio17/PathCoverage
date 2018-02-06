package codecoverage.parsers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

public class ProjectParser {
	public static String lastClassPath;
	public static String tmpLastClassPath;

	public static void saveLastClassPath() {
		lastClassPath = tmpLastClassPath;
	}

	public static List<String> getPackages(IJavaProject javaProj) {
		List<String> listOfPackages = new ArrayList<String>();
		try {
			IPackageFragmentRoot[] packages = javaProj.getAllPackageFragmentRoots();
			for (IPackageFragmentRoot pack : packages) {
				if (pack.getRawClasspathEntry().getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IJavaElement[] elements = pack.getChildren();
					for (IJavaElement element : elements) {
						if (element.getElementName().length() > 0) {
							listOfPackages.add(element.getElementName());
						}
					}
				}
			}
		} catch (JavaModelException e3) {
			e3.printStackTrace();
		}
		return listOfPackages;
	}

	public static IWorkbenchPage getCurrentWorkbenchPage() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window2 = wb.getActiveWorkbenchWindow();
		return window2.getActivePage();
	}
	
	public static IFile getFileOfClass(IWorkspaceRoot root, IJavaProject jp, String className) {
		IType classType = null;
		try {
			classType = jp.findType(className);
			if (classType == null) {
				return null;
			}
		} catch (JavaModelException e2) {
			e2.printStackTrace();
		}
		IResource r2 = root.findMember(classType.getPath());

		if (r2 == null) {
			return null;
		}
		return root.getFileForLocation(r2.getLocation());
	}
	
	public static boolean openEditorOfGivenProject(String className, String javaProject) {
		IWorkbenchPage page = getCurrentWorkbenchPage();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(javaProject);
		IJavaProject jp = JavaCore.create(project);
		
		IFile f2 = getFileOfClass(root, jp, className);
		if (f2 == null) {
			return false;
		}
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(f2.getName());
		try {
			page.openEditor(new FileEditorInput(f2), desc.getId());
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public static boolean openEditorOfCurrentProject(String className,ExecutionEvent event) {
		IWorkbenchPage page = getCurrentWorkbenchPage();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IJavaProject jp = ProjectParser.getCurrentJavaProject(event);
		
		IFile f2 = getFileOfClass(root, jp, className);
		if (f2 == null) {
			return false;
		}
		
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(f2.getName());
		try {
			page.openEditor(new FileEditorInput(f2), desc.getId());
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return true;
	}

	public static List<IClasspathEntry> getClassPathSources(IJavaProject javaProj) {
		List<IClasspathEntry> listOfCPSources = new ArrayList<IClasspathEntry>();
		try {
			for (IClasspathEntry cpe : javaProj.getRawClasspath()) {
				if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					listOfCPSources.add(cpe);
					// System.out.println(cpe.getPath().toString() + " - " +
					// cpe.getPath().toOSString() + " - " +
					// cpe.getPath().lastSegment());
				}
			}
		} catch (JavaModelException e2) {
			e2.printStackTrace();
		}
		return listOfCPSources;
	}

	public static boolean isSourcePath(List<IClasspathEntry> list, String elementType) {
		for (IClasspathEntry cpe : list) {
			if (cpe.getPath().lastSegment().equals(elementType)) {
				return true;
			}
		}
		return false;
	}

	public static IType findOpenCurrentClass(IJavaProject javaProj, boolean isLast) {
		String classPath = "";
		IType p = null;
		if (!isLast) {
			IPath path = getCurrentFilePath();
			List<IClasspathEntry> listOfCPE = getClassPathSources(javaProj);

			// String srcName = "";
			// try {
			// IClasspathEntry[] classpathEntries = javaProj.getRawClasspath();
			//
			// for (int i = 0; i < classpathEntries.length; i++) {
			// int entryKind = classpathEntries[i].getEntryKind();
			//
			// if (entryKind == IClasspathEntry.CPE_SOURCE) {
			// srcName += classpathEntries[i].getPath().toString();
			// break;
			// }
			// }
			// } catch (JavaModelException e2) {
			// e2.printStackTrace();
			// }

			String withoutLoc = path.toString().substring(javaProj.getProject().getLocation().toString().length() + 1);
			System.out.println("witoutLoc:" + withoutLoc);
			IClasspathEntry cpe = null;
			for (IClasspathEntry tmp_cpe : listOfCPE) {
				if (withoutLoc.startsWith(tmp_cpe.getPath().lastSegment())) {
					cpe = tmp_cpe;
				}
			}
			if (cpe == null) {
				System.out.println("didnt find");
				return null;
			}
			withoutLoc = withoutLoc.substring(cpe.getPath().lastSegment().length() + 1);
			withoutLoc = withoutLoc.substring(0, withoutLoc.length() - 5);
			withoutLoc = withoutLoc.replace("/", ".");

			classPath = withoutLoc;
		} else {
			classPath = ProjectParser.lastClassPath;
		}
		if (classPath == null) {
			return null;
		}

		try {
			p = javaProj.findType(classPath);
			tmpLastClassPath = classPath;
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return p;
	}

	private static IPath getCurrentFilePath() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window2 = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = window2.getActivePage();

		IEditorPart editor = page.getActiveEditor();
		IEditorInput input = editor.getEditorInput();

		return ((FileEditorInput) input).getPath();
	}

	public static IJavaProject getCurrentJavaProject(ExecutionEvent event) {
//		IWorkbench wb = PlatformUI.getWorkbench();
//		IWorkbenchWindow window2 = wb.getActiveWorkbenchWindow();
//		IWorkbenchPage page = window2.getActivePage();
//		IEditorPart editorPart = page.getActiveEditor();
		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
				
		if (editorPart == null) {
			return null;
		}

		IFileEditorInput input = (IFileEditorInput) editorPart.getEditorInput();

		IFile file = input.getFile();
		IProject activeProject = file.getProject();
		// ... use activeProjectName
		return JavaCore.create(activeProject);
	}
}
