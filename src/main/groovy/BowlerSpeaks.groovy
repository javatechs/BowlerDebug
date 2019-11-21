import com.neuronrobotics.bowlerstudio.BowlerStudio

import BowlerDebug.ExampleLibrary
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.RoundedCylinder

BowlerStudio.speak("Hello World")

CSG cadPart = new RoundedCylinder(20, 60).toCSG()

ExampleLibrary myObj = new ExampleLibrary()
myObj.someLibraryMethod()

return cadPart