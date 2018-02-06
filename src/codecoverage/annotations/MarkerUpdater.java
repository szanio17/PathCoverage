package codecoverage.annotations;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IMarkerUpdater;

public class MarkerUpdater implements IMarkerUpdater {

	@Override
	public String[] getAttribute() {
	      return null;
	}

	@Override
	public String getMarkerType() {
	      return AnnotationFactory.MARKER;
	}

	@Override
	public boolean updateMarker(IMarker marker, IDocument doc, Position position) {
	      try {
	            int start = position.getOffset();
	            int end = position.getOffset() + position.getLength();
	            marker.setAttribute(IMarker.CHAR_START, start);
	            marker.setAttribute(IMarker.CHAR_END, end);
	            return true;
	      } catch (CoreException e) {
	            return false;
	      }
	}
}
