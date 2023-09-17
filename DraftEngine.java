import java.util.*;
import java.io.*;

// In this class, we actually run the draft simulator
public class DraftEngine {
  // For the draft engine, we have an arraylist of teams, picks, and players
  private ArrayList<Team> teams = new ArrayList<Team>();;
  private ArrayList<Pick> picks = new ArrayList<Pick>();
  private ArrayList<Player> players = new ArrayList<Player>();
  // We have a tree map of each pick's owners and we auto sort it
  // every time we use it by passing in a pick comparator object as a paramater
  private TreeMap<Pick, Team> pickOwners = new TreeMap<>(new PickComparator());
  // We have one input reader
  private Scanner console = new Scanner(System.in);
  // We have our printstream object to get user changes
  private File fileObject = new File("PlayerChanges.txt");
     
  // This method we run the draft
  public void runDraft() throws FileNotFoundException {
    // We "seed" the random number we want to start with 
    // so we do not get the same sequence each time 
    seedRandomNumber();
    // We then set up our draft by getting information from our txt files
    setUpDraft();
    // We then get the user teams they wat to pick for
    getUserTeams();
    int max = getMaxPick();
    // We loop through our number of picks
    for (int index = 0; index < max; index++) {
      // We get the pick and the team which owns that pick
      Pick p = picks.get(index);
      Team current = pickOwners.get(p);
      // We print out the user pick
      System.out.println(p + ". The " + current.getName() + " are on the clock!");
      // If it is a user pick, we handle the User pick
      if (current.getUser()) {
        index = handleUserPick(p, current, index);
      } 
      // else we let the ai pick
      else {
        index = handleAIPick(p, current, index);
      }
    }
    // We handle the end of the draft
    handleEnd();
  }

  // In this method, we "seed" the initial random number
  public void seedRandomNumber() {
    // We do this by continually generating random numbers until our
    // random number is less than or equal to our index, which goes up by 1 each time
    // This ensures we start at a random point in the sequence, making it 
    // so that our draft varies in order for the AI picks
    int r = (int) (Math.random() * 100);
    for (int index = 0; index < r; index++) {
      r = (int) (Math.random() * 100);
    }
  }

  // Here we get the max pick to go to for our mock draft
  public int getMaxPick() {
    // We ask theuser how many rounds they want to pick and try to get
    // an integer out of the next line of input
    System.out.println("How many rounds do you want to pick? ");
    int num = -1;
    try {
      num = Integer.parseInt(console.nextLine());
    } catch (Exception e) {
      // If we get an error, we tell the user that and use recursion
      // to get anotehr guess
      System.out.println("Error, try again");
      getMaxPick();
    }
    // We then set the max pick based on the round number
    if (num == 1) {
      return 32;
    } else if (num == 2) {
      return 64;
    } else if (num == 3) {
      return 105;
    } else if (num == 4) {
      return 143;
    } else if (num == 5) {
      return 179;
    } else if (num == 6) {
      return 221;
    } else if (num == 7) {
      return 262;
    } 
    // If they enter something other than 1-7 rounds, we know there was an
    // error and try again
    else {
      return getMaxPick();
    }
  }

  // In this method, we check if there is at least one user team
  // so we know whether to ask if the user wants to trade up or not
  // becuase te user can not trade if they do not control a team
  public boolean checkIf1Userteam() {
    for (Team t: teams) {
      if (t.getUser()) {
        return true;
      }
    }
    // If we reach this point, no team was a user team so we return false
    return false;
  }

