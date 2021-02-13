@GrabResolver(name='nr', root='https://oss.sonatype.org/content/repositories/staging/')
@Grab(group='com.neuronrobotics', module='SimplePacketComsJava', version='0.12.0')
@Grab('edu.brown.cs.burlap:java_rosbridge:2.0.0')
@Grab('org.eclipse.jetty:jetty-server:9.4.36.v20210114')
@Grab('org.eclipse.jetty:jetty-servlet:9.4.36.v20210114')
@Grab('org.eclipse.jetty:jetty-servlets:9.4.36.v20210114')
@Grab('org.eclipse.jetty:jetty-webapp:9.4.36.v20210114')
@Grab('org.eclipse.jetty.websocket:websocket-client:9.4.36.v20210114')

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
import com.neuronrobotics.sdk.common.DeviceManager;

import edu.wpi.SimplePacketComs.AbstractSimpleComsDevice;
import edu.wpi.SimplePacketComs.PacketType;
import edu.wpi.SimplePacketComs.BytePacketType;
import edu.wpi.SimplePacketComs.device.gameController.GameController;
import edu.wpi.SimplePacketComs.device.UdpDevice;
import edu.wpi.SimplePacketComs.phy.UDPSimplePacketComs;

import com.fasterxml.jackson.databind.JsonNode;
import ros.Publisher;
import ros.RosBridge;
import ros.RosListenDelegate;
import ros.SubscriptionRequestMsg;
import ros.msgs.std_msgs.PrimitiveMsg;
import ros.tools.MessageUnpacker;

import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

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

println "Device: "+device
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

def packetType = device.getPacket(1985)
if (null==packetType) {
	println "Didn't find an existing polling packet"
	try {
		packetType = new BytePacketType(1985, 64);
		device.addPollingPacket(packetType);
		println "Added a polling packet."
	} catch (Exception ex) {
		println "Exception thrown while attempting to add a polling packet."
		BowlerStudio.printStackTrace(ex)
	}
}
else {
	println "Polling packet existed."
}
//
//	Add event handler for polling packet
//		First remove existing event handlers
//		Add new event handler for this ID
//

