package main

import (
	"fmt"
	"github.com/GoogleCloudPlatform/cloudsql-proxy/proxy/dialers/mysql"
	"github.com/GoogleCloudPlatform/cloudsql-proxy/proxy/proxy"
	"golang.org/x/net/context"
	"golang.org/x/oauth2/google"
	"io/ioutil"
	"os"
)

const (
	numberOfCommandLineArgs = 6
	sqlScope                = "https://www.googleapis.com/auth/sqlservice.admin"
)

func main() {
	if len(os.Args) != (numberOfCommandLineArgs + 1) {
		fmt.Printf("Usage: ./run_cloudsql_query instanceName databaseName username password credsFile query\n")
		os.Exit(1)
	}

	instanceName := os.Args[1]
	databaseName := os.Args[2]
	username := os.Args[3]
	password := os.Getenv(os.Args[4])
	credsFile := os.Args[5]
	query := os.Args[6]

	ctx := context.Background()

	creds, err := ioutil.ReadFile(credsFile)

	if err != nil {
		fmt.Printf("Error reading credsFile %s: %v\n", credsFile, err)
	}

	authCfg, err := google.JWTConfigFromJSON(creds, sqlScope)

	if err != nil {
		fmt.Printf("Error creating JWT config: %v\n", err)
	}

	client := authCfg.Client(ctx)

	proxy.Init(client, nil, nil)

	cfg := mysql.Cfg(instanceName, username, password)
	cfg.DBName = databaseName
	db, err := mysql.DialCfg(cfg)

	if err != nil {
		fmt.Printf("Error connecting to database: %v\n", err)
		os.Exit(2)
	}

	defer db.Close()

	// If using a query that returns a result, adapt the following code based on the number and type of rows and columns expected.
	//var queryResult string
	//err = db.QueryRow(query).Scan(&queryResult)

	db.QueryRow(query)

	//if err != nil {
	//	fmt.Printf("Error running query on database: %v\n", err)
	//	os.Exit(3)
	//}

	//fmt.Println(queryResult)
}
