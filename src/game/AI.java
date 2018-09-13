/**
 * AI.java
 *
 * @author Mingjie Deng
 * @version 1.0 (Sep 12, 2018)
 */
package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class build an AI for gomoku by using minimax with alpha beta pruning algorithm.
 */
public class AI {
	private static final boolean DEBUG = false;
	private static final int MAX = 10 * Score.FIVE.val();
	private static final int MIN = -MAX;
	private static final int DEPTH = 10;
	private static final int CHILDREN_LIMIT = 3;
	private static final int OPENING_MOVES = 3;
	private Gomoku gomoku;
	private int[][] board;
	private int[][] p1Score;
	private int[][] p2Score;
	private int winLength;
	private int rowSize;
	private int colSize;

	/**
	 * Constructor, build an AI player
	 * 
	 * @param gomoku a gomoku game instance
	 */
	public AI(Gomoku gomoku) {
		this.gomoku = gomoku;
		this.winLength = gomoku.getWinLength();
		
		//hard copy gomoku.board for board initialization
		int[][] board = gomoku.getBoard();
		this.rowSize = board.length;
		this.colSize = board[0].length;
		this.board = new int[rowSize][colSize];
		this.p1Score = new int[rowSize][colSize];
		this.p2Score = new int[rowSize][colSize];
		
		if (gomoku.getStep() > OPENING_MOVES)
			initScore();
	}
	
	/*
	 * Traverse the entire board to calculate a score for each position
	 */
	private void initScore() {
		int[][] board = gomoku.getBoard();
		this.board = new int[rowSize][];
		for (int i=0; i<rowSize; i++) {
			this.board[i] = board[i].clone();
		}
		
		for (int i=0; i<rowSize; i++) 
			for (int j=0; j<colSize; j++)  
				setScore(i, j);
	}
	
	/**
	 * Return the next move by AI base on the current positions on board
	 * 
	 * @return the move in current AI's turn
	 */
	public Move nextMove() {
		Move aiNextMove;
		int step = gomoku.getStep();
		if (step <= OPENING_MOVES) {
			aiNextMove = openingMove();
		} else {
			//initialize the score when start using real AI
			if (step > OPENING_MOVES && step <= OPENING_MOVES+2) {
				int[][] board = gomoku.getBoard();
				for (int i=0; i<rowSize; i++)
					this.board[i] = board[i].clone();
				initScore();
			}
			
			Move lastMove = gomoku.getMoves().peek();
			doMove(new Node(lastMove.getRow(), lastMove.getCol(), lastMove.getPlayer()));
			Node lastNode = new Node(lastMove.getRow(), lastMove.getCol(), lastMove.getPlayer());
			Node node = negamax(lastNode, DEPTH, MIN, MAX);
			aiNextMove = new Move(node.row, node.col, gomoku.getCurrent(), gomoku.getStep());
			doMove(node);
		}
		if (DEBUG) System.out.println("----------------------------------------------------------------------------");
		return aiNextMove;
	}
	
	/*
	 * Minimax algorithm with alpha beta pruning
	 */
	private Node negamax(Node probeNode, int depth, int alpha, int beta) {
		Node bestMove = probeNode.role == Gomoku.PLAYER1 ? new Node(MIN) : new Node(MAX);
		
		//The base case
		if (gomoku.isWin(probeNode.row, probeNode.col, board)) {
			if (depth == DEPTH) 
				return generateMoves(roleReversal(probeNode.role)).get(0);
			else 
				return probeNode.role == Gomoku.PLAYER1 ? new Node(MIN+1) : new Node(MAX-1);
		}
		if (depth <= 0) 
			return new Node(evaluate());
		
		probeNode.children = generateMoves(roleReversal(probeNode.role));
		for (Node node : probeNode.children) {
			doMove(node);
			node.score = negamax(node, depth-1, alpha, beta).score;
			retreat(node);
			
			if (DEBUG) System.out.println(printNode(node, depth) + ", children:" + Integer.toString(probeNode.children.size()));
			
			//According to evaluate(), Player2 expects max score, Player1 expects minimum score
			if (probeNode.role == Gomoku.PLAYER1) { //means node.role == player2 who expects max score
				if (node.compareTo(bestMove) > 0) bestMove = node;
				if (node.score > alpha) alpha = node.score;
			} else { 								//player who expects minimum score
				if (node.compareTo(bestMove) < 0) bestMove = node;
				if (node.score < beta) beta = node.score;
			}
			if (alpha >= beta) { //cut-off
				probeNode.isCut = true;
				break;
			}
		}
		return bestMove;
	}
	
