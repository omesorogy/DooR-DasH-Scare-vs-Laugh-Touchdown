package game.engine.cells;
import game.engine.monsters.*;
public class ConveyorBelt extends TransportCell {
	
	public ConveyorBelt(String name,int effect){
		super(name,(effect>0)?effect:0);
	}
}
