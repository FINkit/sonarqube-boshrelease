import groovy.json.JsonSlurper

// Script must be called from the script directory to pick up the imported Class.
import SonarApiClient

List<Tuple> buildGroupsData() {
    def jsonSlurper = new JsonSlurper()
    def reader = new BufferedReader(new InputStreamReader(new FileInputStream("groups.json"), "UTF-8"))
    def data = jsonSlurper.parse(reader)

    List<Tuple> groups = []

    data.groups.each {
        Tuple groupsTuple = new Tuple(it.name, it.description, it.permissions)
        System.out.println("Adding group ${groupsTuple}")
        groups << groupsTuple
    }

    return groups
}

def sonarApiGroupsUrl = SonarApiClient.sonarApiUrl + 'user_groups/create'
def sonarApiPermissionsUrl = SonarApiClient.sonarApiUrl + 'permissions/add_group'

for (group in buildGroupsData()) {
    System.out.println("Adding group ${group}")

    def name = SonarApiClient.buildSingleValuedKeyPair('name', group.get(0))
    def description = SonarApiClient.buildSingleValuedKeyPair('description', group.get(1))
    def queryValues = [name, description]
    def queryString = SonarApiClient.buildQueryString(queryValues.iterator())
    def querySucceeded = SonarApiClient.postQueryString(sonarApiGroupsUrl, queryString)

    if (!querySucceeded) {
        System.exit(1)
    }

    def permissions = group.get(2).tokenize(',')

    for (permission in permissions) {
        System.out.println("Adding permission ${permission}")

        def groupName = SonarApiClient.buildSingleValuedKeyPair('groupName', group.get(0))
        def permissionsPair = SonarApiClient.buildSingleValuedKeyPair('permission', permission)

    	queryValues = [groupName, permissionsPair]
        queryString = SonarApiClient.buildQueryString(queryValues.iterator())
        querySucceeded = SonarApiClient.postQueryString(sonarApiPermissionsUrl, queryString)

        if (!querySucceeded) {
            System.exit(1)
        }
    }
}

System.exit(0)