	/*
	 * Update the board and score after make a move
	 */
	private void doMove(Node node) {
		int row = node.row;
		int col = node.col;
		if (board[row][col] == roleReversal(node.role)) 
			throw new RuntimeException("Wrong move, board[" + row + "][" + col + "] is not vacant.");
		
		board[row][col] = node.role;
		updateScore(node);
	}
	
	/*
	 * Update the board and score after make a retreat
	 */
	private void retreat(Node node) {
		int row = node.row;
		int col = node.col;
		if (board[row][col] != node.role) {
			throw new RuntimeException("Wrong retreat, board[" + row + "][" + col + "] is not " + node.role);
		}
		board[row][col] = Gomoku.EMPTY;
		updateScore(node);
	}
	
	private void updateScore(Node node) {
		int row = node.row;
		int col = node.col;
		
		//Horizontal direction "-"
		for (int j = col-(winLength-1); j <= col+(winLength-1); j++) 
			setScore(row, j);

		//Vertical direction "|"
		for (int i = row-(winLength-1); i <= row+(winLength-1); i++) {
			if (i == row) continue;
			setScore(i, col);
		}
		
		//Slash direction "/"
		for (int i = row-(winLength-1), j = col+(winLength-1); i <= row+(winLength-1); i++, j--) {
			if (i == row) continue;
			setScore(i, j);
		}
		
		//Backslash direction "\"
		for (int i = row-(winLength-1), j = col-(winLength-1); i <= row+(winLength-1); i++, j++)  {
			if (i == row) continue;
			setScore(i, j);
		}
	}
	
	private boolean setScore(int row, int col) {
		if (!gomoku.indexLegalCheck(row, col)) return false;
		p1Score[row][col] = countAllWayScore(Gomoku.PLAYER1, row, col);
		p2Score[row][col] = countAllWayScore(Gomoku.PLAYER2, row, col);
		return true;
	}
	
	private int countAllWayScore(int role, int row, int col) {
		int score = 0;
		
		if (board[row][col] != Gomoku.EMPTY) return score;
		
		//Count horizontal direction "-"
		int score1 = countLinearScore(countOneWay(role, row, col, 0, -1), countOneWay(role, row, col, 0, 1));
		score = scoreplus(score, score1);
		
		//Count vertical direction "|"
		int score2 = countLinearScore(countOneWay(role, row, col, -1, 0), countOneWay(role, row, col, 1, 0));
		score = scoreplus(score, score2);
		
		//Count slash direction "/"
		int score3 = countLinearScore(countOneWay(role, row, col, -1, 1), countOneWay(role, row, col, 1, -1));
		score = scoreplus(score, score3);
		
		//Count backslash direction "\"
		int score4 = countLinearScore(countOneWay(role, row, col, -1, -1), countOneWay(role, row, col, 1, 1));
		score = scoreplus(score, score4);
		
		return score;
	}
	
