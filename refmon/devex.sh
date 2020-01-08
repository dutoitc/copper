# c.f. https://portail.etat-de-vaud.ch/outils/dsiwiki/display/CEIAEP/Application+Embedded

export DEVEX_URL=http://outils-devex-ws.etat-de-vaud.ch/outils/devex
export DEVEX_USERNAME=gvd0jenkinsrefent
export DEVEX_PASSWORD=9r8fc4k9m6b239tcbZ38

mvn -U -Pxfile,devex.release,devex.in clean install
