# release (version)
echo "Mettez la bonne version dans le pom"
gvim refmon/pom.xml refmon/delivery/pom.xml
git add refmon/pom.xml refmon/delivery/pom.xml
git commit -m "Release $1"
git push
git tag $1
git push origin $1

