use "Object3D" 

type KeyframeSequence < Object3D;

const INTERPOLATION_LINEAR = 176;
const INTERPOLATION_SLERP = 177;
const INTERPOLATION_SPLINE = 178;
const INTERPOLATION_SQUAD = 179;
const INTERPOLATION_STEP = 180;

const MODE_CONSTANT = 192;
const MODE_LOOP = 193;

def KeyframeSequence.new(numKeyframes: Int, numComponents: Int, interpolation: Int);
def KeyframeSequence.getComponentCount(): Int;
def KeyframeSequence.getKeyframeCount(): Int;
def KeyframeSequence.getInterpolationType(): Int;
def KeyframeSequence.setKeyframe(index: Int, time: Int, value: [Float]);
def KeyframeSequence.getKeyframe(index: Int, value: [Float]);
def KeyframeSequence.setValidRange(first: Int, last: Int);
def KeyframeSequence.getValidRangeFirst(): Int;
def KeyframeSequence.getValidRangeLast(): Int;
def KeyframeSequence.setDuration(duration: Int);
def KeyframeSequence.getDuration(): Int;
def KeyframeSequence.setRepeatMode(mode: Int);
def KeyframeSequence.getRepeatMode(): Int;
