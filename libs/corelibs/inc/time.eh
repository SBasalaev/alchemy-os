// Date and time operations

def datestr(time: Long): String;
def year(time: Long): Int;
def month(time: Long): Int;
def day(time: Long): Int;
def dow(time: Long): Int;
def hour(time: Long): Int;
def minute(time: Long): Int;
def second(time: Long): Int;
def millis(time: Long): Int;

def timeof(year: Int, month: Int, day: Int, hour: Int, min: Int, sec: Int, millis: Int): Long;
