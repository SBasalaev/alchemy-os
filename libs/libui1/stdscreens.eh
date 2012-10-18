use "ui_types.eh"
use "ui_edit.eh"

type TextBox < Screen;

def new_textbox(text: String): TextBox;
def TextBox.get_text(): String;
def TextBox.set_text(text: String);
def TextBox.get_font(): Int;
def TextBox.set_font(f: Int);

type EditBox < Screen;

def new_editbox(mode: Int): EditBox;
def EditBox.get_text(): String;
def EditBox.set_text(text: String);
def EditBox.get_maxsize(): Int;
def EditBox.set_maxsize(size: Int);
def EditBox.get_size(): Int;
def EditBox.get_caret(): Int;

type ListBox < Screen;

def new_listbox(strings: [String], images: [Image], select: Menu): ListBox;
def ListBox.get_index(): Int;
def ListBox.set_index(index: Int);
def ListBox.add(str: String, img: Image);
def ListBox.insert(at: Int, str: String, img: Image);
def ListBox.set(at: Int, str: String, img: Image);
def ListBox.delete(at: Int);
def ListBox.get_string(at: Int): String;
def ListBox.get_image(at: Int): Image;
def ListBox.clear();
def ListBox.len(): Int;