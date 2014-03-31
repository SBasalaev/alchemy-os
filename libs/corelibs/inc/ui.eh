/* Constants for editable components. */
const EDIT_ANY = 0
const EDIT_EMAIL = 1
const EDIT_NUMBER = 2
const EDIT_PHONE = 3
const EDIT_URL = 4
const EDIT_DECIMAL = 5
const EDIT_PASSWORD = 0x10000

/* Menu types. */
const MT_SCREEN = 1
const MT_BACK = 2
const MT_CANCEL = 3
const MT_OK = 4
const MT_HELP = 5
const MT_STOP = 6
const MT_EXIT = 7

/* Menu class. */
type Menu < Any

def Menu.new(label: String, priority: Int, mtype: Int = MT_SCREEN): Menu
def Menu.getLabel(): String
def Menu.getPriority(): Int
def Menu.getType(): Int

/* Screen class. */
type Screen < Any

def Screen.isShown(): Bool
def Screen.getHeight(): Int
def Screen.getWidth(): Int
def Screen.getTitle(): String
def Screen.setTitle(title: String)
def Screen.addMenu(menu: Menu)
def Screen.removeMenu(menu: Menu)

/* UI event kinds. */
const EV_SHOW = -1
const EV_HIDE = -2
const EV_MENU = 1
const EV_ITEM = 2
const EV_KEY = 3
const EV_KEY_HOLD = 4
const EV_KEY_RELEASE = 5
const EV_PTR_PRESS = 6
const EV_PTR_RELEASE = 7
const EV_PTR_DRAG = 8
const EV_ITEMSTATE = 9

/* UI event class. */
type UIEvent {
  kind: Int,
  source: Screen,
  value: Any
}

type Point {
  x: Int,
  y: Int
}

/* UI functions. */

def uiReadEvent(): UIEvent
def uiWaitEvent(): UIEvent

def uiVibrate(millis: Int): Bool
def uiFlash(millis: Int): Bool

def uiGetScreen(): Screen
def uiSetScreen(scr: Screen)

def uiSetDefaultTitle(title: String)

type Image
def uiSetIcon(image: Image)
