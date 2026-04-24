package game.engine.cards;
import game.engine.monsters.*;
public class SwapperCard extends Card {
	public SwapperCard(String name, String description, int rarity){
		super(name,description,rarity,true);
	}
	public void performAction(Monster player, Monster opponent){
		//The method gets the position of the Player and the Opponent and then swaps them together
		int playerPosition=player.getPosition();
		int opponentPosition=opponent.getPosition();
		//Check if the player is behind the opponent; if the player is ahead, no swap occurs
		if(playerPosition>=opponentPosition)
			return;
		player.setPosition(opponentPosition);
		opponent.setPosition(playerPosition);
	}
}
