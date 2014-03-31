use "/inc/canvas.eh"

const new_canvas = `Canvas.new`
const `Canvas.action_code` = `Canvas.actionCode`
const `Canvas.has_ptr_events` = `Canvas.hasPtrEvents`
const `Canvas.has_ptrdrag_event` = `Canvas.hasPtrDragEvent`
const `Canvas.has_hold_event` = `Canvas.hasHoldEvent`

def Canvas.readKey(): Int {
  var ev = uiReadEvent()
  if (ev != null && ev.kind == EV_KEY_PRESS && ev.source == this) {
    return ev.value
  } else {
    return 0
  }
}

const `Canvas.read_key` = `Canvas.readKey`
