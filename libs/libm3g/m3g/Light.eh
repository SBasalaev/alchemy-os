use "Node" 

type Light < Node;

const LIGHT_AMBIENT = 128;
const LIGHT_DIRECTIONAL = 129;
const LIGHT_OMNI = 130;
const LIGHT_SPOT = 131;

def Light.new();
def Light.setMode(mode: Int);
def Light.getMode(): Int;
def Light.setIntensity(intensity: Float);
def Light.getIntensity(): Float;
def Light.setColor(rgb: Int);
def Light.getColor(): Int;
def Light.setSpotAngle(angle: Float);
def Light.getSpotAngle(): Float;
def Light.setSpotExponent(exponent: Float);
def Light.getSpotExponent(): Float;
def Light.setAttenuation(constant: Float, linear: Float, quadratic: Float);
def Light.getConstantAttenuation(): Float;
def Light.getLinearAttenuation(): Float;
def Light.getQuadraticAttenuation(): Float;
