#!/bin/bash -ex

if [[ "${JAVA_HOME}" == $(jdk_switcher home oraclejdk8) && "${TRAVIS_PULL_REQUEST}" == "false" && "${TRAVIS_BRANCH}" == "master" && "${TRAVIS_REPO_SLUG}" == "foundweekends/conscript" ]]; then
  echo -e "Host github.com\n\tStrictHostKeyChecking no\nIdentityFile ~/.ssh/deploy_rsa\n" >> ~/.ssh/config
  openssl aes-256-cbc -k "$SERVER_KEY" -in deploy_key.enc -d -a -out deploy_rsa
  chmod 600 deploy_rsa
  cp deploy_rsa ~/.ssh/
  sbt pushSiteIfChanged
fi
