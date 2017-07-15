#!/bin/bash -ex

if [[ "${TRAVIS_PULL_REQUEST}" == "false" && "${TRAVIS_BRANCH}" == "master" && "${TRAVIS_REPO_SLUG}" == "foundweekends/conscript" ]]; then
  eval "$(ssh-agent -s)" #start the ssh agent
  openssl aes-256-cbc -k "$SERVER_KEY" -in deploy_key.enc -d -a -out deploy_rsa
  chmod 600 deploy_rsa
  ssh-add deploy_rsa
  sbt pushSiteIfChanged
fi
