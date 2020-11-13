Doc spécifique à Ref-Mon


#Release:
Code:
- vim refmon/pom.xml refmon/delivery/pom.xml, mettre version 1.0.0
- git add refmon/pom.xml refmon/delivery/pom.xml
- git commit -m "Release 1.0.0"
- git push
- git tag 1.0.0
- git push origin 1.0.0

Conf:
- git tag 1.0.0
- git push origin 1.0.0

Build Jenkins: https://validation.portail.etat-de-vaud.ch/outils/jenkins-refent/view/Ref-Mon/job/Ref-Mon-CI-IN_Devex/build?delay=0sec

Devex:
- Ref-Mon / Status: Créer une nouvelle release (version 1.0.0/1.0.0)
- Déploier en int ("all")


Nouvelle version:
- vim refmon/pom.xml refmon/delivery/pom.xml, mettre version 1.0.1-SNAPSHOT
- git add refmon/pom.xml refmon/delivery/pom.xml
- git commit -m "New version 1.0.1-SNAPSHOT"
