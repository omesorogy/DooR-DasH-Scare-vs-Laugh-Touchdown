package game.engine;
import java.util.ArrayList;
import game.engine.monsters.*;
import game.engine.dataloader.*;
import java.io.IOException;
import java.util.Random;
import game.engine.exceptions.*;
public class Game {
	private Board board;
	private ArrayList<Monster> allMonsters; //remaining monsters after removing player and opponent
	private Monster player;
	private Monster opponent;
	private Monster current;
	private int lastDiceRoll = 0;
	
	public int getLastDiceRoll() { return lastDiceRoll; }
	public game.engine.cards.Card getLastDrawnCard() { return Board.getLastDrawnCard(); }
	public void resetLastDrawnCard() { Board.resetLastDrawnCard(); }
	
	public Game(Role playerRole) throws IOException{
		
	    this.board = new Board(DataLoader.readCards());  
	    this.allMonsters = DataLoader.readMonsters();
	    this.player = selectRandomMonsterByRole(playerRole);
	    Role opponentRole = (playerRole == Role.SCARER) ? Role.LAUGHER : Role.SCARER;
	    this.opponent = selectRandomMonsterByRole(opponentRole);
	    this.current = player;
	    //ArrayList<Monster> stationedMonsters=new ArrayList<Monster>(allMonsters);
	    //The test checks that that player and opponent are removed from the allMonsters also 
	    allMonsters.remove(player);
	    allMonsters.remove(opponent);
	    Board.setStationedMonsters(allMonsters);
	    board.initializeBoard(DataLoader.readCells());
	    
	}
	public Monster getCurrent() {
		return current;
	}
	public void setCurrent(Monster current) {
		this.current = current;
	}
	public Board getBoard() {
		return board;
	}
	public ArrayList<Monster> getAllMonsters() {
		return allMonsters;
	}
	public Monster getPlayer() {
		return player;
	}
	public Monster getOpponent() {
		return opponent;
	}
	private Monster selectRandomMonsterByRole(Role role) {

	    ArrayList<Monster> filtered = new ArrayList<>();

	    for (Monster m : allMonsters) {
	        if (m.getRole() == role) {
	            filtered.add(m);
	        }
	    }

	    Random rand = new Random();
	    if(filtered.isEmpty())
	    	return null;
	    return filtered.get(rand.nextInt(filtered.size()));
	}
	private Monster getCurrentOpponent(){
		//Checks if the current player is the player himself. If so, return the opponent. Otherwise return the player
		//The method returns the monster that does not have the current turn
		if(current==player)
			return opponent;
		return player;
	}
	private int rollDice(){
		int dice=(int)(Math.random()*6)+1;
		//First, a random value from 0-0.9999.. is generated, which is then multiplied by 6 and typecasted to an integer
		//New range: 0-5. Add 1, and the range is 1-6 inclusive.
		return dice;
	}
	public void usePowerup() throws OutOfEnergyException {
		//First, checks if the energy of the current player is enough for the power-up
		//If it is not enough, an OutofEnergyException() is thrown
		//Otherwise, the power-up is executed on the opponent and the energy decreases
		int currentEnergy=current.getEnergy();
		if(currentEnergy<Constants.POWERUP_COST){
			throw new OutOfEnergyException();
		}
		current.setEnergy(currentEnergy-Constants.POWERUP_COST);
		current.executePowerupEffect(getCurrentOpponent());
		
	}
	public void playTurn() throws InvalidMoveException{
		//Checks if current player's turn is frozen. If so, turn is switched, otherwise the monster's turn is played.
		if(current.isFrozen()){
			current.setFrozen(false);
			switchTurn();
			return;
		}
		lastDiceRoll = rollDice();
		board.moveMonster(current, lastDiceRoll, getCurrentOpponent());
		switchTurn();
		
	}
	private void switchTurn(){
		current=getCurrentOpponent();
	}
	private boolean checkWinCondition(Monster monster){
		//checks if monster is on the winning position and has enough energy to win the game
		int energy=monster.getEnergy();
		int position=monster.getPosition();
		return position==Constants.WINNING_POSITION&&energy>=Constants.WINNING_ENERGY;
	}
	public Monster getWinner(){
		//Checks if either the current monster or the opponent monster have won the game 
		//null is returned if neither meet the conditions to win the game
		Monster winner=null;
		if(checkWinCondition(current))
			winner=current;
		else if(checkWinCondition(getCurrentOpponent()))
			winner=getCurrentOpponent();
		return winner;
	}
}
