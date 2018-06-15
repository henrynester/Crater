//import necessary packages

//a class, extending the abstract Terrain class, for use as a dangerous landing spot

import java.awt.*;
import java.awt.geom.*;
public class RoughTerrain extends Terrain {
   //constructor
   public RoughTerrain(int startx, int starty, int endx, int endy) {
      super(startx, starty, endx, endy); //calls 4-arg superconstructor
   }
   //Implements abstract method
   public void draw(Graphics2D myBuffer) {
      myBuffer.setColor(Color.WHITE); //white color
      myBuffer.setStroke(new BasicStroke(1)); //thin line
      //Draw the line
      myBuffer.drawLine((int)getLine().getX1(), (int)getLine().getY1(), (int)getLine().getX2(), (int)getLine().getY2());
   }
   public int getPointValue() { return -1; } //RoughTerrains have no points, but we have to implement the method   
   public boolean isBelow(Lander lem) {
      //Returns true if this line segment is below the Lander passed in as an argument
      //If any part of the spacecraft is above the line, return true, because we don't want them to land partially on a
         //landing spot and partially on a rough area and then get the points
      return (lem.getX() + 67 > getLine().getX1()) && (lem.getX() + 8 < getLine().getX2());
   }     
}   