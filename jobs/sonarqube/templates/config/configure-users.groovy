import groovy.json.JsonSlurper

// Script must be called from the script directory to pick up the imported Class.
import SonarApiClient

List<Tuple> buildUserData() {
    def jsonSlurper = new JsonSlurper()
    def reader = new BufferedReader(new InputStreamReader(new FileInputStream("users.json"), "UTF-8"))
    def data = jsonSlurper.parse(reader)

    List<Tuple> users = []

    data.users.each {
        Tuple userTuple = new Tuple(it.name, it.login, it.password, it.group)
        System.out.println("Adding user ${userTuple}")
        users << userTuple
    }

    return users
}

def userData = buildUserData()

def sonarApiUsersUrl = SonarApiClient.sonarApiUrl + 'users/create'

for (user in userData) {
    def name = SonarApiClient.buildSingleValuedKeyPair('name', user.get(0))
    def login = SonarApiClient.buildSingleValuedKeyPair('login', user.get(1))
    def password = SonarApiClient.buildSingleValuedKeyPair('password', user.get(2))
    def queryValues = [name, login, password]
    def queryString = SonarApiClient.buildQueryString(queryValues.iterator())
    def querySucceeded = SonarApiClient.postQueryString(sonarApiUsersUrl, queryString)

    if (!querySucceeded) {
        System.exit(1)
    }
}

def sonarApiUserGroupsUrl = SonarApiClient.sonarApiUrl + 'user_groups/add_user'

for (user in userData) {
    def name = SonarApiClient.buildSingleValuedKeyPair('name', user.get(3))
    def login = SonarApiClient.buildSingleValuedKeyPair('login', user.get(1))
    def queryValues = [name, login]
    def queryString = SonarApiClient.buildQueryString(queryValues.iterator())
    def querySucceeded = SonarApiClient.postQueryString(sonarApiUserGroupsUrl, queryString)

    if (!querySucceeded) {
        System.exit(1)
    }
}

System.exit(0)
