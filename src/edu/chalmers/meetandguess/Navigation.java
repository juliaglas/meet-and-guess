package edu.chalmers.meetandguess;

import java.util.ArrayList;



public class Navigation {
	
	private ArrayList<String> navList;
	
	public Navigation()
	{
		navList = new ArrayList<String>();
		navList.add("View Profile");
		navList.add("View Players");
		navList.add("Create New Game");
		navList.add("Join Game");
		
	}
	
	public ArrayList<String> getNavList()
	{
		return navList;
	}

}
