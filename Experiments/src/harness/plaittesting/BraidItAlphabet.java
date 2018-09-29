package harness.plaittesting;

import java.util.ArrayList;
import java.util.Random;

/**
 * A custom alphabet enumerator for the BraidIt String generation route
 * @author Ben Holland
 */
public class BraidItAlphabet {
	
	private char leastBaseChar;
	private ArrayList<Character> enumeratedRange = new ArrayList<Character>();
	
	public BraidItAlphabet(int numStrands) {
		this.leastBaseChar = (char) (122 - numStrands + 2);
		for(int c=Character.MIN_CODE_POINT; c<=Character.MAX_CODE_POINT; c++) {
			if(Character.isAlphabetic(c) && Character.isLowerCase(c) && c >= leastBaseChar && !(Character.toChars(c).length > 1)){
				enumeratedRange.add((char) c);
			}
		}
//		System.out.println("Found " + enumeratedRange.size() + " candidate characters");
	}
	
	public ArrayList<Character> getAlphabet(){
		return enumeratedRange;
	}
	
	public String getRandomString(int length) {
		StringBuilder result = new StringBuilder();
		for(int i=0; i<length; i++) {
			result.append(enumeratedRange.get(new Random().nextInt(enumeratedRange.size())));
		}
		return result.toString();
	}

}
