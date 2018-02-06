package codecoverage.annotations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import codecoverage.coverage.LineCoverageInfo;
import codecoverage.view.Method;

public class AnnotationFactory {

	public static final String MARKER = "codecoverage.markers.marker";
	
	public static final String MARKER_THROW = "codecoverage.markers.markerthrow";
	
	public static final String MARKER_BRANCH = "codecoverage.markers.markerbranch";

	public static final String ANNOTATION = "codecoverage.myannotation";
	
	public static final String ANNOTATION_BAD = "codecoverage.myannotationbad";
	
	public static final String ANNOTATION_BRANCH = "codecoverage.myannotationbranch";

	public static final List<SimpleMarkerAnnotation> annotations = new ArrayList<SimpleMarkerAnnotation>();

	public static IMarker createMarker(IResource res, Position position, boolean isThrowCode, LineCoverageInfo lci, boolean isLineCov) throws CoreException {
		IMarker marker = null;
		if(isLineCov) {
			marker = res.createMarker(isThrowCode? MARKER_THROW : MARKER);
			marker.setAttribute(IMarker.MESSAGE, "My Marker");
			
		}
		else {
			if(lci.isAbstractCode()) {
				marker = res.createMarker(MARKER_BRANCH);
				marker.setAttribute(IMarker.MESSAGE, lci.getCount() + "x");
			}
			else {
				marker = res.createMarker(MARKER);
				marker.setAttribute(IMarker.MESSAGE, "");
			}
		}
//		marker = res.createMarker(isThrowCode? MARKER_THROW : MARKER);
//		
//		marker.setAttribute(IMarker.MESSAGE, "My Marker");
		int start = position.getOffset();
		int end = position.getOffset() + position.getLength();
		marker.setAttribute(IMarker.CHAR_START, start);
		marker.setAttribute(IMarker.CHAR_END, end);
		return marker;
	}

//	public static List<IMarker> findMarkers(IResource resource) {
//		try {
//			return Arrays.asList(resource.findMarkers(MARKER, true, IResource.DEPTH_ZERO));
//		} catch (CoreException e) {
//			return new ArrayList<IMarker>();
//		}
//	}
//
//	public static List<IMarker> findAllMarkers(IResource resource) {
//		try {
//			return Arrays.asList(resource.findMarkers(MARKER, true, IResource.DEPTH_INFINITE));
//		} catch (CoreException e) {
//			return new ArrayList<IMarker>();
//		}
//	}

	public static void removeAllAnnotation(IResource resource, ITextEditor editor) {
		try {
			resource.deleteMarkers(MARKER, true, IResource.DEPTH_INFINITE);
			resource.deleteMarkers(MARKER_THROW, true, IResource.DEPTH_INFINITE);
			resource.deleteMarkers(MARKER_BRANCH, true, IResource.DEPTH_INFINITE);
			IDocumentProvider idp = editor.getDocumentProvider();
			// This is the document we want to connect to. This is taken from
			// the
			// current editor input.
			IDocument document = idp.getDocument(editor.getEditorInput());

			// The IannotationModel enables to add/remove/change annoatation to
			// a
			// Document loaded in an Editor
			IAnnotationModel iamf = idp.getAnnotationModel(editor.getEditorInput());

			// Finally add the new annotation to the model
			iamf.connect(document);
			Iterator<Annotation> iter = iamf.getAnnotationIterator();
			while (iter.hasNext()) {
				Annotation a = iter.next();
				if (a.getType().equals(ANNOTATION) || a.getType().equals(ANNOTATION_BAD) || a.getType().equals(ANNOTATION_BRANCH)) {
					iamf.removeAnnotation(a);
				}
			}
			iamf.disconnect(document);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static void addLineAnnotation(Method m, ITextEditor editor, int index, int line) {
		IDocumentProvider idp = editor.getDocumentProvider();
		IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		IDocument document = idp.getDocument(editor.getEditorInput());
		IAnnotationModel iamf = idp.getAnnotationModel(editor.getEditorInput());

		Map<Integer, LineCoverageInfo> map = m.getLinesCoverage();
		int tmp_line = line < 0? -1 : 1;
		line *= tmp_line;
		LineCoverageInfo lci = map.get(line);

		iamf.connect(document);

		try {
			IRegion regionStart = document.getLineInformation(line - 1);
			int offset = regionStart.getOffset();
			int length = regionStart.getLength();
			int startLine = lci.getStartLine();
			boolean isThrowCode = startLine < 0;
			if(isThrowCode) {
				startLine *= -1;
			}
			for (int z = startLine + 1; z <= lci.getEndLine(); z++) {
				IRegion regionNext = document.getLineInformation(z - 1);
				length += regionNext.getLength();
			}
			IMarker mymarker = AnnotationFactory.createMarker(file, new Position(offset, length),tmp_line == -1, lci, true);
			
			SimpleMarkerAnnotation ma = new SimpleMarkerAnnotation(tmp_line == -1? ANNOTATION_BAD : ANNOTATION, mymarker);
			iamf.addAnnotation(ma, new Position(offset, length));
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		iamf.disconnect(document);
	}

	public static void addAnnotation(Method m, ITextEditor editor, int index) {
		IDocumentProvider idp = editor.getDocumentProvider();
		IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		IDocument document = idp.getDocument(editor.getEditorInput());
		IAnnotationModel iamf = idp.getAnnotationModel(editor.getEditorInput());

		// int maxLine = document.getNumberOfLines();

		iamf.connect(document);

		Map<Integer, LineCoverageInfo> map = m.getLinesCoverage();
		List<Integer> listOfLines = m.getPathCoverage(index + 1);
		for (Integer i : listOfLines) {
			if(i < 0) {
				i = i * -1;
			}
			if (i != m.getStartLine()) {
				LineCoverageInfo lci = map.get(i);
				try {
					IRegion regionStart = document.getLineInformation(i - 1);
					int offset = regionStart.getOffset();
					int length = regionStart.getLength();
					int startLine = lci.getStartLine();
					boolean isThrowCode = startLine < 0;
					if(isThrowCode) {
						startLine *= -1;
					}
					for (int z = startLine + 1; z <= lci.getEndLine(); z++) {
						IRegion regionNext = document.getLineInformation(z - 1);
						length += regionNext.getLength();
					}
					IMarker mymarker = AnnotationFactory.createMarker(file, new Position(offset, length), false, lci , false);
					SimpleMarkerAnnotation ma = null;
					if(lci.isAbstractCode()) {
						ma = new SimpleMarkerAnnotation(ANNOTATION_BRANCH, mymarker);	
					}
					else {
						ma = new SimpleMarkerAnnotation(ANNOTATION, mymarker);
					}
					iamf.addAnnotation(ma, new Position(offset, length));
				} catch (BadLocationException e) {
					e.printStackTrace();
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}

		}

		iamf.disconnect(document);
	}
}
