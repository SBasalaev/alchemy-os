use "ui_types.eh"

def screen_height(scr: Screen): Int;
def screen_width(scr: Screen): Int;

def screen_get_title(scr: Screen): String;
def screen_set_title(scr: Screen, title: String);

def screen_shown(scr: Screen): Bool;

def ui_set_screen(scr: Screen);

type Menu;

def new_menu(text: String, priority: Int): Menu;

def menu_get_text(menu: Menu): String;
def menu_get_priority(menu: Menu): Int;

def screen_add_menu(scr: Screen, menu: Menu);
def screen_remove_menu(scr: Screen, menu: Menu);

const EV_SHOW = -1;
const EV_HIDE = -2;
const EV_MENU = 1;
const EV_KEY = 3;
const EV_PTR_PRESS = 6;
const EV_PTR_RELEASE = 7;
const EV_PTR_DRAG = 8;

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
