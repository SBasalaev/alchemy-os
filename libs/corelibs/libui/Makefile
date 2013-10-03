HEADERS=canvas.eh font.eh form.eh graphics.eh image.eh stdscreens.eh ui.eh ui_edit.eh ui_types.eh

all:

package:
 # libui1
 rm -rf lib
 mkdir lib
 install native/libui.1.so lib
 cat libui1.package > PACKAGE
 arh c libui1.pkg PACKAGE lib
 # libui-dev
 rm -rf lib inc
 mkdir lib inc
 install native/libui.so lib
 install ${HEADERS} inc
 cat libui-dev.package > PACKAGE
 arh c libui-dev.pkg PACKAGE lib inc
 rm -r PACKAGE lib inc