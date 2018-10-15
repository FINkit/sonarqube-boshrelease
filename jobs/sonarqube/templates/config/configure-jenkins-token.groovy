import groovy.json.JsonSlurper
import SonarApiClient

<% if_link('jenkins_master') do |jenkins_master| %>

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

def jenkins_use_github_auth = "<%= link('jenkins_master').p('jenkins.use_github_auth') %>"
def jenkins_username = "<%= link('jenkins_master').p('jenkins.github.integration_user.name') %>"
def jenkins_token = "<%= link('jenkins_master').p('jenkins.github.integration_user.access_token') %>"

if (jenkins_use_github_auth == "false") {
    jenkins_username = "administrator"
    jenkins_token = "<%= link('jenkins_master').p('jenkins.admin.password') %>"
}

def jenkins_url = "http://<%= link('jenkins_master').instances[0].address %>:<%= link('jenkins_master').p('jenkins.server.port') %>"
def crumb_path = '/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,":",//crumb)'

def crumb = "curl --silent --user ${jenkins_username}:${jenkins_token} ${jenkins_url}${crumb_path}".execute().text

def scriptRequestBody = "script=${contents}" 

def scriptResponse = ["curl", "--header", "${crumb}", "--data-urlencode", "${scriptRequestBody}", "--user", "${jenkins_username}:${jenkins_token}", "${jenkins_url}/script"].execute().text

println "${scriptResponse}"

println "Configuring Jenkins... COMPLETE"

<% end %>

System.exit(0)
