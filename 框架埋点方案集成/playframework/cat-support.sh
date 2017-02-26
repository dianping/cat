#!/bin/bash

unzip target/universal/[项目名].zip -d target/universal/

jar uf target/universal/[项目名]/lib/auto-mobile.auto-mobile-1.0-sans-externalized.jar META-INF/app.properties

ln -s target/universal/[项目名] [项目名]

rm -rf target/universal/[项目名].zip 

zip -r target/universal/[项目名].zip [项目名]

rm [项目名]

rm -rf target/universal/[项目名]
