#!/usr/bin/env bash

cd ..

git clone https://github.com/SecuritySquad/webifier-tester.git
cd webifier-tester
sh install.sh
cd ..

cd webifier-platform
./gradlew :buildAll
cd ..

mkdir -p run
cd run
cp ../webifier-platform/build/libs/webifier-platform-all-*.jar .

JAR=$(ls| grep 'webifier\-platform\-all\-.*\.jar')

cat > start.sh << EOF
java -jar ${JAR} > output.log 2>&1 &
EOF

chmod +x start.sh