#!/usr/bin/env bash

VERSION="1.7.0.lv1.0.0"

LTS_BIN="${BASH_SOURCE-$0}"
LTS_BIN="$(dirname "${LTS_BIN}")"
LTS_Bin_Dir="$(cd "${LTS_BIN}"; pwd)"

cd $LTS_Bin_Dir

mvn clean install -U -DskipTests

Dist_Bin_Dir="$LTS_Bin_Dir/dist/lts-jobtracker"
mkdir -p $Dist_Bin_Dir

Dist_Bin_Dir="$(cd "$(dirname "${Dist_Bin_Dir}/.")"; pwd)"

mkdir -p $Dist_Bin_Dir

# 打包
Startup_Dir="$LTS_Bin_Dir/lts-startup/"
cd $Startup_Dir
mvn clean assembly:assembly -DskipTests -Pdefault

cp -rf $Startup_Dir/target/lts-bin/lts/*  $Dist_Bin_Dir



