import javax.media.bean.playerbean.MediaPlayer

@GrabResolver(name='nr', root='https://oss.sonatype.org/content/repositories/staging/')
//@Grab(group='com.mpatric', module='mp3agic', version='0.9.1')

import java.io.File;
import java.net.URL;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;

import org.apache.commons.io.IOUtils;

import com.neuronrobotics.bowlerstudio.BowlerStudio
import com.neuronrobotics.bowlerstudio.BowlerKernel

import com.neuronrobotics.bowlerstudio.physics.*;
import com.neuronrobotics.bowlerstudio.threed.*;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.util.ThreadUtil;

MobileBase base;

//Check if the device already exists in the device Manager
//if(args==null){
	base=DeviceManager.getSpecificDevice( "MediumKat",{ScriptingEngine.gitScriptRun(	"https://github.com/OperationSmallKat/SmallKat_V2.git", "loadRobot.groovy", [ "https://github.com/javatechs/greycat.git", "MediumKat.xml","GameController_22"] )})
//}else
//	base=args.get(0)


//class jointMovement {
//	public def apendage
//	public int index
//	public double beginAngle
//	public double endAngle
//	int timeMs
//}
class Movement {
	// LEG
	public static final int REAR_LEFT = 0
	public static final int FRONT_RIGHT = 1
	public static final int FRONT_LEFT = 2
	public static final int REAR_RIGHT = 3
	// Leg joints
	public static final int PAN = 0
	public static final int TILT = 1
	public static final int ELBOW = 2
	// HEAD/TAIL
	public static final int HEAD = 4
	public static final int TAIL = 5
	// Joint
	public static final int HeadTail_TILT = 0
	public static final int HeadTail_PAN = 1

	MobileBase base = null
	public DHParameterKinematics fr = null
	public DHParameterKinematics fl = null
	public DHParameterKinematics rr = null
	public DHParameterKinematics rl = null
	public DHParameterKinematics head = null
	public DHParameterKinematics tail = null
	
	public Movement(MobileBase base) {
		this.base = base
		assign()
	}
	public void assign() {
		fr = base.getAllDHChains().get(FRONT_RIGHT)
		fl = base.getAllDHChains().get(FRONT_LEFT)
		rr = base.getAllDHChains().get(REAR_RIGHT)
		rl = base.getAllDHChains().get(REAR_LEFT)
		head = base.getAllDHChains().get(HEAD)
		tail = base.getAllDHChains().get(TAIL)
	}
	public static void angleInterp(DHParameterKinematics apendage, int index, double endAngle, int timeMs) {
		double beginAngle = apendage.getAbstractLink(index).getCurrentPosition()
		angleInterp(apendage, index, beginAngle, endAngle, timeMs)
	}

	public static void angleInterp(DHParameterKinematics apendage, int index, double beginAngle, double endAngle, int timeMs) {
		if (endAngle>beginAngle) {
			for (double angle=beginAngle; angle<=endAngle; angle++) {
				// link index, target angle, wait time in seconds
				apendage.getAbstractLink(index).setPosition(angle)
				ThreadUtil.wait(timeMs)
			}
		}
		else {
			for (double angle=beginAngle; angle>=endAngle; angle--) {
				// link index, target angle, wait time in seconds
				apendage.getAbstractLink(index).setPosition(angle)
				ThreadUtil.wait(timeMs)
			}
		}
	}

	/**
	 * 
	 * @param apendage
	 * @param index
	 * @param endAngle
	 * @param timeMs
	 */
	public static void angleInterpolation(DHParameterKinematics apendage, int index, double endAngle, int timeMs) {
		double beginAngle = apendage.getAbstractLink(index).getCurrentEngineeringUnits()
		angleInterpolation(apendage, index, beginAngle, endAngle, timeMs)
	}

