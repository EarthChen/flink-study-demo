#!/bin/sh

cd `dirname $0 `

../gradlew :spendreport-table:clean
../gradlew -Dprod=true :spendreport-table:shadowJar