# stars-crawler
A demo Vert.X application

## Get started

* Pre-requisite: have a running OpenShift with [origin-metrics](https://github.com/openshift/origin-metrics).
* In your terminal, login with `oc login` then:

```bash
oc new-project stars-crawler
git clone https://github.com/jotak/stars-crawler
```

* You need to edit Cluster.java (in stars-crawler-common) to set your openshift oauth access token in 
Authorization header. To get your access token, the quickest is to open your browser on [openshift 
address]/oauth/token/request and login. In the long term, better is to [create a service account](https://docs.openshift.com/container-platform/3.3/rest_api/index.html#rest-api-serviceaccount-tokens).

* Back in your terminal:

```bash
oc new-build --binary --name=stars-crawler-http
oc new-build --binary --name=stars-crawler-stats
oc new-build --binary --name=stars-crawler-crawler
oc new-app stars-crawler-http
oc new-app stars-crawler-stats
oc new-app stars-crawler-crawler
oc expose service stars-crawler-http
```

## Deploy

```bash
mvn install
oc start-build stars-crawler-stats --from-dir=stats
oc start-build stars-crawler-http --from-dir=http
oc start-build stars-crawler-crawler --from-dir=crawler
```

