#!/bin/sh -eux

set -o pipefail

DATE=$(date +%Y%m%d%H%M)
GOPATH="${PWD}"

go get github.com/GoogleCloudPlatform/cloudsql-proxy/proxy/dialers/mysql
go get github.com/GoogleCloudPlatform/cloudsql-proxy/proxy/proxy
go fmt run_cloudsql_query.go
go vet run_cloudsql_query.go
env GOOS=darwin GOARCH=386 go build -o run_cloudsql_query_darwin run_cloudsql_query.go
env GOOS=linux GOARCH=386 go build -o run_cloudsql_query-${DATE} run_cloudsql_query.go
