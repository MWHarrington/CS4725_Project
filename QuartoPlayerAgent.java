	public class QuartoPlayerAgent extends QuartoAgent {
	
		//Example AI
		public QuartoPlayerAgent(GameClient gameClient, String stateFileName) {
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
			if (args.length > 0) {
				ip = args[0];
			} else {
				System.out.println("No IP Specified");
				System.exit(0);
			}
			if (args.length > 1) {
				stateFileName = args[1];
			}
	
			gameClient.connectToServer(ip, 4321);
			QuartoPlayerAgent quartoAgent = new QuartoPlayerAgent(gameClient, stateFileName);
			quartoAgent.play();
	
			gameClient.closeConnection();
	
		}
		
		/*
		 * This code will try to find a piece that the other player can't make a winning move off of
		 */
		@Override
		protected String pieceSelectionAlgorithm() {
			return pieceSelectionAlgorithm(this.quartoBoard, 2, 1);
		}
		
		private String pieceSelectionAlgorithm(QuartoBoard gameBoard) {
			return pieceSelectionAlgorithm(gameBoard, 1, 1);
		}
		private String pieceSelectionAlgorithm(QuartoBoard gameBoard, int maxDepth, int depth) {
			//some useful lines:
			//String BinaryString = String.format("%5s", Integer.toBinaryString(pieceID)).replace(' ', '0');
	
			this.startTimer();
			
			String potential = null;
			boolean skip = false;
			for (int i = 0; i < gameBoard.getNumberOfPieces(); i++) {
				skip = false;
				if (!gameBoard.isPieceOnBoard(i)) {
					for (int row = 0; row < gameBoard.getNumberOfRows(); row++) {
						for (int col = 0; col < gameBoard.getNumberOfColumns(); col++) {
							if (!this.quartoBoard.isSpaceTaken(row, col)) {
								QuartoBoard copyBoard = new QuartoBoard(gameBoard);
								copyBoard.insertPieceOnBoard(row, col, i);
								if (copyBoard.checkRow(row) || copyBoard.checkColumn(col) || copyBoard.checkDiagonals()) {
									skip = true;
									break;
								} else if (depth < maxDepth) {
									potential = String.format("%5s", Integer.toBinaryString(i)).replace(' ', '0');
									boolean skip2 = false;
									for (int row2 = 0; row2 < copyBoard.getNumberOfRows(); row2++) {
										for (int col2 = 0; col2 < copyBoard.getNumberOfColumns(); col2++) {
											if (!copyBoard.isSpaceTaken(row2, col2)) {
												QuartoBoard copyBoard2 = new QuartoBoard(copyBoard);
		
												copyBoard2.insertPieceOnBoard(row2, col2, Integer.parseInt(pieceSelectionAlgorithm(copyBoard2, maxDepth, ++depth), 2));
												if (copyBoard2.checkRow(row) || copyBoard2.checkColumn(col) || copyBoard2.checkDiagonals()) {
													skip2 = true;
													break;
												}
											}
										}
										if (skip2) {
											break;
										}
									}
									if (!skip2) {
										return String.format("%5s", Integer.toBinaryString(i)).replace(' ', '0');
									}
								}
							}
						}
						if (skip) {
							break;
						}
	
					}
					if (!skip) {
						return String.format("%5s", Integer.toBinaryString(i)).replace(' ', '0');
					}
	
				}
				if (this.getMillisecondsFromTimer() > (this.timeLimitForResponse - COMMUNICATION_DELAY)) {
					//handle for when we are over some imposed time limit (make sure you account for communication delay)
				}
				// String message = null;
				//for every other i, check if there is a missed message
				/*
				if (i % 2 == 0 && ((message = this.checkForMissedServerMessages()) != null)) {
					//the oldest missed message is stored in the variable message.
					//You can see if any more missed messages are in the socket by running this.checkForMissedServerMessages() again
				}
				*/
			}
	
			if (potential != null) {
				return potential;
			}
			
			//if we don't find a piece in the above code just grab the first random piece
			int pieceId = gameBoard.chooseRandomPieceNotPlayed(100);
			String BinaryString = String.format("%5s", Integer.toBinaryString(pieceId)).replace(' ', '0');
	
	
			return BinaryString;
		}
	
		/*
		 * Do Your work here
		 * The server expects a move in the form of:   row,column
		 */
		@Override
		protected String moveSelectionAlgorithm(int pieceID){
			return moveSelectionAlgorithm(pieceID, true);
		}
		
		private String moveSelectionAlgorithm(int pieceID, boolean goDeep) {
			String option = null;
			int optionDepth = 100;
	
			//If there is a winning move, take it
			for (int row = 0; row < this.quartoBoard.getNumberOfRows(); row++) {
				for (int column = 0; column < this.quartoBoard.getNumberOfColumns(); column++) {
					if (this.quartoBoard.getPieceOnPosition(row, column) == null) {
						QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);
	
						copyBoard.insertPieceOnBoard(row, column, pieceID);
						if (copyBoard.checkRow(row) || copyBoard.checkColumn(column) || copyBoard.checkDiagonals()) {
							return row + "," + column;
						} else if (goDeep) {
							int secondMove = secondMoveSelectionAlgorithm(copyBoard, 2);
							if (secondMove < optionDepth) {
								optionDepth = secondMove;
								option = row + "," + column;
							}
						}
					}
				}
			}
	
			if (option != null) {
				return option;
			}
			
			int[] move = new int[2];
			QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);
			move = copyBoard.chooseRandomPositionNotPlayed(100);
	
			return move[0] + "," + move[1];
		}
		
		private int secondMoveSelectionAlgorithm(QuartoBoard gameBoard, int maxDepth) {
			return secondMoveSelectionAlgorithm(gameBoard, maxDepth, 1);
		}
		
		private int secondMoveSelectionAlgorithm(QuartoBoard gameBoard, int maxDepth, int depth) {
			int next = 100;
			
			for (int row = 0; row < gameBoard.getNumberOfRows(); row++) {
				for (int column = 0; column < gameBoard.getNumberOfColumns(); column++) {
					if (gameBoard.getPieceOnPosition(row, column) == null) {
						QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);
	
						// opponent's turn
						int opPiece = Integer.parseInt(pieceSelectionAlgorithm(copyBoard), 2);
						String[] move = moveSelectionAlgorithm(opPiece, false).split(",");
						copyBoard.insertPieceOnBoard(Integer.parseInt(move[0]), Integer.parseInt(move[1]), opPiece);
	 					if (copyBoard.checkRow(row) || copyBoard.checkColumn(column) || copyBoard.checkDiagonals()) {
							return 100;
						}
						
						// our turn
						copyBoard.insertPieceOnBoard(row, column, Integer.parseInt(pieceSelectionAlgorithm(copyBoard), 2));
						if (copyBoard.checkRow(row) || copyBoard.checkColumn(column) || copyBoard.checkDiagonals()) {
							return depth;
						} else if (depth < maxDepth) {
							int result = secondMoveSelectionAlgorithm(copyBoard, maxDepth, ++depth);
							if (result < next) {
								next = result;
							}
						}	
					}
				}
			}
			
			return next;
		}
	
	
		//loop through board and see if the game is in a won state
		private boolean checkIfGameIsWon() {
	
			//loop through rows
			for (int i = 0; i < NUMBER_OF_ROWS; i++) {
				if (this.quartoBoard.checkRow(i)) {
					System.out.println("Win via row: " + (i) + " (zero-indexed)");
					return true;
				}
	
			}
			//loop through columns
			for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
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
	
	}
