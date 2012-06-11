/* Example of drawing on canvas.
 * To compile: ex drawing.e -o drawing -lui
 * To run: ./drawing
 */

use "ui"
use "canvas"
use "graphics"
use "sys"

def main(args: Array) {
  /* Creating canvas screen */
  var cnv = new_canvas(false)
  screen_set_title(cnv, "Drawing example")
  ui_set_screen(cnv)
  /* Drawing */
  var g = canvas_graphics(cnv)
  set_color(g, 0xffff00) // yellow
  fill_arc(g, 20, 20, 50, 50, 0, 360)
  set_color(g, 0) // black
  draw_arc(g, 20, 20, 50, 50, 0, 360)
  draw_arc(g, 30, 30, 30, 30, 225, 90)
  fill_rect(g, 33, 34, 4, 4)
  fill_rect(g, 53, 34, 4, 4)
  draw_string(g, "Press any key", 15, 70)
  canvas_refresh(cnv)
  /* Waiting for key press */
  while (canvas_read_key(cnv) == 0) {
    sleep(50)
  }
}
