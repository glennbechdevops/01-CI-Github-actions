
# LAB - CI med GitHub actions 

## Mål for denne Øvingen 

Øvingen er relevant for forelesning #2 og temaet kontinuerlig integrasjon 

* Du skal bli kjent med GitHub Actions, som er helt sentralt for resten av semesteret.
* Du vil lære hvordan GitHub Actions kan brukes til automatisk kompilering, testing og bygging av Java/Spring Boot-applikasjoner.
* Du skal også lære å sette opp et repository for samarbeid med pull requests og beskytte hovedgrenen (main branch) for et utviklingsteam.

## Litt om eksempel-appen

Dette er den samme applikasjonen som i Lab 1. Husk på at den er litt ustabil med hensikt, det er tross alt en Bank App :-)

En norsk bank har brukt flere år og hundretalls milioner på å utvikle et moderne kjernesystem for bank og et "fremoverlent" API som nesten tilfredsstiller Directive (EU) 2015/2366 of the European Parliament and of the Council on Payment Services in the Internal Market, published 25 November 2016 også kjent som PSD.

Dette er en viktig satsning innen området "Open Banking" for SkalBank.

Arkitekturmessig består systemet av to komponenter.

Et API, implementert ved hjelp av Spring Boot. Koden for applikasjonen ligger i dette repoet.
Et kjernesystem som utfører transaksjoner med andre banker, avregner mot Norges bank osv. Dere kan late som metodekall 
som gjøres mot klassen ```ReallyShakyBankingCoreSystemService```, kommuniserer med dette systemet.

I denne øvingen skal vi se på viktige DevOps prinsipper som 

- GitHub actions
- Trunk based development 
- Feature branches
- Branch protection 
- Pull requests

Dere blir også kjent med Cloud 9 utviklingsmiljøet dere skal bruke videre. 

## Før dere starter

- Dere trenger en GitHub Konto
- Lag en fork av dette repositoriet inn i egen GitHub konto

### Sjekk ut Cloud 9 miljøet ditt i AWS og bli kjent med det

```text
OBS! Cloud 9 lagrer ikke dokumenter automatisk! Du må selv gjøre ctrl+s i editoren etter du har gjort
emdringer.
```

* URL for innlogging er https://244530008913.signin.aws.amazon.com/console
* Brukernavnet og passordet er gitt i klasserommet

* Fra hovedmenyen, søk etter tjenesten "cloud9"

![Alt text](img/11.png  "a title")

* Velg "your environments" fra venstremenyen hvis du ikke ser noen miljøer med ditt navn
* Hvis du ikke ser noe å trykke på som har ditt navn, pass på at du er i rett region (gitt i klasserommet)
* Velg "Open IDE"

Du må nå vente litt mens Cloud 9 starter

* Hvis du velger "9" ikonet på øverst til venstre i hovedmenyen vil du se "AWS Explorer". Naviger gjerne litt rundt I AWS Miljøet for å bli kjent.
* Blir kjent med IDE, naviger rundt.

![Alt text](img/cloud9.png  "a title")

Start en ny terminal i Cloud 9 ved å trykke (+) symbolet på tabbene
![Alt text](img/newtab.png  "a title")

Kjør denne kommandoen for å verifisere at Java 11 er installert

```shell
java --version
```
Du skal få 
```
openjdk 11.0.14.1 2022-02-08 LTS
OpenJDK Runtime Environment Corretto-11.0.14.10.1 (build 11.0.14.1+10-LTS)
OpenJDK 64-Bit Server VM Corretto-11.0.14.10.1 (build 11.0.14.1+10-LTS, mixed mode)
```

### Installer Maven i Cloud 9 

Kopier disse kommandoene inn i Cloud9 terminalen. De vil installere Maven. 
```shell
sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
sudo yum install -y apache-maven
```

### Lag et Access Token for GitHub

Når du skal autentisere deg mot din GitHub konto fra Cloud 9 trenger du et access token.  Gå til  https://github.com/settings/tokens og lag et nytt.
(Velg "Classic" token, ikke "Beta")

![Alt text](img/generate.png  "a title")

Access token må ha "repo" tillatelser, og "workflow" tillatelser.

![Alt text](img/new_token.png  "a title")

### Lage en klone av din Fork (av dette repoet) inn i ditt Cloud 9 miljø

For å slippe å autentisere seg hele tiden kan man få git til å cache nøkler i et valgfritt 
antall sekunder. 

* OBS! Anta at det er mulig for kollegaer å få tilgang til ditt Cloud 9 miljø.   

```shell
git config --global credential.helper "cache --timeout=86400"
```

Lag en klone

```shell
git clone https://github.com/≤github bruker>/01-CI-Github-actions.git
```

* Forsøk å kjøre applikasjonen 
```shell
cd 01-CI-Github-actions
mvn spring-boot:run
```

Start en ny terminal i Cloud 9 ved å trykke (+) symbolet på tabbene
![Alt text](img/newtab.png  "a title")

