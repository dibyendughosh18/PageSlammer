pipeline {
agent any
stages {
stage ('Compile Stage') {
steps {
withMaven(maven : 'maven3.6') {
bat'mvn clean compile'
}
}
}
stage ('Testing Stage') {
steps {
withMaven(maven : 'maven3.6') {
bat'mvn test'
}
}
}
stage ('Install Stage') {
steps {
withMaven(maven : 'maven3.6') {
bat'mvn install'
}
}
}
}
}