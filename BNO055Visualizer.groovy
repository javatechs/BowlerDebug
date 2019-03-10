import javafx.scene.transform.Affine;
import javafx.application.Platform;

def field = ScriptingEngine.gitScriptRun(
            "https://github.com/WPIRoboticsEngineering/RBELabCustomParts.git", // git location of the library
            "2002/2002 Field/2002 Field STL.STL" , // file to load
            null
            )
            .rotx(-90)
            .toXMin()
            .toYMin()
            .toZMin()
            .movez(-26)
field=field.movex(-field.getTotalX()/2)
		.movey(-field.getTotalY()/2)
// call another script that will create the robot object and return it. 
def myRobot = ScriptingEngine.gitScriptRun(
            "https://gist.github.com/fd1602bce81ca9096db3f28648b3d312.git", // git location of the library
            "LoadIMUServer.groovy" , // file to load
            // Pass the name of the device. This needs to match what was written to the device in Arduino
            ["IMU-Team21"]
            )
// If no robot exists, return and do nothing
if(myRobot==null)
	return;
	
// Create a simple object that will look different when moved to various angles. 
CSG vitaminFromScript = ScriptingEngine.gitScriptRun(
            "https://github.com/WPIRoboticsEngineering/RBELabCustomParts.git", // git location of the library
            "2000RobotAssembled.stl" , // file to load
            null
            ).toZMin()
// add the object to the 3d window. This can also be done by returning the object.
BowlerStudioController.setCsg([vitaminFromScript,field])

// Affine is a frame transformation object used to place objects in the 3d window. 
// Writing new values will trigger a re-render of the screen. 
Affine manip= vitaminFromScript.getManipulator()

// Data vector object, this object will have the latest data written into it by the stack in another thread
double [] printData = myRobot.getImuData()
try{
	// Waiting for the user to hit the stop button
	while(!Thread.interrupted()){
		
		Thread.sleep(20)//UI thread runs at 16ms, we should wait at least that long before updating.
		println System.currentTimeMillis()+"\r\n Acceleration= "+(printData[0])+" , "+(printData[1])+" , "+
						(printData[2])+"\r\n Gyro= "+
						(printData[3])+" , "+
						(printData[4])+" , "+
						(printData[5])+"\r\n Gravity= "+
						(printData[6])+" , "+
						(printData[7])+" , "+
						(printData[8])+"\r\n Euler= "+
						(printData[9])+" , "+
						(printData[10])+" , "+(printData[11])	
		double x=0;
		double y=0;
		double z=0;
		x=printData[12]
		y=printData[13]
		z=printData[14]
		// Conver from Euler angles to frame transformation
		TransformNR newLoc = new TransformNR(x,y,z,new RotationNR(	-printData[9],	-printData[11],	-printData[10]	))
		// copy frame transformation into the object manipulatyor
		// Platform.runlater uses the UI thread to do the write to prevent UI lockup
		Platform.runLater( {TransformFactory.nrToAffine(newLoc,manip)})
	}
}catch(Throwable t){
	t.printStackTrace(System.out)
}
// Disconnect the IMU device on exit
myRobot.disconnect()