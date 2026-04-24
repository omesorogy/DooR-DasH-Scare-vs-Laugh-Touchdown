package game.engine.cells;
import game.engine.Constants;
import game.engine.monsters.*;
public class ContaminationSock extends TransportCell{
	
	public ContaminationSock(String name,int effect){
		super(name,(effect<0)?effect:0);
	}
	public void transport(Monster monster){
		super.transport(monster);
		monster.alterEnergy(-1*Constants.SLIP_PENALTY);
	}
}
