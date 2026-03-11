package game.engine;
import java.util.ArrayList;
import game.engine.monsters.*;
import game.engine.dataloader.*;
import java.io.IOException;
import java.util.Random;
public class Game {
	private Board board;
	private ArrayList<Monster> allMonsters;
	private Monster player;
	private Monster opponent;
	private Monster current;
	
	public Game(Role playerRole) throws IOException{
		
	    this.board = new Board(DataLoader.readCards());  
	    this.allMonsters = DataLoader.readMonsters();
	    this.player = selectRandomMonsterByRole(playerRole);
	    Role opponentRole = (playerRole == Role.SCARER) ? Role.LAUGHER : Role.SCARER;
	    this.opponent = selectRandomMonsterByRole(opponentRole);
	    this.current = player;
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

	    for (int i = 0;i<allMonsters.size();i++){
	    	Monster m = allMonsters.get(i);
	        if (m.getRole() == role) {
	            filtered.add(m);
	        }
	    }

	    Random rand = new Random();
	    return filtered.get(rand.nextInt(filtered.size()));
	}
	
}