  public int handleAIPick(Pick p, Team current, int i) {
    // If there is at least 1 user team
    if (checkIf1Userteam()) {
      // We prompt the user to trade up and get their answer
      System.out.println("Do you want to trade up?");
      String ans = console.nextLine();
      // We also create a temporary variable to see if a trade did happen
      // later in the method
      Team temp = current;
      boolean done = false;
      // while the user wants to trade
      while (ans.matches("yes|Yes") && !done) {
        // We try to trade and set done to be true
        // if a trade goes through
        done = trade(p, current);
        // We also set the current team to be the one with this pick
        // In case there was a trade
        current = pickOwners.get(p);
        //If we aren't done, we reprompt the user
        if (!done) {
           System.out.println("Do you want to trade up?");
           ans = console.nextLine();
        }
      }
      // If the current team isn't the same team as the team before the trade
      if (!current.getName().equals(temp.getName())) {
        // We return i - 1 so that the for loop in runDraft does not skip
        // the new team
        return i - 1;
      }
    }
    // Now that we have the team that will definitely pick here, we do the pick 
    int index = 0;
    // We have an array list of players called good picks
      // Good picks are players in the top 5 left who play positions of need
      // Or there are players who have fallen from their ranking too far
    ArrayList<Player> goodPick = new ArrayList<Player>();
    // Other picks contains players who are not at positions of need but 
    // are still in the top 5
    ArrayList<Player> otherPicks = new ArrayList<Player>();
    // while there are players left and we have not put 5 players into the 2 lists
    while (index < players.size() && index < 5) {
      // We get the player and the team needs
      Player p2 = players.get(index);
      ArrayList<String> teamNeeds = current.getPositionsOfNeed();
      // if the players plays a position of need and the team has not picked that position yet
      // or if the the player has fallen a lot
      if ((teamNeeds.contains(p2.getPosition()) && positionNotPicked(current, p2.getPosition())) || playerFalling(p2, p)) {
        // We add that player to the good picks category
        goodPick.add(p2);
      } else {
       // We add that player to the bad picks category otherwise
       otherPicks.add(p2);
      }
      // We increase the count
      index++;
    }
    // We then do the AI pick
    doAIPick(goodPick, otherPicks, p, current);
    // We also return the index because the index hasn't changed
    return i;
  }
  
  
  // Here we check if a player if falling too far
  public boolean playerFalling(Player p, Pick current) {
   // Here we get the current round as the defintion of "falling"
   // changes in each round 
   int round = current.getRound();
   // We then get the difference between the current pick number and the player's ranking
   int difference = current.getPickNumber() - p.getRanking();
   // If we are in in round 1 and the difference is greater than 7, the player is falling
   if (round == 1) {
      return (difference > 7);
   }
   // If we are in in round 2 and the difference is greater than 10, the player is falling
   else if (round == 2) {
      return (difference > 10);
   } 
   // If we are in in round 3 and the difference is greater than 15, the player is falling
   else if (round == 3) {
     return (difference > 15);  
   } 
   // If we are in past round 3 and the difference is greater than 20, the player is falling
   else {
     return (difference > 20);
   }
  }

  // In this method, we chec if the team has already picked at that position
  public boolean positionNotPicked(Team current, String position) {
    // We get the players they have picked
    ArrayList<Player> playersPicked = current.getPlayersTaken();
    // for each player
    for (Player p: playersPicked) {
      // If there position matches the position we might draft
      if (p.getPosition().equalsIgnoreCase(position)) {
        // We return false, they have picked at that position
        return false;
      }
    }
    // if we reach here, we know they haven't picked there yet
    return true;
  }


  // In this method we do the AI Pick
  public void doAIPick(ArrayList<Player> goodPick, ArrayList<Player> bpaPicks, Pick p, Team current) {
    int random = (int) (Math.random() * 100) + 1;
    // if goodPick has players, there is a 99% chance that we pick from that list
    if (random <= 99 && goodPick.size() != 0) {  
      pickWeightedRandomPlayer(goodPick, current, p);
    } 
    // If not, we decide between best players available (top 5 left)
    // and the top 5 players at positions of need (50% each way)
    else {
      // We get the players at postions of need
      ArrayList<Player> posOfNeed = getPositionsOfNeed(current);
      int r = (int) (Math.random() * 2) + 1;
      if (r == 1) {
        pickWeightedRandomPlayer(bpaPicks, current, p);
      } else {
         pickWeightedRandomPlayer(posOfNeed, current, p);
      }
    }
  }

