# stars-crawler
A demo Vert.X application

## Get started

* Pre-requisite: have a running OpenShift with [origin-metrics](https://github.com/openshift/origin-metrics).
* In your terminal, login with `oc login` then:

```bash
oc new-project stars-crawler
git clone https://github.com/jotak/stars-crawler
```

* You need to edit Main.java (in src/main/java/com/jotak/stars) to set your openshift oauth access token in 
Authorization header. To get your access token, the quickest is to open your browser on [openshift 
address]/oauth/token/request and login. In the long term, better is to [create a service account](https://docs.openshift.com/container-platform/3.3/rest_api/index.html#rest-api-serviceaccount-tokens).

* Back in your terminal:

```bash
cd stars-crawler
oc new-build --binary --name=stars-crawler
mvn package; oc start-build stars-crawler --from-dir=. --follow
oc new-app stars-crawler
oc expose service stars-crawler
```

## Redeploy

To redeploy, just run again `mvn package; oc start-build stars-crawler --from-dir=. --follow`
