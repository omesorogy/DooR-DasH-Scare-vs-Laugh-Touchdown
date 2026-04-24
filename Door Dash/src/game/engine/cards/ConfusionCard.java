package game.engine.cards;
import game.engine.monsters.*;
import game.engine.Role;
public class ConfusionCard extends Card{
	private int duration;
	public ConfusionCard(String name, String description, int rarity, int duration){
		super(name,description,rarity,false);
		this.duration=duration;
	}
	public int getDuration() {
		return duration;
	}
	public void performAction(Monster player, Monster opponent){
		//Sets confusionTurns of both monsters to the duration
		//Swaps roles of both monsters
		player.setConfusionTurns(duration);
		opponent.setConfusionTurns(duration);
		Role playerRole=player.getRole();
		player.setRole(opponent.getRole());//Sets player's role as the opponent's role
		opponent.setRole(playerRole);//Sets opponent's role as the player's role
	}
	
}