   // Here we randomly pick a player from the list
   public void pickWeightedRandomPlayer(ArrayList<Player> list, Team current, Pick p) {
    // We generate a randomn number between 1 and 100
    int r = (int) (Math.random() * 100) + 1;
    Player choice;
    // Based on the list size, we assign probabilities to each element in the
    // list of getting picked
    // The earlier in the list, the higher the probability
    if (list.size() == 5) {
      // 35%
      if (r <= 35) {
        choice = list.get(0);
      } 
      // 25%
      else if (r <= 60) {
        choice = list.get(1);
      }
      // 20%
      else if (r <= 80) {
        choice = list.get(2);
      } 
      // 15%
      else if (r <= 95) {
        choice = list.get(3);
      } 
      // 5% and so on for the rest of the list sizes
      else {
        choice = list.get(4);
      }
    } else if (list.size() == 4) {
      if (r <= 40) /* 40% */{
        choice = list.get(0);
      } else if (r <= 70) /* 30% */{
        choice = list.get(1);
      } else if (r <= 90) /* 20% */{
        choice = list.get(2);
      } else /* 10% */{
        choice = list.get(3);
      }
    } else if (list.size() == 3) {
      if (r <= 50) /* 50% */{
        choice = list.get(0);
      } else if (r <= 80) /* 30% */{
        choice = list.get(1);
      } else /* 20% */{
        choice = list.get(2);
      }
    } else if (list.size() == 2) {
      if (r <= 75) /* 75% */{
        choice = list.get(0);
      } else /* 25% */{
        choice = list.get(1);
      }
    } else /* 100% */{
      choice = list.get(0);
    } 
    // We print out the pick information and than handle the pick
    System.out.println("The " + current.getName() + " took " + choice.getName() + " with Pick Number " +  p.getPickNumber() + " in Round " + p.getRound());
    handlePick(choice.getName(), current, p);
  }

  // Here we get the top 5 players at positions of need
  public ArrayList<Player> getPositionsOfNeed(Team current) {
    ArrayList<Player> posOfNeed = new ArrayList<Player>();
    // for each position
    for (String position: current.getPositionsOfNeed()) {
        int i = 0;
        int c = 0;
        // while there are players left and we have not added 5 players at this position
        while (i < players.size() && c < 5) {
          // We get the player
          Player p3 = players.get(i);
          // If the player plays at a positon of need
          if (p3.getPosition().equalsIgnoreCase(position)) {
             // We add them to the arraylist and increase the count
             posOfNeed.add(p3);
             c++;
          }
          // We increase the index
          i++;
        }
    }
    // We sort the players in the list by ranking and shorten the list down
    // to the first 5, if it is longer
    Collections.sort(posOfNeed);
    while (posOfNeed.size() > 5) {
      posOfNeed.remove(5);
    }
    // we then return the list
    return posOfNeed;
  }

  // In this method we handle user picks
  public int handleUserPick(Pick p, Team current, int i) {
    // We print out the current team ans top 10 players left
    System.out.println(current);
    System.out.println("Top 10 Players left");
    printPlayersLeft();
    // We then ask to trade
    return promptToTrade(p, current, i);
  }

  public int promptToTrade(Pick p, Team current, int i) {
    // We prime our while loop
    boolean done = false;
    // We do the same temp variable check as for AI Picks
    Team temp = current;
    System.out.println("Do you want to trade down?");
    String ans = console.nextLine();
    while (ans.matches("yes|Yes") && !done) {
      // We check if the trade worked
      done = trade(p, current);
      current = pickOwners.get(p);
      // If it did not, we ask again until they say they don't want to
      if (!done) {
         System.out.println("Do you want to trade down?");
         ans = console.nextLine();
      }
    }
    // If they do not want to trade, we pick
    if (!ans.matches("yes|Yes")) {
       getPick(current, p);
       return i;
    }
    // If there was a trade, we change the index we return
    if (!current.getName().equals(temp.getName())) {
      return i - 1;
    } 
    // else, we keep the index the same
    else {
      return i;
    }
  } 
  
  
  // Here we process the pick info line for trades
  public double processPickLine(String line, Team current) {
    Scanner parser = new Scanner(line);
    // we get the sum of the pick values
    double sum = 0.0;
    while (parser.hasNext()) {
      int index = -1;
      // We use a try catach to make sure there is not user error
      try {
         index = parser.nextInt();
      } catch (Exception e) {
         System.out.println("There was an error.");
         // We return -1.0 to signal an error
         return -1.0;
      }
      // We get the pick and the owner of the pick and their name
      Pick p = picks.get(index - 1);
      String name = pickOwners.get(p).getName();
      String currentName = current.getName();
      // if the pick owner is the same as the current team, we add to the sum
      if (name.equalsIgnoreCase(currentName)) {
        sum += p.getDraftValue();
      } else {
        // We return -1 because something went wrong
        return -1.0;
      }
    }
    // We return the sum
    return sum;
  }

