package codecoverage.view;

public class Project {
	private String name;
	private Class[] classes;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Class[] getClasses() {
		return classes;
	}
	public void setClasses(Class[] classes) {
		this.classes = classes;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
