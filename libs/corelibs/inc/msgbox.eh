use "ui"

type MsgBox < Screen

def MsgBox.new(title: String = null, text: String = null, image: Image = null)
def MsgBox.getText(): String
def MsgBox.setText(text: String)
def MsgBox.getImage(): Image
def MsgBox.setImage(img: Image)
def MsgBox.getFont(): Int
def MsgBox.setFont(font: Int)
