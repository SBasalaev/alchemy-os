use "Group"

type World < Group;

type Background;
type Camera;

def World.new();
def World.setBackground(background: Background);
def World.getBackground(): Background;
def World.setActiveCamera(camera: Camera);
def World.getActiveCamera(): Camera;
