//=================due Tuesday=================
//gif explosion
//sounds?

//Class that can be run to display a GamePanel on the computer screen
import javax.swing.JFrame; //import JFrame class

public class Driver {
   //Method to run at startup
   public static void main(String[] args) {
      //Instantiate a JFrame and configure it
      JFrame f = new JFrame();
      f.setSize(1920, 1200); //size of screen
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //close when exit clicked
      f.setContentPane(new GamePanel()); //set a GamePanel object as the content
      f.setExtendedState(JFrame.MAXIMIZED_BOTH); //fullscreen
      f.setUndecorated(true); //no frame decorations (minimize, maximize, close buttons, etcR)
      f.setVisible(true); //make visible
   }
}