	private OneWayCount countOneWay(int player, int row, int col, int rowInc, int colInc) {
		OneWayCount owc = new OneWayCount();
		int opponent = roleReversal(player);
		
		for (int nextRow, nextCol, i=1; i < winLength; i++) {
			nextRow = row + i * rowInc;
			nextCol = col + i * colInc;
			if (!gomoku.indexLegalCheck(nextRow, nextCol) || board[nextRow][nextCol] == opponent) {
				owc.block++;
				break;
			} else if (board[nextRow][nextCol] == Gomoku.EMPTY) {
				if (owc.spacePosition == 0 && 
						gomoku.indexLegalCheck(nextRow + rowInc, nextCol + colInc) && 
						board[nextRow + rowInc][nextCol + colInc] == player) {
					owc.spacePosition = owc.count;
					continue;
				} else {
					break;
				}
			} else { //board[nextRow][nextCol] == player
				if (owc.spacePosition == 0) {
					owc.count++;
				} else {
					owc.countAfterSpace++;
				}
			}
		}
			
		return owc;
	}
		
	private int countLinearScore(OneWayCount owcFront, OneWayCount owcBack) {
		OneWayCount owc = new OneWayCount();
		owc.count = owcFront.count + owcBack.count - 1;
		owc.block = owcFront.block + owcBack.block;
		
		if (owcFront.countAfterSpace >= owcBack.countAfterSpace) {
			owc.spacePosition = owcFront.countAfterSpace;
			owc.countAfterSpace = owcFront.countAfterSpace;
		} else {
			owc.spacePosition = owc.count;
			owc.countAfterSpace= owcBack.countAfterSpace;
		}
		
		return countToScore(owc);
	}
	
	private int countToScore(OneWayCount owc) {
		if (owc.count >= winLength) return Score.FIVE.val();
		if (owc.count >= winLength - 1 && owc.block == 0) return Score.FOUR.val();
		int count = owc.count + owc.countAfterSpace;
		if (count >= winLength - 1 && owc.block != 2) return Score.HALF_OPEN_FOUR.val();
		
		if (owc.block == 0) {
			switch (count) {
			case 3: return owc.spacePosition == 0 ? Score.THREE.val() : Score.THREE.val() - Score.ONE.val();
			case 2: return owc.spacePosition == 0 ? Score.TWO.val() : Score.TWO.val() - Score.ONE.val();
			case 1: return Score.ONE.val();
			}
		} else if (owc.block == 1) {
			switch (count) {
			case 3: return owc.spacePosition == 0 ? Score.HALF_OPEN_THREE.val() : Score.HALF_OPEN_THREE.val() - Score.ONE.val();
			case 2: return owc.spacePosition == 0 ? Score.HALF_OPEN_TWO.val() : Score.HALF_OPEN_TWO.val() - Score.ONE.val();
			case 1: return Score.HALF_OPEN_ONE.val();
			}
		}
		//owc.block > 1 && count < winLength
		return Score.BLOCKED.val();
	}
	
	private int scoreplus(int score1, int score2) {
		int score = score1 + score2;
		int min = Math.min(score1, score2);
		int max = Math.max(score1, score2);
		
		if (min >= Score.THREE.val() && max >= Score.FOUR.val() ) {
			score += Score.HALF_FOUR_AND_THREE.val();
		} else if (min >= Score.THREE.val()) {
			score += Score.DOUBLE_THREE.val();
		}
		return score;
	}
	
