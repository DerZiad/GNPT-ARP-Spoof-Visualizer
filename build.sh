#!/usr/bin/env zsh

JAVA_FX="https://download2.gluonhq.com/openjfx/21.0.7/openjfx-21.0.7_linux-x64_bin-sdk.zip"
JAVA_FX_FOLDER_DOWNLOAD="javafx-sdk-21.0.7"
JAVA_FX_FOLDER="javafx"

if [ ! -d "fx" ]; then
  echo "[ + ] - Downloading JavaFX SDK"
  curl -L "$JAVA_FX" --output fx.zip

  echo "[ + ] - Extracting JavaFX SDK"
  unzip fx.zip

  echo "[ + ] - Renaming and Moving JavaFX SDK"
  mv "$JAVA_FX_FOLDER_DOWNLOAD" "$JAVA_FX_FOLDER"

  sudo rm -rf fx.zip
else
  echo "[ ✓ ] - JavaFX SDK already present, skipping download"
fi

echo "[ + ] - Starting Docker (if needed)"
sudo systemctl start docker

echo "[ + ] - Building Application"
cd dataservice
sudo mvn clean compile package