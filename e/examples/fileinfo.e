/* Example of using form.
 * Shows properties of given file.
 * To compile: ex fileinfo.e -o fileinfo -lui
 * To run: ./fileinfo
 */

use "io"
use "ui"
use "form"

def fillform(form: Screen, file: String) {
  var item = new_textitem("File:", file)
  form_add(form, item)
  item = new_textitem("Type:",
    if (is_dir(file)) "Directory"
    else "Normal file"
  )
  form_add(form, item)
  item = new_textitem("Size:", to_str(fsize(file)))
  form_add(form, item)
  item = new_dateitem("Modified:",DATE_TIME)
  dateitem_set_date(item, fmodified(file))
  form_add(form, item)
  item = new_checkitem("Access:","Read", can_read(file))
  form_add(form, item)
  item = new_checkitem("", "Write", can_write(file))
  form_add(form, item)
  item = new_checkitem("", "Execute", can_exec(file))
  form_add(form, item)
}

def main(args: Array) {
  if (args.len == 0) {
    println("Syntax: fileinfo file")
  } else {
    // create new form
    var form = new_form()
    // fill it with information
    screen_set_title(form, "File info")
    fillform(form, to_str(args[0]))
    // add "Close" menu
    var mclose = new_menu("Close", 1)
    screen_add_menu(form, mclose)
    // show on the screen
    ui_set_screen(form)
    // read events until "Close" menu is selected
    var e = ui_wait_event()
    while (e.value != mclose) {
      e = ui_wait_event()
    }
  }
}
