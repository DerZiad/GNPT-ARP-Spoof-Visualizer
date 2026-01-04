#!/usr/bin/zsh

set -e

YELLOW='\033[33m'
RESET='\033[0m'
RELEASE_JAR_NAME="gnpt.jar"

log() {
  echo -e "${YELLOW}[BASH]${RESET} $*"
}

log "Building GNPT Jar ..."
java -version
mvn clean compile assembly:single
mv target/gnpt-1.0-jar-with-dependencies.jar target/$RELEASE_JAR_NAME
setopt extendedglob
rm -rf target/^$RELEASE_JAR_NAME
cp -r javafx target
chmod -R 755 target

log "GNPT has been built and is ready for release."

echo "#!/usr/bin/zsh
java --module-path ./javafx/lib --add-modules javafx.controls,javafx.fxml,javafx.swing -jar gnpt.jar
" > target/run.sh

chmod +x target/run.sh

log "Creating gnpt.zip..."
cp -r target gnpt
zip -r target/gnpt.zip gnpt
rm -rf gnpt
