# BOSH release for Sonarqube

## 1. Overview

This is a BOSH release of Sonarqube configured to work as part of FinKit.

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
