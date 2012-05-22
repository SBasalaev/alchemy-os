/* Simple editor
 * (C) 2012, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io.eh"
use "string.eh"
use "sys.eh"
use "ui.eh"
use "stdscreens.eh"

const HELP = "Usage: edit file"
const VERSION = "edit 0.8"

def readfile(f: String): String {
  if (exists(f)) {
    var buf = new BArray(fsize(f))
    var in = fopen_r(f)
    freadarray(in, buf, 0, buf.len)
    fclose(in)
    ba2utf(buf)
  } else {
    ""
  }
}

def writefile(f: String, text: String) {
  var buf = utfbytes(text)
  var out = fopen_w(f)
  fwritearray(out, buf, 0, buf.len)
  fclose(out)
}

def main(args: Array) {
  if (args.len == 0 || args[0] == "-h") {
    println(HELP)
  } else if (args[0] == "-v") {
    println(VERSION)
  } else {
    var f = to_str(args[0])
    var edit = new_editbox(EDIT_ANY)
    screen_set_title(edit, "edit - "+f)
    editbox_set_text(edit, readfile(f))
    var msave = new_menu("Save", 1)
    var mquit = new_menu("Quit", 2)
    screen_add_menu(edit, msave)
    screen_add_menu(edit, mquit)
    ui_set_screen(edit)
    var e = ui_wait_event()
    while (e.value != mquit) {
      if (e.value == msave) {
        writefile(f, editbox_get_text(edit))
      }
      e = ui_wait_event()
    }
  }
}
