node ('main'){
    stage 'Build and test'
    env.PATH = "${tool 'Maven 3'}/bin:${env.PATH}"
    checkout scm
    sh 'mvn clean package'
}
