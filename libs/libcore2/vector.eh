use "/inc/list.eh"

type Vector < List;

def new_vector(): Vector = cast (Vector) new_list()
const v_size = `List.size`
const v_get = `List.get`
const v_set = `List.set`
const v_remove = `List.remove`
const v_add = `List.add`
const v_insert = `List.insert`
const v_indexof = `List.indexof`
const v_lindexof = `List.lindexof`
const v_toarray = `List.toarray`
