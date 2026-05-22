package game.engine;
import game.engine.cells.*;
import game.engine.monsters.*;
import game.engine.cards.*;
import java.util.ArrayList;
import java.util.Collections;
import game.engine.exceptions.*;
public class Board {
	
	private Cell[][] boardCells;
	private static ArrayList<Monster> stationedMonsters;
	private static ArrayList<Card> originalCards;
	public static ArrayList<Card> cards;
	
	public Board(ArrayList<Card> readCards){
		boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];
		cards = new ArrayList<>();
		stationedMonsters = new ArrayList<>();
		originalCards = readCards;
		this.setCardsByRarity();
		reloadCards();
	}

	public static ArrayList<Monster> getStationedMonsters() {
		return stationedMonsters;
	}

	public static void setStationedMonsters(ArrayList<Monster> stationedMonsters) {
		Board.stationedMonsters = stationedMonsters;
	}

	public static ArrayList<Card> getCards() {
		return cards;
	}

	public static void setCards(ArrayList<Card> cards) {
		Board.cards = cards;
	}

	public Cell[][] getBoardCells() {
		return boardCells;
	}

	public static ArrayList<Card> getOriginalCards() {
		return originalCards;
	}
	
	//Helper Method that transforms an index to its 2-d version 
	private int[] indexToRowCol(int index){
		int row = index/Constants.BOARD_COLS;
		int col;
		if(row%2==0)
			col = index%Constants.BOARD_COLS;
		else
			col = (Constants.BOARD_COLS-1) - index%Constants.BOARD_COLS;
		return new int[]{row,col};
	}
	
	private Cell getCell(int index){
		int[] rowCol = this.indexToRowCol(index);
		return boardCells[rowCol[0]][rowCol[1]];
	}
	
	private void setCell(int index,Cell cell){
		int[] rowCol = this.indexToRowCol(index);
		boardCells[rowCol[0]][rowCol[1]] = cell;
	}
	
	public void initializeBoard(ArrayList<Cell> specialCells){
		int j = 0; //keeps track of the index of the last found door cell
		//Fills the cells with normal cells and Door cells
		for(int i = 0;i<Constants.BOARD_SIZE;i++){
			if(i%2==0)
				this.setCell(i,new Cell("Normal Cell " + i));
			else{
				for(int k = j;k<specialCells.size();k++){
					if(specialCells.get(k) instanceof DoorCell){
						this.setCell(i,specialCells.get(k));
						j = k+1;
						break;
					}
				}
			}
		}
		
		//Creating Card Cells and placing them 
		for(int i = 0;i<Constants.CARD_CELL_INDICES.length;i++){
			this.setCell(Constants.CARD_CELL_INDICES[i],
					new CardCell("Card Cell"+Constants.CARD_CELL_INDICES[i] ));
		}
		
		j = 0; // keep tracks of the index of the last conveyor belt
		//Getting the Conveyor Belts and placing them according to the indices
		for(int i = 0;i<Constants.CONVEYOR_CELL_INDICES.length;i++){
			for(int k = j;k<specialCells.size();k++){
				if(specialCells.get(k) instanceof ConveyorBelt){
					this.setCell(Constants.CONVEYOR_CELL_INDICES[i],specialCells.get(k));
					j = k+1;
					break;
				}
			}
		}
		
		j = 0; // keep tracks of the index of the last Contamination Sock
		//Getting the Contamination Socks and placing them according to the indices
		for(int i = 0;i<Constants.SOCK_CELL_INDICES.length;i++){
			for(int k = j;k<specialCells.size();k++){
				if(specialCells.get(k) instanceof ContaminationSock){
					this.setCell(Constants.SOCK_CELL_INDICES[i],specialCells.get(k));
					j = k+1;
					break;
				}
			}
		}
		
		//Creating Monster Cells from the stationedMonsters and placing them 
		for(int i = 0;i<stationedMonsters.size();i++){
			this.setCell(Constants.MONSTER_CELL_INDICES[i],
							new MonsterCell(stationedMonsters.get(i).getName(),
								stationedMonsters.get(i)));
			stationedMonsters.get(i).setPosition(Constants.MONSTER_CELL_INDICES[i]);
		}	
	}
	
	//Repeat each card in the originalCards n times according to its rarity
	private void setCardsByRarity(){
		ArrayList<Card> expandedList = new ArrayList<>();
		for(int i = 0;i<originalCards.size();i++){
			Card current = originalCards.get(i); 
			for(int j = 0;j<current.getRarity();j++){
				expandedList.add(current);
			}
		}
		originalCards = expandedList;
	}
	
	//Resets the cards deck
	public static void reloadCards(){
		cards.clear();
		for(int i = 0;i<originalCards.size();i++){
			cards.add(originalCards.get(i));
		}
		Collections.shuffle(cards);
	}
	
	//Gets and removes the first card in the deck
	public static Card drawCard(){
		if(cards.size()==0){
			reloadCards();
		}
		Card drawn = cards.remove(0);
		lastDrawnCard = drawn;
		return drawn;
	}

	public static void resetLastDrawnCard() { lastDrawnCard = null; }
	private static Card lastDrawnCard = null;
	public static Card getLastDrawnCard() { return lastDrawnCard; }
	
	//Makes a valid move for the current monster
	public void moveMonster(Monster currentMonster, 
			int roll, Monster opponentMonster) throws InvalidMoveException
	{
		//Save the current position in case of collision
		int currentPreviousPosition = currentMonster.getPosition(); 
		boolean wasConfused=currentMonster.isConfused();//new addition
		currentMonster.move(roll);
		this.getCell(currentMonster.getPosition()).onLand(currentMonster, opponentMonster);
		if(currentMonster.getPosition()==opponentMonster.getPosition()){
			currentMonster.setPosition(currentPreviousPosition);
			this.updateMonsterPositions(currentMonster,opponentMonster);
			throw new InvalidMoveException();
		}
		else{
			if(wasConfused){
				currentMonster.decrementConfusion();
				opponentMonster.decrementConfusion();
			}
			this.updateMonsterPositions(currentMonster,opponentMonster);
		}
	}
	
	//Synchronises the board with the new positions
	private void updateMonsterPositions(Monster player,Monster opponent){
		for(int i=0; i<Constants.BOARD_SIZE;i++){
			this.getCell(i).setMonster(null);
		}
		this.getCell(player.getPosition()).setMonster(player);
		this.getCell(opponent.getPosition()).setMonster(opponent);
	}	
}
