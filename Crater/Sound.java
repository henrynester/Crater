//A class to handle sound effect functions
//Import necessary classes
import java.applet.*;

public class Sound {
   //static method to return the audio clip for a given filename
   public static AudioClip getClip(String arg) {
      try {
         java.net.URL file = Sound.class.getResource(arg); //make a URL from the filename
         return Applet.newAudioClip(file); //make an AudioClip from the URL
      }
      catch(Exception e) {
         System.out.println("Error: getClip(" + arg + ")"); //In case of an error print a message and return nothing
         return null;
      }
   }
   //Method to loop a given audio clip
   public static void loop(AudioClip arg) {
      try {
         arg.loop();
      } catch(Exception e) {
         System.out.println("Error: loop");
      }
   }
   //Method to play one time a given audio clip
   public static void play(AudioClip arg) {
      try {
         arg.play();
      } catch(Exception e) {
         System.out.println("Error: play");
      }
   }
   //Method to stop playing a given audio clip
   public static void stop(AudioClip arg) {
      try {
         arg.stop();
      } catch(Exception e) {
         System.out.println("Error: stop");
      }
   }
}                              
            