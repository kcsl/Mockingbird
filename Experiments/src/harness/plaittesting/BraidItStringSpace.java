package harness.plaittesting;

import java.util.Iterator;

/**
 * A custom string space enumerator for the BraidIt challenge application
 * @author Ben Holland
 */
public class BraidItStringSpace implements Iterable<Space> {

	private static class BraidItStringSpaceIterator implements Iterator<Space>{
		private final int MIN_STRANDS = 8;
		private final int MAX_STRANDS = 27;
		private final int MIN_LENGTH = 3;
		private final int MAX_LENGTH = 50;
		
		private int i = MIN_STRANDS;
		private int j = MIN_LENGTH;
		private Iterator<String> current;
		
		@Override
		public boolean hasNext() {
			return (current != null && current.hasNext()) || (j <= MAX_LENGTH || i < MAX_STRANDS);
		}

		@Override
		public Space next() {
			if(current != null && current.hasNext()) {
				return new Space(i, j, current.next());
			} else {
				Space result = null;
				if(i <= MAX_STRANDS) {
					if(j <= MAX_LENGTH) {
//						System.out.println("i: " + i + ", j: " + j);
						current = new StringAlphabet(new BraidItAlphabet(i).getAlphabet(), j).iterator();
						result = new Space(i, j, current.next());
						j++;
					} else {
						j=MIN_LENGTH;
						i++;
//						System.out.println("i: " + i + ", j: " + j);
						current = new StringAlphabet(new BraidItAlphabet(i).getAlphabet(), j).iterator();
						result = new Space(i, j, current.next());
						j++;
					}
				}
				return result;
			}
		}
	}
	
	@Override
	public Iterator<Space> iterator() {
		return new BraidItStringSpaceIterator();
	}

}
