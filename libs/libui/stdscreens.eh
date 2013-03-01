use "ui_types.eh"
use "ui_edit.eh"

type MsgBox < Screen;

def new_msgbox(text: String, image: Image = null): MsgBox;
const `MsgBox.new` = new_msgbox;
def MsgBox.get_text(): String;
def MsgBox.set_text(text: String);
def MsgBox.get_image(): Image;
def MsgBox.set_image(img: Image);

type EditBox < Screen;

def new_editbox(mode: Int = EDIT_ANY): EditBox;
const `EditBox.new` = new_editbox;
def EditBox.get_text(): String;
def EditBox.set_text(text: String);
def EditBox.get_maxsize(): Int;
def EditBox.set_maxsize(size: Int);
def EditBox.get_size(): Int;
def EditBox.get_caret(): Int;

type ListBox < Screen;

def new_listbox(strings: [String], images: [Image], select: Menu): ListBox;
const `ListBox.new` = new_listbox;
def ListBox.get_index(): Int;
def ListBox.set_index(index: Int);
def ListBox.add(str: String, img: Image = null);
def ListBox.insert(at: Int, str: String, img: Image = null);
def ListBox.set(at: Int, str: String, img: Image = null);
def ListBox.delete(at: Int);
def ListBox.get_string(at: Int): String;
def ListBox.get_image(at: Int): Image;
def ListBox.clear();
def ListBox.len(): Int;