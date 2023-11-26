#!/bin/bash
source ./gradle.properties
echo "Building..."
echo "Creating Leader Coffee Bot App $version 🔥"
sh gradlew clean bootJar

echo "Creating Leader Coffee Bot Docker image $version 🐬"
imageName="wakedeer/leaderscoffee:$version"
imageArchive="leaderscoffee-$version.tar"
docker build -t $imageName .
docker save -o $imageArchive $imageName

echo "📨 Copy Docker archive to Environment"
scp $imageArchive coffebot@10.0.105.11:/home/coffebot/images/$imageArchive

echo "💿 Load docker image from archive"
ssh -t coffebot@10.0.105.11 "sudo docker rmi $imageName"
ssh -t coffebot@10.0.105.11 "sudo docker load -i /home/coffebot/images/$imageArchive"

echo "♻️ Clean up"
ssh -t coffebot@10.0.105.11 "rm /home/coffebot/images/$imageArchive"
rm $imageArchive
docker rmi $imageName
