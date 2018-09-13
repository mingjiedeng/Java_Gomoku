/**
 * Score.java
 *
 * @author Mingjie Deng
 * @version 1.0 (Sep 12, 2018)
 */
package game;

/**
 * Store the position score by name
 */
public enum Score {
	FIVE(3000000),
	FOUR(500000),
	THREE(4650),
	TWO(90),
	ONE(10),
	BLOCKED(0),
	HALF_OPEN_FOUR(5000),
	HALF_OPEN_THREE(100),
	HALF_OPEN_TWO(10),
	HALF_OPEN_ONE(1),
	HALF_FOUR_AND_THREE(90000),
	DOUBLE_THREE(20000);
	
	private int val;
	
	private Score(int val) {
		this.val = val;
	}
	
	/**
	 * Get the score
	 * 
	 * @return the score in number
	 */
	public int val() {
		return val;
	}
}
