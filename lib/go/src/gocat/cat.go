package gocat

import (
	"ccat"
)

type Config struct {
	host       string
	port       string
	domain     string
	routerType int
}

func Init(domain string) {
	ccat.Init(domain)
	go ccat.Background()
}

func Shutdown() {
	ccat.Shutdown()
}

func Wait() {
	ccat.Wait()
}
