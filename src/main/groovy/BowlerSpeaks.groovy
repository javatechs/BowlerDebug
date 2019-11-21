import com.neuronrobotics.bowlerstudio.BowlerStudio

import Bowler.Library
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.RoundedCylinder

BowlerStudio.speak("Hello World")

CSG cadPart = new RoundedCylinder(20, 60).toCSG()

return cadPart