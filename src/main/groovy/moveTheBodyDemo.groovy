import javax.xml.crypto.dsig.Transform

import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics
import com.neuronrobotics.sdk.addons.kinematics.MobileBase
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import com.neuronrobotics.sdk.common.DeviceManager

/**
 * Description:
 * Move body demo
 */

/**
 * 
 */
// Make sure MediumKat is in Bowler's creature lab.
MobileBase cat = DeviceManager.getSpecificDevice("MediumKat")

DHParameterKinematics leg = cat.getLegs().get(0)

// Set leg to home position by passing 
// 1) taskSpaceTransform and 2) seconds
// taskSpaceTransform = leg.calcHome() as we want it homed and
// seconds = 0 (abrupt)
leg.setDesiredTaskSpaceTransform(leg.calcHome(), 5)

// Define new body transform for SmallKat
// Note 20deg rotation
TransformNR T_deltBody = new TransformNR(0, 0, 0, new RotationNR(0,20,0)).inverse()

//
// Retrieve tip in global space
TransformNR T_tg = leg.getCurrentPoseTarget()
// Transform fiducial to limb
TransformNR T_f_l = leg.getRobotToFiducialTransform()
// Transform from global to fiducial
TransformNR T_g_f = cat.getFiducialToGlobalTransform()

// Calculate inverse offset
// Transform tip in limbspace
TransformNR T_tl = leg.inverseOffset(T_tg)
// Compute
TransformNR T_gdelt = T_g_f.times(T_deltBody)
// Compute new tip
TransformNR T_newtip = T_gdelt.times(T_f_l).times(T_tl)

if (leg.checkTaskSpaceTransform(T_newtip)) {
	leg.setDesiredTaskSpaceTransform(T_newtip, 0)
}