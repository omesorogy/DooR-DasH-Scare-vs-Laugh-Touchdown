package game.engine.cells;
import game.engine.monsters.*;
public class MonsterCell extends Cell{
	
	private Monster cellMonster;
	
	public MonsterCell(String name,Monster cellMonster){
		super(name);
		this.cellMonster = cellMonster;
	}
	
	public Monster getCellMonster(){
		return this.cellMonster;
	}
	public void onLand(Monster landingMonster,Monster opponentMonster){
		super.onLand(landingMonster, opponentMonster); // Sets the cell's attributes
		//if the landing monster has the same role as the cell monster then a free powerup
		if(landingMonster.getRole()==cellMonster.getRole()){
			landingMonster.executePowerupEffect(opponentMonster);
		}	
		//if the landing monster has more energy
		else if(landingMonster.getEnergy()>cellMonster.getEnergy()){
			//if the landing monster is shielded, 
			//cell monster will have the same energy as the landing monster 
			// and the shield of the landing monster is consumed
			if (landingMonster.isShielded()){
				cellMonster.setEnergy(landingMonster.getEnergy());
				landingMonster.setShielded(false);
			}
			//if not then we swap their energies
			else{
				int temp = cellMonster.getEnergy();
				cellMonster.setEnergy(landingMonster.getEnergy());
				landingMonster.setEnergy(temp);	
			}
		}
	}
}
