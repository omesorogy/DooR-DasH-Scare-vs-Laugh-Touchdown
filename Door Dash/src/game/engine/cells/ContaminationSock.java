package game.engine.cells;
import game.engine.Constants;
import game.engine.interfaces.*;
import game.engine.monsters.*;

public class ContaminationSock extends TransportCell implements CanisterModifier{
	
	public ContaminationSock(String name,int effect){
		super(name,(effect<0)?effect:0);
	}
	public void transport(Monster monster){
		super.transport(monster);
		this.modifyCanisterEnergy(monster, -1*Constants.SLIP_PENALTY);
	}
	public void modifyCanisterEnergy(Monster monster, int canisterValue){
		monster.alterEnergy(canisterValue);
	}
}
