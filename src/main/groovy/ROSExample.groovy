@Grab('edu.brown.cs.burlap:java_rosbridge:2.0.0')

import com.fasterxml.jackson.databind.JsonNode;

import ros.Publisher;
import ros.RosBridge;
import ros.RosListenDelegate;
import ros.SubscriptionRequestMsg;
import ros.msgs.std_msgs.PrimitiveMsg;
import ros.tools.MessageUnpacker;
import java.lang.reflect.*;

import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.HttpCookieStore;

//
// CODE: java_rosbridge
//
String host = "ws://localhost:9090";
RosBridge bridge = new RosBridge();
println "Attempt connection to rosbridge websocket"

try {
    bridge.connect(host, true);
    println "Connected to: "+host
} catch (Exception e) {
    println "Exception!"
    println e
}
Publisher pub = new Publisher("/java_to_ros", "std_msgs/String", bridge);
for(int i = 0; i < 10; i++) {
    pub.publish(new PrimitiveMsg<String>("hello from java " + i));
    try {
        Thread.sleep(500);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
    
//bridge.subscribe(SubscriptionRequestMsg.generate("/java_to_ros")
//    .setType("std_msgs/String")
//    .setThrottleRate(1)
//    .setQueueLength(1),
//    new RosListenDelegate() {
//        public void receive(JsonNode data, String stringRep) {
//            MessageUnpacker<PrimitiveMsg<String>> unpacker = new MessageUnpacker<PrimitiveMsg<String>>(PrimitiveMsg.class);
//            PrimitiveMsg<String> msg = unpacker.unpackRosMessage(data);
//            System.out.println(msg.data);
//        }
//    }
//);

println "Connected: "+bridge.hasConnected()
Method[] methods = RosBridge.class.getMethods()
for (int i = 0; i < methods.length; i++) {
	println methods[i].toString()
}

//Thread.sleep(2500);
//bridge.closeConnection()

int x = 1
