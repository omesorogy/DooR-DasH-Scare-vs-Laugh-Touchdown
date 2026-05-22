package game.engine.cards;
import game.engine.interfaces.*;
import game.engine.monsters.*;
public class EnergyStealCard extends Card implements CanisterModifier{
	private int energy;
	public EnergyStealCard(String name, String description, int rarity, int energy){
		super(name,description,rarity,true);
		this.energy=energy;
	}
	public int getEnergy() {
		return energy;
	}
	public void performAction(Monster player, Monster opponent){
		//Steals the energy amount set by the card from the opponent unless shielded
		//Player gains the stolen amount
		if(opponent.isShielded()){
			opponent.setShielded(false);
			return;
		}
		int stolenAmount=(energy>opponent.getEnergy())?opponent.getEnergy():energy;
		modifyCanisterEnergy(player, stolenAmount);
		modifyCanisterEnergy(opponent,-1*stolenAmount);
		//-1 refers to the stolenAmount being subtracted as opposed to being added
		
		
	}
	public void modifyCanisterEnergy(Monster monster,int canisterValue){
		//Implements the modifyCanisterEnergy abstract method in the interface
		//Adds the canisterValue to the current energy of the monster
		monster.alterEnergy(canisterValue);
	}
	
}
