#!/bin/bash
hostname -i | awk -F'.' '{printf("%s\n", $2*256*256 + $3*256 + $4)}'  