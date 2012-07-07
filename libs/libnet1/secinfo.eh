/* Security information. */

type Certificate < Any;
type SecInfo < Any;

def SecInfo.certificate(): Certificate;
def SecInfo.protocol_name(): String;
def SecInfo.protocol_version(): String;
def SecInfo.cipher_suite(): String;

def Certificate.subject(): String;
def Certificate.issuer(): String;
def Certificate.certtype(): String;
def Certificate.version(): String;
def Certificate.signalg(): String;
def Certificate.notbefore(): Long;
def Certificate.notafter(): Long;
