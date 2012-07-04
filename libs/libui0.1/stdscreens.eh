use "ui_types.eh"
use "ui_edit.eh"

def new_textbox(text: String): Screen;
def textbox_get_text(box: Screen): String;
def textbox_set_text(box: Screen, text: String);

def new_editbox(mode: Int): Screen;
def editbox_get_text(box: Screen): String;
def editbox_set_text(box: Screen, text: String);

def new_listbox(strings: [String], images: [Image], select: Menu): Screen;
def listbox_get_index(list: Screen): Int;
def listbox_set_index(list: Screen, index: Int);
