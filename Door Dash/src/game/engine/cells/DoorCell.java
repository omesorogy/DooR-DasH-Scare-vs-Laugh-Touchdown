package game.engine.cells;
import game.engine.interfaces.*;
import game.engine.*;
import game.engine.monsters.*;
import java.util.ArrayList;
public class DoorCell extends Cell implements CanisterModifier{
	
	private Role role;
	private int energy;
	private boolean activated;
	
	public DoorCell(String name,Role role,int energy){
		super(name);
		this.role = role;
		this.energy = energy;
		this.activated = false;
	}

	public boolean isActivated() {
		return activated;
	}
	public void setActivated(boolean activated) {
		this.activated = activated;
	}
	public Role getRole() {
		return role;
	}
	public int getEnergy() {
		return energy;
	}
	public void modifyCanisterEnergy(Monster monster,int canisterValue){
		monster.alterEnergy(canisterValue);
	}
	public void onLand(Monster landingMonster,Monster opponentMonster){
		super.onLand(landingMonster, opponentMonster); //Setting attributes of the cell
		if(this.activated) //If the cell is already activated, do nothing
			return;
		int changeInEnergy; //Stores the change in energy that will be applied to the monsters
		Role landingMonsterRole = landingMonster.getRole(); // stores the role of the landingMonster
		//Decides the sign of the change according to the role
		if(this.role == landingMonsterRole)
			changeInEnergy = this.energy; //change is positive
		else
			changeInEnergy = -1*this.energy; //change is negative
		ArrayList<Monster> stationedMonsters = Board.getStationedMonsters();
		int temp = landingMonster.getEnergy(); //Stores the energy of the monster before changing
		this.modifyCanisterEnergy(landingMonster,changeInEnergy);
		//if the energy of the monster is changed, the cell is activated
		if(temp!=landingMonster.getEnergy())
			this.activated = true;
		//Loops on all stationed monsters with the same role as the landing monster and do the same
		for(int i =0;i<stationedMonsters.size();i++){
			Monster current = stationedMonsters.get(i); // stores the current monster in the array
			if(current.getRole()==landingMonsterRole){
				temp = current.getEnergy();
				this.modifyCanisterEnergy(current,changeInEnergy);
				if(temp!=current.getEnergy())
					this.activated = true;
			}
		}
	}
}