  // We get the other team to trade with
  public Team getOtherTeam(Team current) {
    System.out.println("What team do you want to trade for this pick?");
    String ans = console.nextLine();
    for (Team t: teams) {
      String name = t.getName();
      // If there are a valid team name and a user team or the current team
      // is a user one, we continue
      if (name.equalsIgnoreCase(ans) && (t.getUser() || current.getUser())) {
        return t;
      }
    }
    // If we got here, error happened
    System.out.println("Non-user or inccorrect team, try again");
    return getOtherTeam(current);
  }
  
  // we do the trade here
  public boolean trade(Pick p, Team current) {
    // we get the other team and than try to do the trade
    Team other = getOtherTeam(current);
    // if the trade was fair and no user error, we return true
    return getTradeInfo(other, current);
  }

  public boolean getTradeInfo(Team other, Team current) {
    // We get the picks given up for each team and determine their value
    System.out.println("What picks will the " + other.getName() + " trade?. Enter the pick number followed by spaces");
    String line = console.nextLine();
    double val = processPickLine(line, other);
    System.out.println("What picks will the " + current.getName() + " trade?. Enter the pick number followed by spaces");
    String line2 = console.nextLine();
    double val2 = processPickLine(line2, current);
    // If it was a fair trade and we had no errors in the previous section,
    // we do the trade
    if (fairTrade(val, val2) && val > 0 && val2 > 0) {
      processTrade(line, line2, other, current);
      return true;
    } 
    // Otherwise, there was an arror
    else {
      System.out.println("Trade did not go through, try again");
      return false;
    }
  }

  // Here we process trades
  // line has info for the other team, (one trading into the current pick)
  // line 2 has the current teams info
  public void processTrade(String line, String line2, Team other, Team current) {
    // We add the picks the non-current team is giving up in an array
    ArrayList<Pick> otherPicks = new ArrayList<Pick>();
    Scanner parser = new Scanner(line);
    while (parser.hasNextInt()) {
      int num = parser.nextInt();
      // We do num -1 cause zero index
      Pick p = picks.get(num - 1);
      otherPicks.add(p);
    }
    // We do the same process for the current teams picks
    ArrayList<Pick> currentPicks = new ArrayList<Pick>();
    Scanner parse = new Scanner(line2);
    while (parse.hasNextInt()) {
      int num = parse.nextInt();
      Pick p = picks.get(num - 1);
      currentPicks.add(p);
    }
    // for each pick the other team is giving up
    for (Pick p : otherPicks) {
      // We change the pick owners to be the current team
      pickOwners.put(p, current);
      // We add these picks to the current team
      current.getPicks().add(p);
      // We remove them from the trading team
      other.getPicks().remove(p);
    }
    // Same process for the current teams picks
    for (Pick p : currentPicks) {
      pickOwners.put(p, other);
      current.getPicks().remove(p);
      other.getPicks().add(p);
    }
    // We then resort the picks to ensure proper ordering
    Collections.sort(current.getPicks());
    Collections.sort(other.getPicks());
  }

  // Here we calculate if a trade was fair
  public boolean fairTrade(double val, double val2) {
    // We calculate "fairness" using a value changed formula
    // (value - other)/value
    // This tells us the percentage change in draft value
    // each team gets in this trade
    // If it is less than 10%, we say the trade is faur
    double margin = 100.0 * Math.abs((val - val2)/val);
    double margin2 = 100.0 * Math.abs((val2 - val)/val2);
    return (margin < 10 && margin2 < 10); 
  }
  
  // Here we get the user teams
  public void getUserTeams() {
    System.out.println("Do you want to pick for all teams");
    String ans = console.nextLine();
    // If they want to pick for all teams, we set user to be true for all of them
    if (ans.matches("yes|Yes")) {
      for (Team t : teams) {
        t.setUser(true);
      }
    } else if (ans.matches("No|no")) {
      // We do it to a fixed number
      setUserTeams();
    } 
    // Else there is a malformed command and we use recursion to try again
    else {
      System.out.println("Error, try again");
      getUserTeams();
    }
  }

