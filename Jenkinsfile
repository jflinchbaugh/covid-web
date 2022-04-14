pipeline {
    agent { docker { image 'clojure:lein-buster' } }
    stages {
        stage('build') {
            steps {
                sh 'lein version'
            }
        }
    }
}
