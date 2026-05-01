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
		if (monster.getRole()==this.role)
			monster.alterEnergy(canisterValue);
		else
			monster.alterEnergy(-1*canisterValue);
	}
	public void onLand(Monster landingMonster, Monster opponentMonster){
	    super.onLand(landingMonster, opponentMonster);
	    //If the door is already activated, do nothing
	    if(this.activated)
	        return;
	    
	    Role landingMonsterRole = landingMonster.getRole(); //Store the role of the landing monster
	    ArrayList<Monster> stationedMonsters = Board.getStationedMonsters();
	    //The energy is only modified either if the landing monster has the same role as the cell
	    // or the landing monster is not shielded 
	    if(landingMonsterRole == this.role || !landingMonster.isShielded()){
		    this.modifyCanisterEnergy(landingMonster, this.energy);
		    //Changes the energy of all stationed monsters with the same role as the landing one
		    for(int i = 0; i < stationedMonsters.size(); i++){
		        Monster current = stationedMonsters.get(i);
		        if(current.getRole() == landingMonsterRole){
		            this.modifyCanisterEnergy(current, this.energy);
		        }
		    }
		    //Since the energy of the landing monster is changed then the cell is set to be activated
		    this.activated = true;
	    }
	    //Otherwise only break the shield of the landing monster
	    else{
	    	this.modifyCanisterEnergy(landingMonster, this.energy);
	    }
	}
}
