package codecoverage.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.texteditor.ITextEditor;

import codecoverage.annotations.AnnotationFactory;
import codecoverage.view.Method;

public class CoverageFrame extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Method method;
	private JList<String> list;
	private JComboBox<String> comboBox;
	private int currentCovLine = 0;
	private ITextEditor editor;
	private JButton btnNewButton;
	
	public CoverageFrame(String frameTitle, Method method, ITextEditor editor) {
		super(frameTitle);
		this.method = method;
		this.editor = editor;
		setSize(new Dimension(300,500));
		getContentPane().setLayout(new GridLayout(2, 0, 0, 0));
		
		JPanel panelCovDetail = new JPanel();
		getContentPane().add(panelCovDetail);
		panelCovDetail.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Coverage detail"));
		panelCovDetail.setLayout(new GridLayout(5, 2, 0, 0));
		
		JLabel lblNewLabel_1 = new JLabel("Project:");
		panelCovDetail.add(lblNewLabel_1);
		
		JLabel lblProject = new JLabel("New label");
		panelCovDetail.add(lblProject);
		
		JLabel lblNewLabel_3 = new JLabel("Class:");
		panelCovDetail.add(lblNewLabel_3);
		
		JLabel lblClass = new JLabel("New label");
		panelCovDetail.add(lblClass);
		
		JLabel lblNewLabel = new JLabel("Method:");
		panelCovDetail.add(lblNewLabel);
		
		JLabel lblMethod = new JLabel("New label");
		panelCovDetail.add(lblMethod);
		
		JLabel lblNewLabel_5 = new JLabel("Max Coverage path:");
		panelCovDetail.add(lblNewLabel_5);
		
		JLabel lblMaxCoveragePath = new JLabel(Integer.toString(method.getMaxPathCoverages()));
		panelCovDetail.add(lblMaxCoveragePath);
		
		JLabel lblNewLabel_8 = new JLabel("Actual coverage path:");
		panelCovDetail.add(lblNewLabel_8);
		
		
		JLabel lblActualCoveragePath = new JLabel(Integer.toString(method.getActualPathCoverages()));
		panelCovDetail.add(lblActualCoveragePath);
		
		lblProject.setText(method.getClass1().getProject().getName());
		lblClass.setText(method.getClass1().getFullName());
		lblMethod.setText(method.getName());
		
		
		JPanel panelPaintCov = new JPanel();
		getContentPane().add(panelPaintCov);
		panelPaintCov.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Coverage paint"));
		panelPaintCov.setLayout(new BorderLayout(0, 0));
		
		JPanel panelSelCov = new JPanel();
		panelPaintCov.add(panelSelCov, BorderLayout.NORTH);
		panelSelCov.setLayout(new FlowLayout());
		JLabel label_3 = new JLabel("Show coverage:");
		label_3.setHorizontalAlignment(SwingConstants.LEFT);
		panelSelCov.add(label_3);
		
		comboBox = new JComboBox<String>();
		panelSelCov.add(comboBox);
		
		for(int i = 1; i <= method.getCountOfCoverages(); i++) {
			comboBox.addItem(Integer.toString(i));
		}
		
		
		
		JPanel panelShowCoverage = new JPanel();
		panelPaintCov.add(panelShowCoverage, BorderLayout.CENTER);
		panelShowCoverage.setLayout(new GridLayout(1, 0, 0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		panelShowCoverage.add(tabbedPane);
		//		panelButton.setVisible(true);
				
		JPanel panelDrawCov = new JPanel();
		tabbedPane.addTab("Show coverage", null, panelDrawCov, null);
		
		
		tabbedPane.addChangeListener(new ChangeListener() {
	        public void stateChanged(ChangeEvent e) {
	            cleanAnnotations();
	            if(tabbedPane.getSelectedIndex() == 1) {
	            	resetDebugCoverage();
	            }
	        }
	    });	
		
		comboBox.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		    	changeCoverageList(comboBox.getSelectedIndex());
		    	resetDebugCoverage();
		    	cleanAnnotations();
		    }
		});
		JButton btnNewButton_3 = new JButton("Draw");
		panelDrawCov.add(btnNewButton_3);
		
		JButton btnNewButton_4 = new JButton("Clean");
		panelDrawCov.add(btnNewButton_4);
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cleanAnnotations();
			}
		});
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cleanAnnotations();
				if(comboBox.getItemCount() == 0) {
					return;
				}
				int index = comboBox.getSelectedIndex();
				AnnotationFactory.addAnnotation(method, editor, index);
			}
		});
		
		JPanel panelDebugCov = new JPanel();
		tabbedPane.addTab("Simulator coverage", null, panelDebugCov, null);
		panelDebugCov.setBorder(new MatteBorder(1, 1, 0, 0, (Color) new Color(0, 0, 0)));
		panelDebugCov.setLayout(new GridLayout(1,2,0,0));
		JPanel panel_7 = new JPanel();
		panelDebugCov.add(panel_7);
		DefaultListModel<String> model = new DefaultListModel<String>();
		
		list = new JList<String>(model);
		changeCoverageList(comboBox.getSelectedIndex());
		JScrollPane scrollPane = new JScrollPane(list);
		panel_7.add(scrollPane, BorderLayout.EAST);
		
		
		JPanel panel_6 = new JPanel();
		panelDebugCov.add(panel_6);
		panel_6.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnNewButton_1 = new JButton("Reset");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetDebugCoverage();
			}
		});
		panel_6.add(btnNewButton_1);
		
		btnNewButton = new JButton("Next");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextDebugLineCoverage();
			}
		});
		panel_6.add(btnNewButton);
		
		JButton btnNewButton_2 = new JButton("Clean");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cleanAnnotations();
			}
		});
		panel_6.add(btnNewButton_2);
		
