package game.engine.cells;
abstract public class TransportCell extends Cell{
	
	private int effect; 
	
	public TransportCell(String name,int effect){
		super(name);
		this.effect = effect;
	}
	
	public int getEffect(){
		return this.effect;
	}
	
}
