/**
 * GomokuGUI.java
 *
 * @author Mingjie Deng
 * @version 1.0 (Sep 12, 2018)
 */
package game;

import java.util.Stack;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.text.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.geometry.VPos;

/**
 * This class run a GUI for the game
 */
public class GomokuGUI extends Application {
	private Gomoku gomoku;
	private AI ai;
	private Stack<Move> moves;
	private Canvas canvas;
	private GraphicsContext gc;
	private int gridNum;
	private double gridWidth;
	private double offset;
	private Pane root;
	private StackPane stones[][];
	
	/**
	 * Constructor
	 */
	public GomokuGUI() {
		this.gomoku = new Gomoku();
		this.ai = new AI(gomoku);
		this.moves = gomoku.getMoves();
		this.canvas = new Canvas(600, 600);
		this.gc = canvas.getGraphicsContext2D();
		this.gridNum = gomoku.getGridNum();
		double width = Math.min(canvas.getWidth(), canvas.getHeight());
		this.gridWidth = width / (gridNum + 2);
		this.offset = gridWidth * 3 / 2;
		this.root = new Pane();
		this.stones = new StackPane[gridNum][gridNum];
	}
	
	/**
	 * The entry point of the GUI
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Get started by drawing the board and listening the mouse event
	 */
	@Override
	public void start(Stage primaryStage) {
		drawBoard();
		canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				MouseButton button = e.getButton();
				if(button==MouseButton.PRIMARY) {
					clickBoard(e);
				} else if (button==MouseButton.SECONDARY) {
					boolean isHuman[] = gomoku.getIsHuman();
					if (isHuman[0] && isHuman[1])
						retreat();
				}
			}
		});
		root.getChildren().add(canvas);
		primaryStage.setTitle("Game: Gomoku / Gobang / Five_In_A_Row");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();		
	}
	
	private void clickBoard(MouseEvent e) {
		boolean[] isHuman = gomoku.getIsHuman();
		boolean currentIsHuman = gomoku.getCurrent() == Gomoku.PLAYER1 ? isHuman[0] : isHuman[1];
		if (currentIsHuman) {
			double x = e.getX();
			double y = e.getY();
			int row = positionToIndex(y);
			int col = positionToIndex(x);
			
			makeAMove(row, col);
		}
	}
	
	private boolean makeAMove(int row, int col) {
		if (gomoku.makeAMove(row, col)) {
			Move lastMove = moves.peek();
			drawMove(row, col, lastMove.getPlayer(), lastMove.getStep());
			
			//Check if the game end
			checkStatus();
			return true;
		} else {
			//Prompt position is not available
			return false;
		}
	}
	
	private void retreat() {
		Move lastMove = gomoku.retreat();
		if (lastMove != null) {
			int row = lastMove.getRow();
			int col = lastMove.getCol();
			root.getChildren().remove(stones[row][col]);
		}
	}
	
	private void checkStatus() {		
		switch(gomoku.getStatus()) {
			case Gomoku.GAME_DRAW:
				terminalGame("Game Is A Draw", Color.NAVY);
				break;
				
			case Gomoku.PLAYER1:
				terminalGame("Player Black Won", Color.DARKGREEN);
				break;
				
			case Gomoku.PLAYER2:
				terminalGame("Player Red Won", Color.DARKRED);
				break;
				
			case Gomoku.NOT_OVER:
			default:
				boolean[] isHuman = gomoku.getIsHuman();
				boolean currentIsHuman = gomoku.getCurrent() == Gomoku.PLAYER1 ? isHuman[0] : isHuman[1];
				if (!currentIsHuman) {
					Move move = ai.nextMove();
					makeAMove(move.getRow(), move.getCol());
				}
				break;
		}
		
	}
	
	private void terminalGame(String txt, Color color) {
		//Remove mouse event listener
		canvas.setOnMouseClicked(null);
		
		Text msg = new Text(canvas.getWidth()/4, canvas.getHeight()-10, txt);
		msg.setFill(color);
		msg.setFont(new Font(40));
		msg.setTextAlignment(TextAlignment.CENTER);
		root.getChildren().add(msg);
	}
	
	private int positionToIndex(double x) {
		return (int) ((x - offset + gridWidth / 2) / gridWidth);
	}
	
	private void drawMove(int row, int col, int player, int step) {
		double ballWidth = gridWidth * 3 / 4;
		double x = offset + col * gridWidth;
		double y = offset + row * gridWidth;
		Color bgColor, fontColor;
		if (player == Gomoku.PLAYER1) {
			bgColor = Color.BLACK;
			fontColor = Color.RED;
		} else {
			bgColor = Color.RED;
			fontColor = Color.BLACK;
		}

        Circle circle = new Circle(ballWidth/2);
        circle.setFill(bgColor);
        circle.relocate(0, 0);
		
        Text text = new Text(Integer.toString(step));
        text.setFont(new Font(15));
        text.setFill(fontColor);
        
        stones[row][col] = new StackPane();
        stones[row][col].getChildren().addAll(circle, text);
        stones[row][col].relocate(x-ballWidth/2, y-ballWidth/2);
        
        root.getChildren().add(stones[row][col]);
	}

	private void drawBoard() {
		//Draw the background image first
		Image img = new Image(getClass().getResourceAsStream("../bg1.jpg"));
		gc.drawImage(img, 0, 0, canvas.getWidth(), canvas.getHeight());
		
		//Draw the grids and the numbers of the row and column
		double start, end = offset + gridWidth * (gridNum - 1);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setTextBaseline(VPos.CENTER);	
		for (int i=0; i<gridNum; i++) {
			start = offset + gridWidth * i;
			gc.strokeLine(offset, start, end, start);
			gc.strokeLine(start, offset, start, end);
			gc.fillText(Integer.toString(i), start , gridWidth*7/8);
			gc.fillText(Integer.toString(i), gridWidth*7/8, start);
		}
	}
}
