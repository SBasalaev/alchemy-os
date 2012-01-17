type Screen;

def ui_set_screen(scr: Screen);

def screen_height(scr: Screen): Int;
def screen_width(scr: Screen): Int;

def screen_get_title(scr: Screen): String;
def screen_set_title(scr: Screen, title: String);

def screen_shown(scr: Screen): Bool;

//type Command;

//def new_command(lbl: String, t: Int, pr: Int): Command;

//def screen_add_command(scr: Screen, c: Command);
//def screen_rm_command(scr: Screen, c: Command);
