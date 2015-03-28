import java.util.*;
import java.util.Random;

public class QuartoPlayer2Agent extends QuartoAgent {

	private static final int NUMBER_OF_BOARD_LINES = 12;
	private static final int INVALID_CHARACTERISTIC = 2;

	//Example AI
	public QuartoPlayer2Agent(GameClient gameClient, String stateFileName) {
		// because super calls one of the super class constructors(you can overload constructors), you need to pass the parameters required.
		super(gameClient, stateFileName);
	}

	//MAIN METHOD
	public static void main(String[] args) {
		//start the server
		GameClient gameClient = new GameClient();
		
		String ip = null;
		String stateFileName = null;
		//IP must be specified
		if(args.length > 0) {
			ip = args[0];
		} else {
			System.out.println("No IP Specified");
			System.exit(0);
		}
		if (args.length > 1) {
			stateFileName = args[1];
		}
		
		gameClient.connectToServer(ip, 4321);
		QuartoPlayer2Agent quartoAgent = new QuartoPlayer2Agent(gameClient, stateFileName); 
		quartoAgent.play();
		
		gameClient.closeConnection();
	}

	private boolean checkForWin(QuartoBoard gameBoard, int row, int col, int piece){
		QuartoBoard copyBoard = new QuartoBoard(gameBoard);
		copyBoard.insertPieceOnBoard(row, col, piece);
		if (copyBoard.checkRow(row) || copyBoard.checkColumn(col) || copyBoard.checkDiagonals()) {
			return true;
		}
		return false;
	}
	
	/*
	 * Do Your work here
	 * The server expects a move in the form of:   row,column
	 */
	@Override
	protected String moveSelectionAlgorithm(int pieceId) {
		//If there is a winning move, take it
		for(int row = 0; row < this.quartoBoard.getNumberOfRows(); row++) {
			for(int column = 0; column < this.quartoBoard.getNumberOfColumns(); column++) {
				if(this.quartoBoard.getPieceOnPosition(row, column) == null) {
					if (checkForWin(this.quartoBoard,row,column,pieceId)) {
						return row + "," + column;
					}

				}
			}
		}

		return evaluateUtility(this.quartoBoard, pieceId);
	}
	
	private String evaluateUtility(QuartoBoard gameBoard, int pieceId) {
		BoardLine[] boardLines = utility(gameBoard);
		boolean[] pieceCharacteristics = new QuartoPiece(pieceId).getCharacteristicsArray();
		
		int bestUtilityIndex = 0;
		for (int line = 0; line < boardLines.length; line++) {
			if (boardLines[line].isValid() && !boardLines[line].getAvailableSpots().isEmpty()) {
				for (int i = 0; i < boardLines[line].getCharacteristics().length; i++) {
					if (((boardLines[line].getCharacteristics()[i] == 0 && !pieceCharacteristics[i]) || 
							(boardLines[line].getCharacteristics()[i] == 1 && pieceCharacteristics[i])) && 
							boardLines[line].getCount() > boardLines[bestUtilityIndex].getCount()) {
						bestUtilityIndex = line;
						break;
					}
				}
			}
		}
		
		if (boardLines[bestUtilityIndex].getAvailableSpots().isEmpty()) {
			int[] location = gameBoard.chooseRandomPositionNotPlayed(100);
			return  location[0] + "," + location[1];
		}
		
		String bestLocaiton = "";
		int bestUtility = 0;
		for (String location : boardLines[bestUtilityIndex].getAvailableSpots()) {
			QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);
			int row = Integer.parseInt(location.split(",")[0]);
			int col = Integer.parseInt(location.split(",")[1]);
			copyBoard.insertPieceOnBoard(row, col, pieceId);
			
			BoardLine[] testLines = utility(copyBoard);
			
			int tempTotal = 0;
			
			//Utitility for row
			tempTotal += testLines[row].getCount();
			
			//Utitility for col
			tempTotal += testLines[col + 5].getCount();
			
			//Utitility for top left to bottom right
			if (row == col){
				tempTotal += testLines[10].getCount();
			}
			
			//Utitility for top right to bottom left
			if ((row + col) == 4){
				tempTotal += testLines[11].getCount();
			}
			
			if (tempTotal > bestUtility) {
				bestUtility = tempTotal;
				bestLocaiton = location;
			}
		}
		
