/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package BowlerDebug;

import com.neuronrobotics.bowlerstudio.BowlerStudio;

public class ExampleLibrary {
    public boolean someLibraryMethod() {
    	BowlerStudio.speak("Called from the method");
        return true;
    }
}