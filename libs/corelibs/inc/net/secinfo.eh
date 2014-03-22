/* Security information. */

type Certificate < Any
type SecInfo < Any

def SecInfo.certificate(): Certificate
def SecInfo.protocolName(): String
def SecInfo.protocolVersion(): String
def SecInfo.cipherSuite(): String

def Certificate.subject(): String
def Certificate.issuer(): String
def Certificate.certtype(): String
def Certificate.version(): String
def Certificate.signalg(): String
def Certificate.notbefore(): Long
def Certificate.notafter(): Long
