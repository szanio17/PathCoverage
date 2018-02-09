package codecoverage.ui.parts;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ui.ISharedImages;

import codecoverage.view.Class;
import codecoverage.view.Method;
import codecoverage.view.Project;

public class ProjectLabelProvider extends CellLabelProvider {

	private final ISharedImages images;
	public static final Project LOADING_ELEMENT = new Project();

	public ProjectLabelProvider(ISharedImages images) {
		super();
		this.images = images;
	}

	@Override
	public void update(ViewerCell cell) {
		if (cell.getElement() == LOADING_ELEMENT) {
			cell.setText("Loading...");
			cell.setImage(null);
		} else {
			if (cell.getElement() instanceof Project) {
				cell.setText(((Project) cell.getElement()).getName());
				cell.setImage(images.getImage(ISharedImages.IMG_OBJ_FOLDER));
			}

			if (cell.getElement() instanceof Class) {
				cell.setText(((Class) cell.getElement()).getFullName());
				cell.setImage(images.getImage(ISharedImages.IMG_OBJ_FILE));
			}

			if (cell.getElement() instanceof Method) {
				cell.setText(((Method) cell.getElement()).getName());
				cell.setImage(images.getImage(ISharedImages.IMG_OBJ_ELEMENT));
			}

		}

	}

}
