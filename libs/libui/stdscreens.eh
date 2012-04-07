type Screen;

def new_textbox(mode: Int): Screen;

def textbox_get_text(box: Screen): String;
def textbox_set_text(box: Screen, text: String);

def new_listbox(strings: Array, images: Array): Screen;

def listbox_get_index(list: Screen): Int;
def listbox_set_index(list: Screen, index: Int);
