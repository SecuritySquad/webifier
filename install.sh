#!/usr/bin/env bash

cd ..

rm -rf run
rm -rf webifier-tester
git clone https://github.com/SecuritySquad/webifier-tester.git
cd webifier-tester
sh install.sh
cd ..

if [ -e persistent/platform-application.extension ]
then
    cat persistent/platform-application.extension >> webifier-platform/src/main/resources/application.properties
fi

cd webifier-platform
bower install
./gradlew :buildAll
cd ..

mkdir -p run
cd run
cp ../webifier-platform/build/libs/webifier-platform-all-*.jar .

JAR=$(ls| grep 'webifier\-platform\-all\-.*\.jar')

cat > start-platform.sh << EOF
killall webifier-plat
docker stop \$(docker ps -a -q)
docker rm \$(docker ps -a -q)

LD_PRELOAD=../persistent/libprocname.so PROCNAME=webifier-plat java -jar ${JAR} > output.log 2>&1 &
EOF

chmod +x start-platform.sh