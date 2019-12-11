pipeline {
  agent none
  parameters {
    booleanParam (
      defaultValue: true,
      description: '',
      name: 'clean'
    )
    booleanParam (
      defaultValue: true,
      description: '',
      name: 'linux_64'
    )
  }
  triggers {
    cron('H 20 * * *')
  }
  stages {
    stage('Prepare') {
      steps {
        script {
          def branchName = env.BRANCH_NAME
          def productVersion = "1.4.99"
          def pV = branchName =~ /^(release|hotfix)\\/v[\d]+.(.*)$/
          if(pV.find()) {
            productVersion = "1." + pV.group(2)
          }
          env.PRODUCT_VERSION = productVersion
        }
        script {
          env.PUBLISHER_NAME = "AO \"NOVYE KOMMUNIKACIONNYE TEHNOLOGII\""
          env.COMPANY_NAME = "R7-Office"
        }
      }
    }
    stage('Build') {
      parallel {
        stage('Linux 64-bit build') {
          agent { label 'linux_64' }
          steps {
            script {
              def utils = load "utils.groovy"
              if ( params.linux_64 ) {
                utils.linuxBuild(env.BRANCH_NAME, "linux_64", params.clean)
              }
            }
          }
        }
      }
    }
  }
}
