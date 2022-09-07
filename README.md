
# LAB - CI with GitHub actions 

## A bit about the example app

A Norwegian bank has spent several years and hundreds of millions developing a modern core system for banking and a "forward-leaning" API that almost satisfies Directive (EU) 2015/2366 of the European Parliament and of the Council on Payment Services in the Internal Market, published 25 November 2016 also known as PSD.

This is an important investment in the area of ​​"Open Banking" for SkalBank.

Architecturally, the system consists of two components.

An API, implemented using Spring Boot. The code for the application is located in this repo.
A core system that carries out transactions with other banks, settles against Norges Bank, etc. You can pretend to be method calls
which is done against the ```ReallyShakyBankingCoreSystemService''' class, communicates with this system.

In this exercise, we will look at important DevOps principles such as

- GitHub actions
- Trunk based development 
- Feature branches
- Branch protection 
- Pull requests

You will also get to know the Cloud 9 development environment you will use further. 

## Before you start

- You need a GitHub account
- Create a fork of this repository into your own GitHub account

![Alt text](img/fork.png  "a title")

### Check out your Cloud 9 environment in AWS and get to know it

```text
ATTENTION! Cloud 9 does not save documents automatically! You have to do ctrl+s in the editor yourself after you have done
amendments.
```

* URL for login is https://244530008913.signin.aws.amazon.com/console
* The username and password are given in the classroom

* From the main menu, search for the service "cloud9"

![Alt text](img/11.png  "a title")

* Select "your environments" from the left menu if you do not see any environments with your name
* If you don't see something to press that has your name on it, make sure you're in the right region (provided in the classroom)
* Select "Open IDE"

You now have to wait a bit while Cloud 9 starts
@

* If you select the "9" icon on the top left of the main menu you will see "AWS Explorer". Feel free to navigate around the AWS environment to get familiar.
* Get to know the IDE, navigate around.

![Alt text](img/cloud9.png  "a title")

Start a new terminal in Cloud 9 by pressing the (+) symbol on the tabs
![Alt text](img/newtab.png  "a title")

Run this command to verify that Java 11 is installed

```shell
java -version
```
You will get 
```
openjdk 11.0.14.1 2022-02-08 LTS
OpenJDK Runtime Environment Corretto-11.0.14.10.1 (build 11.0.14.1+10-LTS)
OpenJDK 64-Bit Server VM Corretto-11.0.14.10.1 (build 11.0.14.1+10-LTS, mixed mode)
```

### Install Maven in Cloud 9 

Copy these commands into the Cloud9 terminal. They will install Maven. 
```shell
sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
sudo yum install -y apache-maven
```

### Create an Access Token for GitHub

When you need to authenticate against your GitHub account from Cloud 9, you need an access token. Go to https://github.com/settings/tokens and create a new one. 

![Alt text](img/generate.png  "a title")

The access token must have "repo" permissions, and "workflow" permissions.

![Alt text](img/new_token.png  "a title")

### Create a clone of your Fork (of this repo) into your Cloud 9 environment

To avoid having to authenticate yourself all the time, you can make git cache keys in an optional
number of seconds. 

* ATTENTION! Assume that it is possible for colleagues to gain access to your Cloud 9 environment.   

```shell
git config --global credential.helper "cache --timeout=86400"
```

Create a clone

```shell
git clone https://github.com/≤github bruker>/01-CI-Github-actions.git
```

* Try to run the application 
```shell
cd 01-CI-Github-actions
mvn spring-boot:run
```

Start a new terminal in Cloud 9 by pressing the (+) symbol on the tabs
![Alt text](img/newtab.png  "a title")

You can test the application with CURL from Cloud 9

```
curl -X POST \
http://localhost:8080/account/1/transfer/2 \
-H 'Content-Type: application/json' \
-H 'Postman-Token: e674b4f3-6e48-41a0-9e6f-de155a4baf02' \
-H 'cache-control: no-cache' \
-d '{
"amount": 1500
}'
```

Remember that this is the application "Shakybank", a 500 Internal server error is very common :-)
```json
{
  "timestamp": "2022-04-04T21:34:45.542+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "",
  "path": "/account/1/transfer/2"
}
```
When you get no output from the terminal after the CURL command, the request has gone well. 

## Create a GitHub Actions workflow
Use Cloud 9 to create two folders and a file called ````.github/workflows/main.yml```` under the root folder of the repository you cloned.
NB!
Remember to press ctrl+s after you have created this file in cloud 9, otherwise you will check in an empty file and your workflow will not work
```yaml
# This workflow will build a Java project with Maven, and cache/restore any dependencies to improve the workflow execution time
# For more information see: https://help.github.com/actions/language-and-framework-guides/building-and-testing-java-with-maven
name: Java CI with Maven
on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    - name: Set up JDK 11
      uses: actions/setup-java@v2
      with:
        java-version: '11'
        distribution: 'adopt'
        cache: maven
    - name: Build with Maven
      run: mvn -B package --file pom.xml
