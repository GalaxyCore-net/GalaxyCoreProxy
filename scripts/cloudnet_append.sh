#!/bin/bash
PROJECT_DIR="/home/flo/Development/Java/GalaxyCoreProxy/"
JAR_NAME="galaxycoreproxy-1.0-SNAPSHOT.jar"
MINECRAFT_DIR="/home/flo/Minecraft-Server/CloudNet/local/templates/GalaxyCore-Proxy-Test/default"

cd $PROJECT_DIR || exit
mvn clean compile package || exit
cp target/$JAR_NAME $MINECRAFT_DIR/plugins/ || exit
cd $MINECRAFT_DIR || exit
screen -S CloudNet -X stuff "ser GalaxyCore-Proxy-Test-1 stop\n"
echo "Plugin should be installed, server starting soon" || exit
