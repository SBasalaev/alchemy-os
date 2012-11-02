use "ui_types.eh"
use "ui_edit.eh"
use "font.eh"

type Form < Screen;

def new_form(): Form;

type Item < Any;

def Item.get_label(): String;
def Item.set_label(label: String);

def Form.add(item: Item);
def Form.get(at: Int): Item;
def Form.set(at: Int, item: Item);
def Form.insert(at: Int, item: Item);
def Form.remove(at: Int);
def Form.size(): Int;
def Form.clear();

type TextItem < Item;

def new_textitem(label: String, text: String): TextItem;
def TextItem.get_text(): String;
def TextItem.set_text(text: String);
def TextItem.get_font(): Int;
def TextItem.set_font(font: Int);

type HyperlinkItem < TextItem;

def new_hyperlinkitem(label: String, text: String);

type ImageItem < Item;

def new_imageitem(label: String, img: Image): ImageItem;
def ImageItem.get_image(): Image;
def ImageItem.set_image(img: Image);
def ImageItem.get_alttext(): String;
def ImageItem.set_alttext(text: String);

type EditItem < Item;

def new_edititem(label: String, text: String, mode: Int, maxsize: Int): EditItem;
def EditItem.get_text(): String;
def EditItem.set_text(text: String);
def EditItem.get_maxsize(): Int;
def EditItem.set_maxsize(size: Int);
def EditItem.get_size(): Int;
def EditItem.get_caret(): Int;

type GaugeItem < Item;

def new_gaugeitem(label: String, max: Int, init: Int): GaugeItem;
def GaugeItem.get_value(): Int;
def GaugeItem.set_value(val: Int);
def GaugeItem.get_maxvalue(): Int;
def GaugeItem.set_maxvalue(val: Int);

const DATE_ONLY = 1
const TIME_ONLY = 2
const DATE_TIME = 3

type DateItem <  Item;

def new_dateitem(label: String, mode: Int): DateItem;
def DateItem.get_date(): Long;
def DateItem.set_date(date: Long);

type CheckItem < Item;

def new_checkitem(label: String, text: String, checked: Bool): CheckItem;
def CheckItem.get_checked(): Bool;
def CheckItem.set_checked(checked: Bool);
def CheckItem.get_text(): String;
def CheckItem.set_text(text: String);

type RadioItem < Item;

def new_radioitem(label: String, strings: [String]): RadioItem;
def RadioItem.get_index(): Int;
def RadioItem.set_index(index: Int);
def RadioItem.add(str: String);
def RadioItem.insert(at: Int, str: String);
def RadioItem.set(at: Int, str: String);
def RadioItem.delete(at: Int);
def RadioItem.get(at: Int): String;
def RadioItem.clear();
def RadioItem.len(): Int;

type PopupItem < RadioItem;

def new_popupitem(label: String, strings: [String]): PopupItem;