```
* ATTENTION! If you later can't find this file again, it's because Cloud 9 by default hides files and folders starting with ". If that happens, select the "cogwheel" at the top right of the file explorer, and select "show hidden files"

* This is a very simple *workflow* with a *job* that has a number of *steps*. The code is checked out. JDK11 is configured,
Maven creates an installation package.

Commit and push to your repo. 

```shell
cd 01-CI-Github-actions
git add .github/workflows/main.yml 
git commit -m"workflow"
git push
```

*NOTE*
When you do a ```git push''' you must authenticate yourself. You must use a GitHub Access token when prompted for a password.

## Check that workflow is activated

* Go to your fork of this repo on Github
* Select "Actions" - you should see that a job has been run.

* ![Alt text](img/workflow.png  "a title")

Make a change in the code, preferably in the main branch, commit and push. Observe while the commit event starts the WorkFlow, and the job runs.

## Configure main as protected branch

![Alt text](img/branches.png  "a title")

We will now ensure that only code that compiles and with tests that run enters the main branch.
It is also good practice not to commit code directly on main, so we will make it impossible to do this.

By configuring main as a protected branch, and by using "status checks" we can 
- Moved to your fork of this repo.
- Go to Settings/Branches and Look for the "Branch Protection Rules" section.
- Select *Add*
- Select *main* as branch
- Select ```require a pull request before merging```
- Select ````Require status check to pass before merging````
- In the search field enter the text *build* which should let you select "GitHub Actions". 

* Now we cannot merge a pull request into Main without the status check being OK. It means that our Workflow has run OK. 
* Nor can anyone in the team "sneak away" from this check by committing code directly to the main branch.
* A good start!

## Test to break the code 

- Create a new branch 

```
git checkout -b will_break_4_sure
```
- Make a compile error
- Commit and push the change to GitHub 

```shell
 git add src/
 git commit -m"compilation error introduced"
 git push --set-upstream origin will_break_4_sure
```

- ATTENTION! GitHub selects the repository you made the fork FROM as the source when you make a pull request the first time. You need to change the dropdown to your own repo.
- Go to your repo on GitHub.com and try to make a pull request from your branch ```will_break_4_sure``` to main. 
- Check that you are not allowed to do a Merge because the code does not compile


## Peer review

- Go to gitHub.com and your fork of this repo.
- Go to Settings/Branches and Look for the "Branch Protection Rules" section.
- Select *main* branch
- Select "Edit" for the existing branch protection rule
- Under ````Require a pull request before passing````
- Then tick the box ````Require approvals````

## Test

![Alt text](img/addpeople.png  "a title")
 
- Add another person as a "collaborator" in your repo
- Go to Github and create a new Pull request, as shown above
- Get the person to approve your pull request
- Feel free to try to provoke an error by causing a unit test to fail. 
- Note that it is still possible to merge ```main```.

## Bonus challenge 

- Make a feature branch from main - make many commits on this where you only fix typos, for example. 
- Create a PR against main, where you "squash" the unnecessary commits in an interactive rebase ```git rebase -i origin/main``` 

## Bonus challenge 2

- Can you find any open "actions" for Github that, for example, check code quality or any vulnerabilities in dependencies?

Finished!
