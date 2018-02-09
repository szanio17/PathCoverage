package codecoverage.launching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.internal.analysis.IfProbes;

import codecoverage.core.ExecutionDataServer;
import codecoverage.core.MethodASTVisitor;
import codecoverage.coverage.CoveragePlugin;
import codecoverage.coverage.LineCoverageInfo;
import codecoverage.elements.MethodElement;
import codecoverage.view.Class;
import codecoverage.view.Method;
import codecoverage.view.Project;

public class MethodAnalyzer {
	private static Map<String, IClassCoverage> classCoverages;
	private IJavaProject javaProject;

	public void setJavaProject(IJavaProject javaProject) {
		this.javaProject = javaProject;
	}

	public void analyzeMethods() {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		Analyzer analyzer = new Analyzer(ExecutionDataServer.store, coverageBuilder);
		IfProbes.setOffline(false);
		IfProbes.resetUsedTimestamps();
		System.out.println(javaProject.getProject().getLocation());
		try {
			analyzer.analyzeAll(new File(javaProject.getProject().getLocation() + "/bin"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<IType> classes = new ArrayList<>();
		classCoverages = new HashMap<String, IClassCoverage>();
		for (IClassCoverage cc : coverageBuilder.getClasses()) {
			try {

				String className = cc.getName().replaceAll("/", ".");
				IType findedType = javaProject.findType(className);
				if (findedType != null) {
					classes.add(findedType);
					classCoverages.put(className, cc);
				} else {
					System.out.println("type: null");
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		List<MethodElement> listOfMethod = new ArrayList<>();
		// CompilationUnit cu2 = null;
		// MethodElement me2 = null;
		Project p = new Project();
		p.setName(javaProject.getElementName());
		List<Class> listOfClasses = new ArrayList<Class>();
		Map<String, Class> mapOfClasses = new HashMap<String, Class>();
		for (IType type : classes) {

			// IType iType = ProjectParser.findOpenCurrentClass(javaProject,
			// false);
			ICompilationUnit iCompilationUnit = type.getCompilationUnit();
			CompilationUnit cu = parse(iCompilationUnit);

			ASTNode rootNode = cu.getRoot();
			Class c = new Class();
			c.setFullName(type.getFullyQualifiedName());
			c.setProject(p);
			listOfClasses.add(c);
			mapOfClasses.put(type.getFullyQualifiedName(), c);

			List<Method> listOfMethods = new ArrayList<Method>();
			Map<String, Method> mapOfMethods = new HashMap<String, Method>();

			rootNode.accept(new ASTVisitor() {

				@Override
				public boolean visit(MethodDeclaration node) {
					SimpleName name = node.getName();

					Method m = new Method();
					m.setClass1(c);
					m.setName(name.getIdentifier());
					m.setPathCoverages(new HashMap<Integer, List<Integer>>());
					m.setLinesCoverage(new HashMap<Integer, LineCoverageInfo>());
					listOfMethods.add(m);
					mapOfMethods.put(m.getName(), m);

					MethodElement me = new MethodElement();
					me.setName(name.getIdentifier());
					me.setThisNode(node);
					me.setCu(cu);
					me.setMethod(m);
					me.setClassName(type.getFullyQualifiedName());
					me.setStartLine(cu.getLineNumber(name.getStartPosition()));
					me.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
					listOfMethod.add(me);
					return false;
				}
			});
			Method[] methods = new Method[listOfMethods.size()];
			for (int i = 0; i < methods.length; i++) {
				methods[i] = listOfMethods.get(i);
			}
			c.setMethods(methods);

		}
		Class[] classesL = new Class[listOfClasses.size()];
		for (int i = 0; i < classesL.length; i++) {
			classesL[i] = listOfClasses.get(i);
		}
		p.setClasses(classesL);
		CoveragePlugin.updateProjectTree(p);

		for (MethodElement me : listOfMethod) {
			System.out.println("-----------");
			System.out.println(me.getClassName() + "." + me.getName());
			MethodASTVisitor mVisitor = new MethodASTVisitor(me, classCoverages.get(me.getClassName()));
			// MethodASTVisitor2 mVisitor2 = new MethodASTVisitor2(me,
			// me.getCu(), null); // for
			// testing,
			// how
			// AST
			// works
			// me.getThisNode().accept(mVisitor2);
			me.getThisNode().accept(mVisitor);
			me.analyze(null, null, -1);
			int c = CoveragePlugin.calcCountOfPathCoverage(me.getMethod().getPathCoverages());
			me.getMethod().setActualPathCoverages(c);
			int max = me.getCountBranches();
			me.getMethod().setMaxPathCoverages(max);
		}
		// Display.getDefault().asyncExec(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(OwnView.ID);
		// } catch (PartInitException e) {
		// e.printStackTrace();
		// }
		// }
		// });

	}

	protected CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit); // set source
		parser.setResolveBindings(true); // we need bindings later on
		return (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse
	}
}
