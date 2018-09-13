/**
 * Move.java
 *
 * @author Mingjie Deng
 * @version 1.0 (Sep 12, 2018)
 */
package game;

/**
 * This class store a move in the game
 */
public class Move {
	private int row;
	private int col;
	private int player;
	private int step;
	
	public Move(int row, int col, int player, int step) {
		this.row = row;
		this.col = col;
		this.player = player;
		this.step = step;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	public int getPlayer() {
		return player;
	}

	public int getStep() {
		return step;
	}

	@Override
	public String toString() {
		return "The " + step + " move[" + row + ", " + col + "], player=" + player;
	}
	
}