	/*
	 * Generate some better moves by heuristic searching for evaluation
	 */
	private List<Node> generateMoves(int role) {
		List<Node> nodes = new ArrayList<>();
		ArrayList<Node> opFive = new ArrayList<>();
		ArrayList<Node> myFourThree = new ArrayList<>();
		ArrayList<Node> opFourThree = new ArrayList<>();
		ArrayList<Node> myDoubleThree = new ArrayList<>();
		ArrayList<Node> opDoubleThree = new ArrayList<>();
		ArrayList<Node> myHALF_OPEN_FOUR = new ArrayList<>();
		ArrayList<Node> opHALF_OPEN_FOUR = new ArrayList<>();
		ArrayList<Node> myThree = new ArrayList<>();
		ArrayList<Node> opThree = new ArrayList<>();
		ArrayList<Node> myTwo = new ArrayList<>();
		ArrayList<Node> theRest = new ArrayList<>();
		int[][] myScore, opScore;
		if (role == Gomoku.PLAYER1) {
			myScore = p1Score;
			opScore = p2Score;
		} else {
			myScore = p2Score;
			opScore = p1Score;
		}
		
		for (int i=0; i<rowSize; i++) 
			for (int j=0; j<colSize; j++) 
				if (board[i][j] == Gomoku.EMPTY) {
					//Won when I have five
					if (myScore[i][j] >= Score.FIVE.val()) {
						nodes.add(new Node(i, j, role));
						return nodes;
					} 
					else if (opScore[i][j] >= Score.FIVE.val()) opFive.add(new Node(i, j, role, opScore[i][j]));
					else if (myScore[i][j] >= Score.HALF_FOUR_AND_THREE.val()) myFourThree.add(new Node(i, j, role, myScore[i][j]));
					else if (opScore[i][j] >= Score.HALF_FOUR_AND_THREE.val()) opFourThree.add(new Node(i, j, role, opScore[i][j]));
					else if (myScore[i][j] >= Score.DOUBLE_THREE.val()) myDoubleThree.add(new Node(i, j, role, myScore[i][j]));
					else if (opScore[i][j] >= Score.DOUBLE_THREE.val()) opDoubleThree.add(new Node(i, j, role, opScore[i][j]));
					else if (myScore[i][j] >= Score.HALF_OPEN_FOUR.val()) myHALF_OPEN_FOUR.add(new Node(i, j, role, myScore[i][j]));
					else if (opScore[i][j] >= Score.HALF_OPEN_FOUR.val()) opHALF_OPEN_FOUR.add(new Node(i, j, role, opScore[i][j]));
					else if (myScore[i][j] >= Score.THREE.val()) myThree.add(new Node(i, j, role, myScore[i][j]));
					else if (opScore[i][j] >= Score.THREE.val()) opThree.add(new Node(i, j, role, opScore[i][j]));
					else if (myScore[i][j] >= Score.TWO.val()) myTwo.add(new Node(i, j, role, myScore[i][j]));
					else theRest.add(new Node(i, j, role, opScore[i][j]));
				}
		
		if (opFive.size() > 0) {
			addNodesTo(nodes, opFive);
		} else if (myFourThree.size() > 0) {
			addNodesTo(nodes, myFourThree);
		} else if (opFourThree.size() > 0) {
			addNodesTo(nodes, opFourThree);
			addNodesTo(nodes, myHALF_OPEN_FOUR);
		} else if (myDoubleThree.size() > 0) {
			addNodesTo(nodes, myDoubleThree);
			addNodesTo(nodes, myHALF_OPEN_FOUR);
			addNodesTo(nodes, opHALF_OPEN_FOUR);
		} else if (opDoubleThree.size() > 0) {
			addNodesTo(nodes, opDoubleThree);
			addNodesTo(nodes, myHALF_OPEN_FOUR);
			addNodesTo(nodes, opHALF_OPEN_FOUR);
			addNodesTo(nodes, myThree);
		} else {
			addNodesTo(nodes, myHALF_OPEN_FOUR);
			addNodesTo(nodes, opHALF_OPEN_FOUR);
			addNodesTo(nodes, myThree);
			addNodesTo(nodes, opThree);
			addNodesTo(nodes, myTwo);
			addNodesTo(nodes, theRest);
			if (nodes.size() > CHILDREN_LIMIT) nodes = nodes.subList(0, CHILDREN_LIMIT);
		} 

		return nodes;
	}
	
	private void addNodesTo(List<Node> targetList, List<Node> nodes) {
		if (nodes.size() > 0) {
			Collections.sort(nodes, Collections.reverseOrder());
			targetList.addAll(nodes);
		}
	}
	
	/*
	 * Evaluate a score for the whole board. Positive is good for player2, negative is good for player1
	 */
	private int evaluate() {
		int score = 0;	
		for (int i=0; i<rowSize; i++) 
			for (int j=0; j<colSize; j++) 
				if (board[i][j] == Gomoku.EMPTY)
					score += p2Score[i][j] - p1Score[i][j];		
		return score;
	}
	
