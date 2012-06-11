/* Example of application using canvas.
 * To compile: ex canvasinput.e -o canvasinput -lui
 * To run: ./canvasinput
 */

use "ui"
use "canvas"
use "graphics"
use "string"
use "sys"

def keystr(key: Int): String {
  var sb = new_sb()
  sb_append(sb, "Key code: ")
  sb_append(sb, key)
  if (key >= ' ') {
    sb_append(sb, ", Char: '")
    sb_addch(sb, key)
    sb_addch(sb, '\'')
  }
  to_str(sb)
}

def main(args: Array) {
  /* Create canvas screen */
  var cnv = new_canvas(false)
  screen_set_title(cnv, "Key input example")
  ui_set_screen(cnv)
  /* Draw initial text */
  var g = canvas_graphics(cnv)
  set_color(g, 0)
  draw_string(g, "No key pressed", 5, 5)
  draw_string(g, "To quit press #", 5, 30)
  canvas_refresh(cnv)
  /* Read keys in a loop */
  var key = canvas_read_key(cnv)
  while (key != '#') {
    var newkey = canvas_read_key(cnv)
    if (newkey != 0 && newkey != key) {
      /* Clear screen */
      set_color(g, 0xffffff)
      fill_rect(g, 0, 0, screen_width(cnv), screen_height(cnv))
      /* Draw new text */
      set_color(g, 0)
      draw_string(g, keystr(newkey), 5, 5)
      draw_string(g, "To quit press #", 5, 30)
      canvas_refresh(cnv)
      key = newkey
    }
    sleep(100)
  }
}
