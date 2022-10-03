JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
mkdir pool
./gradlew clean distZip

cd ./build/distributions
unzip DPMiner.zip

cd ./DPMiner/bin
#cd ../../../..

# apply options
./DPMiner patch -i -jk hadoop -o /home/codemodel/leshen/DPMiner/pool/camel-quarkus/patch
