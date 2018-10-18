import groovy.json.JsonSlurper
import SonarApiClient

<% if_link('jenkins_master') do |jenkins_master| %>

def now

def sonarUsername = "<%= link('jenkins_master').p('jenkins.github.integration_user.name') %>"

def sonarUseGithubAuth = <%= p('sonar.use_github_auth') %>

if (!sonarUseGithubAuth) {
    sonarUsername = "<%= p('sonar.admin.username') %>"
}

def jenkinsLoginPair = SonarApiClient.buildSingleValuedKeyPair("login", sonarUsername)

def sonarApiTokensSearchUrl = SonarApiClient.sonarApiUrl + 'user_tokens/search'
def tokensResponse = SonarApiClient.getQueryResponse(sonarApiTokensSearchUrl + "?" + jenkinsLoginPair)

def jsonParser = new JsonSlurper()
def tokens = jsonParser.parseText(tokensResponse.get(1))
def jenkinsSonarTokenName = "Jenkins-green"
def oldJenkinsSonarTokenName = "Jenkins-blue"

def userTokens = tokens.userTokens
for (userToken in userTokens) { 
    if (userToken.name == "Jenkins-green") {
       jenkinsSonarTokenName = "Jenkins-blue"
       oldJenkinsSonarTokenName = "Jenkins-green"
       break
    }
}

// generate new jenkins sonar access token
now = new Date().format("yyy/MM/dd HH:mm:ss")
println "${now} - Generating a ${jenkinsSonarTokenName} access token"
def sonarApiTokenUrl = SonarApiClient.sonarApiUrl + 'user_tokens/generate'
def jenkinsTokenPair = SonarApiClient.buildSingleValuedKeyPair("name", jenkinsSonarTokenName)
def queryValues = [jenkinsTokenPair, jenkinsLoginPair]
def queryString = SonarApiClient.buildQueryString(queryValues.iterator())
def tokenResponse = SonarApiClient.postQueryStringAndGetResponse(sonarApiTokenUrl, queryString)

if (!tokenResponse.get(0)) {
    System.exit(1)
}

def responseBody = tokenResponse.get(2)
def data = jsonParser.parseText(responseBody)
def token = data.token

String contents = new File('/var/vcap/jobs/sonarqube/config/configure-jenkins-sonar.groovy').getText('UTF-8') 
contents = contents.replaceAll("SONAR_AUTH_TOKEN", "${token}")

def jenkinsUsername = "<%= link('jenkins_master').p('jenkins.github.integration_user.name') %>"
def jenkinsToken = "<%= link('jenkins_master').p('jenkins.github.integration_user.access_token') %>"

def jenkinsUseGithubAuth = <%= link('jenkins_master').p('jenkins.use_github_auth') %>

if (!jenkinsUseGithubAuth) {
    jenkinsUsername = "administrator"
    jenkinsToken = "<%= link('jenkins_master').p('jenkins.admin.password') %>"
}

def jenkinsUrl = "http://<%= link('jenkins_master').instances[0].address %>:<%= link('jenkins_master').p('jenkins.server.port') %>"
def crumbPath = '/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,":",//crumb)'

def crumb = "curl --silent --user ${jenkinsUsername}:${jenkinsToken} ${jenkinsUrl}${crumbPath}".execute().text

def scriptRequestBody = "script=${contents}"

now = new Date().format("yyy/MM/dd HH:mm:ss")
println "${now} - Configuring Jenkins with access token"
def scriptResponse = ["curl", "--header", "${crumb}", "--data-urlencode", "${scriptRequestBody}", "--user", "${jenkinsUsername}:${jenkinsToken}", "${jenkinsUrl}/script"].execute().text

now = new Date().format("yyy/MM/dd HH:mm:ss")
println "${now} - ${scriptResponse}"

// revoke old jenkins sonar access token
now = new Date().format("yyy/MM/dd HH:mm:ss")
println "${now} - Revoking any existing ${oldJenkinsSonarTokenName} access token"
def sonarApiRevokeTokensUrl = SonarApiClient.sonarApiUrl + 'user_tokens/revoke'
def oldJenkinsSonarTokenPair = SonarApiClient.buildSingleValuedKeyPair("name", oldJenkinsSonarTokenName)
queryValues = [oldJenkinsSonarTokenPair, jenkinsLoginPair]
queryString = SonarApiClient.buildQueryString(queryValues.iterator())
SonarApiClient.postQueryString(sonarApiRevokeTokensUrl, queryString)

now = new Date().format("yyy/MM/dd HH:mm:ss")
println "${now} - Configuring Jenkins... COMPLETE"

<% end %>

System.exit(0)
