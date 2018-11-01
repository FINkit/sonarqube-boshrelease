package main

import (
    "os"
    "net/http"
    "github.com/DATA-DOG/godog"
    "io/ioutil"
    "fmt"
    "net/url"
    "time"
    "strings"
)

var sonarUrl string
var body string
var httpClient = &http.Client{
      Timeout: time.Second * 10,
}

const (
    SONAR_AUTH_ENDPOINT            string = "/api/authentication/login"
    SONAR_GITHUB_AUTH_ENDPOINT     string = "/sessions/new?return_to=%2Fprojects"
    SONAR_TEST_STANDALONE_USER     string = "teststandaloneuser"
    SONAR_TEST_STANDALONE_PASSWORD string = "TEST_STANDALONE_PASSWORD"
    SONAR_FAIL_TEST_PASSWORD       string = "iamafailureinlogins"
)

func getSonarEndPoint(path string) string {
    sonarUrl = os.Getenv("SONAR_URL")

    if sonarUrl == "" {
        panic("SONAR_URL is empty")
    }

    return sonarUrl + path
}

func thereIsASonarqubeInstall() error {
    sonarUrl = os.Getenv("SONAR_URL")
    return nil
}

func loginToSonarqube (username, password string) (*http.Response, error) {
    sonarAuthenticatonEndpoint := getSonarEndPoint(SONAR_AUTH_ENDPOINT)
    resp, err := httpClient.PostForm(sonarAuthenticatonEndpoint, url.Values{"login": {username}, "password": {password}})

    return resp, err
}

func iAccessTheLoginScreen() error {
    resp, err := http.Get(sonarUrl)
    if err != nil {
        return err
    }

    defer resp.Body.Close()
    body_bytes, err := ioutil.ReadAll(resp.Body)

    if err != nil {
        return err
    }

    body = string(body_bytes)
    return nil
}

func sonarQubeShouldBeUnlocked() error {
    if !strings.Contains(body, "SonarQube") {
        return fmt.Errorf("expected %s to contain 'SonarQube'", body)
    }
    return nil
}

func iAmAbleToLogin() error {
    username := SONAR_TEST_STANDALONE_USER
    password := os.Getenv(SONAR_TEST_STANDALONE_PASSWORD)

    resp, err := loginToSonarqube (username, password)

    defer resp.Body.Close()

    if resp.StatusCode != http.StatusOK {
        return fmt.Errorf("Login was unsuccessful. HTTP Status = %d", resp.StatusCode)
    }

    if err != nil {
        return err
    }

    return nil
}

func iAmUnableToLogin() error {
    username := SONAR_TEST_STANDALONE_USER
    password := SONAR_FAIL_TEST_PASSWORD

    resp, err := loginToSonarqube (username, password)

    defer resp.Body.Close()

    if resp.StatusCode != http.StatusUnauthorized {
        return fmt.Errorf("Login was successful and it should not have been. HTTP Status = %d", resp.StatusCode)
    }

    if err != nil {
        return err
    }

    return nil
 }

func iAmPresentedWithAGithubLoginOption() error {
    resp, err := http.Get(getSonarEndPoint(SONAR_GITHUB_AUTH_ENDPOINT))
    if err != nil {
        return err
    }

    defer resp.Body.Close()
    body_bytes, err := ioutil.ReadAll(resp.Body)

    if err != nil {
        return err
    }

    body = string(body_bytes)

    if !strings.Contains(body, "Log in with GitHub") {
        return fmt.Errorf("Expected response body to contain 'Log in with GitHub'")
    }
    return nil
}

func FeatureContext(s *godog.Suite) {
	s.Step(`^there is a sonarqube install$`, thereIsASonarqubeInstall)
	s.Step(`^I access the login screen$`, iAccessTheLoginScreen)
	s.Step(`^sonarqube should be unlocked$`, sonarQubeShouldBeUnlocked)
	s.Step(`^I am able to login$`, iAmAbleToLogin)
    s.Step(`^I am unable to login$`, iAmUnableToLogin)
    s.Step(`^I am presented with a Github login option$`, iAmPresentedWithAGithubLoginOption)
}
