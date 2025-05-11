#!/usr/bin/zsh

sudo rm -rf target
sudo mvn clean compile assembly:single
sudo java -jar target/my-app-1.0-SNAPSHOT-jar-with-dependencies.jar
