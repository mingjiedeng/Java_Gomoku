/**
 * Gomoku.java
 *
 * @author Mingjie Deng
 * @version 1.0 (Sep 12, 2018)
 */
package game;

import java.util.Stack;

/**
 * This class hold all the information and the logic that make the game run
 */
public class Gomoku {
	public final static int DEF_BOARD_SIZE = 15;	//there are 15x15 grids on board by default
	public final static int DEF_WIN_LENGTH = 5;		//winning condition is 5 in row by default
	public final static int EMPTY = 0;				//for grid status
	public final static int PLAYER1 = 1;			//for grid status or game status indicating player1 won
	public final static int PLAYER2 = 2;			//for grid status or game status indicating player2 won
	public final static int NOT_OVER = 0;			//game status, indicate game ongoing
	public final static int GAME_DRAW = 3;			//game status, indicate end of draw game 
	
	
	private int boardSize;		//how many grids in one row or column
	private int winLength;		//how many stones in one row for winning
	private int[][] board;		//store the status which PLAYER1 or PLAYER2 placed or EMPTY for each grid on board
	private boolean isHuman[];	//isHuman[0]: is player1 human; isHuman[1]: is player2 human
	private int step; 			//current step number
	private int current;		//current player role
	private int status;			//values include: NOT_OVER, PLAYER1, PLAYER2, GAME_DRAW
	private Stack<Move> moves;	//store each step of move
	
	/**
	 * Default constructor. 
	 * Generate a gomoku game that human moves first and computer moves later.
	 */
	public Gomoku() {
		this(DEF_BOARD_SIZE, DEF_WIN_LENGTH, true, false);
	}
	
	/**
	 * Constructor
	 * 
	 * @param gridNum			how many rows or columns on the game board
	 * @param winLength			winning condition: how many stone in a line 
	 * @param player1IsHuman	true if the move-first player is human, otherwise false
	 * @param player2IsHuman	true if the move-later player is human, otherwise false
	 */
	public Gomoku(int gridNum, int winLength, boolean player1IsHuman, boolean player2IsHuman) {
		this.boardSize = gridNum;
		this.winLength = winLength;
		this.isHuman = new boolean[]{player1IsHuman, player2IsHuman};
		initBoard();
		this.step = 1;
		this.current = PLAYER1;
		this.status = NOT_OVER;
		this.moves = new Stack<>();
	}
	
	/**
	 * Make a move: place a stone on specified position
	 * 
	 * @param row row of the position
	 * @param col column of the position
	 * @return	false if the move is illegal
	 */
	public boolean makeAMove(int row, int col) {
		if (!indexLegalCheck(row, col) || board[row][col] != EMPTY || status != NOT_OVER) 
			return false;
		
		moves.push(new Move(row, col, current, step++));
		board[row][col] = current;
		status = gameStatus();
		current = current == PLAYER1 ? PLAYER2 : PLAYER1;
		
		return true;
	}
	
	/**
	 * Retreat the last move
	 * 
	 * @return a Move instance of last move
	 */
	public Move retreat() {
		Move lastMove = null;
		if (!moves.empty()) {
			lastMove = moves.pop();
			int row = lastMove.getRow();
			int col = lastMove.getCol();
			board[row][col] = EMPTY;
			current = current == PLAYER1 ? PLAYER2 : PLAYER1;
			step--;
		}
		return lastMove;
	}
	
	/**
	 * Check game status after each move
	 * 
	 * @return int 	NOT_END for game ongoing; 
	 * 				PLAYER1 for player1 won; 
	 * 				PLAYER2 for player2 won; 
	 * 				GAME_DRAW for game draws
	 */
	public int gameStatus() {
		int status = NOT_OVER;
		Move lastMove = moves.peek();
		int row = lastMove.getRow();
		int col = lastMove.getCol();
		
		if (moves.size() == boardSize * boardSize) {
			status = GAME_DRAW;
		} else if (isWin(row, col, board)) {
			status =  board[row][col];
		}
		
		return status;
	}
	
	/**
	 * Check if the position is eligible inside the board boundary
	 * @param row row of the position
	 * @param col column of the position
	 * @return true if position is legal
	 */
	public boolean indexLegalCheck(int row, int col) {
		return (row >= 0 && row < boardSize && col >= 0 && col < boardSize);
	}
	
	/**
	 * Winning condition check on all the lines cross this position
	 * 
	 * @param row row of the position
	 * @param col column of the position
	 * @param board	the game board
	 * @return	true if match winning condition
	 */
	public boolean isWin(int row, int col, int[][] board) {		
		//Check horizontal
		if (linearCount(board, row, col, 0, 1) + linearCount(board, row, col, 0, -1) == winLength - 1)
			return true;
		
		//Check vertical
		if (linearCount(board, row, col, 1, 0) + linearCount(board, row, col, -1, 0) == winLength - 1)
			return true;
		
		//Check slash direction "/"
		if (linearCount(board, row, col, 1, -1) + linearCount(board, row, col, -1, 1) == winLength - 1)
			return true;
		
		//Check backslash direction "\"
		if (linearCount(board, row, col, 1, 1) + linearCount(board, row, col, -1, -1) == winLength - 1)
			return true;
		
		return false;
	}
	
	private int linearCount(int[][] board, int row, int col, int rowInc, int colInc) {
		int count = 0;
		int player = board[row][col];
		if (player != EMPTY) {
			for (int nextRow,nextCol,i=1; i < winLength; i++) {
				nextRow = row + i * rowInc;
				nextCol = col + i * colInc;
				if (!indexLegalCheck(nextRow, nextCol) || board[nextRow][nextCol] != player) break;
				count++;
			}
		}
		return count;
	}
	
	private void initBoard() {
		board = new int[boardSize][boardSize];
		for (int i=0; i<boardSize; i++) {
			for (int j=0; j<boardSize; j++) {
				board[i][j] = EMPTY;
			}
		}
	}

	/**
	 * Getter for grid number
	 * 
	 * @return how many rows or columns on board
	 */
	public int getGridNum() {
		return boardSize;
	}

	/**
	 * Getter for moves history
	 * 
	 * @return a stack of moves in this game
	 */
	public Stack<Move> getMoves() {
		return moves;
	}

	/**
	 * Get the board
	 * 
	 * @return 
	 */
	public int[][] getBoard() {
		return board;
	}

	/**
	 * Get the roles of the players
	 * 
	 * @return a two-elements array, first element for player1, second element for player2
	 */
	public boolean[] getIsHuman() {
		return isHuman;
	}

	/**
	 * Get which player on current move
	 * 
	 * @return
	 */
	public int getCurrent() {
		return current;
	}

	/**
	 * Get the status of the game
	 * 
	 * @return 0: NOT_OVER, 1: PLAYER1, 2: PLAYER2, 3: GAME_DRAW
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Get the step on current move
	 * 
	 * @return
	 */
	public int getStep() {
		return step;
	}

	/**
	 * Get how many stone in row for winning
	 * 
	 * @return
	 */
	public int getWinLength() {
		return winLength;
	}
}
