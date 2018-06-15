//import necessary packages
//Class to display flight info in the game as an overlay, like the Head's Up Display in some military aircraft
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DataDisplay extends JPanel {
   private JLabel[][] labels; //2d array JLabels for flight info
   private JLabel messageLabel; //Label to hold a message or warning
   private Font labelFont; //variable to store font
   private Timer flashTimer; //timer to flash warnings
   public DataDisplay() {
      setOpaque(false); //clear to see gamepanel
      setLayout(new BorderLayout());
      //add a panel with a 4row x 2col layout in the NORTH
      JPanel textPanel = new JPanel(new GridLayout(4, 2));
      textPanel.setOpaque(false);
      add(textPanel, BorderLayout.NORTH);
      
      labels = new JLabel[4][2]; //4x2 array of JLabels set up
      flashTimer = new Timer(500, new FlashListener()); //Timer set up and connected to an ActionListener
      
      //Attempt to find the Orbitron font and set it to size 30 for use in the DataDisplay
      try {
         labelFont = Font.createFont(Font.TRUETYPE_FONT, 
            getClass().getResource("/resources/fonts/Orbitron-Regular.ttf").openStream())
            .deriveFont(30f).deriveFont(Font.BOLD);   
      } 
      catch(Exception e) { }
      //Loop thru the JLabels and set them to white text, proper alignment, orbitron font, then add them to the grid
      for(int r = 0; r < labels.length; r++) {
         for(int c = 0; c < labels[0].length; c++) {
            labels[r][c] = new JLabel("Test: ###");
            labels[r][c].setForeground(Color.WHITE);
            labels[r][c].setFont(labelFont);
            //Labels in the first collumn align left, those in the second align right
            labels[r][c].setHorizontalAlignment((c == 0) ? JLabel.LEFT : JLabel.RIGHT);
            textPanel.add(labels[r][c]);
         }
      }
      messageLabel = new JLabel(""); //Instantiate a label for messages/warnings
      messageLabel.setForeground(Color.WHITE); //white text
      messageLabel.setFont(labelFont); //orbitron font
      messageLabel.setHorizontalAlignment(JLabel.CENTER); //center at the top
      messageLabel.setVerticalAlignment(JLabel.TOP);
      
      add(messageLabel, BorderLayout.CENTER); //the label goes in the CENTER
      add(new JLabel(), BorderLayout.SOUTH); //an empty label goes in the SOUTH to keep the DataDisplay at the top of the screen
   }
   public void update(int[] data) {
      //set each JLabel's text to an indicator string plus the flight data item needed plus a unit
      labels[0][0].setText("SCORE: " + data[0]);
      //The next line displays a time in seconds in the format min:secsec using modulus and division
      labels[1][0].setText("TIME: " + (data[1] / 60) + ((data[1] % 60 < 10) ? ":0" : ":") + (data[1] % 60) );
      labels[2][0].setText("FUEL: " + data[2]);
      labels[3][0].setText("RCS FUEL: " + data[3]);
      labels[0][1].setText("RADAR ALT: " + data[4] + "m");
      labels[1][1].setText("H-VEL: " + data[5] + "m/s");
      labels[2][1].setText("V-VEL: " + data[6] + "m/s");
      labels[3][1].setText("ATT: " + data[7] + "deg");
   }
   public void message(String text) {
      messageLabel.setText(text); //set the text of the message label from any class
      flashTimer.stop(); //stop flashing if it is happening
   }
   public void warning(String text) {
      /*if(messageLabel.getText()!="") //if a warning already exists
         messageLabel.setText(messageLabel.getText() + "      " + text); //add on to it
      else*/
      messageLabel.setText(text); //otherwise, start from scratch with a new warning
      flashTimer.start(); //start flashing the label
   }
   public void clear() {
      messageLabel.setText(""); //clear the message
      flashTimer.stop(); //stop flashing if it is happening
   }
   private class FlashListener implements ActionListener {
      public void actionPerformed(ActionEvent evt) {
         messageLabel.setVisible(!messageLabel.isVisible()); //when this is called (every 1 sec), the visibility of the message label is toggled
      }
   }           
}