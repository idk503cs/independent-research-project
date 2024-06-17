#!/bin/bash
set -xv

export amdHost1=${1:-localhost}
export amdJmxPort1=${2:-10001}
export amdHttpPort1=${3:-8081}
export armHost1=${4:-localhost}
export armJmxPort1=${5:-10002}
export armHttpPort1=${6:-8082}

export amdHost2=${7:-localhost}
export amdJmxPort2=${8:-10003}
export amdHttpPort2=${9:-8083}
export armHost2=${10:-localhost}
export armJmxPort2=${11:-10004}
export armHttpPort2=${12:-8084}

export amdHost3=${13:-localhost}
export amdJmxPort3=${14:-10005}
export amdHttpPort3=${15:-8085}
export armHost3=${16:-localhost}
export armJmxPort3=${17:-10006}
export armHttpPort3=${18:-8086}

cd independent-research-project

formatted_timestamp=$(date +'%Y%m%d-%H%M%S')
branch_name="data/${formatted_timestamp}"
git checkout -b "${branch_name}"
git config --global user.name "Performance Test"
git config --global user.email "performance.test@example.com"

java --enable-preview \
  -cp ../performance-test-0.0.1-RELEASE-jar-with-dependencies.jar \
      uk.ac.york.idk503.performancetest.mbean.PerformanceTestMonitorClient "${amdHost1}" "${amdJmxPort1}" "${amdHttpPort1}" "${armHost1}" "${armJmxPort1}" "${armHttpPort1}" "${amdHost2}" "${amdJmxPort2}" "${amdHttpPort2}" "${armHost2}" "${armJmxPort2}" "${armHttpPort2}" "${amdHost3}" "${amdJmxPort3}" "${amdHttpPort3}" "${armHost3}" "${armJmxPort3}" "${armHttpPort3}"
git add *.csv
git commit -m "job completed"
git push origin ${branch_name}
sudo shutdown -h
