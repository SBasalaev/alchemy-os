use "ui_types.eh"
use "ui_edit.eh"
use "font.eh"

def new_form(): Screen;

type Item;

def form_add(form: Screen, item: Item);
def form_get(form: Screen, at: Int): Item;
def form_set(form: Screen, at: Int, item: Item);
def form_insert(form: Screen, at: Int, item: Item);
def form_remove(form: Screen, at: Int);
def form_size(form: Screen): Int;
def form_clear(form: Screen);

def item_get_label(item: Item): String;
def item_set_label(item: Item, label: String);

def new_textitem(label: String, text: String): Item;
def textitem_get_text(item: Item): String;
def textitem_set_text(item: Item, text: String);
def textitem_get_font(item: Item): Int;
def textitem_set_font(item: Item, font: Int);

def new_imageitem(label: String, img: Image): Item;
def imageitem_get_image(item: Item): Image;
def imageitem_set_image(item: Item, img: Image);

def new_edititem(label: String, text: String, mode: Int, size: Int): Item;
def edititem_get_text(item: Item): String;
def edititem_set_text(item: Item, text: String);

def new_gaugeitem(label: String, max: Int, init: Int): Item;
def gaugeitem_get_value(item: Item): Int;
def gaugeitem_set_value(item: Item, val: Int);
def gaugeitem_get_maxvalue(item: Item): Int;
def gaugeitem_set_maxvalue(item: Item, val: Int);

const DATE_ONLY = 1
const TIME_ONLY = 2
const DATE_TIME = 3

def new_dateitem(label: String, mode: Int): Item;
def dateitem_get_date(item: Item): Long;
def dateitem_set_date(item: Item, date: Long);

def new_checkitem(label: String, text: String, checked: Bool): Item;
def checkitem_get_checked(item: Item): Bool;
def checkitem_set_checked(item: Item, checked: Bool);
def checkitem_get_text(item: Item): String;
def checkitem_set_text(item: Item, text: String);

def new_radioitem(label: String, strings: Array): Item;
def radioitem_get_index(item: Item): Int;
def radioitem_set_index(item: Item, index: String);