	/*
	 * Return the next turn player
	 */
	private int roleReversal(int player) {
		return player == Gomoku.PLAYER1 ? Gomoku.PLAYER2 : Gomoku.PLAYER1;
	}
	
	/*
	 * This method return the move for the condition that game run in the initial three moves turn.
	 */
	private Move openingMove() {
		int step = gomoku.getStep();
		Move move = null;
		int[] firstMove = {rowSize/2, colSize/2};
		int[][] thirdMoves = {
				{firstMove[0] - 2, firstMove[1]}, 
				{firstMove[0] + 2, firstMove[1]}, 
				{firstMove[0], firstMove[1] - 2}, 
				{firstMove[0], firstMove[1] + 2}};
		
		if (step <= 1) {
			move = new Move(firstMove[0], firstMove[1], gomoku.getCurrent(), gomoku.getStep());
		} else if (step <= 2) {
			Move lastMove = gomoku.getMoves().peek();
			int row = lastMove.getRow();
			int col = lastMove.getCol();
			row = rowSize - row > row ? row + 1 : row - 1;
			col = colSize - col > col ? col + 1 : col - 1;
			move = new Move(row, col, gomoku.getCurrent(), gomoku.getStep());
		} else if (step <= 3) {
			for (int[] m : thirdMoves) {
				if (!isInStarDirection(m[0], m[1], firstMove[0], firstMove[1]))
					return new Move(m[0], m[1], gomoku.getCurrent(), gomoku.getStep());
			}
			return new Move(thirdMoves[0][0], thirdMoves[0][1], gomoku.getCurrent(), gomoku.getStep());
		} else {
			throw new RuntimeException("openingMove only support the initial 3 moves.");
		}
		
		return move;
	}
	
	/*
	 * Check if point1 is on one of the star directions line of point2
	 */
	private boolean isInStarDirection(int row1, int col1, int row2, int col2) {
		int rowInc = row2 - row1;
		int colInc = col2 - col1;
		if (Math.abs(rowInc) == Math.abs(colInc) || rowInc == 0 || colInc == 0)
			return true;
		return false;
	}
	
	private String printNode(Node node, int depth) {
		String unitSpace = "   ";
		String space = "";
		for (int i=0; i<DEPTH-depth; i++)
			space += unitSpace;
		int step = gomoku.getStep() + DEPTH - depth;
		return space + node + " Step=" + step;
	}
	
	private class OneWayCount {
		int count;
		int countAfterSpace;
		int block;
		int spacePosition;
		
		public OneWayCount() {
			count = 1;
			countAfterSpace = 0;
			block = 0;
			spacePosition = 0;
		}
	}
	
	private class Node implements Comparable<Node> {
		private int row;
		private int col;
		private int score;
		private int role;
		private List<Node> children;
		private boolean isCut;
		
		public Node(int row, int col, int role) {
			this.row = row;
			this.col = col;
			this.role = role;
			this.score = 0;
			this.children = new ArrayList<>();
			this.isCut = false;
		}
		
		public Node(int row, int col, int role, int score) {
			this(row, col, role);
			this.score = score;
		}
		
		//Only for build the MAX and MIN nodes
		public Node(int score) {
			this(-1, -1, Gomoku.EMPTY);
			this.score = score;
		}

		@Override
		public int compareTo(Node other) {
			return Integer.compare(this.score, other.score);
		}

		@Override
		public String toString() {
			String player;
			switch (role) {
				case Gomoku.EMPTY:
					player = "Empty";
					break;
				case Gomoku.PLAYER1:
					player = "Black";
					break;
				case Gomoku.PLAYER2:
					player = "Red";
					break;
				default:
					player = "Error: can't reconize " + role;
			}
			String cut = isCut ? "##_cut_##" : "";
			return "Node[" + row + ", " + col + "]: score=" + score + ", role=" + player + " " + cut;
		}
	}
}
