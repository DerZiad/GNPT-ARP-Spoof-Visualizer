#!/usr/bin/zsh

sudo rm -rf target
sudo mvn clean compile package
sudo java -jar target/my-app-1.0-SNAPSHOT-jar-with-dependencies.jar
