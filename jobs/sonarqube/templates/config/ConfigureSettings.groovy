import org.yaml.snakeyaml.*

// Script must be called from the script directory to pick up the imported Class.
import SonarApiClient

sonarApiSettingsUrl = SonarApiClient.sonarApiUrl + 'settings/set'
// Could connect to 'settings/get' after to test without exercising functionality.

separator = ','

// In 7.1, a different API is used for defining webhooks - see https://jira.sonarsource.com/browse/SONAR-9058.

void addWebhook(String webhooksKey, int number, String key, String value) {
    String numberString = Integer.toString(number)
    String numberedKey = SonarApiClient.buildSingleValuedKeyPair('key', "${webhooksKey}.${numberString}.${key}") 

    String valueKeyPair = SonarApiClient.buildSingleValuedKeyPair('value', value)

    final List<String> keyValues = [numberedKey, valueKeyPair]

    String queryString = SonarApiClient.buildQueryString(keyValues.iterator())
    final boolean querySucceeded = SonarApiClient.postQueryString(sonarApiSettingsUrl, queryString)

    if (!querySucceeded) {
        System.exit(1)
    }
}

void addWebhooks(String key, ArrayList<String> webhookNameUrlPairs) {
    boolean querySucceeded = false
    String webhooksKey = SonarApiClient.buildSingleValuedKeyPair('key', key) 

    int numberOfPairs = webhookNameUrlPairs.size()

    if (numberOfPairs <= 0) {
        println "Number of webhook pairs was not positive - ${numberOfPairs}"
        System.exit(3)
    }

    String webhookNumbers = "1"

    if (numberOfPairs > 1) {
        (2..webhookNameUrlPairs.size()).each {
            webhookNumbers = "${webhookNumbers}${separator}${Integer.toString(it)}"
        }
    }

    String webhookNumbersKeyPair = SonarApiClient.buildSingleValuedKeyPair('value', webhookNumbers)
    final ArrayList<String> numbersKeyValues = [webhooksKey, webhookNumbersKeyPair]

    String queryString = SonarApiClient.buildQueryString(numbersKeyValues.iterator())

    querySucceeded = SonarApiClient.postQueryString(sonarApiSettingsUrl, queryString)

    if (!querySucceeded) {
        System.exit(1)
    }

    int number = 0

    webhookNameUrlPairs.each {
        String[] webhook = it.split(separator)

        if (webhook.length == 2) {
            String name = webhook[0]
            String url = webhook[1]

            addWebhook(key, ++number, "name", name)
            addWebhook(key, number, "url", url)
        } else {
            println "Webhook data ${webhookNameUrlPair} is not formed as a pair"
            System.exit(2)
        }
    }
}

final DumperOptions options = new DumperOptions()

options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)

final Yaml yaml = new Yaml(options)

def globalWebhooks = yaml.load(SonarApiClient.globalWebhooks)

addWebhooks('sonar.webhooks.global', globalWebhooks)

System.exit(0)
