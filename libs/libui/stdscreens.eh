type Screen;

const TEXT_ANY = 0;
const TEXT_EMAIL = 1;
const TEXT_NUMBER = 2;
const TEXT_PHONE = 3;
const TEXT_URL = 4;
const TEXT_DECIMAL = 5;
const TEXT_PASSWORD = 0x10000;

def new_textbox(mode: Int): Screen;

def textbox_get_text(box: Screen): String;
def textbox_set_text(box: Screen, text: String);

def new_listbox(strings: Array, images: Array): Screen;

def listbox_get_index(list: Screen): Int;
def listbox_set_index(list: Screen, index: Int);

type Menu;
def listbox_default_menu(): Menu;
