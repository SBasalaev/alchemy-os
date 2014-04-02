use "ui"

type EditBox < Screen

def EditBox.new(mode: Int = EDIT_ANY): EditBox
def EditBox.getText(): String
def EditBox.setText(text: String)
def EditBox.getMaxSize(): Int
def EditBox.setMaxSize(size: Int)
def EditBox.getSize(): Int
def EditBox.getCaret(): Int
