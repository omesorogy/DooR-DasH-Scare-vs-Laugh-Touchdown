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
	    if(this.activated)
	        return;

	    Role landingMonsterRole = landingMonster.getRole();
	    ArrayList<Monster> stationedMonsters = Board.getStationedMonsters();

	    if(landingMonsterRole == this.role || !landingMonster.isShielded()){
		    int temp = landingMonster.getEnergy();
		    this.modifyCanisterEnergy(landingMonster, this.energy);
		    if(temp != landingMonster.getEnergy())
		        this.activated = true;
	
		    for(int i = 0; i < stationedMonsters.size(); i++){
		        Monster current = stationedMonsters.get(i);
		        if(current.getRole() == landingMonsterRole){
		            temp = current.getEnergy();
		            this.modifyCanisterEnergy(current, this.energy);
		            if(temp != current.getEnergy())
		                this.activated = true;
		        }
		    }
	    }
	    else{
	    	this.modifyCanisterEnergy(landingMonster, this.energy);
	    }
	}
}
