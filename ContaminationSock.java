package game.engine.cells;
import game.engine.monsters.*;
public class ContaminationSock extends TransportCell{
	
	public ContaminationSock(String name,int effect){
		super(name,(effect<0)?effect:0);
	}
}
