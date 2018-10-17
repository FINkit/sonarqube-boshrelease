import groovy.json.JsonSlurper

// Script must be called from the script directory to pick up the imported Class.
import SonarApiClient

static List<Tuple> buildUserData() {
    def jsonSlurper = new JsonSlurper()
    def reader = new BufferedReader(new InputStreamReader(new FileInputStream("users.json"), "UTF-8"))
    def data = jsonSlurper.parse(reader)

    List<Tuple> users = []

    data.users.each {
        Tuple userTuple = new Tuple(it.name, it.login, it.password, it.group, it.globalPermissions, it.projectPermissions)
        System.out.println("Adding user ${userTuple}")
        users << userTuple
    }

    return users
}

static void addPermission(login, permission, permissionsUrl) {
    addPermission(login, permission, permissionsUrl, "")
}

static void addPermission(login, permission, permissionsUrl, template) {
    System.out.println("Adding permission ${permission} for ${login}")

    def loginPair = SonarApiClient.buildSingleValuedKeyPair('login', login)
    def permissionsPair = SonarApiClient.buildSingleValuedKeyPair('permission', permission)
    def queryValues = [loginPair, permissionsPair]

    // Currently this seems only to be needed for project permissions.
    // The API claims both are optional, but when not included, an error is specified indicating one of the other, not both, is required.
    if (template && !template.allWhitespace) {
        def templatePair = SonarApiClient.buildSingleValuedKeyPair('templateName', template)
        queryValues = [loginPair, permissionsPair, templatePair]
    }

    def queryString = SonarApiClient.buildQueryString(queryValues.iterator())
    def querySucceeded = SonarApiClient.postQueryString(permissionsUrl, queryString)

    if (!querySucceeded) {
        System.exit(1)
    }
}

def userData = buildUserData()

def sonarApiUsersUrl = SonarApiClient.sonarApiUrl + 'users/create'
def sonarApiGlobalPermissionsUrl = SonarApiClient.sonarApiUrl + 'permissions/add_user'
def sonarApiProjectPermissionsUrl = SonarApiClient.sonarApiUrl + 'permissions/add_user_to_template'

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

    def globalPermissionsList = user.get(4)

    if (globalPermissionsList && !globalPermissionsList.allWhitespace) {
        System.out.println("Adding global permissions list ${globalPermissionsList}")

        def globalPermissions = globalPermissionsList.tokenize(',')

        for (globalPermission in globalPermissions) {
            addPermission(user.get(1), globalPermission, sonarApiGlobalPermissionsUrl)
        }
    }

    def projectPermissionsList = user.get(5)

    if (projectPermissionsList && !projectPermissionsList.allWhitespace) {
        System.out.println("Adding project permissions list ${projectPermissionsList}")

        def projectPermissions = projectPermissionsList.tokenize(',')

        for (projectPermission in projectPermissions) {
            addPermission(user.get(1), projectPermission, sonarApiProjectPermissionsUrl, 'Default Template')
        }
    }
}

def sonarApiUserGroupsUrl = SonarApiClient.sonarApiUrl + 'user_groups/add_user'

for (user in userData) {
    def group = user.get(3)

    if (group && !group.allWhitespace) {
        def groupName = SonarApiClient.buildSingleValuedKeyPair('name', user.get(3))
        def login = SonarApiClient.buildSingleValuedKeyPair('login', user.get(1))
        def queryValues = [groupName, login]
        def queryString = SonarApiClient.buildQueryString(queryValues.iterator())
        def querySucceeded = SonarApiClient.postQueryString(sonarApiUserGroupsUrl, queryString)

        if (!querySucceeded) {
            System.exit(1)
        }
    }
}

System.exit(0)
