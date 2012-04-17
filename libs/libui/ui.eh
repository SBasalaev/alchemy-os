type Screen;

def screen_height(scr: Screen): Int;
def screen_width(scr: Screen): Int;

def screen_get_title(scr: Screen): String;
def screen_set_title(scr: Screen, title: String);

def screen_shown(scr: Screen): Bool;

def screen_add_menu(scr: Screen, caption: String, order: Int);

type UIEvent {
  source: Screen,
  kind: Int,
  x: Int,
  y: Int
}

def ui_set_screen(scr: Screen);

def ui_read_event(): UIEvent;
