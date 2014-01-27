use "/inc/io.eh"
use "/inc/bufferio.eh"
use "connection.eh"

const fopen_r = fread;
const fopen_w = fwrite;
const fopen_a = fappend;
const `IStream.readarray` = `IStream.readArray`;
const `IStream.readfully` = `IStream.readFully`;
const `OStream.writearray` = `OStream.writeArray`;
const `OStream.writeall` = `OStream.writeAll`;
const readarray = readArray;
const writearray = writeArray;
const is_dir = isDir;
const set_read = setRead;
const set_write = setWrite;
const set_exec = setExec;
const can_read = canRead;
const can_write = canWrite;
const can_exec = canExec;
const get_cwd = getCwd;
const set_cwd = setCwd;
const space_total = spaceTotal;
const space_free = spaceFree;
const space_used = spaceUsed;
const readurl = readUrl;
const matches_glob = matchesGlob;
const istream_from_ba = `BufferIStream.new`;
const new_baostream = `BufferOStream.new`;
const `BArrayOStream.len` = `BufferOStream.len`;
const `BArrayOStream.tobarray` = `BufferOStream.getBytes`;
const `BArrayOStream.reset` = `BufferOStream.reset`;
