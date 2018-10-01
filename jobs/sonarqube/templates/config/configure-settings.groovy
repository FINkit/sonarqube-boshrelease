// Script must be called from the script directory to pick up the imported Class.
import SonarApiClient

def sonarApiSettingsUrl = SonarApiClient.sonarApiUrl + 'settings/set'
// Could connect to 'settings/get' after to test without exercising functionality.

def gitHubUrl = SonarApiClient.gitHubUrl

def keySingleValuePairs = [
    new Tuple2('sonar.forceAuthentication', 'true'),
]

def querySucceeded = false

for (pair in keySingleValuePairs) {
    def key = SonarApiClient.buildSingleValuedKeyPair('key', pair.first)
    def value = SonarApiClient.buildSingleValuedKeyPair('value', pair.second)
    def keyValue = [key, value]
    def queryString = SonarApiClient.buildQueryString(keyValue.iterator())

    querySucceeded = SonarApiClient.postQueryString(sonarApiSettingsUrl, queryString)

    if (!querySucceeded) {
        System.exit(1)
    }
}

System.exit(0)
