#! /bin/bash
launchWeb() {
    mvn clean
    mvn package -Dmaven.test.skip=true
    rm webDemo/ -rf 
    mkdir webDemo
    mkdir ./webDemo/library
    cp target/soul-client-0.4.jar ./webDemo
    cp -r target/lib ./webDemo
    cp bashScript/webDemo.sh ./webDemo
    cp library/ ./webDemo/ -r
    #cd ./webDemo
    #chmod +x ./webDemo.sh
    #./webDemo.sh
}
if [ "x$1" = "xweb" ]; then
    launchWeb
fi