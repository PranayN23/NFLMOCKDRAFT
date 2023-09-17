/* This is the main class where we create 
our DraftEngine object and ask it to run the game */
import java.io.*;

public class Main {
  // In our main method, we call run to start the game
  public static void main(String[] args) {
    run();
  }


  // In run, we start the game
  public static void run() {
    // We handle the FileNotFoundException here
    // We then create our Draft Engine object and ask it to run the draft
    try {
      DraftEngine engine = new DraftEngine();
      engine.runDraft();
    } catch (FileNotFoundException e) {
      System.out.println("Error");
    }
  }
}