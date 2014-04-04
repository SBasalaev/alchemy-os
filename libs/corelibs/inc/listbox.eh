use "ui"

type ListBox < Screen

def ListBox.new(title: String, strings: [String], images: [Image], select: Menu): ListBox
def ListBox.getIndex(): Int
def ListBox.setIndex(index: Int)
def ListBox.add(str: String, img: Image = null)
def ListBox.insert(at: Int, str: String, img: Image = null)
def ListBox.set(at: Int, str: String, img: Image = null)
def ListBox.delete(at: Int)
def ListBox.getString(at: Int): String
def ListBox.getImage(at: Int): Image
def ListBox.getFont(at: Int): Font
def ListBox.setFont(at: Int, font: Int)
def ListBox.clear()
def ListBox.len(): Int