Du kan teste applikasjonen med CURL fra Cloud 9

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

Husk at dette er applikasjonen "Shakybank", en 500 Internal server error er *svært vanlig* :-)
```json
{
  "timestamp": "2022-04-04T21:34:45.542+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "",
  "path": "/account/1/transfer/2"
}
```
Når du ikke får noe output fra terminalen etter CURL kommandoen har requesten gått bra. 

## Lag en GitHub Actions workflow

Bruk  Cloud 9 til å lage to mapper og en fil som heter ````.github/workflows/main.yml```` under rotmappen til repositoriet du har klonet.
NB!
Husk å trykke ctrl+s etter du har laget denne filen i cloud 9, hvis ikke vil du sjekke inn en tom fil, og din workflow vil ikke fungere
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
* OBS! Hvis du senere ikke finner igjen denne filen, er det fordi Cloud 9 default skjuler filer og mapper som begynner på ". Hvis det skjer, velg "tannhjulet" øverst til høyre i fil-explorer, og velg "show hidden files"

* Dette er en vekdig enkel *workflow* med en *job* som har en rekke *steps*. Koden sjekkes ut. JDK11 konfigureres,
Maven lager en installasjonspakke.

Commit og push til ditt repo. 

```shell
cd 01-CI-Github-actions
git add .github/workflows/main.yml 
git commit -m"workflow"
git push
```

*OBS*
Når du gjør en ```git push``` må du autentisere deg. Du må bruke et GitHub Access token når du blir bedt om passord.

## Sjekk at workflow er aktivert 

* Gå til din fork av dette repoet på Github 
* Velg "Actions" - du skal se at en jobb er kjørt.

* ![Alt text](img/workflow.png  "a title")

Gjør en endring i koden, gjerne i main branch, commit og push. Observer mens commit hendelsen starter WorkFlowen, og jobben kjører.

## Konfigurer main som beskyttet branch

![Alt text](img/branches.png  "a title")

Vi skal nå sørge for at bare kode som kompilerer og med tester som kjører, inn i main branch.
Det er også bra praksis å ikke comitte kode direkte på main, så vi skal gjøre det umulig å gjøre dette. 

Ved å konfigurerere main som en beskyttet branch, og ved å bruke "status sjekker" kan vi 
- Gåt til din fork av dette repoet.  
- Gå til Settings/Branches og Se etter seksjonen "Branch Protection Rules".
- Velg *Add*
- Velg *main* Som branch
- Velg ```require a pull request before merging```
- Velg ````Require status check to pass before merging````
- Velg ```Do not allow bypassing the above settings```
- I søkefeltet skriv inn teksten *build* som skal la deg velge "GitHub Actions". 

* Nå kan vi ikke Merge en pull request inn i Main uten at status sjekken er i orden. Det betyr at vår Workflow har kjørt OK. 
* Ingen i teamet kan nå "snike seg unna" denne sjekken ved å comitte kode rett på main branch, selv ikke admin
* En bra start!

## Test å brekke koden 

- Lag en ny branch 

```
git checkout -b will_break_4_sure
```
- Lag en kompileringsfeil
- Commit og push endringen til GitHub 

```shell
 git add src/
 git commit -m"compilation error introduced"
 git push --set-upstream origin will_break_4_sure
```

- OBS! GitHub velger repository du lagde forken FRA som kilde når du lager en pull request første gang. Du må endre nedtrekksmenyen til ditt eget repo.
- Gå til ditt repo på GitHub.com og forsøk å lage en Pull request fra din branch ```will_break_4_sure``` til main. 
- Sjekk at du ikke får lov til å gjøre en Merge fordi koden ikke kompilerer

## Peer review

- Gå til gitHub.com og din fork av dette repoet.
- Gå til Settings/Branches og Se etter seksjonen "Branch Protection Rules".
- Velg *main* branch
- Velg "Edit" for  eksisterende branch protection rule
- Under ````Require a pull request before passing````
- Kryss deretter av for ````Require approvals````

## Test

![Alt text](img/addpeople.png  "a title")
 
- Legg til en annen person som "collaborator" i ditt repo
- Gå til Github og lag en ny Pull request, som vist over 
- Få personen til å godkjenne din pull request
- Forsøk gjerne å fremprovosere en feil ved å få en unit test til å feile. 
- Legg merke til at det fortsatt er mulig å merge til ```main```.

## Oppgave 

Lag et eget repo,  i ditt GitHub repo - helt fra scratch, og konfigurer alle elementene fra denne øvingen.  

## Oppgave 

- Lag en feature branch fra main -lag mange commits på denne hvor du for eksempel bare fikser skrivefeil. Lag deretter en pull requrst mot main branch, der du "squasher/fixup" de unødvendige committene i en interaktive rebase ```git rebase -i origin/main``` 

## Oppgave

- Kan du finne noen åpne "actions" for Github som for eksempel sjekker kodekvalitet eller eventuelle sårbarheter i avhengigheter ?
- Dykk mer selv i GitHub Actions

Ferdig!
