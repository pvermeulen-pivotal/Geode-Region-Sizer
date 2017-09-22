#!/usr/bin/bash

HERE=$(dirname "$0")

export JAVA_HOME=
export CLASSPATH=$HERE/conf;$HERE/lib/*

$JAVA_HOME/bin/java -cp $CLASSPATH com.humana.base.gemfire.client.region.sizer.ClientRegionSizer $*