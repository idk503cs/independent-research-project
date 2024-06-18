<b>Video instructions are in WATCH_ME.mp4</b>

<b>Prerequisits</b>: AWS account, Docker (optional), Terraform, IntelliJ IDEA 2023.2.2 (and above), Windows 10 and above, internet connection, access to artefact.zip (optional)
<b>NOTE</b>: It is possible to run the performance test outside of IntelliJ or any other IDE. It requires Maven, Java 17 and Java 21 to be installed correctly locally.

1. Extract the artefact.zip file. It contains two git repository folders and a data folder.
   <br>a. "independent-research-project" is the git repository with branches containing all the data from the performance tests. 
   <br>b. "performance-test" is a git repository containing the application code and scripts for executing the performance test and summarising the data.
   <br>c. "data" contains all the CSV datafiles from the various Performance test runs, copied out of the "independent-research-project" git repo branches.

2. Open the "performance-test" project in intelliJ and accept any prompts for Java 21.
   <br>a. File -> Project Structure -> Project
   <br>b. Change the language level to Java 21
   <br>c. SDK -> Add SDK -> Download JDK -> [version] = 21, [Vendor] = Eclipse Temurin (AdoptOpenJDK HotSpot) -> Download -> Apply -> OK
   <br>d. File -> Project Structure -> SDK -> + -> Download JDK -> [version] = 17, [Vendor] = Eclipse Temurin (AdoptOpenJDK HotSpot) -> Download -> Apply -> OK

3. Start SonarQube using either docker (a) or a local setup (b)
   <br>a. 
   <br>  i. Download and install Docker desktop: https://www.docker.com/products/docker-desktop/
   <br>  ii. Execute Docker command for running SonarQube
   <code>
   docker run -d --name sonarqube -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true -p 9000:9000 sonarqube:10.4.1-community
   </code>

   <br>b.
   <br>   i. Download the SonarQube zip: https://binaries.sonarsource.com/Distribution/sonarqube/sonarqube-10.5.1.90531.zip
   <br>   ii. Extract the zip.
   <br>   iii. Open the command prompt at the: "sonarqube-10.5.1.90531\bin\windows-x86-64" folder or navidate there on the command line.
   <br>   iv. This build of SonarQube runs on Java 17. Set SONAR_JAVA_PATH to the Java executable. e.g. set "SONAR_JAVA_PATH=C:\Users\idk503\.jdks\temurin-17.0.11\bin\java.exe"
   <br>   v. Execute the "StartSonar.bat" script from the command line.
   <br>   vi. SonarQube should be available on the browser using the following link: http://localhost:9000 but it may take several minutes to become available.
   
4. Once SonarQube has finished starting
   <br>a. Login using the default credentials admin/admin
   <br>b. Set your new password
   <br>c. Create a local project -> [Project display name] = performance test, [Project Key] = performance-test, [Main branch name] = main -> Use the global setting -> Create Project
   <br>d. Locally -> Generate
   <br>e. Copy the provided token e.g. "sqp_4e9d39f919d0593bcad30f28194f1db52eef0a5e". It is needed for the maven build command. 

5. Execute the maven build in IntelliJ.
   <br>Maven -> Execute Maven Goal -> 
<code>
mvn clean verify sonar:sonar -D"sonar.scm.disabled=true" -D"sonar.verbose=true" -D'sonar.java.enablePreview=true' -D"sonar.projectKey=performance-test" -D"sonar.projectName=performance test" -D"sonar.host.url=http://localhost:9000" -D"sonar.token=sqp_4e9d39f919d0593bcad30f28194f1db52eef0a5e"
</code>

