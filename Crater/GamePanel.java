//import necessary packages
//class that coordinates actions of all other classes and displays them on the screen as a JPanel
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.font.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Scanner;
import java.applet.*;

public class GamePanel extends JPanel {
   private DataDisplay hud; //displays flight info
   private Lander lem; //the lander spacecraft
   private Terrain[] terrain; //array of abstract Terrain references
   private JPanel end, menu; //subpanels for the game over screen and the main menu
   private CardLayout cardLayout; //a card layout allows to flip between these panels
   private JButton playButton, howToButton, backButton, exitButton; //  GUI
   private JLabel title, highScore, gameover, yourScore, message;   //Elements
   private Font orbitronRoot, orbitronTitle, orbitronText; //3 font types to be used
   private javax.swing.Timer t, tSeconds; //a timer for animation and for keeping a mission timer
   private BufferedImage myImage; //BufferedImage speeds animation drawing process
   private Graphics2D myBuffer; //Graphics object for BufferedImage
   private ImageIcon background; //Background image (starry sky)
   private GamePanel instance; //Allows to return the current instance of GamePanel
   private Scanner levelReader, highScoreReader; //reads the game levels from a file
   private AudioClip thrusterSound, explosionSound, rcsSound, alarmSound, successSound; //sound effect clips
   
   private boolean playing; //are we playing or not?
   private boolean noFuelAlarm, noRcsAlarm; //has the alarm already started sounding for low fuel?
   private int time, radarAlt, pointsEarned, score, highScoreNum; //other private vars
   //Constructor
   public GamePanel() {
      setFocusable(true);     //    get user focus to
      requestFocusInWindow(); //allow use of keyboard input
      instance = this;        //set up the instance variable for use in other classes
      myImage = new BufferedImage(1920, 1200, BufferedImage.TYPE_INT_RGB); //set up a BufferedImage the size of the screen
      myBuffer = myImage.createGraphics(); //get the graphics object for it
      background = new ImageIcon("resources/images/background2.jpg"); //instantiate the background ImageIcon
      t = new Timer(10, new TimerListener()); //Instantiate the animation timer
      tSeconds = new Timer(1000, new SecondsListener());  //the mission timer
      
      addKeyListener(new Keyboard()); //add a keyboard listener to the panel for controls
      
      //Get AudioClips for the audio files stored in the resources folder
      alarmSound = Sound.getClip("resources/sounds/alarm.wav");
      successSound = Sound.getClip("resources/sounds/success.wav");
      thrusterSound = Sound.getClip("resources/sounds/thruster.wav");
      rcsSound = Sound.getClip("resources/sounds/rcs.wav");
      explosionSound = Sound.getClip("resources/sounds/explosion.wav");
      
      try { //attempt to read in the Orbitron font from a file
         orbitronRoot = Font.createFont(Font.TRUETYPE_FONT, getClass().getResource("/resources/fonts/Orbitron-Regular.ttf").openStream());   
      } 
      catch(Exception e) { }
      
      Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
      map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON); //Setup for underlining
      //Title text is 175pt, BOLD, ITALIC, UNDERLINED
      orbitronTitle = orbitronRoot.deriveFont(175f).deriveFont(Font.BOLD | Font.ITALIC).deriveFont(map);
      //Normal text is 40pt and BOLD
      orbitronText = orbitronRoot.deriveFont(40f).deriveFont(Font.BOLD);
      
      //Set layout of the GamePanel to a CardLayout
      cardLayout = new CardLayout(); //this reference is to be used later for flipping the panels
      setLayout(cardLayout);
      
      //set up the main menu subpanel with a six row layout
      menu = new JPanel();
      menu.setOpaque(false); //clear to see background
      menu.setLayout(new GridLayout(6, 1));
      menu.add(new JLabel()); //placeholder
      title = new JLabel();
      addText(menu, title, "CRATER", true); //title
      menu.add(new JLabel()); //placeholder
      addButton(menu, playButton, "START GAME", new StartListener()); //start button
      addButton(menu, howToButton, "HOW TO PLAY", new HowListener()); //rules button
      menu.add(new JLabel()); //placeholder
      add(menu, "menu"); //add under the tag "menu"
      
