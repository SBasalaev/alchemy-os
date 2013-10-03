use "ui_types.eh"

def ui_set_app_title(title: String);
def ui_set_app_icon(icon: Image);

def ui_vibrate(millis: Int): Bool;
def ui_flash(millis: Int): Bool;

type Screen < Any;

def Screen.get_height(): Int;
def Screen.get_width(): Int;

def Screen.get_title(): String;
def Screen.set_title(title: String);

def Screen.is_shown(): Bool;

def ui_get_screen(): Screen;
def ui_set_screen(scr: Screen);

type Menu < Any;

const MT_SCREEN = 1
const MT_BACK = 2
const MT_CANCEL = 3
const MT_OK = 4
const MT_HELP = 5
const MT_STOP = 6
const MT_EXIT = 7

def new_menu(text: String, priority: Int, mtype: Int = MT_SCREEN): Menu;
const `Menu.new` = new_menu;
def Menu.get_text(): String;
def Menu.get_priority(): Int;

def Screen.add_menu(menu: Menu);
def Screen.remove_menu(menu: Menu);

const EV_SHOW = -1;
const EV_HIDE = -2;
const EV_MENU = 1;
const EV_ITEM = 2;
const EV_KEY = 3;
const EV_KEY_HOLD = 4;
const EV_KEY_RELEASE = 5;
const EV_PTR_PRESS = 6;
const EV_PTR_RELEASE = 7;
const EV_PTR_DRAG = 8;
const EV_ITEMSTATE = 9;

type UIEvent {
  kind: Int,
  source: Screen,
  value: Any
}

type Point {
  x: Int,
  y: Int
}

def ui_read_event(): UIEvent;
def ui_wait_event(): UIEvent;
