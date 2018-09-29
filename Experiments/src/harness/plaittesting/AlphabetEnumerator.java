package harness.plaittesting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * A custom alphabet enumerator for the BraidIt String generation route
 * @author Ben Holland
 */
public class AlphabetEnumerator implements Iterable<Character> {

	private LinkedList<Integer[]> lowercaseAlphabeticUnicodeRanges = new LinkedList<Integer[]>();
	
	private char leastBaseChar;
	private ArrayList<Integer> enumeratedRange = new ArrayList<Integer>();
	
	public AlphabetEnumerator(int numStrands) {
		this.leastBaseChar = (char) (122 - numStrands + 2);
		
		boolean inRange = false;
		for(int c=Character.MIN_CODE_POINT; c<=Character.MAX_CODE_POINT; c++) {
			if(Character.isAlphabetic(c) && Character.isLowerCase(c) && c >= leastBaseChar && !(Character.toChars(c).length > 1)){
				// just here for ease of use
				enumeratedRange.add(c);
				
				if(!inRange) {
//					System.out.println(String.format("Start Lowercase Alphabetic: 0x%08X", c));
					Integer[] range = new Integer[2];
					range[0] = c;
					lowercaseAlphabeticUnicodeRanges.add(range);
					inRange = true;
				}
			} else {
				if(inRange) {
//					System.out.println(String.format("End Lowercase Alphabetic: 0x%08X", (c-1)));
					lowercaseAlphabeticUnicodeRanges.getLast()[1] = (c-1);
					inRange = false;
				}
			}
		}
		
		System.out.println("Found " + enumeratedRange.size() + " candidate characters");
	}
	
	public String getUnicodeDisplayString(String str) {
		StringBuilder unicodeDisplayString = new StringBuilder();
		unicodeDisplayString.append("\"");
		for(Character c : str.toCharArray()) {
			unicodeDisplayString.append(String.format("\\u%04x", (int)c));
		}
		unicodeDisplayString.append("\"");
		return unicodeDisplayString.toString();
	}

	public String getString(int i, int length) {
		throw new RuntimeException("Not implemented");
	}
	
	public String getRandomString(int length) {
		StringBuilder result = new StringBuilder(26);
		for(int i=0; i<length; i++) {
			result.append(getRandomCharacter());
		}
		return result.toString();
	}

	public Character getCharacter(int i) {
		return Character.toChars(enumeratedRange.get(i))[0];
	}
	
	public Character getRandomCharacter() {
		return Character.toChars(enumeratedRange.get(new Random().nextInt(enumeratedRange.size())))[0];
	}
	
	@Override
	public Iterator<Character> iterator() {
		return new Iterator<Character>() {
			
			private Iterator<Integer[]> ranges = lowercaseAlphabeticUnicodeRanges.iterator();
			private Integer[] range = ranges.next();
			private int i=range[0];
			
			@Override
			public boolean hasNext() {
				return i<=range[1] || ranges.hasNext();
			}

			@Override
			public Character next() {
				if(i<=range[1]) {
					return Character.toChars(i++)[0];
				} else {
					range = ranges.next();
					i = range[0];
					return Character.toChars(i++)[0];
				}
			}
			
		};
	}

}
