use "ui"

type EditBox < Screen

def EditBox.new(title: String = null, text: String = null): EditBox
def EditBox.getText(): String
def EditBox.setText(text: String)
def EditBox.getMaxSize(): Int
def EditBox.setMaxSize(size: Int)
def EditBox.getSize(): Int
def EditBox.getCaret(): Int