  // Here we set up user teams if the user does not want to do for all 32
  public void setUserTeams() {
    System.out.println("How many teams do you want to pick for");
    int numOfTeams = 0;
    try {
      numOfTeams = Integer.parseInt(console.nextLine());
    } catch (Exception e) {
      // There was an error if not a number
      System.out.println("Error. Try again");
      setUserTeams();
    }
    // if they entered more than 32
    if (numOfTeams > 32 || numOfTeams < 0) {
      System.out.println("Error. Try again");
      setUserTeams();
    }
    // We the get the list
    getListOfTeams(numOfTeams);
  }

  // We get the list of teams here
  public void getListOfTeams(int numOfTeams) {
    boolean valid = false;
    for (int index = 0; index < numOfTeams; index++) {
      // We check if the input is valid if it is not, we try again and go back one
      // index so that we still gte the right amound
      valid = getUserInput();
      if (!valid) {
      System.out.println("Error try again. Reenter all teams");
      index--;
      }
    } 
  }
  
  // We then get the teams here
  public boolean getUserInput() {
    boolean valid = false;
    System.out.println("Enter team name ");
     String name = console.nextLine();
      for (Team t: teams) {
        String tName = t.getName();
        // if it is an actual team
        if (name.equalsIgnoreCase(tName)) {
          // We make it a user and return true
          t.setUser(true);
          valid = true;
        }
    }
    // we return the validity of the user team name
    return valid;
  }

  // Here we get the position to draft while making sure it is a vlid one
  public String getPositionInput() {
    System.out.println("Enter the position to draft: ");
    String position = console.nextLine();
    while (!validPosition(position)) {
      System.out.println("Try again");
      System.out.println("Enter the position to draft: ");
      position = console.nextLine();      
    }
    return position;
  }

  // Here we output information based on the position entered
  public String positionOutput() {
    String position = getPositionInput();
    // We print the top players at that position
    System.out.println("Top 10 Players at that position: ");
    printPlayerAtPosition(position);
    // And ask if they want to see another one
    System.out.println("Do you want to see another position ");
    String ans = console.nextLine();
    return ans;
  }
  
  // Here we get the pick for users
  public void getPick(Team current, Pick p) {
    // We get the position to output and keep outputing 
    // position info untilthey decide to draft
    String ans = positionOutput();
    boolean done = false;
    while (ans.matches("yes|Yes") && !done) {
      ans = positionOutput();
    }
    // We then get a player to drfat and make sure it is valid 
    System.out.println("Pick a player: ");
    String name = console.nextLine();
    while (!validPlayer(name)) {
      System.out.println("Error, try again");
      System.out.println("Pick a player: ");
      name = console.nextLine();
    }  
    // We then do the Pick     
    handlePick(name, current, p);
  }

  // We print the top 10 players left
  public void printPlayersLeft() {
    int index = 0;
    int count = 0;
    // As long as there are more players and less than
    // ten players outputted, we print
    while (index < players.size() && count < 10) {
      Player p = players.get(index);
      System.out.print(p + " ");
      count++;
      index++;
    }
    // We move to a new line
    System.out.println();
  }
  
  // We handle the end of the game by showwing all 
  //players taken for user teams
  public void handleEnd() throws FileNotFoundException {
    System.out.println("\nThanks for playing. Below are the players taken for your team\n\n");
    for (Team t: teams) {
      // for every user team
      if (t.getUser()) {
        // We get the players taken tree Map with what pick for the team
        TreeMap<Pick, Player> playersTaken = t.getPlayersTakenWithPick();
        // for each map entry
        for (Map.Entry<Pick, Player> entry : playersTaken.entrySet()) {
          // We get the pick, the player, and the player name and than output it
          Pick pick = entry.getKey();
          Player p = entry.getValue();
          System.out.println("The " + t.getName() + " took " + p.getName() + " with Pick number " + pick.getPickNumber() + " in round " + pick.getRound());
        }              
        // Move to next line
        System.out.println();
      }
    }
    // We ask to play again
    askToPlayAgain();
  }

  // We aks to play again
  public void askToPlayAgain() throws FileNotFoundException {
    System.out.println("Do you want to play again? ");
    String ans = console.nextLine();
    if (ans.matches("Yes|yes")) {
      // If they do want to, we call run again
      Main.run();
      return;
    } else if (!ans.matches("No|no")) /* Error */ {
      System.out.println("Error, try again");
      askToPlayAgain();
    }
    // if we got here, we know they did not want to play and close the program
    console.close();
    System.exit(0);
  }

