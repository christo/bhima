#!/usr/bin/env bash

# builds distribution then unpacks and checks its contents

#set -vx 
shopt -s failglob

echo  $JAVA_HOME

thisDir=`dirname $0`
pushd "$thisDir/.." 2>&1 >/dev/null

if [[ -f "sourceme.sh" ]]; then
  source "sourceme.sh"
fi

./gradlew -i distTar || exit

if [[ ! -f client/build/index.html ]]; then
    echo no index.html in client/build
else
    echo got client/build/index.html
fi

if [[ ! -d client/buildJar ]]; then
    echo no buildJar in client
else
    echo got buildJar
fi

mkdir -p tmp
rm -rf tmp/*
tar xf dragonmind/build/distributions/dragonmind-*.tar -C tmp/
dd=tmp/dragonmind-*
ln -s $PWD/dragonmind/video ${dd#}/
cd ${dd#}

echo; echo
find . -name '*SNAPSHOT*' | grep client
RETVAL1=$?
RETVAL2=0
if [[ $RETVAL1 -ne 0 ]]; then 
    echo cannot find a client jar in the dist
else 
    echo got client jar in dist, checking contents includes js
    jar -tvf lib/client*.jar | grep js
    RETVAL2=$?
    if [[ $RETVAL2 -ne 0 ]]; then
        echo cannot find any javascript in the client jar
    fi
fi
if [[ $RETVAL1 -ne 0 || $RETVAL2 -ne 0 ]]; then 
    echo
    echo CHECK FAILED
    popd 2>&1 >/dev/null
    exit 1
fi
popd 2>&1 >/dev/null
exit 0
