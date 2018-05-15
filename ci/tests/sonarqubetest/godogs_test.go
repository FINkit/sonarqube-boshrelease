package main

import (
	"strings"
	"fmt"
	"os"
	"net/http"
	"io/ioutil"
	"github.com/DATA-DOG/godog"
)

var url string
var body string

func thereIsASonarQubeInstall() error {
	url = os.Getenv("SONARQUBE_HOST")
	return nil
}

func iAccessTheLoginScreen() error {
	resp, err := http.Get(url)
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

func FeatureContext(s *godog.Suite) {
	s.Step(`^there is a sonarqube install$`, thereIsASonarQubeInstall)
	s.Step(`^I access the login screen$`, iAccessTheLoginScreen)
	s.Step(`^sonarqube should be unlocked$`, sonarQubeShouldBeUnlocked)
}

