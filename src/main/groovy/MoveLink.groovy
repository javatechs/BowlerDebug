import org.apache.commons.io.IOUtils;
import  com.neuronrobotics.bowlerstudio.physics.*;
import com.neuronrobotics.bowlerstudio.threed.*;
def base;

int waitTimeMs = 500
double waitTimeS = 0.50
//Check if the device already exists in the device Manager
if(args==null){
	base=DeviceManager.getSpecificDevice( "MediumKat",{ScriptingEngine.gitScriptRun(	"https://github.com/OperationSmallKat/SmallKat_V2.git", "loadRobot.groovy", [ "https://github.com/OperationSmallKat/greycat.git", "MediumKat.xml","GameController_22"] )})
}else
	base=args.get(0)
DHParameterKinematics leg0 = base.getAllDHChains().get(1)
println "Now move just one link"
for(int i=0;i<leg0.getNumberOfLinks();i++){
	leg0.setDesiredJointAxisValue(i,// link index
							15, //target angle
							waitTimeS) // 2 seconds
	ThreadUtil.wait(waitTimeMs)// wait for the link to fully arrive
	leg0.setDesiredJointAxisValue(i,// link index
							0, //target angle
							waitTimeS) // 2 seconds
	ThreadUtil.wait(waitTimeMs)// wait for the link to fully arrive
}
return null;