@GrabResolver(name='nr', root='https://oss.sonatype.org/content/repositories/staging/')
@Grab(group='com.neuronrobotics', module='SimplePacketComsJava', version='0.12.0')

/**
 * Client Android Game Controller.
 * - Connects to the game controller.
 * - Adds a polling packet for command id 1985.
 * - Next adds an event handler, a Runnable class to service command id 1985.
 * - Retrieves game controller's command id 1970.
 * TODO should the above feature be removed?
 */

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.addons.kinematics.imu.*;
import com.neuronrobotics.sdk.common.*;

import edu.wpi.SimplePacketComs.AbstractSimpleComsDevice;
import edu.wpi.SimplePacketComs.PacketType;
import edu.wpi.SimplePacketComs.BytePacketType;
import edu.wpi.SimplePacketComs.FloatPacketType;
import edu.wpi.SimplePacketComs.device.gameController.GameController;
import edu.wpi.SimplePacketComs.device.UdpDevice;
import edu.wpi.SimplePacketComs.phy.UDPSimplePacketComs;


if(args == null) {
	args = ["Game*"]
}

String name=args[0]
if(name.contains("*")) {
	name = name.split("\\*")[0];
}

println "Running LoadGameController"

def device = DeviceManager.getSpecificDevice(name, {
	try{
		println "Called DeviceManager.getSpecificDevice..."

		GameController control =  GameController.get(args[0]).toArray()[0]
		println "Got past GameController.get..."
		if(control==null)
			return null
		control.connect()
		int i=0;
		while(control.getName().getBytes().size()==0 && i++<10){
			println "Waiting for device name..."
			Thread.sleep(500)// wait for the name packet to be sent
		}
		String n = control.getName();
		println "Device named ="+n.getBytes()+" " + n

		return control;
	}catch(Throwable t){
		return null;
	}
});

println "Returning device: "+device
if (null!=device) {
	println " Name: "+device.getName();
}
println ""

//
//	Adding a polling packet to the device 
//  creates a data path from the client to the device.
//	
//	If polling packet for this ID doesn't exist
//		Create packet for this ID
//		Modify packet object before we need BEFORE activating it
//		by adding the polling packet to the device
//		- Set packet to waitToSend (if desired)
//		NOW add polling packet to device
//
//def packetType2 = device.getPacket(1970)
//packetType2.waitToSendMode();

def packetType = device.getPacket(1985)
if (null==packetType) {
	println "Didn't find an existing polling packet"
	try {
		packetType = new BytePacketType(1985, 64);
		// Set the packet to waitToSend,
		// before adding a polling packet
//		packetType.waitToSendMode();
		device.addPollingPacket(packetType);
		println "Added a polling packet."
	} catch (Exception ex) {
		println "Exception thrown while attempting to add a polling packet."
	}
}
else {
	println "Polling packet existed."
}
// Set initial packet
def num = packetType.getDownstream();
num[0]=253;
packetType.setDownstream(num);
//
//	Add event handler for polling packet
//		First remove existing event handlers
//		Add new event handler for this ID
//

// Remove old event handlers for our ID
//ArrayList<Runnable> arr;
//arr = device.getEvents((int)1985);
//println arr.size()
String nm;
nm = device.getName();
device.isTimedOut()
println "nm: "+nm
device.removeAllEvents(1985)
//arr = device.getEvents((int)1985);
//println arr.size()
nm= device.getName();
device.isTimedOut()
println "nm: "+nm

Runnable eventHandler = new Runnable() {
	byte[] status = new byte[64];
	byte[] data = new byte[64];
	byte curSeqNum = 0xFD;
	String cmd = "";
	String part = "";
	boolean doit = true;

	public void run() {
//		// Indicate run() called
//		println "."
//		device.readBytes(1985, data);
		data = device.readBytes(1985);
		// Get msg seq #
		byte msgSeqNum = data[0];
		boolean isMulti = (data[1]==1);
		boolean gotNewCommand = (curSeqNum == msgSeqNum)
		//
		println "msgSeqNum: " + (int)(msgSeqNum&0xff) + "; curSeqNum: " + (int)(curSeqNum&0xff)
		println data
//		Thread.sleep(3000);
		if (gotNewCommand) {
			String[] tokens;
			String url;
			String prog; // = "src/main/groovy/DialogExample.groovy";
			ArrayList<String> progArgs = new ArrayList<>();
			// Append command
			String part = new String(data, 2, data.length-2);
			part = part.trim();
			println "cmd: " + cmd
			println "part: "+ part
			cmd = cmd + part;
			println "cmd+part: " + cmd
			if (  (!isMulti)
			   && (cmd!=null)
			   && (cmd.length()>0)
			   ) {
				// Parse and run
				tokens = cmd.split(",");
				println "tokens"+tokens
				if (tokens.length>0) {
					url = tokens[0];
					println "  URL: " + url;
				}
				// Create args
				boolean isLocalFile = url.startsWith("file://");
				int argsStart = ((isLocalFile)?1:2);
				if (tokens.length>argsStart) {
					progArgs = Arrays.copyOfRange(tokens, argsStart, tokens.length);
				}
				println "\n  progArgs: " + progArgs
				if (tokens.length>1) {
					if (!isLocalFile) {
						prog = tokens[1];
						println "  prog: " + tokens[1];
					}
					// Run script
					try {
//						Thread.sleep(3000);
						def retVal;
						if (!isLocalFile) {
							retVal = ScriptingEngine.gitScriptRun(url, prog, progArgs);
						}
						else {
							// e.g. "file:///home/user/somefile.groovy"
							println "Local file name: "+url.substring(7)
							File file = new File(url.substring(7));
							retVal = ScriptingEngine.inlineFileScriptRun(file, progArgs);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				else {
					println "No program. Not enough parameters. Count = " + tokens.length
				}
				// Clear accumulated command string
				cmd = "";
				curSeqNum++;
				println "Increment curSeqNum";
			}
			if (cmd.length()>0) {
				curSeqNum++;
				println "Increment curSeqNum";
			}
			println "curSeqNum: " + (int)(curSeqNum&0xff)
		}
		// Set sequence # to be parsed
		status[0] = curSeqNum;
		if (isMulti&&doit) {
			doit = false;
			sleep(1000)
			println "2nd oneshot"
//			device.getPacket(1985).oneShotMode();
		}
		// Write bytes
		device.writeBytes(1985, status);
//		Thread.sleep(3000);
//		println "end of loop"
	}
}
// Add event handler for this ID
device.addEvent(1985, eventHandler);

//
// Controller loop
//
println "Starting controller loop..."
double sleepSeconds=1.0 	//	Converted to ms
int iteration = 0;				//	Loop counter

while (!Thread.interrupted() ) {
	iteration++;
	Thread.sleep((long)(sleepSeconds*1000))
	data = device.getData()

	double xdata = data[3]
	double rzdata = data[2]
	double rxdata = data[0]
	double rydata = data[1]
//	println "!!"+iteration+"!!"+data[0] + ":"+data[1] + ":"+data[2] + ":"+data[3]
	if (iteration==3) {
		println "Setting one shot"
//		device.getPacket(1985).oneShotMode();
	}
	// Remove event test
//	if (iteration==2) {
//		device.removeAllEvents(1985)
//	}
}

return device