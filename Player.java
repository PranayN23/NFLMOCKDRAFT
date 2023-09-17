/* This is our player class, representing draft eligible players
It implements Comparable so we can compare players based on their rank and sort players
by said rank
*/
public class Player implements Comparable<Player> {
  // These are the instance fields
  private String name;
  private String position;
  private int ranking;
  private double grade;

  // This is the contructor to construct each player
  // We take their name, position, grade as a prospect (which is based on my personal views
  // on each player along with other people's online)
  // We also take their ranking, which is where me and others think they will go
  // Basically, the ranking is predictive but the grade represents their actual talent
  public Player(String name, String position, double grade, int ranking) {
    this.name = name;
    this.position = position;
    this.ranking = ranking;
    this.grade = grade;
  }

  // Here we implement compareTo based on the player ranking for Collections.sort()
  public int compareTo(Player other) {
    if (this.ranking > other.ranking) {
      return 1;
    } else if (this.ranking < other.ranking) {
      return -1;
    } 
    return 0;
  }
  
  // This is our error player which we need as a default player for DraftEngine
  // This player should never be created
  public Player() {
    this("L Bozo", "QB", 10.0, 1);
  }

  // Here we override toString to present players in a better way
  public String toString() {
    return String.format("%s %s Ranking %d Grade %.2f", name, position, ranking, grade);
  }

  // Below are the getters ans setters for the instanceFields
  public void setName(String name) {
    this.name = name;
  }

  public void setPosition(String name) {
    this.name = name;
  }

  public void setRanking(int ranking) {
    this.ranking = ranking;
  }

  public void setGrade(double grade) {
    this.grade = grade;
  }

  public String getName() {
    return name;
  }

  public String getPosition() {
    return position;
  }

   public int getRanking() {
    return ranking;
  }

   public double getGrade() {
    return grade;
  }
  
}