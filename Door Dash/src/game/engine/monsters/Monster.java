package game.engine.monsters;

import game.engine.Constants;
import game.engine.Role ; // need sorogys enum

public abstract class Monster implements Comparable<Monster> {
	private final String name ;
	private final String description ;
	private Role role ;
	private final Role originalRole;
	private int energy;
	private int position;
	private boolean frozen;
	private boolean shielded ;
	private int confusionTurns;
	
	public Monster(String name , String description , Role originalRole , int energy) {
		this.name = name ;
		this.description = description ;
		this.originalRole = originalRole ;
		this.role = originalRole ; 
		this.energy = Math.max(0 , energy) ; // so energy never becomes negative 
		this.position = 0 ;
		this.frozen = false ;
		this.shielded = false ;
		this.confusionTurns = 0 ;
	}
	@Override
	public int compareTo(Monster o) {
		return this.position - o.position ; 
	}
	
	
	
	public String getName() {
		return name ;
	}
	public String getDescription() {
		return description ; 
	}
	public Role getRole() {
		return role ;
	}
	public Role getOriginalRole() {
		return originalRole ;
	}
	public int getEnergy() {
		return energy ;
	}
	public int getPosition() {
		return position ; 
	}
	public boolean isFrozen() {
		return frozen ;
	}
	public boolean isShielded() {
		return shielded ;
	}
	public int getConfusionTurns() {
		return confusionTurns ;
	}
	
	
	
	public void setRole(Role role) {
		this.role = role ;
	}
	public void setEnergy(int energy) {
		this.energy = Math.max(0,energy) ;
	}
	public void setPosition(int position) {
		this.position = position%Constants.BOARD_SIZE;
	}
	public void setFrozen(boolean frozen) {
		this.frozen = frozen ;
	}
	public void setShielded(boolean shielded) {
		this.shielded = shielded ;
	}
	public void setConfusionTurns(int confusionTurns) {
		this.confusionTurns = Math.max(0,  confusionTurns) ;
	}
}
