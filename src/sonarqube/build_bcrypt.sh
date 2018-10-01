#!/bin/sh -eux

DATE=$(date +%Y%m%d%H%M)
GOPATH="${PWD}"

go get golang.org/x/crypto/bcrypt
go fmt bcrypt.go
go vet bcrypt.go
env GOOS=darwin GOARCH=386 go build -o bcrypt_darwin bcrypt.go
env GOOS=linux GOARCH=386 go build -o bcrypt-${DATE} bcrypt.go
