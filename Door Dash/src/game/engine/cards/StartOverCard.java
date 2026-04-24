package game.engine.cards;
import game.engine.monsters.*;
public class StartOverCard extends Card {
	public StartOverCard(String name, String description, int rarity, boolean lucky){
		super(name,description,rarity,lucky);
	}
	public void performAction(Monster player, Monster opponent){
		//If the card is lucky, the opponent starts over, otherwise; the player starts over
		if(isLucky())
			opponent.setPosition(0);
		else
			player.setPosition(0);
	}
}
