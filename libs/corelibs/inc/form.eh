use "ui"

type Form < Screen

def Form.new(title: String = null)

type Item < Any

def Item.getLabel(): String
def Item.setLabel(label: String)

def Form.add(item: Item)
def Form.get(at: Int): Item
def Form.set(at: Int, item: Item)
def Form.insert(at: Int, item: Item)
def Form.remove(at: Int)
def Form.size(): Int
def Form.clear()

type TextItem < Item

def TextItem.new(label: String, text: String)
def TextItem.getText(): String
def TextItem.setText(text: String)
def TextItem.getFont(): Int
def TextItem.setFont(font: Int)

type HyperlinkItem < TextItem

def HyperlinkItem.new(label: String, text: String)

type ImageItem < Item

def ImageItem.new(label: String, img: Image)
def ImageItem.getImage(): Image
def ImageItem.setImage(img: Image)
def ImageItem.getAltText(): String
def ImageItem.setAltText(text: String)

type HyperimageItem < ImageItem

def HyperimageItem.new(label: String, img: Image)

type EditItem < Item

const EDIT_ANY = 0
const EDIT_EMAIL = 1
const EDIT_NUMBER = 2
const EDIT_PHONE = 3
const EDIT_URL = 4
const EDIT_DECIMAL = 5

def EditItem.new(label: String, text: String = "", mode: Int = EDIT_ANY, maxsize: Int = 50)
def EditItem.getText(): String
def EditItem.setText(text: String)
def EditItem.getMaxSize(): Int
def EditItem.setMaxSize(size: Int)
def EditItem.getSize(): Int
def EditItem.getCaret(): Int

type PasswordItem < EditItem

def PasswordItem.new(label: String, text: String = "", maxsize: Int = 50)

type GaugeItem < Item

def GaugeItem.new(label: String, max: Int, init: Int)
def GaugeItem.getValue(): Int
def GaugeItem.setValue(val: Int)
def GaugeItem.getMaxValue(): Int
def GaugeItem.setMaxValue(val: Int)

type ProgressItem < GaugeItem

const PROGRESS_INDEF = -1

def ProgressItem.new(label: String, max: Int, init: Int)

type DateItem <  Item

const DATE_ONLY = 1
const TIME_ONLY = 2
const DATE_TIME = 3

def DateItem.new(label: String, mode: Int = DATE_ONLY)
def DateItem.getDate(): Long
def DateItem.setDate(date: Long)

type CheckItem < Item

def CheckItem.new(label: String, text: String, checked: Bool)
def CheckItem.getChecked(): Bool
def CheckItem.setChecked(checked: Bool)
def CheckItem.getText(): String
def CheckItem.setText(text: String)

type RadioItem < Item

def RadioItem.new(label: String, strings: [String])
def RadioItem.getIndex(): Int
def RadioItem.setIndex(index: Int)
def RadioItem.add(str: String)
def RadioItem.insert(at: Int, str: String)
def RadioItem.set(at: Int, str: String)
def RadioItem.delete(at: Int)
def RadioItem.get(at: Int): String
def RadioItem.clear()
def RadioItem.len(): Int

type PopupItem < RadioItem

def PopupItem.new(label: String, strings: [String]): PopupItem