		return bestLocaiton;
	}
	
	/*
	 * This code will try to find a piece that the other player can't make a winning move off of
	 */
	@Override
	protected String pieceSelectionAlgorithm() {
		return pieceSelectionAlgorithm(this.quartoBoard);
	}
	
	private String pieceSelectionAlgorithm(QuartoBoard gameBoard) {
		// if (this.playerNumber == 1) {
			return String.format("%5s", Integer.toBinaryString(findWorstPiece())).replace(' ', '0');
		// }
		// return String.format("%5s", Integer.toBinaryString(pickPieceWithUtility(gameBoard))).replace(' ', '0');
	}
	
	// private int pickPieceWithUtilityHelper(QuartoBoard gameBoard, int[] valueOfCharacteristics, int depth) {
	// 	int[] chosenPiece = new int[5];
	   
	// 	if (valueOfCharacteristics[0] < valueOfCharacteristics[1]) {
	// 		chosenPiece[0] = 0;
	// 	} else {
	// 		chosenPiece[0] = 1;
	// 	}
		
	// 	if (valueOfCharacteristics[2] < valueOfCharacteristics[3]) {
	// 		chosenPiece[1] = 0;
	// 	} else {
	// 		chosenPiece[1] = 1;
	// 	}
		
	// 	if (valueOfCharacteristics[4] < valueOfCharacteristics[5]) {
	// 		chosenPiece[2] = 0;
	// 	} else {
	// 		chosenPiece[2] = 1;
	// 	}
		
	// 	if (valueOfCharacteristics[6] < valueOfCharacteristics[7]) {
	// 		chosenPiece[3] = 0;
	// 	} else {
	// 		chosenPiece[3] = 1;
	// 	}
		
	// 	if (valueOfCharacteristics[8] < valueOfCharacteristics[9]) {
	// 		chosenPiece[4] = 0;
	// 	} else {
	// 		chosenPiece[4] = 1;
	// 	}
		
	// 	// Binary conversion of the array
	// 	int piece = chosenPiece[0] + 
	// 				(chosenPiece[1] * 2) + 
	// 				(chosenPiece[2] * 4) + 
	// 				(chosenPiece[3] * 8) + 
	// 				(chosenPiece[4] * 16);
	 
	// 	boolean skip = false;
	// 	if (!gameBoard.isPieceOnBoard(piece) && depth != 0) {
	// 		for (int row = 0; row < gameBoard.getNumberOfRows(); row++) {
	// 			for (int col = 0; col < gameBoard.getNumberOfColumns(); col++) {
	// 				if (!this.quartoBoard.isSpaceTaken(row, col)) {
	// 					if (checkForWin(gameBoard,row,col,piece)) {
	// 						skip = true;
	// 						break;
	// 					}
	// 				}
	// 			}
	// 			if (skip) {
	// 				break;
	// 			}
				
	// 		}
	// 		if (!skip) {
	// 			return piece;
	// 		}
	// 	} else if (depth == 0) {
	// 		for (int i = 0; i < this.quartoBoard.getNumberOfPieces(); i++) {
	// 			skip = false;
	// 			if (!this.quartoBoard.isPieceOnBoard(i)) {
	// 				for (int row = 0; row < this.quartoBoard.getNumberOfRows(); row++) {
	// 					for (int col = 0; col < this.quartoBoard.getNumberOfColumns(); col++) {
	// 						if (!this.quartoBoard.isSpaceTaken(row, col)) {
	// 							if (checkForWin(gameBoard,row,col,i)) {
	// 								skip = true;
	// 								break;
	// 							}
	// 						}
	// 					}
	// 					if (skip) {
	// 						break;
	// 					}
	// 				}
	// 				if (!skip) {
	// 					return i;
	// 				}
	// 			}
	// 		}
	// 		//Nothing found just give a random Piece
	// 		int pieceId = this.quartoBoard.chooseRandomPieceNotPlayed(100);
			
	// 		return pieceId;
	// 	}
		
	// 	int lowestIndex = 0;
	// 	for (int i = 0; i < valueOfCharacteristics.length; i++) {
	// 		if (valueOfCharacteristics[i] < valueOfCharacteristics[lowestIndex]) {
	// 			lowestIndex = i;
	// 		}
	// 	}
		
	// 	valueOfCharacteristics[lowestIndex] = valueOfCharacteristics[lowestIndex] * 2 + 1;
	// 	return pickPieceWithUtilityHelper(gameBoard, valueOfCharacteristics, --depth);
	// }
	
	private int findWorstPiece() {
		BoardLine[] boardLines = utility(this.quartoBoard);
		
		// Find the worst characteristics
		int hightestIndex = 0;
		int[] badCharacteristics = null;
		
		for (int i = 0; i < boardLines.length; i++) {
			if (boardLines[hightestIndex].isValid()) {
				if (boardLines[hightestIndex].getCount() < boardLines[i].getCount()) {
					hightestIndex = i;
					badCharacteristics = boardLines[hightestIndex].getCharacteristics();
				} else if (boardLines[hightestIndex].getCount() == boardLines[i].getCount()) {
					int[] newCharacteristics = boardLines[i].getCharacteristics();
					int[] characteristics = boardLines[hightestIndex].getCharacteristics();
					
					for (int j = 0; j < characteristics.length; j++) {
						if (characteristics[j] == 2 && newCharacteristics[j] == 0) {
							characteristics[j] = 0;
						} else if (characteristics[j] == 2 && newCharacteristics[j] == 1) {
							characteristics[j] = 1;
						} else if (characteristics[j] != newCharacteristics[j]) {
							characteristics[j] = 2;
						}
					}
					badCharacteristics = characteristics;
				}
			}
		}
		
		// If no bad characteristics are found, just send back a random piece 
		// that doesn't allow them to win.
		if (badCharacteristics == null) {
			boolean skip = false;
			for (int i = 0; i < this.quartoBoard.getNumberOfPieces(); i++) {
				skip = false;
				if (!this.quartoBoard.isPieceOnBoard(i)) {
					for (int row = 0; row < this.quartoBoard.getNumberOfRows(); row++) {
						for (int col = 0; col < this.quartoBoard.getNumberOfColumns(); col++) {
							if (!this.quartoBoard.isSpaceTaken(row, col)) {
								if (checkForWin(this.quartoBoard,row,col,i)) {
									skip = true;
									break;
								}
							}
						}
						if (skip) {
							break;
						}
					}
					if (!skip) {
						return i;
					}
				}
			}
			//Nothing found just give a random Piece
			int pieceId = this.quartoBoard.chooseRandomPieceNotPlayed(100);
			
			return pieceId;
		}
		
		// Flip all for certain characteristics
		for (int i = 0; i < badCharacteristics.length; i++) {
			if (badCharacteristics[i] == 1) {
				badCharacteristics[i] = 0;
			} else if (badCharacteristics[i] == 0) {
				badCharacteristics[i] = 1;
			}
		}
		
		// Handle the wildcards (2)
		int[] worstPiece = handleWildCards(this.quartoBoard, badCharacteristics);
		
		// A -1 represents that all the worst pieces have been taken
		if (worstPiece[0] == -1) {
			System.out.println("NO GOOD PIECE");
			boolean skip = false;
			for (int i = 0; i < this.quartoBoard.getNumberOfPieces(); i++) {
				skip = false;
				if (!this.quartoBoard.isPieceOnBoard(i)) {
					for (int row = 0; row < this.quartoBoard.getNumberOfRows(); row++) {
						for (int col = 0; col < this.quartoBoard.getNumberOfColumns(); col++) {
							if (!this.quartoBoard.isSpaceTaken(row, col)) {
								if (checkForWin(this.quartoBoard,row,col,i)) {
									skip = true;
									break;
								}
							}
						}
						if (skip) {
							break;
						}
	
					}
					if (!skip) {
						return i;
					}
				}
			}
			//if we don't find a piece in the above code just grab the first random piece
			return this.quartoBoard.chooseRandomPieceNotPlayed(100);
		}
		
		// Binary conversion of the array
		int pieceId = worstPiece[0] + 
					(worstPiece[1] * 2) + 
					(worstPiece[2] * 4) + 
					(worstPiece[3] * 8) + 
					(worstPiece[4] * 16);
		return pieceId;
	}
	
	private int[] handleWildCards(QuartoBoard gameBoard, int[] characteristics) {
		
		int[] piece0 = null;
		int[] piece1 = null;
		for(int i = 0; i < characteristics.length; i++) {
			if (characteristics[i] == 2) {
				characteristics[i] = 0;
				piece0 = handleWildCards(gameBoard, characteristics);
				
				characteristics[i] = 1;
				piece1 = handleWildCards(gameBoard, characteristics);
			}
		}
		
		if (piece0 == null || piece1 == null) {
			return characteristics;
		}
		
		int utility0 = handleWildCardsHelper(gameBoard, piece0);
		int utility1 = handleWildCardsHelper(gameBoard, piece1);
		
		if (utility0 == -1 && utility1 == -1) {
			return new int[]{-1, -1, -1, -1, -1};
		} else if (utility0 == -1) {
			return piece1;
		} else if (utility1 == -1) {
			return piece0;
		} else if (utility0 < utility1) {
			return piece0;
		}
		
		return piece1;
	}
	
	private int handleWildCardsHelper(QuartoBoard gameBoard, int[] characteristics) {
		
		int maxUtility = 0;
		
		// Binary conversion of the array
		int piece = characteristics[0] + 
					(characteristics[1] * 2) + 
					(characteristics[2] * 4) + 
					(characteristics[3] * 8) + 
					(characteristics[4] * 16);
					
		if (gameBoard.isPieceOnBoard(piece)){
			return -1;
		}

		for (int row = 0; row < this.quartoBoard.getNumberOfRows(); row++) {
			for (int col = 0; col < this.quartoBoard.getNumberOfColumns(); col++) {
				if (!this.quartoBoard.isSpaceTaken(row, col)) {
					if (checkForWin(this.quartoBoard,row,col,piece)) {
						//return -1 becasue peice would let opponent win.
						return -1;
					}
				}
			}
		}
		
		for (int row = 0; row < gameBoard.getNumberOfRows(); row++){
			for (int col = 0; col < gameBoard.getNumberOfColumns(); col++){
				QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);
				copyBoard.insertPieceOnBoard(row, col, piece);
				
				BoardLine[] testLines = utility(copyBoard);
				
				int tempTotal = 0;
				for (BoardLine line : testLines) {
					tempTotal += line.getCount();
				}
				
				if (tempTotal > maxUtility) {
					maxUtility = tempTotal;
				}
			}
		}
		
		return maxUtility;
	}
	
	private BoardLine[] utility(QuartoBoard gameBoard) {
		BoardLine[] boardLines = new BoardLine[NUMBER_OF_BOARD_LINES];
		
		int line = 0;
		
		// !----  Rows  ----!
		for (int row = 0; row < gameBoard.getNumberOfRows(); row++) {
			int[] lineCharacteristics = null;
			ArrayList<String> availableSpots = new ArrayList<String>();
			int count = 0;
			
			for (int column = 0; column < gameBoard.getNumberOfColumns(); column++) {
				QuartoPiece piece = gameBoard.getPieceOnPosition(row, column);
				if (piece != null) {
					if (lineCharacteristics == null) {
						lineCharacteristics = new int[5];
						for (int i = 0; i < piece.getCharacteristicsArray().length; i++) {
							if (piece.getCharacteristicsArray()[i]) {
								lineCharacteristics[i] = 1;
							} else {
								lineCharacteristics[i] = 0;
							}
						}
					} else {
						for (int i = 0; i < piece.getCharacteristicsArray().length; i++) {
							if (piece.getCharacteristicsArray()[i] && lineCharacteristics[i] == 0) {
								lineCharacteristics[i] = INVALID_CHARACTERISTIC;
							} else if (!piece.getCharacteristicsArray()[i] && lineCharacteristics[i] == 1) {
								lineCharacteristics[i] = INVALID_CHARACTERISTIC;
							}
						}
					}
					count++;
				} else {
					availableSpots.add(row + "," + column);
				}
			}
			
			boolean valid = false;
			if (lineCharacteristics != null) {
				for (int charac : lineCharacteristics) {
					if (charac != INVALID_CHARACTERISTIC) {
						valid = true;
						break;
					}
				}
			}
			
			if (valid) {
				boardLines[line] = new BoardLine(lineCharacteristics, count, availableSpots);
			} else {
				boardLines[line] = new BoardLine(valid); 
			}
			
			line++;
		}
		
		// !----  Columns  ----!
		for (int column = 0; column < gameBoard.getNumberOfColumns(); column++) {
			int[] lineCharacteristics = null;
			ArrayList<String> availableSpots = new ArrayList<String>();
			int count = 0;
			
			for (int row = 0; row < gameBoard.getNumberOfRows(); row++) {
				QuartoPiece piece = gameBoard.getPieceOnPosition(row, column);
				if (piece != null) {
					if (lineCharacteristics == null) {
						lineCharacteristics = new int[5];
						for (int i = 0; i < piece.getCharacteristicsArray().length; i++) {
							if (piece.getCharacteristicsArray()[i]) {
								lineCharacteristics[i] = 1;
							} else {
								lineCharacteristics[i] = 0;
							}
						}
					} else {
						for (int i = 0; i < piece.getCharacteristicsArray().length; i++) {
							if (piece.getCharacteristicsArray()[i] && lineCharacteristics[i] == 0) {
								lineCharacteristics[i] = INVALID_CHARACTERISTIC;
							} else if (!piece.getCharacteristicsArray()[i] && lineCharacteristics[i] == 1) {
								lineCharacteristics[i] = INVALID_CHARACTERISTIC;
							}
						}
					}
					count++;
				} else {
					availableSpots.add(row + "," + column);
				}
			}
			
			boolean valid = false;
			if (lineCharacteristics != null) {
				for (int charac : lineCharacteristics) {
					if (charac != INVALID_CHARACTERISTIC) {
						valid = true;
						break;
					}
				}
			}
			
			if (valid) {
				boardLines[line] = new BoardLine(lineCharacteristics, count, availableSpots);
			} else {
				boardLines[line] = new BoardLine(valid); 
			}
			
			line++;
		}
		
		// !----  Top Left to Bottom Right  ----!
		int[] lineCharacteristics = null;
		ArrayList<String> availableSpots = new ArrayList<String>();
		int count = 0;
		
		int row = 0;
		for (int column = 0; column < gameBoard.getNumberOfColumns(); column++) {
			QuartoPiece piece = gameBoard.getPieceOnPosition(row, column);
			if (piece != null) {
				if (lineCharacteristics == null) {
					lineCharacteristics = new int[5];
					for (int i = 0; i < piece.getCharacteristicsArray().length; i++) {
						if (piece.getCharacteristicsArray()[i]) {
							lineCharacteristics[i] = 1;
						} else {
							lineCharacteristics[i] = 0;
						}
					}
				} else {
					for (int i = 0; i < piece.getCharacteristicsArray().length; i++) {
						if (piece.getCharacteristicsArray()[i] && lineCharacteristics[i] == 0) {
							lineCharacteristics[i] = INVALID_CHARACTERISTIC;
						} else if (!piece.getCharacteristicsArray()[i] && lineCharacteristics[i] == 1) {
							lineCharacteristics[i] = INVALID_CHARACTERISTIC;
						}
					}
				}
				count++;
			} else {
				availableSpots.add(row + "," + column);
			}
			row++;
		}
		
		boolean valid = false;
		if (lineCharacteristics != null) {
			for (int charac : lineCharacteristics) {
				if (charac != INVALID_CHARACTERISTIC) {
					valid = true;
					break;
				}
			}
		}
		
		if (valid) {
			boardLines[line] = new BoardLine(lineCharacteristics, count, availableSpots);
		} else {
			boardLines[line] = new BoardLine(valid); 
		}
		
		line++;
		
		// !----  Top Right to Bottom Left  ----!
		lineCharacteristics = null;
		availableSpots = new ArrayList<String>();
		count = 0;
		
		row = this.quartoBoard.getNumberOfRows();
		for (int column = 0; column < gameBoard.getNumberOfColumns(); column++) {
			QuartoPiece piece = gameBoard.getPieceOnPosition(row, column);
			if (piece != null) {
				if (lineCharacteristics == null) {
					lineCharacteristics = new int[5];
					for (int i = 0; i < piece.getCharacteristicsArray().length; i++) {
						if (piece.getCharacteristicsArray()[i]) {
							lineCharacteristics[i] = 1;
						} else {
							lineCharacteristics[i] = 0;
						}
					}
				} else {
					for (int i = 0; i < piece.getCharacteristicsArray().length; i++) {
						if (piece.getCharacteristicsArray()[i] && lineCharacteristics[i] == 0) {
							lineCharacteristics[i] = INVALID_CHARACTERISTIC;
						} else if (!piece.getCharacteristicsArray()[i] && lineCharacteristics[i] == 1) {
							lineCharacteristics[i] = INVALID_CHARACTERISTIC;
						}
					}
				}
				count++;
			} else {
				availableSpots.add(row + "," + column);
			}
			
			row--;
		}
		
		valid = false;
		if (lineCharacteristics != null) {
			for (int charac : lineCharacteristics) {
				if (charac != INVALID_CHARACTERISTIC) {
					valid = true;
					break;
				}
			}
		}
		
		if (valid) {
			boardLines[line] = new BoardLine(lineCharacteristics, count, availableSpots);
		} else {
			boardLines[line] = new BoardLine(valid); 
		}
		
		line++;
		
		return boardLines;
	}
	
	//loop through board and see if the game is in a won state
	private boolean checkIfGameIsWon() {
		//loop through rows
		for (int i = 0; i < NUMBER_OF_ROWS; i++) {
			//gameIsWon = this.quartoBoard.checkRow(i);
			if (this.quartoBoard.checkRow(i)) {
				System.out.println("Win via row: " + (i) + " (zero-indexed)");
				return true;
			}
		}
		
		//loop through columns
		for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
			//gameIsWon = this.quartoBoard.checkColumn(i);
			if (this.quartoBoard.checkColumn(i)) {
				System.out.println("Win via column: " + (i) + " (zero-indexed)");
				return true;
			}
		}
		
		//check Diagonals
		if (this.quartoBoard.checkDiagonals()) {
			System.out.println("Win via diagonal");
			return true;
		}
		
		return false;
	}
	
	private int pickPieceWithUtility(QuartoBoard gameBoard) {
		BoardLine[] boardLines = utility(gameBoard); 
		
		int[] valueOfCharacteristics = new int[10];
		int index = 0;
		for (BoardLine line : boardLines) {
			if (line.isValid()) {
				for (int i = 0; i < line.getCharacteristics().length; i++) {
					if (line.getCharacteristics()[i] == 0) {
						valueOfCharacteristics[index] += line.getCount();
					} else if (line.characteristics[i] == 1) {
						valueOfCharacteristics[index+1] += line.getCount();
					}
					index = index + 2;
				}
				index = 0;
			}
		}
		
		return pickPieceWithUtilityHelper(gameBoard, valueOfCharacteristics, 64);
	}
	
	private class BoardLine {
		private int[] characteristics;
		private int count = 0;
		private ArrayList<String> availableSpots;
		private boolean valid;
		
		public BoardLine(int[] characteristics, int count, ArrayList<String> availableSpots) {
			this.characteristics = characteristics;
			this.count = count*count;
			this.availableSpots = availableSpots;
			this.valid = true;
		}
		
		public BoardLine(boolean valid) {
			this.valid = valid;
			this.availableSpots = new ArrayList<String>();
		}
		
		public int[] getCharacteristics() {
			return this.characteristics;
		}
		
		public void setCharacteristics(int[] characteristics) {
			this.characteristics = characteristics;
		}
		
		public int getCount() {
			return this.count;
		}
		
		public ArrayList<String> getAvailableSpots() {
			return availableSpots;
		}
		
		public boolean isValid() {
			return this.valid;
		}
		
		public void setValid(boolean valid) {
			this.valid = valid;
		}
	}
	
	private class Node {
		private Node parent;
		private ArrayList<Node> children;
		private int utility;
		private QuartoBoard gameBoard;
		private boolean[] pieceCharacteristics;
		private Node bestChild;
		
		public Node(QuartoBoard gameBoard, boolean[] pieceCharacteristics) {
			this.parent = null;
			this.children = new ArrayList<Node>();
			this.utility = -1;
			this.gameBoard = gameBoard;
			this.pieceCharacteristics = pieceCharacteristics;
		}
		
		public Node(Node parent, QuartoBoard gameBoard, boolean[] pieceCharacteristics) {
			this.parent = parent;
			this.children = new ArrayList<Node>();
			this.utility = -1;
			this.gameBoard = gameBoard;
			this.pieceCharacteristics = pieceCharacteristics;
		}
		
		public void setUtility(int utility) {
			this.utility = utility;
		}
		
		public void addChild(Node child) {
			this.children.add(child);
			
			if(child.getUtility() > this.bestChild.getUtility()) {
				this.bestChild = child;
			}
			else if(child.getUtility() == this.bestChild.getUtility()) {
				Random randomGenerator = new Random();
				int randomNumber = randomGenerator.nextInt(2);
				if(randomNumber % 2 == 0) {
					this.bestChild = child;
				}
			}
		}
		
		public void removeChild(Node child) {
			this.children.remove(child);
		}
		
		public void removeChild(int index) {
			this.children.remove(index);
		}
		
		public Node getChild(Node child) {
			for (Node n : children) {
				if (n.equals(child)) {
					return n;
				}
			}
			return null;
		}
		
		public Node getChild(int index) {
			return children.get(index);
		}
		
		public int getUtility() {
			return utility;
		}
		
		public Node getParent() {
			return parent;
		}
		
		public QuartoBoard getGameBoard() {
			return gameBoard;
		}
		
		public boolean[] getCharacteristics(){
			return this.pieceCharacteristics;
		}
	}
}