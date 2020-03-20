pipeline {
  agent any
  stages {
    stage('Launch') {
      when {
        expression {
          params.myParam == null
        }

      }
      steps {
        sh 'echo "Launch pipelines"'
        build(job: 'pipelines/master', propagate: true)
      }
    }

    stage('build') {
      agent any
      when {
        expression {
          params.myParam != null
        }

      }
      steps {
        sh 'echo "Launch motor-bpm build"'
        sh 'echo "Launch motor-bpm tests"'
        sh 'echo "Publish artifact on Nexus"'
      }
    }

  }
  options {
    skipStagesAfterUnstable()
  }
}
