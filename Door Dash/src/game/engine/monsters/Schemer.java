package game.engine.monsters;
import java.util.ArrayList;

import game.engine.Constants; 
import game.engine.Role;
import game.engine.Board;

public class Schemer extends Monster {
	
	public Schemer(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
	}
	
	private int stealEnergyFrom(Monster target) {
		int targets_energy = target.getEnergy();
		int amount;
		if (targets_energy >= Constants.SCHEMER_STEAL ) {
			target.setEnergy(targets_energy - Constants.SCHEMER_STEAL);
			amount = Constants.SCHEMER_STEAL ;
		}
		else {
			target.setEnergy(0) ;
			amount = targets_energy;
		}
			
		return amount ;
	}
	public void executePowerupEffect(Monster opponentMonster) {
		ArrayList<Monster> stationedMonsters = Board.getStationedMonsters();
		int totalEnergy = stealEnergyFrom(opponentMonster);
		for (int i = 0; i < stationedMonsters.size(); i++) {
			totalEnergy += stealEnergyFrom(stationedMonsters.get(i));
		}
		alterEnergy(totalEnergy);
	}
	public int modifyIncomingEnergy(int energy) {
        return energy + Constants.SCHEMER_STEAL;
    }
	
}
