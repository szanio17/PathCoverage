package codecoverage.view;

public class Class {
	Method[] methods = new Method[0];
	private String name;
	private String fullName;
	private Project project;
	
	
	public Class() {

	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setMethods(Method[] methods) {
		this.methods = methods;
	}

	public Method[] getMethods() {
		return methods;
	}

	public String toString() {
		return fullName;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
}
