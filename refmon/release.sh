# release (version)
git pull
echo "Mettez la bonne version dans le pom"
gvim pom.xml delivery/pom.xml
read -n 1 -p "Appuyez sur une touche pour poursuivre"
git add pom.xml delivery/pom.xml
git commit -m "Release $1"
git push origin master
git tag $1
git push origin $1

