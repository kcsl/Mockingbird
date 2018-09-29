package harness.plaittesting;

public class Space {

	private int numStrands;
	private int length;
	private String string;

	public Space(int numStrands, int length, String string) {
		this.numStrands = numStrands;
		this.length = length;
		this.string = string;
	}

	public int getNumStrands() {
		return numStrands;
	}
	
	public int getLength() {
		return length;
	}
	
	public String getString() {
		return string;
	}
	
}
