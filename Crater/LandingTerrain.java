//A class, extending the abstract Terrain class, for use as a landing spot

//import necessary packages

import java.awt.*;
import java.awt.geom.*;
public class LandingTerrain extends Terrain {
   private int myPointValue; //private field for the point value
   //constructor
   public LandingTerrain(int startx, int endx, int y, int pts) { //take args for point value, start/end x pts
                                                                  //and the height of the line (landing spots
                                                                  //are always flat
      super(startx, y, endx, y); //call superconstructor with this info
      myPointValue = pts; //initialize private data for point value
   }
   //accessor method to get the point value
   public int getPointValue() {
      return myPointValue;
   }   
   //abstract draw method implementation
   public void draw(Graphics2D myBuffer) {
      myBuffer.setColor(Color.WHITE); //white color
      myBuffer.setStroke(new BasicStroke(3)); //thicker line (3pt) helps identify landing spots
      myBuffer.setFont(new Font("Arial", Font.BOLD, 25)); //25pt bold Arial font for point value text
      //Draw the line
      myBuffer.drawLine((int)getLine().getX1(), (int)getLine().getY1(), (int)getLine().getX2(), (int)getLine().getY2());
      //Draw the text just below the left side of the line
      myBuffer.drawString(""+myPointValue, (int)getLine().getX1() + 5, (int)getLine().getY1() + 25);
   }
   public boolean isBelow(Lander lem) {
      //only return true when the spacecraft is entirely above the line
      //e.g., when both the left edge and right edge of the ship are between the endpoints of the line
      return (lem.getX() + 8 > getLine().getX1()) && (lem.getX() + 67 < getLine().getX2());
   }        
}   