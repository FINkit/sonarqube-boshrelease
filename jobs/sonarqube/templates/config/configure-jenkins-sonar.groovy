import jenkins.model.*
import hudson.tasks.*
import hudson.plugins.sonar.*
import hudson.plugins.sonar.model.*

def instance = Jenkins.getInstance()
def descriptor = instance.getDescriptor("hudson.plugins.sonar.SonarGlobalConfiguration")
def sonarAuthToken = "SONAR_AUTH_TOKEN"

def sonarInstallation = new SonarInstallation(
  "sonar",
  "",
  "${sonarAuthToken}",
  "",
  "",
  new TriggersConfig(),
  ""
)

descriptor.setInstallations(sonarInstallation)
descriptor.save()
