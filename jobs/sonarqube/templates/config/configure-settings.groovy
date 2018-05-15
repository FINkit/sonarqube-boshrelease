// Script must be called from the script directory to pick up the imported Class.
import SonarApiClient

def sonarApiSettingsUrl = SonarApiClient.sonarApiUrl + 'settings/set'
// Could connect to 'settings/get' after to test without exercising functionality.

def gitHubUrl = SonarApiClient.gitHubUrl

def keySingleValuePairs = [
    new Tuple2('sonar.core.serverBaseURL', SonarApiClient.sonarUrl),
    new Tuple2('sonar.auth.github.enabled', 'true'),
    new Tuple2('sonar.auth.github.apiUrl', gitHubUrl + '/api/v3/'),
    new Tuple2('sonar.auth.github.allowUsersToSignUp', 'true'),
    new Tuple2('sonar.auth.github.webUrl', gitHubUrl),
	new Tuple2('sonar.auth.github.clientSecret.secured', SonarApiClient.clientSecret),
    new Tuple2('sonar.auth.github.loginStrategy', 'Same as GitHub login'),
    new Tuple2('sonar.auth.github.clientId.secured', SonarApiClient.clientId)
]

for (pair in keySingleValuePairs) {
    def key = SonarApiClient.buildSingleValuedKeyPair('key', pair.first)
    def value = SonarApiClient.buildSingleValuedKeyPair('value', pair.second)
    def keyValues = [key, value]
    def queryString = SonarApiClient.buildQueryString(keyValues.iterator())
    def querySucceeded = SonarApiClient.postQueryString(sonarApiSettingsUrl, queryString)

    if (!querySucceeded) {
        System.exit(1)
    }
}

System.exit(0)
