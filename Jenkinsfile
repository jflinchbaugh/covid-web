pipeline {
    agent {
      docker {
        image 'clojure:lein-buster'
        args "bash"
      }
    }
    stages {
        stage('build') {
            steps {
                sh 'lein version'
            }
        }
    }
}
