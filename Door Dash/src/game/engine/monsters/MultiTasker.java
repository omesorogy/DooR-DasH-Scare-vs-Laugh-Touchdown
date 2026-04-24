package game.engine.monsters;

import game.engine.Constants;
import game.engine.Role;

public class MultiTasker extends Monster {
	private int normalSpeedTurns;
	
	public MultiTasker(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
		this.normalSpeedTurns = 0;
	}

	public int getNormalSpeedTurns() {
		return normalSpeedTurns;
	}

	public void setNormalSpeedTurns(int normalSpeedTurns) {
		this.normalSpeedTurns = normalSpeedTurns;
	}
	
	public void executePowerupEffect(Monster opponentMonster) {
		this.normalSpeedTurns = 2 ;
	}
	public void move(int distance) {
		if (normalSpeedTurns > 0) {
			super.move(distance);
			normalSpeedTurns--;
		} 
		else {
			super.move(distance / 2);
		}
    }
	public int modifyIncomingEnergy(int energy) {
        return energy + Constants.MULTITASKER_BONUS;
    }
}