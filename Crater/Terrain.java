//import necessary packages
//an abstract class for a segment of line in the lunar terrain

import java.awt.*;
import java.awt.geom.*;

public abstract class Terrain {
   private Line2D line; //private data for that line (Line2D object)
   public Terrain(int startx, int starty, int endx, int endy) { //constructor takes coordinates for the start and end pts
      line = new Line2D.Float(startx, starty, endx, endy); //and instantiates the line object
   }
   public Line2D getLine() { return line; } //return line (for use in extending classes)
   public int getRadarAlt(Lander lem) { //calculates altitude of the Lander passed in above this segment
      //find the line slope
      double m = (line.getY2() - line.getY1()) / (line.getX2() - line.getX1());
      //use the formula y=mx+b to find the contribution of 
      //the line's slope to the altitude at the lander's x-position
      double y = m * (lem.getX() - line.getX1()) + line.getY1();
      //subtract the y-position of the lander from this. Use a correction factor for the collision mesh
      return (int)(-lem.getY() - 60 + y); //return this altitude
   }
   public boolean touching(Lander lem) { //returns true if the Lander has collided with this line
      //We need to make the line into a polygon to test collisions with the Lander polygon easily
      //Make it a little thicker, a rectangle
      //Two lists of points for the rectangle:
      int[] xpts = { (int)line.getX1(), (int)line.getX1(), (int)line.getX2(), (int)line.getX2() };
      int[] ypts = { (int)line.getY1(), (int)line.getY1()+5, (int)line.getY2()+5, (int)line.getY2() };
      //Construct an Area object from this rectangle
      Area lineArea = new Area(new Polygon(xpts, ypts, 4));
      //Get the bounding Area object for the Lander
      Area intersectionArea = lem.getBoundingArea();
      //Find the mathematical intersection of the collision mesh with the rectangle polygon
      intersectionArea.intersect(lineArea);
      //If the intersection area is an empty set, the Lander collision mesh and the rectangle polygon
      //Do not intersect (touch) at all
      return !intersectionArea.isEmpty();
   }
   public abstract boolean isBelow(Lander lem); //abstract method that checks if this segment is below the spacecraft
   public abstract int getPointValue(); //Abstract method to find point value. 
   //It is unknown what subclass object a superclass reference points to, 
   //so we can't just implement this in LandingTerrain
   public abstract void draw(Graphics2D myBuffer); //Abstract method to draw the line segment
   //Implemented differently for landing spots versus rough terrains.
}   