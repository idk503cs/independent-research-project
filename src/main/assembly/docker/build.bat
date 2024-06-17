REM This script can be used to build the application JARs, images and create a network.
REM It can then deploy the client and test instances.
REM The client will run the test automatically using JMX.
REM **
REM ** keys need to be provided on the relative path as they are not included in the project.
REM ** Java, Maven and Docker need to be correctly installed for this to work. Internet access is also required.
REM **
REM The environment variable TF_VAR_repo should have been set globally in the environment. This variable holds the
REM repository URL that will store the performance test data. If that was not possible to set this globally it
REM can be set in this script or on the command line before calling this script.

docker network create local_network --subnet="10.0.1.0/24"
rem mvn clean package -f ..\..\..\..\pom.xml && ^
copy ..\..\..\..\target\*.jar . && ^
copy ..\..\resources\*.sh . && ^
xcopy ..\keys\ .\keys\ && ^
docker pull ubuntu:22.04 && ^
docker build --build-arg repo=%TF_VAR_repo% --tag test.image . && ^
del *.jar *.sh *.log && ^
rmdir /s /q keys && ^
start cmd /k "docker run -it --rm --ip 10.0.1.10 --network=local_network --name client test.image bash run_client.sh 10.0.1.11 10000 8080 10.0.1.12 10000 8080 10.0.1.13 10000 8080 10.0.1.14 10000 8080 10.0.1.15 10000 8080 10.0.1.16 10000 8080" && ^
start cmd /k "docker run -it --rm --ip 10.0.1.11 -p 8081:8080 --network=local_network --name test1 test.image bash run_performance_test.sh 10.0.1.11 10000 1000m 1000m 0 100 100 10 100 100 localDummyAmdServer1" && ^
start cmd /k "docker run -it --rm --ip 10.0.1.12 -p 8082:8080 --network=local_network --name test2 test.image bash run_performance_test.sh 10.0.1.12 10000 1000m 1000m 0 100 100 10 100 100 localDummyArmServer1" && ^
start cmd /k "docker run -it --rm --ip 10.0.1.13 -p 8083:8080 --network=local_network --name test3 test.image bash run_performance_test.sh 10.0.1.13 10000 1000m 1000m 0 100 100 10 100 100 localDummyAmdServer2" && ^
start cmd /k "docker run -it --rm --ip 10.0.1.14 -p 8084:8080 --network=local_network --name test4 test.image bash run_performance_test.sh 10.0.1.14 10000 1000m 1000m 0 100 100 10 100 100 localDummyArmServer2" && ^
start cmd /k "docker run -it --rm --ip 10.0.1.15 -p 8085:8080 --network=local_network --name test5 test.image bash run_performance_test.sh 10.0.1.15 10000 1000m 1000m 0 100 100 10 100 100 localDummyAmdServer3" && ^
start cmd /k "docker run -it --rm --ip 10.0.1.16 -p 8086:8080 --network=local_network --name test6 test.image bash run_performance_test.sh 10.0.1.16 10000 1000m 1000m 0 100 100 10 100 100 localDummyArmServer3"
