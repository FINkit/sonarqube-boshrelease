import groovy.json.JsonSlurper
import SonarApiClient

def sonarApiTokenUrl = SonarApiClient.sonarApiUrl + 'user_tokens/generate'

def tokenResponse = SonarApiClient.postQueryString(sonarApiTokenUrl, "name=Jenkins")

if (!tokenResponse) {
    System.exit(1)
}

def responseBody = SonarApiClient.responseBody
def jsonParser = new JsonSlurper()
def data = jsonParser.parseText(responseBody)
def token = data.token

String contents = new File('/var/vcap/jobs/sonarqube/config/configure-jenkins-sonar.groovy').getText('UTF-8') 
contents = contents.replaceAll( "SONAR_AUTH_TOKEN", "${token}")

def jenkins_url = 'http://10.244.0.3:8080'
def jenkins_username = "administrator"
def jenkins_token = "IEeK1sVfqldxHOoinBrnUW7eUU9100"
def crumb_path = '/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,":",//crumb)'

def crumb = "curl --silent --user ${jenkins_username}:${jenkins_token} ${jenkins_url}${crumb_path}".execute().text

def scriptRequestBody = "script=${contents}" 

def scriptResponse = ["curl", "--header", "${crumb}", "--data-urlencode", "${scriptRequestBody}", "--user", "${jenkins_username}:${jenkins_token}", "${jenkins_url}/script"].execute().text

println "${scriptResponse}"

println "Configuring Jenkins... COMPLETE"

System.exit(0)
