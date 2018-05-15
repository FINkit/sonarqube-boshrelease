import groovy.json.JsonSlurper

// Script must be called from the script directory to pick up the imported Class.
import SonarApiClient

List<Tuple> buildGroupPermissionsData() {
    def jsonSlurper = new JsonSlurper()
    def reader = new BufferedReader(new InputStreamReader(new FileInputStream("group-permissions.json"), "UTF-8"))
    def data = jsonSlurper.parse(reader)

    List<Tuple> groupPermissions = []

    data.groupPermissions.each {
        Tuple groupPermissionsTuple = new Tuple(it.name, it.description, it.permissions)
        System.out.println("Adding group permissions ${groupPermissionsTuple}")
        groupPermissions << groupPermissionsTuple
    }

    return groupPermissions
}

def sonarApiGroupsUrl = SonarApiClient.sonarApiUrl + 'user_groups/create'
def sonarApiPermissionsUrl = SonarApiClient.sonarApiUrl + 'permissions/add_group'

for (groups in buildGroupPermissionsData()) {
    def name = SonarApiClient.buildSingleValuedKeyPair('name', groups.get(0))
    def description = SonarApiClient.buildSingleValuedKeyPair('description', groups.get(1))
    def queryValues = [name, description]
    def queryString = SonarApiClient.buildQueryString(queryValues.iterator())
    def querySucceeded = SonarApiClient.postQueryString(sonarApiGroupsUrl, queryString)

    if (!querySucceeded) {
        System.exit(1)
    }

    def permissions = groups.get(2).tokenize(',')

    System.out.println("Adding permissions ${permissions}")

    for (permission in permissions) {
        def groupName = SonarApiClient.buildSingleValuedKeyPair('groupName', groups.get(0))
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
