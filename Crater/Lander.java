//import necessary packages
import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;

/**
*Class for the lunar lander spacecraft. A Lander has properties to store its motion and state.
*It is designed to be drawn onto a Graphics2D JPanel. 
*@author Henry Nester
*/
public class Lander {
   //Class private data:
   /**
   *BufferedImage for the spacecraft
   */
   private BufferedImage img;
   /**
   *Graphics object we can draw on to add features of the spacecraft
   */
   private Graphics2D imgGraphics;
   /**
   *Polygon to store collision bounds of the ship
   */
   private Polygon myBounds;
   /**
   *An AffineTransform object allows rotation and translation of the spacecraft on the screen
   */
   private AffineTransform at, defaultAt;
   /**
   *Variable to store fuel level
   */
   private int myFuel;
   /**
   *Variable to store RCS propellant level, used for maneuvering
   */
   private int myRcs;
   /**
   *Stores the state of spacecraft leftward thruster. True for engine running, false for stopped.
   */
   private boolean isLeftRcs;
   /**
   *Stores the state of spacecraft rightward thruster. True for engine running, false for stopped.
   */
   private boolean isRightRcs;
   /**
   *Stores the state of spacecraft main thruster. True for engine running, false for stopped.
   */
   private boolean isThrust;
   /**
   *X-position on the screen (pixels)
   */
   private double myX;
   /**
   *Y-position on the screen (pixels)
   */
   private double myY;
   /**
   Vertical component of spacecraft velocity in pixels per animation step
   */
   private double myVertVel;
   /**
   *Horizontal component of spacecraft velocity in pixels per animation step
   */
   private double myHorizVel;
   /**
   *Horizontal component of spacecraft acceleration in pixels per animation step per animation step
   */
   private double myHorizAccel;
   /**
   *Vertical component of spacecraft acceleration in pixels per animation step per animation step
   */
   private double myVertAccel;
   /**
   *Attitude angle of the spacecraft. 0deg is a perfectly upright ship, negative numbers are tilted left, and positive numbers are tilted right
   */
   private double myAtt;
   /**
   *Angular velocity of spacecraft, in degrees per animation step
   */
   private double myAttVel;
   /**
   *Angular acceleration of spacecraft, in degrees per animation step per animation step
   */
   private double myAttAccel;
   /**
   *Constant arrays for the points of polygons for collisions and in thruster firing drawings
   */
   /**Constant array for the x-coordinates of the main thrust polygon*/
   private static final int[] thrusterX = { 30, 25, 28, 28, 30, 37, 44, 46, 46, 49, 44 };
   /**Constant array for the y-coordinates of the main thrust polygon*/
   private static final int[] thrusterY = { 60, 67, 63, 70, 68, 75, 68, 70, 63, 67, 60 }; 
   /**Constant array for the x-coordinates of the left thrust polygon*/
   private static final int[] rcsLeftX = { 17, -10, 17 };
   /**Constant array for the y-coordinates of the left thrust polygon*/
   private static final int[] rcsLeftY = { 24, 26, 28 };
   /**Constant array for the x-coordinates of the right thrust polygon*/
   private static final int[] rcsRightX = { 58, 85, 58 }; 
   /**Constant array for the y-coordinates of the right thrust polygon*/
   private static final int[] rcsRightY = { 24, 26, 28 }; 
   /**Constant array for the x-coordinates of the collision bounds polygon*/
   private static final int[] boundsX = {18, 18, 8, 8, 67, 67, 57, 57}; 
   /**Constant array for the y-coordinates of the collision bounds polygon*/
   private static final int[] boundsY = {10, 40, 50, 61, 61, 50, 40, 10};
   /**Gravitational constant (9.81m/s^2 on Earth) in pixels/step^2*/
   private static final double GRAVITY = -0.005; 
   /**Thrust constant in pixels/step^2*/
   private static final double THRUST = 0.03; 
   /**RCS thrust constant in pixels/step^2*/
   private static final double RCS_THRUST = 0.01; 
   /**Default constructor. Sets up the Lander by initializing private fields to default values and setting up the image and graphics objects*/
   public Lander() {
      img = getDefaultConfiguration().createCompatibleImage(75, 75, BufferedImage.TYPE_INT_ARGB); //create a transparent BufferedImage, 75x75 pixels
      imgGraphics = img.createGraphics(); //get the Graphics object for the BufferedImage
      imgGraphics.drawImage(new ImageIcon("resources/images/lander.png").getImage(), 0, 0, 75, 75, null); //Draw the lander image
      imgGraphics.setColor(Color.WHITE); //set a white pen for the Graphics object
      imgGraphics.setBackground(new Color(0,0,0,0)); //set a clear eraser pen
      myBounds = new Polygon(boundsX, boundsY, 8); //set collision bounds to a shape approximating spacecraft
      //setting flight variables to default values
      myFuel = 1200;
      myRcs = 300;
      myX = 100; //start in the upper left of the screen (100, 100)
      myY = 100;
      myHorizVel = Math.random() * 1.5 + 0.75; //moving rightward at a random velocity
      myVertVel = 0.0;
      myAttVel = 0.0;
      myAtt = 0.0;
      myAttAccel = 0.0;
      myHorizAccel = 0.0;
      myVertAccel = GRAVITY; //begin with only a downward acceleration
   }
   /**Method to redraw the spacecraft in the new location specified by its private fields.
   *@param myBuffer A Graphics object onto which to draw the spacecraft*/
   public void draw(Graphics myBuffer) { //Draws the BufferedImage onto the GamePanel Graphics object
      defaultAt = imgGraphics.getTransform(); //Store the default AffineTransform (nothing) for later reset
      //rotate the spacecraft graphics object to the needed angle (convert from our angle system to a 0->360 one)
      imgGraphics.rotate(Math.toRadians( ( (myAtt > 0.0) ? myAtt : (360 + myAtt) ) ), 75 / 2, 75 / 2);
      imgGraphics.clearRect(0, 0, 75, 75); //clear the spacecraft BufferedImage Graphics
      //Redraw the lander image
      imgGraphics.drawImage(new ImageIcon("resources/images/lander.png").getImage(), 0, 0, 75, 75, null);
      //Draw polygons for main thrust fire and left and right RCS fires based on firing state variables
      if(isThrust)
         imgGraphics.fillPolygon(thrusterX, thrusterY, 11);
      if(isLeftRcs)
         imgGraphics.fillPolygon(rcsLeftX, rcsLeftY, 3);
      if(isRightRcs)
         imgGraphics.fillPolygon(rcsRightX, rcsRightY, 3);
      //Draw the BufferedImage, rotated to the correct angle (myAtt) using the rotate() method at (myX, myY)  
      myBuffer.drawImage(img,(int)myX,(int)myY,75,75,null);
      imgGraphics.setTransform(defaultAt); //restore the original "no transformation" state
   }
   /**Method to move the spacecraft, called from the animation timer listener of a panel.
    *It updates the private fields related to motion on the basis of velocity and forces.*/
   public void move() {
      myVertVel += myVertAccel + GRAVITY; //Combine rocket acceleration, gravity and add to vertical velocity
      myHorizVel += myHorizAccel; //Combine rocket horizontal force component, add to horiz vel
      myX += myHorizVel; //increment position by velocity
      myY -= myVertVel;
      if(isThrust) //if thrusting, lose fuel
         myFuel -= 1;
      if(isLeftRcs || isRightRcs) //if using RCS, lose RCS propellant
         myRcs -= 1;  
      myAttVel += myAttAccel; //Add angular acceleration to angular velocity
      myAtt += myAttVel; //Increment angle by angular velocity
      myAtt = constrainAngle(myAtt); //constrain the attitude angle to the range -180 -> 0 -> 180
      
      //Get an instance of a rotation transformation for the needed tilt angle (a conversion from our angle system to a 0->360 system is needed)
      //Rotate about the center, the point (w/2, h/2)
      at = AffineTransform.getRotateInstance(Math.toRadians((myAtt > 0.0) ? myAtt : (360 + myAtt) ), 75/2.0,75/2.0);
      
      //Get point coordinates for the starting collision bounds of the spacecraft
      int[] rx = new int[boundsX.length]; //rx and ry will store the transformed points
      int[] ry = new int[boundsY.length];
      
      for(int i=0; i<boundsX.length; i++){ //traverse the array of points
        Point p = new Point(boundsX[i], boundsY[i]); //instantiate a Point
        at.transform(p,p); //use the at we set up for the spacecraft earlier to transform it
        rx[i]=p.x; //set rx and ry to the Point's new coordinates
        ry[i]=p.y;
      }
      
      //Create a polygon with these transformed points
      myBounds = new Polygon(rx, ry, boundsX.length);
      myBounds.translate((int)myX, (int)myY); //translate the polygon over to the spaceship
   }
   //helper method to constrain an angle to the range -180 -> 0 -> 180, where positive angles mean a rightward tilt
   private double constrainAngle(double angle) {
      if(angle >= 180) {
         angle = -360 + angle; //If exceeded upside down while turning clockwise, use negative angles
      }
      if(angle <= -180) { //If exceeded upside down while turning counterclockwise, start using positive angles
         angle = 360 + angle;
      }
      return angle;
   }
   /**Resets the spacecraft to flight above the moon. Used after a successful landing.*/
   public void reset() {
      myHorizVel = Math.random() * 2.0; //random rightward motion
      myVertVel = GRAVITY; //falling downward
      myAttVel = 0.0;
      myX = 100; //start at (100, 100) on the screen
      myY = 100;
   }
   /**Shows an image of an explosion on the spacecraft when it has crashed.
   *@param myBuffer A Graphics object onto which to draw the spacecraft*/
   public void explode(Graphics myBuffer) {
      imgGraphics.clearRect(0, 0, 75, 75);
      imgGraphics.drawImage(new ImageIcon("resources/images/explosion.png").getImage(), 0, 0, 75, 75, null);
      myBuffer.drawImage(img,(int)myX,(int)myY,75,75,null);
   }   
   //Modifier methods to access private fields
   /**Sets x-position 
   *@param x The desired x-position*/
   public void setX(int x) {
      myX = x;
   }
   /**Sets y-position 
   *@param y The desired y-position*/
   public void setY(int y) {
      myY = y;
   }
   /**Sets attitude angle 
   *@param att The desired angle*/
   public void setAtt(int att) {
      myAtt = att;
   }
   /**Sets vertical velocity 
   *@param v The desired vertical velocity*/
   public void setVertVel(double v) {
      myVertVel = v;
   }
   /**Sets horizontal velocity 
   *@param v The desired horizontal velocity*/
   public void setHorizVel(double v) {
      myHorizVel = v;
   }
   /**Sets fuel level 
   *@param fuel The desired fuel level*/
   public void setFuel(int fuel) {
      myFuel = fuel;
   }
   /**Sets RCS propellant level 
   *@param rcs The desired RCS level*/
   public void setRcs(int rcs) {
      myRcs = rcs;
   }
   /**Sets main thruster state 
   *@param b The desired state*/
   public void setThrust(boolean b) { //set the engine firing state (true = running, false = off)
      if(isThrust = b) {
         myHorizAccel = THRUST * Math.sin(Math.toRadians(myAtt)); //calculate horizontal and vertical components of thrust
         myVertAccel = THRUST * Math.cos(Math.toRadians(myAtt)); //using trigonometry
      } else {
         myHorizAccel = 0.0; //without an engine firing, there is no acceleration
         myVertAccel = 0.0;
      }      
   }
   /**Sets left RCS thruster state 
   *@param b The desired state*/
   public void setLeftRcs(boolean b) { //sets state of the left RCS jet
      if(isLeftRcs = b) {
         myAttAccel = RCS_THRUST; //sets the angular acceleration to the RCS jet force
      } else {
         myAttAccel = 0.0; //otherwise, no angular acceleration
      }      
   }
   /**Sets right RCS thruster state 
   *@param b The desired state*/
   public void setRightRcs(boolean b) { //same again for right RCS jet
      if(isRightRcs = b) {
         myAttAccel = -RCS_THRUST;
      } else {
         myAttAccel = 0.0;
      }         
   }
   //Accessor methods               
   /**Gets x-position on the screen of the spacecraft 
   *@return The x-position in pixels*/          
   public double getX() { 
      return myX;
   }   
   /**Gets y-position on the screen of the spacecraft 
   *@return The y-position in pixels*/
   public double getY() {
      return myY;
   }
   /**Gets attitude angle of the spacecraft 
   *@return The angle of the spacecraft in degrees away from the vertical. Negative means tilted left and vice versa.*/
   public double getAtt() {
      return myAtt;
   }
   /**Get vertical velocity of the spacecraft 
   *@return The vertical velocity in pixels/animation step^2*/
   public double getVertVel() {
      return myVertVel;
   }
   /**Get horizontal velocity of the spacecraft 
   *@return The horizontal velocity in pixels/animation step^2*/
   public double getHorizVel() {
      return myHorizVel;
   }
   /**Get fuel level of the spacecraft 
   *@return The fuel level in fuel units*/
   public int getFuel() {
      return myFuel;
   }
   /**Get RCS fuel level of the spacecraft 
   *@return The RCS fuel level in fuel units*/
   public int getRcs() {
      return myRcs;
   }
    //these 3 accessor methods return the thruster states (true = engine running, false = off)
   /**Get state of the spacecraft main thruster 
   *@return Whether the engine is active and running*/
   public boolean getThrust() {
      return isThrust;
   }
   /**Get state of the spacecraft left thruster 
   *@return Whether the thruster is active and running*/
   public boolean getLeftRcs() {
      return isLeftRcs;
   }
   /**Get state of the spacecraft right thruster 
   *@return Whether the thruster is active and running*/
   public boolean getRightRcs() {
      return isRightRcs;
   }
  /**Get the spacecraft's collision mesh of the spacecraft as an Area object (for collision checking)
  *@return An Area object representing the spacecraft collision bounds polygon*/
  public Area getBoundingArea() {
      return new Area(myBounds);
  }   
  /**Check if the spacecraft has gone out of bounds (off the screen) 
  *@param RIGHT_EDGE right edge of screen in pixels
  *@param BOTTOM_EDGE bottom edge of screen in pixels
  *@return Whether it has left the screen*/
   public boolean isOutOfBounds(int RIGHT_EDGE, int BOTTOM_EDGE) {
      if(myY + 75 > BOTTOM_EDGE || myY < 1 || myX < 1 || myX + 75 > RIGHT_EDGE)
         //if top edge passes top edge screen
         //if bottom edge passes bottom edge screen
         //if right edge passed right of screen
         //if left edge passed left of screen
         return true;
      return false; //otherwise, you are still in bounds, so return false
   }
   /**Check if the spacecraft has run out of fuel 
   *@return Whether it has run out*/
   public boolean isOutOfFuel() {
      return myFuel <= 0;
   }
   /**Check if the spacecraft has run out of RCS propellant 
   *@return Whether it has run out*/
   public boolean isOutOfRcs() {
      return myRcs <= 0;
   }
   //helper method to get the proper graphics environment for the system in use (helps with the rotation of a graphics object)
  private static GraphicsConfiguration getDefaultConfiguration() {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    return gd.getDefaultConfiguration();
  }
}   