  // Here we handle the pick finally
  public void handlePick(String name, Team current, Pick pick) {
    // we remove the player
    Player p = removePlayer(name);
    // we add the player to the team
    ArrayList<Player> playersTaken = current.getPlayersTaken();
    playersTaken.add(p);
    // We make the pick unusable
    pick.setUsable(false);
    // We change the playersTakenWithPick to add the new player
    TreeMap<Pick, Player> plyersTakenWithPick = current.getPlayersTakenWithPick();
    plyersTakenWithPick.put(pick, p);
    // We go to the next line
    System.out.println();
  }

  // We remove a player based on their name
  public Player removePlayer(String name) {
    for (int index = 0; index < players.size(); index++) {
      Player real = players.get(index);
      String pName = real.getName();
      if (pName.equalsIgnoreCase(name)) {
        return players.remove(index);
      }
    }
    // default player I talked about earlier, should never reach here
    return new Player();
  }

  // Valid player checks if a player is a real player based on their name
  public boolean validPlayer(String name) {
    for (Player p: players) {
      String pName = p.getName();
      if (pName.equalsIgnoreCase(name)) {
        return true;
      }
    }
    return false;
  }

  
  // We check if the position is valid using regex
  public boolean validPosition(String p) {
    return p.matches("QB|RB|WR|TE|OT|G|C|DT|EDGE|LB|CB|S|K|P");
  }

 // We print players at positions of need here
  public void printPlayersAtPositionsOfNeed(Team t) {
    // we get the posisitions needed and than print
    ArrayList<String> positionsOfNeed = t.getPositionsOfNeed();
    for (String position: positionsOfNeed) {
      printPlayerAtPosition(position);
    }
  }

  // Here we print the top 5 players at a position
  public void printPlayerAtPosition(String position) {
    int index = 0;
    int count = 0;
    // while there are players left and we haven't printed 5, we print
    while (index < players.size() && count < 5) {
      Player p = players.get(index);
      if (p.getPosition().equalsIgnoreCase(position)) {
        System.out.print(p + " ");
        count++;
      }
      index++;
    }
    // Move to next line
    System.out.println();
  }

  // Here we set up the draft simulator by file processing
  public void setUpDraft() {
    try {
      setUpPicks();
      setUpTeams();
      setUpPlayers();
    } catch (FileNotFoundException e) {
      System.out.println("Error: File not Found");
    }
    // We sort them to make  sure they are in the right order
    Collections.sort(teams); 
    Collections.sort(picks);
    Collections.sort(players);
    // We let the user know that this is a mock draft
    System.out.println("This is a 2022 NFL Mock Draft. Hope you have fun");
    // We do the user changes
    doUserChanges();
  }

  // We create our parser for the File
  public void doUserChanges() {
    Scanner parser;
    try {
      parser = new Scanner(fileObject);
    } catch (FileNotFoundException e) {
      return;
    } 
    // We process line by line
    while (parser.hasNextLine()) {
      String line = parser.nextLine();
      processChangesLine(line);
    }
  }
  
  // Here we determine the command and change player info based on it
  public void processChangesLine(String line) {
    if (line.contains("change")) {
      changePlayer(line);
    } else if (line.contains("add")) {
      addPlayer(line);
    } else {
      permRemovePlayer(line);
    }
  }

  // This methd, we remove players
  public void permRemovePlayer(String line) {
    Scanner parser = new Scanner(line);
    String name = parser.next();
    name += " " + parser.next();
    // We get the name to remove
    removePlayer(name);
    // We remove the player and reset the rankings
    for (int index = 0; index < players.size(); index++) {
      Player p = players.get(index);
      p.setRanking(index + 1);
    }
    parser.close();
  }

  // We add players in this method
  public void addPlayer(String line) {
    line.replace("add", "");
    Scanner parser = new Scanner(line);
    int rank = parser.nextInt();
    String name = parser.next();
    name += " " + parser.next();
    double grade = parser.nextDouble();
    String position = parser.next();
    // We create the player and insert him into the list
    Player p = new Player(name, position, grade, rank);
    players.add(rank - 1, p);
    // we then reset the rankings
    for (int index = 0; index < players.size(); index++) {
      Player current = players.get(index);
      current.setRanking(index + 1);
    }
    parser.close();
  }