// Remove old event handlers for our ID
device.removeAllEvents(1985)
//
// Execute command
//
def execCmd(String cmd) {
	ArrayList<String> progArgs = new ArrayList<>();

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
}
//
Runnable eventHandler = new Runnable() {
	byte[] status = new byte[64];
	byte[] data = new byte[64];
	byte curSeqNum = 0x01;
	String cmd = "";

	public void run() {
//		device.readBytes(1985, data);
		data = device.readBytes(1985);
		// Get msg seq #
		byte msgSeqNum = data[0];
		boolean isMulti = (data[1]==1);
		boolean gotNewCommand = (curSeqNum == msgSeqNum)
		//
//		println "msgSeqNum: " + (int)(msgSeqNum&0xff) + "; curSeqNum: " + (int)(curSeqNum&0xff)
//		println data
		if (gotNewCommand) {
			String[] tokens;
			String url;
			String prog; // = "src/main/groovy/DialogExample.groovy";

			// Append command
			String part = new String(data, 2, data.length-2);
			part = part.trim();
			println "cmd: " + cmd
			println "part: "+ part
			cmd = cmd + part;
			println "cmd+part: " + cmd + " multi: "+ isMulti
			if (  (!isMulti)
			   && (cmd!=null)
			   && (cmd.length()>0)
			   ) {
				// Execute command
				execCmd(cmd)
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
		// Write bytes
		device.writeBytes(1985, status);
//		Thread.sleep(1000);
	}
}
// Add event handler for this ID
device.addEvent(1985, eventHandler);

//
def cat = ScriptingEngine.gitScriptRun(
	"https://github.com/OperationSmallKat/SmallKat_V2.git", 
	"loadRobot.groovy",
        [
			"https://github.com/javatechs/greycat.git"
			,"MediumKat.xml"
			,device.getName()
			,"hidDevice"
		]
	);

// --------------------------------------------------
// ROSBridge section
// --------------------------------------------------
@WebSocket
class RosBridgePlus extends RosBridge {
	public boolean isOpen() {
		boolean retVal = false;
		if (null!=session) {
			retVal = session.isOpen();
		}
		return retVal;
	};
}
//
// Open rosbridge connection
//
String host = "ws://localhost:9090";
//RosBridge bridge = new RosBridge();
RosBridgePlus bridge = new RosBridgePlus();
println "Attempt connection to rosbridge websocket"

try {
	bridge.connect(host, true);
	println "Connected to: "+host
} catch (Exception e) {
	println "Exception!"
	println e
}

//
// Read properties file
//
Properties prop = new Properties();

// Note! Select local or internet file name. Modify as desired.
// Form network file name
String fileName = "https://raw.githubusercontent.com/javatechs/BowlerDebug/master/src/main/groovy/movement.conf"
// Form local file name
userHome = System.getProperty("user.home");
fileName = "file://"+userHome+"/Documents/workspace/BowlerDebug/src/main/groovy/movement.conf"
println("File name: "+fileName)
// Open properties file
URL url = new URL(fileName);
InputStream input = url.openStream();
// load properties file
prop.load(input);

//
// Subscribe to remo command topic
//
// TODO Add connection keep alive
bridge.subscribe(SubscriptionRequestMsg.generate("/remo_cmd")
	.setType("std_msgs/String")
	.setThrottleRate(1)
	.setQueueLength(1),
	new RosListenDelegate() {
		public void receive(JsonNode data, String stringRep) {
			MessageUnpacker<PrimitiveMsg<String>> unpacker = new MessageUnpacker<PrimitiveMsg<String>>(PrimitiveMsg.class);
			PrimitiveMsg<String> msg = unpacker.unpackRosMessage(data);
			// Get command name from message data
			String str = msg.data;
			// Replace:
			//  ' with "
			str = str.replace("'","\"");
			//  True with "True"
			str = str.replace("True","\"True\"");
			//  False with "False"
			str = str.replace("False","\"False\"");
			//  None with "None"
			str = str.replace("None","\"None\"");
//			println "mod:"+str
			JSONObject jo = new JSONParser().parse(str);
			jo = jo.get("button");
			String cmd_name = jo.get("command");
			// Using command name find command in properties
			String cmd = prop.getProperty(cmd_name);
			// Execute command
			if (  (null!=cmd)
			   && (cmd.length()>0)
			   ) {
				execCmd(cmd);
			}
			else {
				println "No or empty command found."
			}
		}
	}
);

class KeepAlive extends Thread {
	RosBridgePlus bridge;
	String host;
	String pingTopic = "/rosbridge_bowler_keepalive";
	int intervalMS = 30000;
	// 
	public KeepAlive (RosBridgePlus inpBridge, String inpHost) {
		super();
		this.bridge = inpBridge;
		this.host = inpHost;
	}
	public void run(){
		while (true) {
//			Thread.sleep(intervalMS);
			// Is connection alive?
			if (!bridge.isOpen()) {
				// If connection isn't alive, reconnect
				println "Reconnecting to rosbridge."
				try {
					bridge.connect(host);
				} catch (Exception e) {
					println ("Could not connect to rosbridge.")
					BowlerStudio.printStackTrace(e);
				}
			}
			// Ping rosbridge
			pingSettings();
		}
	}
	protected void pingSettings() {
		println "ping!"
		bridge.subscribe(SubscriptionRequestMsg.generate(pingTopic)
			.setType("std_msgs/String")
			.setThrottleRate(1)
			.setQueueLength(1),
			new RosListenDelegate() {
				public void receive(JsonNode data, String stringRep) {
					MessageUnpacker<PrimitiveMsg<String>> unpacker = new MessageUnpacker<PrimitiveMsg<String>>(PrimitiveMsg.class);
					PrimitiveMsg<String> msg = unpacker.unpackRosMessage(data);
					// Grab settings if desired.
					println "pong"
				}
			}
		);
		Thread.sleep(intervalMS);
		// And drop
		bridge.unsubscribe(pingTopic);
	}
}
KeepAlive keepAlive = new KeepAlive(bridge, host);
keepAlive.start()

//
// Controller handler loop
//
double toSeconds=0.1

println "Starting controller loop..."
while (!Thread.interrupted() ){
	Thread.sleep((long)(toSeconds*1000))
	byte[] data = device.readBytes(1970);//device.getData()

	double xdata = data[4]
	double rzdata = data[3]
	double rxdata = data[1]
	double rydata = data[2]
	//
	if(xdata<0)
		xdata+=256
	if(rzdata<0)
		rzdata+=256
	if(rxdata<0)
		rxdata+=256
	if(rydata<0)7
		rydata+=256

	double scale = 0.15
	double displacement = 40*(scale*(xdata/255.0)-scale/2)
	double displacementY =-10*(scale*(rxdata/255.0)-scale/2)
	
	double rot =((scale*2.0*rzdata/255.0)-scale)*-2.5
	double rotx =((rxdata/255.0)-scale/2)*5
	double roty =((rydata/255.0)-scale/2)*-5
	if(Math.abs(displacement)<0.1 ){
		displacement=0
	}
	if( Math.abs(rot)<0.1){
		rot=0
	}
	try{
		if(Math.abs(displacement)>0.16 || Math.abs(rot)>0.16 ||Math.abs(displacementY)>0.16  ){
			println "displacement "+displacement+" rot "+rot+" straif = "+displacementY
//			println data
			
			TransformNR move = new TransformNR(displacement,displacementY,0,new RotationNR(rotx,rot,roty))
			cat.DriveArc(move, toSeconds);
		}
		if(Math.abs(rotx)>2 || Math.abs(roty)>2){
			//println "tilt "+rotx+" rot "+roty
			TransformNR move = new TransformNR(displacement,displacementY,0,new RotationNR(rotx,0,roty))
			//cat.getWalkingDriveEngine().pose(move)
		}
	}catch(Throwable t){		
		BowlerStudio.printStackTrace(t)
	}
}
return device