//		JLabel lblNewLabel = new JLabel("Bla - bla.Example.baf1()");
//		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
//		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
//		panel.add(lblNewLabel, BorderLayout.NORTH);
//		
//		JPanel panelButton = new JPanel();
//		
//		panel.add(panelButton, BorderLayout.SOUTH);
//		
//		JButton btnNewButton_1 = new JButton("Draw coverage");
//		panelButton.add(btnNewButton_1);
//		
//		JButton btnNewButton = new JButton("Clear");
//		btnNewButton.setToolTipText("");
//		panelButton.add(btnNewButton);
		
		
		
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//pack();
	}
	
	private void cleanAnnotations() {
		IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		AnnotationFactory.removeAllAnnotation(file, editor);
	}
	
	private void nextDebugLineCoverage() {
		if(comboBox.getItemCount() == 0) {
			return;
		}
		int index = comboBox.getSelectedIndex() +1;
		cleanAnnotations();
		List<Integer> lst = method.getPathCoverages().get(index);
		currentCovLine++;
		list.setSelectedIndex(currentCovLine);
		if(lst.size()-1 == currentCovLine) {
			btnNewButton.setEnabled(false);
		}
		AnnotationFactory.addLineAnnotation(method, editor, index, lst.get(currentCovLine));
	}
	
	private void resetDebugCoverage() {
		cleanAnnotations();
		if(comboBox.getItemCount() == 0) {
			return;
		}
		int index = comboBox.getSelectedIndex() + 1;
		list.setSelectedIndex(0);
		List<Integer> lst = method.getPathCoverages().get(index);
		currentCovLine = 0;
		btnNewButton.setEnabled(true);
		AnnotationFactory.addLineAnnotation(method, editor, index, lst.get(currentCovLine));
		
	}
	
	private void changeCoverageList(int index) {
		DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
		model.clear();
		if(index != -1) {
			for(Integer i : this.method.getPathCoverage(index+1)) {
				model.addElement(i.toString());
			}
		}
	}
}