	/**
	 * 
	 * @param apendage
	 * @param index
	 * @param beginAngle
	 * @param endAngle
	 * @param timeMs
	 */
	public static void angleInterpolation(DHParameterKinematics apendage, int index, double beginAngle, double endAngle, int timeMs) {
		if (endAngle>beginAngle) {
			for (double angle=beginAngle; angle<=endAngle; angle++) {
				// link index, target angle, wait time in seconds
				apendage.setDesiredJointAxisValue(index, angle, timeMs/1000)
				ThreadUtil.wait(timeMs)
			}
		}
		else {
			for (double angle=beginAngle; angle>=endAngle; angle--) {
				// link index, target angle, wait time in seconds
				apendage.setDesiredJointAxisValue(index, angle, timeMs/1000)
				ThreadUtil.wait(timeMs)
			}
		}
	}
	
	/**
	 *
	 * @param waitMS
	 * @param iterations
	 */
	public void nod(int waitMS, int iterations) {
		for (int i=0; i<iterations; i++) {
			// Alert
			angleInterpolation(head, HeadTail_TILT, -30, waitMS)
			double curpos = head.getAbstractLink(HeadTail_TILT).getCurrentEngineeringUnits()
			angleInterpolation(head, HeadTail_TILT, curpos, curpos+20, waitMS)
			curpos = head.getAbstractLink(HeadTail_TILT).getCurrentEngineeringUnits()
			angleInterpolation(head, HeadTail_TILT, curpos, 0.0, waitMS)
		}
	}
	/**
	 * 
	 * @param waitMS
	 * @param iterations
	 */
	public void wag(int waitMS, final int iterations) {
		angleInterpolation(tail, HeadTail_TILT, -11, waitMS)
		printLink(tail.getAbstractLink(HeadTail_PAN))
		angleInterpolation(tail, HeadTail_PAN, 0, waitMS)
		printLink(tail.getAbstractLink(HeadTail_PAN))
		for (int i=0; i<iterations; i++) {
			double curpos;
			// Alert
			angleInterpolation(tail, HeadTail_PAN, -20, waitMS)
			curpos = tail.getAbstractLink(HeadTail_PAN).getCurrentEngineeringUnits()
			angleInterpolation(tail, HeadTail_PAN, 20, waitMS)
			angleInterpolation(tail, HeadTail_PAN, 0, waitMS)
		}
		// Pause for user
		ThreadUtil.wait(500)
		angleInterpolation(tail, HeadTail_TILT, 0, 25)
	}
	
	/**
	 * 
	 * @param waitMS
	 */
	public void no(int waitMS) {
		printLink(head.getAbstractLink(HeadTail_TILT))
		angleInterpolation(head, HeadTail_TILT, -20, waitMS)
		printLink(head.getAbstractLink(HeadTail_TILT))
		for (int i=0; i<2; i++) {
			angleInterpolation(head, HeadTail_PAN, 0, waitMS)
			angleInterpolation(head, HeadTail_PAN, -20, waitMS)
			angleInterpolation(head, HeadTail_PAN,  0, waitMS)
			angleInterpolation(head, HeadTail_PAN, 20, waitMS)
			angleInterpolation(head, HeadTail_PAN, 0, waitMS)
		}
		angleInterpolation(head, HeadTail_TILT, 0, waitMS)
		angleInterpolation(head, HeadTail_TILT, 0, waitMS)
		ThreadUtil.wait(waitMS) 	// wait for the link to fully arrive
	}

