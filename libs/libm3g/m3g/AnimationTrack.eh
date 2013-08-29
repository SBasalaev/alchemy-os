use "Object3D"
 
type AnimationTrack < Object3D;

type KeyframeSequence;
type AnimationController;

const AT_ALPHA = 256
const AT_AMBIENT_COLOR = 257
const AT_COLOR = 258
const AT_CROP = 259
const AT_DENSITY = 260
const AT_DIFFUSE_COLOR = 261
const AT_EMISSIVE_COLOR = 262
const AT_FAR_DISTANCE = 263
const AT_FIELD_OF_VIEW = 264
const AT_INTENSITY = 265
const AT_MORPH_WEIGHTS = 266
const AT_NEAR_DISTANCE = 267
const AT_ORIENTATION = 268
const AT_PICKABILITY = 269
const AT_SCALE = 270
const AT_SHININESS = 271
const AT_SPECULAR_COLOR = 272
const AT_SPOT_ANGLE = 273
const AT_SPOT_EXPONENT = 274
const AT_TRANSLATION = 275
const AT_VISIBILITY = 276

def AnimationTrack.new(seq: KeyframeSequence, property: Int);
def AnimationTrack.setController(controller: AnimationController);
def AnimationTrack.getController(): AnimationController;
def AnimationTrack.getKeyframeSequence(): KeyframeSequence;
def AnimationTrack.getTargetProperty(): Int;