      end = new JPanel(); //game over screen subpanel
      end.setOpaque(false); //clear, again
      end.setLayout(new GridLayout(6, 1)); //6-row layout
      gameover = new JLabel();
      addText(end, gameover, "GAME OVER", true); //game over text
      message = new JLabel();
      addText(end, message, "XXX", false); //why you lost message
      yourScore = new JLabel();
      addText(end, yourScore, "SCORE: XXXX", false); //your score
      highScore = new JLabel();
      addText(end, highScore, "HIGH SCORE: XXXX (XXXX)", false); //high score and who earned it
      addButton(end, backButton, "PLAY AGAIN", new BackListener()); //play again button
      addButton(end, exitButton, "EXIT", new ExitListener()); //exit button if they are done playing
      add(end, "end"); //add under tag "end"
      
      score = 0; //set score to 0
      noFuelAlarm = true; //set alarm flags
      noRcsAlarm = true;
      
      hud = new DataDisplay(); //instantiate the DataDisplay
      add(hud, "play"); //add under tag "play"
      
      lem = new Lander(); //instantiate the spacecraft
      
      cardLayout.show(this, "menu"); //start showing the menu panel
      
      playing = false; time = 0; //start not playing with a time of 0
      
      stepAnimation(); //step the animation once to draw the background image    
   }
   public GamePanel getInstance() { //this is useful for other classes to get the current instance of GamePanel
      return instance; 
   }
   public void paintComponent(Graphics g) {
      g.drawImage(myImage, 0, 0, getWidth(), getHeight(), null); //draw the BufferedImage on the panel
   }   
   public void stepAnimation() { //runs in animation timer
      myBuffer.drawImage(background.getImage(), 
         0, 0, 1920, 1200, 
         (int)lem.getX()/2, (int)lem.getY()/2, 
         (int)lem.getX()/2+1920, (int)lem.getY()/2+1200, 
         null);
      //myBuffer.drawImage(background.getImage(), 0, 0, 1920, 1200, null); //draw the background image and clear it
      if(playing) { //during a game...
         lem.move(); //move the lander
         if(lem.isOutOfBounds(1900, 1200)) { //if they tried to escape the world
            endGame("LOST IN DEEP SPACE"); //show the game over screen and the message "LOST IN DEEP SPACE"
         }
         if(lem.isOutOfFuel()) { //if out of fuel
            hud.warning("NO FUEL");
            lem.setThrust(false);
         }
         if(lem.isOutOfRcs()) { //if out of rcs fuel
            hud.warning("NO RCS");
            lem.setLeftRcs(false);
            lem.setRightRcs(false);
         }
         if(lem.getRcs() < 100 && noRcsAlarm) { //provide a low rcs warning on the DataDisplay if it isn't there already from the low fuel condition
            hud.warning("RCS LOW");
            Sound.loop(alarmSound); //play alarm
            noRcsAlarm = false;
         }   
         if(lem.getFuel() < 300 && noFuelAlarm) { //provide a low fuel warning
            hud.warning("FUEL LOW");
            Sound.loop(alarmSound); //play alarm
            noFuelAlarm = false;
         }        
         lem.draw(myBuffer); //draw the lander in the new position
         for(int i = 0; i < terrain.length; i++) { //iterate over the array of terrain segment objects
            terrain[i].draw(myBuffer); //draw it on the BufferedImage
            if(terrain[i].isBelow(lem)) { //if a certain segment is the one below the lander
               radarAlt = terrain[i].getRadarAlt(lem); //compute the radar altitude and store it for displaying
            }      
            if(terrain[i].touching(lem)) { //if a terrain is touching the lander
            //check this for all segments and not just the ones below the lander to avoid problems where the lander
            //is over two segments at once
               if(terrain[i] instanceof RoughTerrain) //if the current segment is a rough terrain
                  endGame("CRASH LANDED ON THE MOON"); //you've crashed into it, so end the game
               else if(terrain[i] instanceof LandingTerrain) //if instead it is a landing spot
                  if(Math.abs(lem.getAtt()) > 7.0) //if tilted too much when landing
                     endGame("LANDED OFF KILTER");
                  else if(Math.abs(lem.getVertVel()) > 0.9)
                  //if velocity (vertically) is too high
                     endGame("LANDED TOO HARD");
                  else if(Math.abs(lem.getHorizVel()) > 0.9)
                  //if the spacecraft's horizontal velocity is too high when it hits a landing spot
                     endGame("FLEW IN SIDEWAYS");
                  else {
                     //otherwise, you landed well! Find the points to be earned.
                     pointsEarned = terrain[i].getPointValue();
                     //show a message on the HUD with the points earned
                     hud.message("PERFECT LANDING! +" + pointsEarned);
                     //stop any sound if playing
                     Sound.stop(alarmSound);
                     Sound.stop(thrusterSound);
                     Sound.stop(rcsSound);
                     noFuelAlarm = true; noRcsAlarm = true;
                     //play a congratulating sound
                     Sound.play(successSound);
                     //add to the score total
                     score += pointsEarned;
                     //stop the animation timer to prevent further motion of the spacecraft
                     t.stop();
                     tSeconds.stop(); //stop the mission clock
                     //Make a one-shot timer that waits 2 secs while the player basks in the glory
                     //of their landing, before starting the craft up in the air again
                     Timer landedDelay = new Timer(2000, new LandedDelayListener());     
                     landedDelay.setRepeats(false); //no repeats = one-shot timer
                     landedDelay.start();
                  }           
            }   
         }   
      }   
      repaint(); //repaint the panel with each animation cycle
   }
   //Method used to end the game with a specific message to the user (prevents repeated code)
   private void endGame(String info) {
      t.stop();
      playing = false; //not playing anymore
      lem.explode(myBuffer); //show the spacecraft as exploding
      noFuelAlarm = true; noRcsAlarm = true;
      Sound.stop(alarmSound); //stop any sound that is playing
      Sound.stop(thrusterSound);
      Sound.stop(rcsSound);
      Sound.play(explosionSound); //play an explosion sound effect
      Timer crashedDelay = new Timer(1000, new CrashedDelayListener(info));     
      crashedDelay.setRepeats(false); //no repeats = one-shot timer
      crashedDelay.start();
   }
   //Listener for the How To Play button
   private class HowListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         JOptionPane.showMessageDialog(null, 
            "HOW TO PLAY:\n"
            +'\u2022'+"The object in CRATER is to safely land your spacecraft on the moon\n"
            +'\u2022'+"Only land in flat areas, marked with a point value and thicker line\n"
            +'\u2022'+"Use the UP arrow key to activate your main thruster\n"
            +'\u2022'+"Use the LEFT/RIGHT arrow keys to fire the RCS (Reaction Control System) for maneuving\n"
            +'\u2022'+"Note that you will keep spinning after firing the RCS jets\n"
            +"GOOD LUCK!"); //show a dialog with playing instructions
                           //unicode #2022 is a bullet point
         
      }
   }
   //Listener for the Start Game button
   private class StartListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         //Attempt to have the user enter a level #
         String filename = null;
         try {
            int input = Integer.parseInt(JOptionPane.showInputDialog("Enter a level number, 1 - 3"));
            //create filename from this level #
            filename = "resources/levels/level" + input + ".txt";
         } 
         catch(Exception ex) {
            //if they entered text or an invalid #, show a dialog
            JOptionPane.showMessageDialog(null, "Invalid input. Follow the directions next time");
            return;
         }      
         //attempt to make a Scanner for this file
         try {
            levelReader = new Scanner(new File(filename));
         } 
         catch(Exception ex) {
            //show a dialog if it fails
            JOptionPane.showMessageDialog(null, "Level file " + filename + " not found");
            return;
         }
         //attempt to parse the file for level data
         try {
            /*File format:
            3 <=number of points described in the file
            100   500   0 <= point at (100, 500) starting a terrain segment of 0 points,
            200   1000  500 <= point at (200, 1000) that gives 500 points, etc
            400   300   0
            */ 
            //the 1st # in the file is the number of points
            //the number of terrain segments is this - 1
            //set up the array
            terrain = new Terrain[Integer.parseInt(levelReader.nextLine())-1];
            //read in a line
            String line = levelReader.nextLine();
            //find the indices of the two spaces
            int space1 = line.indexOf(',');
            int space2 = line.lastIndexOf(',');
            //parse the string
            //first #: x coordinate
            int xOld = Integer.parseInt(line.substring(0,space1));
            //second #: y coordinate
            int yOld = 1200 - Integer.parseInt(line.substring(space1 + 1, space2));
            //third #: point value (0 for a RoughTerrain)
            int ptsOld = Integer.parseInt(line.substring(space2 + 1));
            //traverse the terrain array
            for(int i = 0; i < terrain.length; i++) {
               //read in a line and repeat the process above
               line = levelReader.nextLine();
               space1 = line.indexOf(',');
               space2 = line.lastIndexOf(',');
               int x = Integer.parseInt(line.substring(0,space1)); //note the "new" and not "old" vars
               int y = 1200 - Integer.parseInt(line.substring(space1 + 1, space2));
               int pts = Integer.parseInt(line.substring(space2 + 1));
               //if it earns no points (RoughTerrain), instantiate it as such
               if(ptsOld == 0) {
                  terrain[i] = new RoughTerrain(xOld, yOld, x, y); //use the xOld and yOld values as the start point
               }
               //if it earns points, it should be a LandingTerrain
               if(ptsOld > 0) {
                  terrain[i] = new LandingTerrain(xOld, x, y, ptsOld); //use the ptsOld value because the pts for
                  //a landing terrain are mentioned with its start coordinate
               }   
               xOld = x; //move the "new" values into the "old" variables for the next run of the loop
               yOld = y;
               ptsOld = pts;   
            }
            levelReader.close();   
         } 
         catch(Exception ex) {
            //catch any errors and show a dialog
            JOptionPane.showMessageDialog(null, "Error reading level file " + filename);
            ex.printStackTrace();
            return;
         }
         //show the playing panel
         cardLayout.show(getInstance(), "play");
         playing = true; //set "playing" flag
         time = 0; //reset time, score
         score = 0;
         noFuelAlarm = true; noRcsAlarm = true; //reset alarm flags
         t.start(); //start animation timer
         tSeconds.start(); //start mission timer
         hud.clear(); //reset the DataDisplay
         lem = new Lander(); //instantiate a new Lander object
      }
   }
   //When the Exit button is clicked, exit the game with a normal exit code of 0
   private class ExitListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         System.exit(0);
      }
   }
   //When the "Play Again"/"Back" button is clicked
   private class BackListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         //Return to the main menu, thus hiding the game
         cardLayout.show(getInstance(), "menu");
         //stop game timers
         t.stop();
         tSeconds.stop();
      }
   }      
   //Runs with every fire of the animation timer      
   private class TimerListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         stepAnimation(); //step the animation
         //make an array of flight info
         int[] info = { score, 
                        time, 
                        lem.getFuel(), 
                        lem.getRcs(), 
                        radarAlt, 
                        (int)(lem.getHorizVel()*20.0), 
                        (int)(lem.getVertVel()*20.0), 
                        (int)lem.getAtt() 
                      };
         hud.update(info); //update the DataDisplay with this info
      }
   }
   //Every fire of the 1-sec interval timer
   private class SecondsListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         time++; //increment the number of seconds on the mission clock
      }
   }
   //Listens for the end of the succesful landing delay
   private class LandedDelayListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         //start the lem at the top of the screen with same amount of fuel
         lem.reset();
         //clear messages from the DataDisplay
         hud.clear();
         //start the animation timer and clock timer again
         t.start();
         tSeconds.start();
      }
   }
   //Listens for the end of the crash landing delay
   private class CrashedDelayListener implements ActionListener {
      private String info;
      public CrashedDelayListener(String s) { //allow a custom game over message to be displayed at the end of the delay after the crash
         info = s;
      }   
      public void actionPerformed(ActionEvent e) {
         stepAnimation();
         Sound.stop(alarmSound);
         hud.clear();
         message.setText(info); //show the message needed on the game over screen (the reason the player lost)
         cardLayout.show(getInstance(), "end"); //show the game over screen
         highScoreReader = null;
         String name;
         try { //attempt to set up a high score file Scanner
            highScoreReader = new Scanner(new File("resources/data/highscores.txt"));
            //also attempt to read the high score and name of the scorer
            String line = highScoreReader.nextLine();
            //find the index of the space
            int pos = line.indexOf(',');
            //Format example: Amy   158
            //Get the text up to the space (name)
            name = line.substring(0, pos);
            //Parse an integer from the text after the space (score)
            highScoreNum = Integer.parseInt(line.substring(pos + 1));
            highScoreReader.close();
         } 
         catch(Exception exp) {
            highScoreNum = 0;
            name = null;
         }
         if(score > highScoreNum) { //if your score is better than the old high score
            //show a message and ask player to input their name
            String message;
            if(highScoreNum == 0) {
               message = "You set a new high score of " + score + "!";
            }
            else {
               message = "You beat " + name + "'s score of " + highScoreNum + " with a score of " + score + "!";
            }
            name = JOptionPane.showInputDialog(message + "\nEnter your name to save this high score!");
            highScoreNum = score; //store your score in the highscore var
            try {
               //attempt to make a PrintStream so we can write the new high score to the file
               PrintStream highScoreWriter = new PrintStream(new FileOutputStream(
                                                   new File("resources/data/highscores.txt")));
                //write high score in the format "name   score"
               highScoreWriter.println(name + ',' + highScoreNum);
               highScoreWriter.close();            
            } 
            catch(Exception exp2) {
               //upon failure, show a dialog
               JOptionPane.showMessageDialog(null, "Error writing high scores");
               return;
            }
         }
         if(highScoreNum == 0) {
            highScore.setText("NO HIGH SCORE");
         }
         else {
            highScore.setText("HIGH SCORE: " + highScoreNum + " (" + name + ")");
         }
         yourScore.setText("SCORE: " + score);
      }
   }
   //Listener to light up the button text when the mouse rolls over
   private class RolloverListener implements ChangeListener {
      private JButton button;
      public RolloverListener(JButton b) { //constructor sets the private button reference to a certain button
         button = b;
      }   
      public void stateChanged(ChangeEvent evt) { //when the state changes
         if (button.getModel().isRollover()) { //if the mouse rolls over the button
            button.setForeground(Color.YELLOW); //set the text as yellow
         } 
         else {
            button.setForeground(Color.WHITE); //otherwise set it to a normal white color
         }
      }
   }
   //KeyAdapter subclass to listen for keyboard controls input
   private class Keyboard extends KeyAdapter { //extends KeyAdapter since we don't need to implement all the methods
      public void keyPressed(KeyEvent e) { //at the moment a key is pressed down
         if(!playing) 
            return; //ignore this when not playing
         if(e.getKeyCode() == KeyEvent.VK_UP) { //if it's the up arrow key
            if(!lem.isOutOfFuel()) {
               lem.setThrust(true); //start thrusting
               Sound.loop(thrusterSound); //start the noise
            }   
         }   
         if(e.getKeyCode() == KeyEvent.VK_LEFT && lem.getRightRcs() == false) { //left key
            if(!lem.isOutOfRcs()) {
               lem.setLeftRcs(true); //left RCS jet on, start tilting right
               Sound.loop(rcsSound); //start the noise
            }   
         }   
         if(e.getKeyCode() == KeyEvent.VK_RIGHT && lem.getLeftRcs() == false) { //right key
            if(!lem.isOutOfRcs()) {
               lem.setRightRcs(true); //right RCS jet on, start tilting left
               Sound.loop(rcsSound); //start the noise
            }
         }   
      }
      public void keyReleased(KeyEvent e) { //at the moment a key is let up
         if(!playing) 
            return; //ignore this when not playing
         if(e.getKeyCode() == KeyEvent.VK_UP) { //if it's the up key
            lem.setThrust(false); //stop thrusting
            Sound.stop(thrusterSound); //end the noise
         }   
         if(e.getKeyCode() == KeyEvent.VK_LEFT) { //left
            lem.setLeftRcs(false); //left RCS off
            Sound.stop(rcsSound); //end the noise
         }   
         if(e.getKeyCode() == KeyEvent.VK_RIGHT) { //right
            lem.setRightRcs(false); //right RCS off
            Sound.stop(rcsSound); //end the noise
         }   
      }
   }
   //method to add a JLabel
   private void addText(JPanel panel, JLabel label, String text, boolean isTitle) {
      label.setText(text); //set the text to argument
      label.setForeground(Color.WHITE); //white text
      label.setHorizontalAlignment(JLabel.CENTER); //centered
      label.setFont(isTitle ? orbitronTitle : orbitronText); //regular if isTitle is false, title font if true
      panel.add(label); //add the label
   }
   //method to add a JButton
   private void addButton(JPanel panel, JButton button, String text, ActionListener listener) {
      button = new JButton(text); //instantiate button from reference passed in
      button.setFont(orbitronText); //regular font
      button.setOpaque(false); //clear background
      button.setContentAreaFilled(false); //nothing in the background
      button.setBorderPainted(false); //no border
      button.setForeground(Color.WHITE); //white text
      button.setFocusPainted(false); //none of the default ugly painting with mouse rollover
      button.addActionListener(listener); //add the given listener
      button.addChangeListener(new RolloverListener(button)); //add a rollover color change listener
      panel.add(button); //add to the panel
   }            
}