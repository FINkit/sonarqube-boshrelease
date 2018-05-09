# BOSH release of SonarQube

[![Build Status](https://travis-ci.org/FinKit/sonarqube-boshrelease.svg?branch=master)](https://travis-ci.org/FinKit/sonarqube-boshrelease)

## 1. Overview

This is a BOSH release of SonarQube, configured with a base set of plugins.

## 2. Release

### Create

```
bosh -e MY_ENV \
  create-release
```
### Upload

```
bosh -e MY_ENV \
  upload-release
```