6. SonarQube (local project = <url>http://localhost:9000/project/information?id=performance-test</url>). Links below can be used to access the metrics.
   <br>a. Database: src/main/java/uk/ac/york/idk503/performancetest/database/component/loader
   <br>   i. Cyclomatic Complexity: http://localhost:9000/component_measures?metric=complexity&selected=performance-test%3Asrc%2Fmain%2Fjava%2Fuk%2Fac%2Fyork%2Fidk503%2Fperformancetest%2Fdatabase%2Fcomponent%2Floader&id=performance-test
   <br>   ii. Cognitive Complexity: http://localhost:9000/component_measures?metric=cognitive_complexity&selected=performance-test%3Asrc%2Fmain%2Fjava%2Fuk%2Fac%2Fyork%2Fidk503%2Fperformancetest%2Fdatabase%2Fcomponent%2Floader&id=performance-test
   <br>   iii. Lines of Code: http://localhost:9000/component_measures?metric=ncloc&selected=performance-test%3Asrc%2Fmain%2Fjava%2Fuk%2Fac%2Fyork%2Fidk503%2Fperformancetest%2Fdatabase%2Fcomponent%2Floader&id=performance-test
   <br>b. REST: src/main/java/uk/ac/york/idk503/performancetest/rest/service
   <br>   i. Cyclomatic Complexity: http://localhost:9000/component_measures?metric=complexity&selected=performance-test%3Asrc%2Fmain%2Fjava%2Fuk%2Fac%2Fyork%2Fidk503%2Fperformancetest%2Frest%2Fservice&id=performance-test
   <br>   ii. Cognitive Complexity: http://localhost:9000/component_measures?metric=cognitive_complexity&selected=performance-test%3Asrc%2Fmain%2Fjava%2Fuk%2Fac%2Fyork%2Fidk503%2Fperformancetest%2Frest%2Fservice&id=performance-test
   <br>   iii. Lines of Code: http://localhost:9000/component_measures?metric=ncloc&selected=performance-test%3Asrc%2Fmain%2Fjava%2Fuk%2Fac%2Fyork%2Fidk503%2Fperformancetest%2Frest%2Fservice&id=performance-test
   <br>c. Multistage: src/main/java/uk/ac/york/idk503/performancetest/multistage
   <br>   i. Cyclomatic Complexity: http://localhost:9000/component_measures?metric=complexity&selected=performance-test%3Asrc%2Fmain%2Fjava%2Fuk%2Fac%2Fyork%2Fidk503%2Fperformancetest%2Fmultistage&id=performance-test
   <br>   ii. Cognitive Complexity: http://localhost:9000/component_measures?metric=cognitive_complexity&selected=performance-test%3Asrc%2Fmain%2Fjava%2Fuk%2Fac%2Fyork%2Fidk503%2Fperformancetest%2Fmultistage&id=performance-test
   <br>   iii. Lines of Code: http://localhost:9000/component_measures?metric=ncloc&selected=performance-test%3Asrc%2Fmain%2Fjava%2Fuk%2Fac%2Fyork%2Fidk503%2Fperformancetest%2Fmultistage&id=performance-test
   <br>d. Sort: src/main/java/uk/ac/york/idk503/performancetest/sort
   <br>   i. Cyclomatic Complexity: http://localhost:9000/component_measures?metric=complexity&selected=performance-test%3Asrc%2Fmain%2Fjava%2Fuk%2Fac%2Fyork%2Fidk503%2Fperformancetest%2Fsort&id=performance-test
   <br>   ii. Cognitive Complexity: http://localhost:9000/component_measures?metric=cognitive_complexity&selected=performance-test%3Asrc%2Fmain%2Fjava%2Fuk%2Fac%2Fyork%2Fidk503%2Fperformancetest%2Fsort&id=performance-test
   <br>   iii. Lines of Code: http://localhost:9000/component_measures?id=performance-test&metric=ncloc&selected=performance-test%3Asrc%2Fmain%2Fjava%2Fuk%2Fac%2Fyork%2Fidk503%2Fperformancetest%2Fsort

7. Setup the AWS environment
   <br>a. Log into the AWS account
   <br>b. Navidate to Search -> IAM (Manage access to AWS resources) -> Users
   <br>c. Create User -> Attach policies directly -> Check "AdministratorAccess" -> Next -> Create User
   <br>d. Click on the New user -> Security Credentials -> Create access key -> Other -> Next -> Download .csv file -> Done
   <br>e. Store the credentials safely. The "Secret access key" is needed by Terraform for provisioning the performance test environment.

8. Generate a key pair in AWS london region (eu-west-2) 
   <br>a. Navigate to Search -> EC2 -> Key Pairs (https://eu-west-2.console.aws.amazon.com/ec2/home?region=eu-west-2#KeyPairs)
   <br>b. Create key pair -> [name] = performance-test-key, Key pair type [RSA], Private key file format [.pem] -> Create key pair
   <br>c. Save the performance-test-key.pem file to the "performance-test\src\main\assembly\keys" folder

9. Create a new private key for accessing the git repo that will store the integration test results
   <br>a. Call the key independent-research-project.pem.
   <br>b. Copy the key to the "performance-test\src\main\assembly\keys" folder.

10. Set environment variables
   <br>a. The following environment variables should be set globally or in src/main/assembly/terraform/t2.t4g/run.cmd as they are used by the performance test:
   <br>   i.   The following environment variables are used by the performance test:
   <br>   ii.  TF_VAR_region - Represents the region to use in AWS. There is a close relationship between the chosen AMI and resion so this should be set to: "eu-west-2".
   <br>   iii. TF_VAR_availability_zone - The AWS availability zone is tied to the region. It should be set to "eu-west-2b".
   <br>   iv.  TF_VAR_secret_key - The AWS secret key for the account accessing AWS resources via terraform.
   <br>   v.   TF_VAR_access_key - The AWS access key for the account accessing AWS resources via terraform.
   <br>   vi.  TF_VAR_repo - The git repository that stores the performance test data. e.g. git@github.com:idk503cs/independent-research-project.git
   <br>b. If the variables are set globally then IntelliJ and any other applications needing access to them will need to be restarted.

11. Build the performance test application from the "performance-test" folder.
<br>mvn package
12. Execute the performance test by running "src/main/assembly/terraform/t2.t4g/run.cmd" from the command line.
   <br>NOTE: if the process fails due to AWS connectivity issues then wait for 5 mins and try again. Intermittent problems are common.

13. Summarise the data
   <br>a. get the abcolute path to the "independent-research-project" git repository from the provided: "artefact.zip". It should already be extracted to disk
   <br>b. Execute the "uk.ac.york.idk503.performancetest.results.Summarizer" class from the command line
   <br>i.   Pass in the absolute path to the extracted "independent-research-project" or the newly created repo used to store the new test results. 
   <br>ii.  Pass in the absolute path to a temp folder used to stage the data from the git repo. e.g. "C:\temp\data" folder.
   <br>iii. Locate the java 21 jdk used to build the software
   <br>iv.  Call the program from the "performance-test" on the commandline. e.g. 
   <br><code>C:\Users\%USERNAME%\.jdks\temurin-21.0.3\bin\java ^
    -cp target/performance-test-0.0.1-RELEASE-jar-with-dependencies.jar ^
    uk.ac.york.idk503.performancetest.results.Summarizer "M:\Dev\independent-research-project" "M:\Dev\data"</code>
   <br>e. Review the results in:
   <br>i.   Database: performance-test/DataLoad.xlsx
   <br>ii.  Sort: performance-test/MergeSort.xlsx
   <br>iii. Multistage: performance-test/Multistage.xlsx
   <br>iv.  REST: performance-test/Service.xlsx

14. Future research and development.
<br>If building on this project it may be useful to test the changes in docker before deploying them in AWS. This can be 
accomplished by calling "src/main/assembly/docker/build.bat" from the command line.


Accessing the using JConsole when running on the same machine as the performance test application: <br>
<code>
service:jmx:rmi:///jndi/rmi://localhost:10000/jmxrmi
</code>