	/**
	 * 	
	 * @param iterations number of times to shake head
	 * @param waitMS number of milliseconds per movement
	 */
	public void no2(int waitMS, int iterations) {
		int panHome = head.getAbstractLink(HeadTail_PAN).getHome()
		int tiltHome = head.getAbstractLink(HeadTail_TILT).getHome()
		angleInterp(head, HeadTail_TILT, tiltHome+45, waitMS)
		for (int i=0; i<iterations; i++) {
			angleInterp(head, HeadTail_PAN, panHome, waitMS)
			angleInterp(head, HeadTail_PAN, panHome - 30, waitMS)
//			angleInterp(head, HeadTail_PAN, panHome, waitMS)
//			angleInterp(head, HeadTail_PAN, panHome + 30, waitMS)
		}
		angleInterp(head, HeadTail_PAN, panHome, waitMS)
		angleInterp(head, HeadTail_TILT, tiltHome, waitMS)
	}
	/**
	 * 
	 * @param waitMS number of milliseconds per movement
	 */
	public void sit(int waitMS) {	
		// *****
		// HEAD & TAIL
		// *****
//		angleInterpolation(tail, HeadTail_TILT, -15, waitMS)
		angleInterpolation(tail, HeadTail_TILT, 25, waitMS)
		angleInterpolation(head, HeadTail_TILT, -32, waitMS)
		
		// *****
		// REAR
		// *****
		// Spread legs
		// Before
		printLink(rr.getAbstractLink(PAN))
	
		angleInterpolation(rr, PAN, 0, 22, waitMS)
		angleInterpolation(rl, PAN, 0, -22, waitMS)
	
		// After
		printLink(rr.getAbstractLink(PAN))
	
		// Fold tilt
		angleInterpolation(rr, TILT, -5, waitMS)
		angleInterpolation(rl, TILT, -5, waitMS)
		angleInterpolation(rr, TILT, -10, waitMS)
		angleInterpolation(rl, TILT, -10, waitMS)
	
		// Fold elbow
		angleInterpolation(rr, ELBOW, 15, waitMS)
		angleInterpolation(rl, ELBOW, 15, waitMS)
		angleInterpolation(rr, ELBOW, 30, waitMS)
		angleInterpolation(rl, ELBOW, 30, waitMS)
	
		// *****
		// FRONT
		// *****
		// Fold tilt
		angleInterpolation(fr, TILT, 0, 20, waitMS)
		angleInterpolation(fl, TILT, 0, 20, waitMS)
	
		// Fold elbow
		angleInterpolation(fr, ELBOW, 0, -10, waitMS)
		angleInterpolation(fl, ELBOW, 0, -10, waitMS)
	
	//	nod(head)
		
		// Pause for user
		ThreadUtil.wait(1500)
	
		angleInterpolation(fr, PAN, 22, waitMS)
		angleInterpolation(fl, PAN, -22, waitMS)
		// Reset
		angleInterpolation(fr, ELBOW, 0, waitMS)
		angleInterpolation(fl, ELBOW, 0, waitMS)
		angleInterpolation(fr, TILT, 0, waitMS)
		angleInterpolation(fl, TILT, 0, waitMS)
		angleInterpolation(rr, ELBOW, 0, waitMS)
		angleInterpolation(rl, ELBOW, 0, waitMS)
		angleInterpolation(rl, TILT, 0, waitMS)
		angleInterpolation(rr, TILT, 0, waitMS)
		angleInterpolation(rl, PAN, 0, waitMS)
		angleInterpolation(rr, PAN, 0, waitMS)
		angleInterpolation(fl, PAN, 0, waitMS)
		angleInterpolation(fr, PAN, 0, waitMS)
		for (int i=0;i<head.getNumberOfLinks();i++) {
			angleInterpolation(head, i, head.getCurrentJointSpaceVector()[i], 0, waitMS)
			angleInterpolation(tail, i, tail.getCurrentJointSpaceVector()[i], 0, waitMS)
		}
	}
	public void shakeit(DHParameterKinematics leg) {
		int waitMS = 10
		int angle = 20
		angleInterpolation(leg, ELBOW, -10, waitMS)
		angleInterpolation(leg, TILT, 20, waitMS)
		angleInterpolation(leg, ELBOW, 0, waitMS)
		angleInterpolation(leg, TILT, 0, waitMS)
	}
	public void stampFoot(DHParameterKinematics leg) {
		// Paw
		int angle = 10
		int waitMS = 5
		//
		angleInterpolation(leg, TILT,-angle/2,waitMS)
		angleInterpolation(leg, ELBOW,angle, waitMS)
		//
		angleInterpolation(leg, TILT,-angle, waitMS)
		angleInterpolation(leg, ELBOW,angle*1.5,waitMS)

		// RESET
		angleInterpolation(leg, TILT,0, waitMS)
		angleInterpolation(leg, ELBOW,0,waitMS)
	}
	/**
	 * 
	 */
	public void printLink(AbstractLink link) {
		println "Name: " + link.getLinkConfiguration().getName()
		println "Upper pan limit: " + link.getUpperLimit()
		println "Lower pan limit: " + link.getLowerLimit()
		println "Offset/home: " + link.getHome()
		println "Current pos: " + link.getCurrentPosition()
		println "Current engr units: " + link.getCurrentEngineeringUnits()
		println ""
	}
	/**
	 * 
	 * @param leg
	 * @return
	 */
	def convertStrToLeg(String leg) {
		def retVal = null;
		if ("rearleft".equals(leg)) {
			retVal = rl;
		}
		else if ("rearright".equals(leg)) {
			retVal = rr;
		}
		else if ("frontleft".equals(leg)) {
			retVal = fl;
		}
		else if ("frontright".equals(leg)) {
			retVal = fr;
		}
		return retVal;
	}
	public void playFile(String fileStr) {
		AudioInputStream audioIn;
		boolean isLocalFile = fileStr.startsWith("file://");
		if (isLocalFile) {
			File fl = new File(fileStr);
			audioIn = AudioSystem.getAudioInputStream(fl);
		}
		else {
			URL url = new URL(fileStr);
			audioIn = AudioSystem.getAudioInputStream(url);
		}
		Clip clip = AudioSystem.getClip();
		clip.open(audioIn);
		clip.start();
	}
}

