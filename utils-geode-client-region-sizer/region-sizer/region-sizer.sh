#!/usr/bin/bash

HERE=$(dirname "$0")

export JAVA_HOME=
export CLASSPATH=$HERE/conf;$HERE/lib/*

$JAVA_HOME/bin/java -cp $CLASSPATH pivotal.geode.client.region.sizer.ClientRegionSizer $*