package harness.plaittesting;

import java.util.ArrayList;
import java.util.Iterator;

public class StringAlphabet implements Iterable<String> {

	private ArrayList<Character> alphabet;
	private int length;
	
	public StringAlphabet(ArrayList<Character> alphabet, int length) {
		if (alphabet.isEmpty()) {
			throw new IllegalArgumentException("Alphabet is empty");
		} else {
			this.alphabet = alphabet;
			this.length = length;
		}
	}
	
	
	
	private static class StringAlphabetIterator implements Iterator<String> {

		private ArrayList<Character> alphabet;
		private int length;
		private char firstChar;
		private char lastChar;
		private long keyspace;
		private long i;
		private char[] next;
		
		public StringAlphabetIterator(ArrayList<Character> alphabet, int length) {
			this.alphabet = alphabet;
			this.length = length;
			
			// get some basic information used for iterating
			firstChar = alphabet.get(0);
			lastChar = alphabet.get(alphabet.size() - 1);
			keyspace = (long) Math.pow(alphabet.size(), length);

			// make an array and initialize it
			next = new char[length];
			for (int c = 0; c < length; c++) {
				next[c] = firstChar;
			}
			
			i = 0;
		}
		
		@Override
		public boolean hasNext() {
			return i < keyspace;
		}

		@Override
		public String next() {
			String result = null;
			if((i++) < keyspace) {
				result = new String(next);
				// update next
				for (int k = 0; k < length; k++) {
					if (next[k] == lastChar) {
						continue;
					}
					next[k] = alphabet.get(alphabet.indexOf(next[k]) + 1);
					if (k > 0) {
						for (int z = 0; z < k; z++) {
							next[z] = firstChar;
						}
					}
					break;
				}
			}
			return result;
		}
		
	}

	@Override
	public Iterator<String> iterator() {
		return new StringAlphabetIterator(alphabet, length);
	}

}
