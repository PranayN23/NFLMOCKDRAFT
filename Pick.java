/*
This is our Pick class, it represents every pick in the draft
We implement the comparale interface so we can sort our picks based on their rankings
*/
public class Pick implements Comparable<Pick> {
  
  // Here we set up our instance fields
  private int round;
  private int pickNumber;
  private double draftValue;
  // We set usable to b true by default as we only make the picks unusable later
  private boolean usable = true;

   // We use this constrctor for our Pick objects
   // Each pick has a round: the round which the pick is in
   // a pickNumber, aka the actual number in the draft the pick is
   // We have  the draft value of the pick which we use for trades
         // It is derived from the Rich Eisen Pick value chart
   // And we have whether or not the pick is usable
   public Pick(int round, int pickNumber, double draftValue, boolean usable) {
      this.round = round;
      this.pickNumber = pickNumber;
      this.draftValue = draftValue;
      this.usable = usable;
   }

   // Here we implement compareTo so we can use Collections.sort in DraftEngine
   // Our paramater is the other pick
   public int compareTo(Pick other) {
     // If the current pickNumber is bigger than the other pick's pick number, 
     // we return 1 to signal that it is bigger
     if (this.pickNumber > other.pickNumber) {
       return 1;
      } 
      // If it is smaller, we return -1
      else if (this.pickNumber < other.pickNumber) {
        return -1;
      } 
      // If it is neiher, we return 0
      return 0;
    }
 
 
 // Below are the getters and setters for the instance fields
 public int getRound() {
   return round;
 }

 public int getPickNumber() {
    return pickNumber;
 } 

 public double getDraftValue() {
    return draftValue;
 } 

 public boolean getUsable() {
    return usable;
 } 

 public void setUsable(boolean usable) {
    this.usable =  usable;
 } 


 // This is our toString method where e print out the pick with it's round number and
 // pick number
 public String toString() {
   return "Round " + round + " Pick " + pickNumber;
 }
}