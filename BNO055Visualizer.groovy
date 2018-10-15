import javafx.scene.transform.Affine;
import javafx.application.Platform;

def myRobot = ScriptingEngine.gitScriptRun(
            "https://gist.github.com/fd1602bce81ca9096db3f28648b3d312.git", // git location of the library
            "LoadIMUServer.groovy" , // file to load
            // Parameters passed to the funcetion
            ["IMU-Team21"]
            )
if(myRobot==null)
	return;

CSG vitaminFromScript = Vitamins.get("hobbyServo","Dynam");
BowlerStudioController.setCsg([vitaminFromScript])
Affine manip= vitaminFromScript.getManipulator()

double [] printData = myRobot.getImuData()
try{
	while(!Thread.interrupted()){
		Thread.sleep(20)
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
		//x=-printData[0]
		//y=-printData[1]
		//z=-printData[2]
		TransformNR newLoc = new TransformNR(x,y,z,new RotationNR(	printData[9],	-printData[11],	printData[10]	))
				Platform.runLater( {
							TransformFactory.nrToAffine(newLoc,manip);
						}
					);
	}
}catch(Throwable t){
	t.printStackTrace(System.out)
}

myRobot.disconnect()