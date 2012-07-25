//work with hashtables

use "/inc/dict.eh"

type Hashtable < Dict;

def new_ht(): Hashtable = cast (Hashtable) new_dict()
const ht_put = `Dict.set`
const ht_get = `Dict.get`
const ht_rm = `Dict.remove`
const ht_keys = `Dict.keys`
