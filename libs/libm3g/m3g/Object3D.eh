type Object3D < Any;

type AnimationTrack;

def Object3D.animate(time: Int): Int;
def Object3D.duplicate(): Object3D;
def Object3D.find(userID: Int): Object3D;
def Object3D.getReferences(refs: [Object3D]): Int;
def Object3D.setUserID(userID: Int);
def Object3D.getUserID(): Int;
def Object3D.setUserObject(userObj: Any);
def Object3D.getUserObject(): Any;
def Object3D.addAnimationTrack(track: AnimationTrack);
def Object3D.getAnimationTrack();
def Object3D.removeAnimationTrack(track: AnimationTrack);
def Object3D.getAnimationTrackCount(): Int;
