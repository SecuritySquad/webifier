#!/usr/bin/env bash

cd ..

ls | grep -v "webifier-platform|persistent" | xargs rm -rf

git clone https://github.com/SecuritySquad/webifier-tester.git
cd webifier-tester
sh install.sh
cd ..

if [ -e persistent/application.extension ]
then
    cat persistent/application.extension >> webifier-platform/src/main/resources/application.properties
fi

cd webifier-platform
./gradlew :buildAll
cd ..

mkdir -p run
cd run
cp ../webifier-platform/build/libs/webifier-platform-all-*.jar .

JAR=$(ls| grep 'webifier\-platform\-all\-.*\.jar')

cat > start.sh << EOF
killall java
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)

java -jar ${JAR} > output.log 2>&1 &
EOF

chmod +x start.sh