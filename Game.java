package game.engine;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Random;
public class Game {
	private Board board;
	private ArrayList<Monster> allMonsters;
	private Monster player;
	private Monster opponent;
	private Monster currrent;
	public Game(Role rolePlayer) throws IOException{
		/*Important notice:
		 * At the time of writing this code, Monster and Board classes have not yet been produced
		 * This constructor should be revised accordingly after these classes have been made.
		 */
		// (a) create the board with loaded cards
	    this.board = new Board();  
	    // (b) load all monsters from CSV
	    DataLoader data=new DataLoader();
	    this.allMonsters = data.readMonsters();

	    // (c) randomly select player monster
	    this.player = selectRandomMonsterByRole(playerRole);

	    // (d) select opponent monster (opposite role)
	    Role opponentRole = (playerRole == Role.SCARER) ? Role.LAUGHER : Role.SCARER;
	    this.opponent = selectRandomMonsterByRole(opponentRole);

	    // (e) set current player
	    this.current = player;
	}
	public Monster getCurrrent() {
		return currrent;
	}
	public void setCurrrent(Monster currrent) {
		this.currrent = currrent;
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
	    return filtered.get(rand.nextInt(filtered.size()));
	}
	
}
