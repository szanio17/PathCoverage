package codecoverage.core;

public class AnalyzeReturn {
	/*
	 *	Static constant
	 */
	
	//In path was return code
	public static final int RETURN = 1;
	
	//In path was break code
	public static final int BREAK = 2;
	
	//In path was continue code
	public static final int CONTINUE = 3;
	
	//In path was throw code
	public static final int THROW = 4;
	
	//This block is not in this path through this method
	public static final int NOT_THIS_BLOCK = 5;
	
	//Coverage analyze was complete
	public static final int NO_SUCH_TIMESTAMP = 6; 
	
	//Coverage was OK
	public static final int OK = 7;
	
	//Method throws exception
	public static final int THROWS = 8;
	
	
	private int returnType;
	private String breakLabel;
	private long throwTS;

	public int getReturnType() {
		return returnType;
	}


	public void setReturnType(int returnType) {
		this.returnType = returnType;
	}


	public String getBreakLabel() {
		return breakLabel;
	}


	public void setBreakLabel(String breakLabel) {
		this.breakLabel = breakLabel;
	}


	public long getThrowTS() {
		return throwTS;
	}


	public void setThrowTS(long throwTS) {
		this.throwTS = throwTS;
	}
	
	
}
