/* This is our pick Comparator class where we compare picks
We have this class so we can sort out the TreeMap of picks and their owners in Draft Engine
*/
import java.util.*;
public class PickComparator implements Comparator<Pick>{
  
  // Once again, we compare the pick numbers like in Pick to dertermine th bigger and
  // smaller picks
  public int compare(Pick p, Pick p2) {
    if (p.getPickNumber() > p2.getPickNumber()) {
      return 1;
    } else if (p.getPickNumber() < p2.getPickNumber()) {
      return -1;
    }
    return 0;
  }
}