package main

import (
	"fmt"
	"golang.org/x/crypto/bcrypt"
	"os"
)

func main() {
	if len(os.Args) > 1 {
		password := []byte(os.Args[1])
		encodedPassword, err := bcrypt.GenerateFromPassword(password, bcrypt.DefaultCost)

		if err != nil {
			fmt.Printf("Error encoding password: %v\n", err)
			os.Exit(2)
		}

		fmt.Printf("%s\n", string(encodedPassword))
	} else {
		fmt.Printf("Must specify a password to encode\n")
		os.Exit(1)
	}
}
