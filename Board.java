package game.engine;
import game.engine.cells.*;
import game.engine.monsters.*;
import game.engine.cards.*;
import java.util.ArrayList;

public class Board {
	
	private Cell[][] boardCells;
	private static ArrayList<Monster> stationedMonsters;
	private static ArrayList<Card> originalCards;
	private static ArrayList<Card> cards;
	
	public Board(ArrayList<Card> readCards){
		boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];
		originalCards = new ArrayList<>();
		stationedMonsters = new ArrayList<>();
		originalCards = readCards;
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
	
}
