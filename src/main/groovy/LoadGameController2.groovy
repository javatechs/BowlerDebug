@GrabResolver(name='nr', root='https://oss.sonatype.org/content/repositories/staging/')
@Grab(group='com.neuronrobotics', module='SimplePacketComsJava', version='0.10.1')


import com.neuronrobotics.sdk.addons.kinematics.imu.*;
import com.neuronrobotics.sdk.common.*;

import edu.wpi.SimplePacketComs.*;
import edu.wpi.SimplePacketComs.phy.*;
import edu.wpi.SimplePacketComs.*;
import edu.wpi.SimplePacketComs.phy.UDPSimplePacketComs;
import edu.wpi.SimplePacketComs.device.gameController.*;
import edu.wpi.SimplePacketComs.device.gameController.GameController;
import edu.wpi.SimplePacketComs.device.*

import edu.wpi.SimplePacketComs.device.UdpDevice;
import edu.wpi.SimplePacketComs.phy.UDPSimplePacketComs;
import edu.wpi.SimplePacketComs.AbstractSimpleComsDevice;
import edu.wpi.SimplePacketComs.PacketType;
import edu.wpi.SimplePacketComs.BytePacketType;
import edu.wpi.SimplePacketComs.FloatPacketType;


if(args == null) {
	args = ["Game*"]
}

String name=args[0]
if(name.contains("*")) {
	name = name.split("\\*")[0];
}

println "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"

def dev = DeviceManager.getSpecificDevice(name, {
	try{
		println "Called DeviceManager.getSpecificDevice..."

		def control =  GameController.get(args[0]).toArray()[0]
		println "Got past GameController.get..."
		if(control==null)
			return null
		control.connect()
		int i=0;
		while(control.getName().getBytes().size()==0 && i++<10){
			println "Waiting for device name..."
			Thread.sleep(500)// wait for the name packet to be sent
			//String n = control.getName();
		}
		String n = control.getName();
		println "Device named ="+n.getBytes()+" " + n

		return control;
	}catch(Throwable t){
		return null;
	}
});

println "Returning devicer: "+dev
if (null!=dev) {
	println " Name: "+dev.getName();
}
println ""

//
//
//
def havePacket = dev.getPacket(1985)
if (null==havePacket) {
	println "Didn't find an existing polling packet"
	try {
		dev.addPollingPacket(new BytePacketType(1985, 20))
		println "Added a polling packet."
	} catch (Exception ex) {
		println "Exception thrown while attempting to add a polling packet."
	}
}
else {
	println "Polling packet existed."
}
dev.removeAllEvents(1985)
dev.addEvent(1985, new Runnable() {
	byte[] status = new byte[64];
	byte[] data = new byte[64];
	
	public void run() {
		print "## "
//		dev.getControllerIndex()
//		dev.readBytes(1985, data);
//		dev.writeBytes(1985, status);
		// TODO What does oneshot do?
	}
});

println "Starting controller loop..."
double toSeconds=1.0//100 ms for each increment
int i = 0;
while (!Thread.interrupted() ) {
	i++;
	Thread.sleep((long)(toSeconds*1000))
	data = dev.getData()
	
	double xdata = data[3]
	double rzdata = data[2]
	double rxdata = data[0]
	double rydata = data[1]
	println "!!"+i+"!!"+data[0] + ":"+data[1] + ":"+data[2] + ":"+data[3]
//	if (i>=100) {
//		dev.removeAllEvents(1985)
//	}
}
return dev