  // We chnage players in this method
  public void changePlayer(String line) {
    line.replace("change", "");
    Scanner parser = new Scanner(line);
    // We get our info
    String name = parser.next();
    name += " " + parser.next();
    int rank = parser.nextInt();
    double grade = parser.nextDouble();
    // we get the current index of the player based on their name
    int index = getPlayerIndex(name);
    // We get the player
    Player p = players.get(index);
    // We change the grade to the new one
    p.setGrade(grade);
    // We remove the player at that index and add him back in where his new ranking is
    players.remove(index);
    int newIndex = rank - 1;
    players.add(newIndex, p);
    // We then reset the rankings
    for (int i = 0; i < players.size(); i++) {
      Player current = players.get(i);
      current.setRanking(i + 1);
    }
  }

  // Here we get the player's index based on their name
  public int getPlayerIndex(String name) {
    for (int index = 0; index < players.size(); index ++) {
      Player p = players.get(index);
      if (p.getName().equals(name)) {
        return index;
      }
    }
    // We return -1 for not found
    return -1;
  }

 // Here wet up players from their ranking line by line
 public void setUpPlayers() throws FileNotFoundException {
    File file  = new File("PlayerRankings.txt");
    Scanner reader = new Scanner(file);
    while (reader.hasNextLine()) {
      String line = reader.nextLine();
      processPlayer(line);
    }
  }

  // Here we set up the player
  public void processPlayer(String line) {
    Scanner parser = new Scanner(line);
    int rank = parser.nextInt();
    String firstName = parser.next();
    String lastName = parser.next();
    String name = firstName + " " + lastName;
    double grade = parser.nextDouble();
    String position = parser.next();
    // We get the ifno, create tha player and add him to the list
    Player p = new Player(name, position, grade, rank);
    players.add(p);
  }

 // Here we process team info from a file line by line
  public void setUpTeams() throws FileNotFoundException {
    File file  = new File("PicksByTeam.txt");
    Scanner reader = new Scanner(file);
    while (reader.hasNextLine()) {
      String line = reader.nextLine();
      processTeam(line);
    }
  }

  // We set up each team here
  public void processTeam(String line) {
    Scanner parser = new Scanner(line);
    // We get the team name
    String name = parser.next();
    // We set up the teams picks here
    ArrayList<Pick> picksPerTeam = new ArrayList<>();
    while (parser.hasNextInt()) {
      int pick = parser.nextInt();
      Pick p = picks.get(pick - 1);
      picksPerTeam.add(p);
    }
    // We get the positions of need here
    ArrayList<String> positionsOfNeed = new ArrayList<>();
    while (parser.hasNext()) {
      String position = parser.next();
      positionsOfNeed.add(position);
    }
    // We create the team andadd it
    Team t = new Team(name, positionsOfNeed, picksPerTeam, new ArrayList<Player>(), new TreeMap<Pick, Player>(), false);
    teams.add(t);
    // We set the pick owners to be the current team
    for (Pick p: picksPerTeam) {
      pickOwners.put(p, t);
    }
  }
  
  // Here we set up the picks info from a file line by line
  public void setUpPicks() throws FileNotFoundException {
    File file = new File("PicksInfo.txt");
    // load the file and process the commands in the file
    Scanner reader = new Scanner(file);
    while (reader.hasNextLine()) {
      String line = reader.nextLine();
      processPickLine(line);
    }
    
  }

  // We set up each pick here
  public void processPickLine(String line) {
    Scanner parser = new Scanner(line);
    int pick = parser.nextInt();
    double value = parser.nextDouble();
    int round = 0;
    // We get their number and value
    // Based on the pick number, we get the round
    if (pick <= 32) {
      round = 1;
    } else if (pick <= 64) {
      round = 2;
    } else if (pick <= 105) {
      round = 3;
    } else if (pick <= 143) {
      round = 4;
    } else if (pick <= 179) {
      round = 5;
    } else if (pick <= 222) {
      round = 6;
    } else {
      round = 7;
    }
    // We then create the pick and add it to the list
    Pick p = new Pick(round, pick, value, true);
    picks.add(p);
  }
}