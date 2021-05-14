#!/bin/sh

cd `dirname $0 `

../gradlew :playground-clickcountjob:clean
../gradlew -Dprod=true :playground-clickcountjob:shadowJar