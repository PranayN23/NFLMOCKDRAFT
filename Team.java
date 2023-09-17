/*
This class represents each team in the NFL
It contains information about each team, like their name, 
positions of need, picks and much more
*/

import java.util.*;

// We implement comparable in order to use Collections.sort()
public class Team implements Comparable<Team> {
  // Below are our instance fields
  private String name;
  private ArrayList<String> positionsOfNeed;
  private ArrayList<Pick> picks;
  private ArrayList<Player> playersTaken;
  private TreeMap<Pick, Player> playersTakenWithPick = new TreeMap<>();
  private boolean user;
  
  // This is our team constructor where we make each team
  // We take in the team's name, positions of need, the picks that they have in the coming draft
  // the players they have already taken, a linkedHashMap of the picks and the players taken with said picks
  // and a boolean value representing whether or not the team is a user team
  public Team(String name, ArrayList<String> positionsOfNeed, ArrayList<Pick> picks, ArrayList<Player> playersTaken,
      TreeMap<Pick, Player> playersTakenWithPick, boolean user) {
    this.name = name;
    this.positionsOfNeed = positionsOfNeed;
    this.picks = picks;
    this.playersTaken = playersTaken;
    this.playersTakenWithPick = playersTakenWithPick;
    this.user = user;
  }

  // We compare teams based on their names in alphabetical order
  public int compareTo(Team other) {
    return name.compareTo(other.getName());
  }

  // Here we output each team in the way we want to
  public String toString() {
    // We first make sure we are only outputting usable picks
    ArrayList<Pick> usablePicks = new ArrayList<Pick>();
    for (int index = 0; index < picks.size(); index++) {
      Pick p = picks.get(index);
      if (p.getUsable()) {
        usablePicks.add(p);
      }
    }
    // We then output the team, their positions of need, and players taken
    return String.format("%s Positions of Need %s Picks left %s Players Taken %s", name, positionsOfNeed, usablePicks,
        playersTaken);
  }

  // Below are getters and setters for the instance fields
  public void setPlayersTakenWithPick(TreeMap playersTakenWithPick) {
    this.playersTakenWithPick = playersTakenWithPick;
  }

  public TreeMap<Pick, Player> getPlayersTakenWithPick() {
    return playersTakenWithPick;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setUser(boolean user) {
    this.user = user;
  }

  public void setPositionOfNeed(ArrayList<String> positionsOfNeed) {
    this.positionsOfNeed = positionsOfNeed;
  }

  public void setPicks(ArrayList<Pick> picks) {
    this.picks = picks;
  }

  public void setPlayersTaken(ArrayList<Player> playersTaken) {
    this.playersTaken = playersTaken;
  }

  public String getName() {
    return name;
  }

  public boolean getUser() {
    return user;
  }

  public ArrayList<String> getPositionsOfNeed() {
    return positionsOfNeed;
  }

  public ArrayList<Pick> getPicks() {
    return picks;
  }

  public ArrayList<Player> getPlayersTaken() {
    return playersTaken;
  }
}