@GrabResolver(name='sonatype', root='https://oss.sonatype.org/content/repositories/releases/')
@Grab(group='com.neuronrobotics', module='SimplePacketComsJava', version='0.1.6')

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.wpi.SimplePacketComs.*;
import edu.wpi.SimplePacketComs.PacketType;
import edu.wpi.SimplePacketComs.device.UdpDevice;
import edu.wpi.SimplePacketComs.device.gameController.GameController;
import edu.wpi.SimplePacketComs.phy.UDPSimplePacketComs;
import javafx.scene.transform.Affine;

import javafx.application.Platform;


public class IMUData extends UDPSimplePacketComs {
    private def IMU = new FloatPacketType(1804, 64);
    double[] data = new double[15];
    public IMUData(InetAddress add) throws Exception {
        super(add);
        addPollingPacket(IMU);
        addEvent(1804,{
            readFloats(1804, data);
        });
    }
 
}
public class IMUDevice extends NonBowlerDevice{
	IMUData simple;
	public IMUDevice(IMUData s){
		simple = s
		setScriptingName("ImuDevice")
	}
	@Override
	public  void disconnectDeviceImp(){		
		simple.disconnect()
		println "UDP device Termination signel shutdown"
	}
	
	@Override
	public  boolean connectDeviceImp(){
		simple.connect()
	}

	public double[] getImuData() {
		return simple.data;
	}
	@Override
	public  ArrayList<String>  getNamespacesImp(){
		// no namespaces on dummy
		return [];
	}
	
	
}

def myRobot = DeviceManager.getSpecificDevice( "ImuDevice",{
	//If the device does not exist, prompt for the connection
	HashSet<InetAddress> addresses = UDPSimplePacketComs.getAllAddresses("IMU-Team21 ");
	if (addresses.size() < 1) {
	  System.out.println("No IMU controllers found named ");
	  return null;
	}
	for (InetAddress add : addresses) {
	  System.out.println("Got " + add.getHostAddress());
	  def e = new IMUDevice(new IMUData(add));
	  println "Connecting new device: "+e
	  e.connect()
	  return e
	}
	return null
	
})
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
}catch(Throwable t){}

myRobot.disconnect()