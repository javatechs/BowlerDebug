# LoadGameController2.groovy
**IMPORTANT NOTE!** <span style="color:red">The current software **requires** the 
[Android game controller app](https://github.com/javatechs/BStudioGameController)</span>.
A future revision will eliminate this requirement.

## Installs and Builds
- Install [remo.tv controller software with rosbridge for 'hardware'](https://github.com/javatechs/controller/blob/master/hardware/rosbridge.md).
Be sure to use the small_kat.json button files.
- Install [Bowler Studio](http://neuronrobotics.com/#downloads)

## [movement.conf](https://github.com/javatechs/BowlerDebug/blob/master/src/main/groovy/movement.conf)
The file **movement.conf** maps remo.tv button command to 
either a movement.groovy command 
or a MobileBase.DriveArc(...) controlled movement.