package game.engine.dataloader;

import java.util.ArrayList;
import game.engine.Role;
import game.engine.cells.*;
import game.engine.cards.*;
import game.engine.exceptions.*;
import game.engine.monsters.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DataLoader {
	
	public static final String CARDS_FILE_NAME = "cards.csv";
	public static final String CELLS_FILE_NAME = "cells.csv";
	public static final String MONSTERS_FILE_NAME = "monsters.csv";
	
	 public static ArrayList<Card> readCards() throws IOException{
		 ArrayList<Card> cards = new ArrayList<>();
		 BufferedReader reader = new BufferedReader(new FileReader(CARDS_FILE_NAME));
		 String line = reader.readLine();
		 while (line!= null){
			 String[] data = line.split(",");
			 String name = data[1];
			 String description = data[2];
			 int rarity = Integer.parseInt(data[3]);
			 switch(data[0]){
			 	case "SwapperCard": 
			 		cards.add(new SwapperCard(name,description,rarity));
			 		break;
			 	case "ShieldCard": 
			 		cards.add(new ShieldCard(name,description,rarity));
			 		break;
			 	case "EnergyStealCard":
			 		int energy = Integer.parseInt(data[4]);
			 		cards.add(new EnergyStealCard(name,description,rarity,energy));
			 		break;
			 	case "StartOverCard":
			 		boolean lucky = Boolean.parseBoolean(data[4]);
			 		cards.add(new StartOverCard(name,description,rarity,lucky));
			 		break;
			 	case "ConfusionCard":
			 		int duration = Integer.parseInt(data[4]);
			 		cards.add(new ConfusionCard(name,description,rarity,duration));
			 		break;
			 }
			 line = reader.readLine();
		 }
		 reader.close();
		 return cards;
	 }
	 
	 private static Role loadRole(String role){
		 if(role.equals("SCARER"))
			 return Role.SCARER;
		 else
			 return Role.LAUGHER;
	 }
	 
	 public static ArrayList<Cell> readCells() throws IOException{
		 ArrayList<Cell> cells = new ArrayList<>();
		 BufferedReader reader = new BufferedReader(new FileReader(CELLS_FILE_NAME));
		 String line = reader.readLine();
		 while (line!= null){
			 String[] data = line.split(",");
			 String name = data[0];
			 switch(data.length){
			 	case 3: 
			 		String role = data[1];
			 		int energy = Integer.parseInt(data[2]);
			 		cells.add(new DoorCell(name,loadRole(role),energy));
			 		break;
			 	case 2: 
			 		int effect = Integer.parseInt(data[1]);
			 		if(effect<0)
			 			cells.add(new ContaminationSock(name,effect));
			 		else
			 			cells.add(new ConveyorBelt(name,effect));
			 		break;
			 	
			 }
			 line = reader.readLine();
		 }
		 reader.close();
		 return cells;
	 }
	
	 public static ArrayList<Monster> readMonsters() throws IOException{
		 ArrayList<Monster> monsters = new ArrayList<>();
		 BufferedReader reader = new BufferedReader(new FileReader(MONSTERS_FILE_NAME));
		 String line = reader.readLine();
		 while (line!= null){
			 String[] data = line.split(",");
			 String name = data[1];
			 String description = data[2];
			 String role = data[3];
			 int energy = Integer.parseInt(data[4]);
			 switch(data[0]){
			 	case "Dasher": 
			 		monsters.add(new Dasher(name,description,loadRole(role),energy));
			 		break;
			 	case "Dynamo": 
			 		monsters.add(new Dynamo(name,description,loadRole(role),energy));
			 		break;
			 	case "MultiTasker":
			 		monsters.add(new MultiTasker(name,description,loadRole(role),energy));
			 		break;
			 	case "Schemer":
			 		monsters.add(new Schemer(name,description,loadRole(role),energy));
			 		break;
			 }
			 line = reader.readLine();
		 }
		 reader.close();
		 return monsters;
	 }
}