/**
 * @param tokens
 * @param pos
 * @param _default
 */
def parseInt(String[] tokens, int pos, int _default) {
	int retVal = _default;
	if (tokens.length>pos) {
		retVal = Integer.parseInt(tokens[pos]);
	}
	return retVal;
}

// Do something!
Movement movement = new Movement(base)
println "Now move some links"
if(args==null) {
	BowlerStudio.speak("Action")
//	Typical
//	movement.no2(10, 15)
//	BowlerKernel.speak("Watch me sit")
//	movement.no(20)
//	movement.no2(10, 15)
//	movement.wag(10, 5)
//	movement.nod(12)
//	movement.sit(10);
//	movement.shakeit(movement.rr)
//	movement.stampFoot(movement.fl)
	return null;
} else {
	// Argument format
	// e.g. nod/2/2,
//	args.size()
//	println args
	for (int i=0; i<args.size(); i++) {
		tokens = args[i].split("/");
		if ("no".equals(tokens[0])) {
			waitMS = parseInt(tokens, 1, 15);
			movement.no(waitMS);
		}
		else if ("nod".equals(tokens[0])) {
			waitMS = parseInt(tokens, 1, 15);
			iterations = parseInt(tokens, 2, 1);
			movement.nod(waitMS, iterations);
		}
		else if ("sit".equals(tokens[0])) {
			waitMS = parseInt(tokens, 1, 20);
			movement.sit(waitMS);
		}
		else if ("wag".equals(tokens[0])) {
			waitMS = parseInt(tokens, 1, 15);
			iterations = parseInt(tokens, 2, 1);
			movement.wag(waitMS, iterations);
		}
		else if ("speak".equals(tokens[0])) {
			text = "Meow!"
			if (tokens.length>1) {
				text = tokens[1];
			}
			BowlerKernel.speak(text)
		}
		else if ("playaudio".equals(tokens[0])) {
			if (tokens.length>1) {
				movement.playFile(tokens[1])
			}
		}
		else if ("shakeit".equals(tokens[0])) {
			println "Got Shakeit"
			if (tokens.length>1) {
				leg = movement.convertStrToLeg(tokens[1])
				if (null!=leg) movement.shakeit(leg);
			}
		}
		else if ("stomp".equals(tokens[0])) {
			if (tokens.length>1) {
				leg = movement.convertStrToLeg(tokens[1])
				if (null!=leg) movement.stampFoot(leg);
			}
		}
	}
}