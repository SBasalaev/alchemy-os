use "Object3D.eh"

type AnimationController < Object3D;

def AnimationController.new();
def AnimationController.setActiveInterval(start: Int, end: Int);
def AnimationController.getActiveIntervalStart(): Int;
def AnimationController.getActiveIntervalEnd(): Int;
def AnimationController.setSpeed(speed: Float, worldTime: Int);
def AnimationController.getSpeed(): Float;
def AnimationController.setPosition(seqTime: Float, worldTime: Int);
def AnimationController.getPosition(worldTime: Int): Float;
def AnimationController.getRefWorldTime(): Int;
def AnimationController.setWeight(weight: Float);
def AnimationController.getWeight(): Float;
