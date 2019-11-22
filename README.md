# Using an Eclipse Project to Debug Bowler Studio code
This project was created to:
- Debug Bowler Studio code and libraries.
- Contain groovy examples

## Example: Running Bowler Studio in debug
1. First create a debug configuration.
    - Select the project from project window in Eclipse.
    - From the 'Run' menu item, select 'Debug Configurations'
    - Select Java Application then press New Launch Configuration. Fill out the dialog:
        - Name the launch configuration: BowlerDebug.
        - Press browse and select BowlerDebug to select project.
        - Main class: press search and select BowlerStudio, click OK then apply.
1. Then select the launch configuration and press debug.
1. Add BowlerDebug to your BowlerStudio workspace.

## Example: [BowlerSpeaks.groovy](src/main/groovy/BowlerSpeaks.groovy)
Simple demonstration of speach synthesis with Bowler Studio.

1. To see how it works, in Bowler Studio, select the BowlerDebug file 
BowlerSpeaks.groovy. 
1. When the script appears, press run.

## Example: [DialogExample.groovy](src/main/groovy/DialogExample.groovy)
This is an example of a simple Groovy dialog in Bowler Studio.

It includes an call to java code in 'ExampleLibrary' class.

1. in Bowler Studio, select the BowlerDebug file DialogExample.groovy. 
1. When the script appears, press run.
1. For extra credit, try setting a break point in 
   ExampleLibrary's someLibraryMethod() and rerun the script.

## Example: [BNO055Visualizer.groovy](src/main/groovy/BNO055Visualizer.groovy)
Visualize the BNO055 IMU using SimplePacketComs over UDP.

- The BNO055 IMU must be connected to the robot's ESP32 i2c port!
- You might want to try this out using [ESP32ServoServer](https://github.com/madhephaestus/ESP32ServoServer) 
  running on the robot.
- Alternately, install BNO055SimplePacketComs Arduino library. 
    1. Run the example from BNO055SimplePacketComs library called BNO055Server. 
    1. Run BNO055Visualizer.groovy in Bowler Studio to visualize the IMU data.
