// I/O operations

use "/inc/io.eh"

def fclose(stream: Any);

const fread = `IStream.read`
const freadarray = `IStream.readarray`
const fskip = `IStream.skip`

const fwrite = `OStream.write`
const fwritearray = `OStream.writearray`
const fprint = `OStream.print`
const fprintln = `OStream.println`
const fprintf = `OStream.printf`
const fflush = `OStream.flush`
