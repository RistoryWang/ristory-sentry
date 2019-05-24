# ristory-sentry

### 1.0.5 `init verison`
##### Config
* change default config file from 'sentry.properties' to 'application.properties'

##### Lookup
* dist、environment、app_name、os_arch、os_name、os_version

##### LogInterceptor
* MDC(ThreadContext):request_id,host_ip,host_port,client_ip,user_id,url,verb,params
* Add unified http header: "X-PG-Request-ID"
* applicaiton.properties config demo


```
###sentry-basic
dsn=http://abcdefg@a.b.c.d:9070/2
stacktrace.app.packages=
release=@project.version@
#app.name=
#dist=
#environment=
#servername=
#tags=
mdctags=request_id,host_ip,host_port,client_ip,user_id,url,verb,params
extra=key1:value1,key2:value2
stacktrace.hidecommon=false
sample.rate=1.0
uncaught.handler.enabled=true
buffer.dir=/home/deployer/sentry-events/@project.artifactId@
###sentry-advance
buffer.size=1000
buffer.flushtime=30000
buffer.shutdowntimeout=5000
buffer.gracefulshutdown=true
async=true
async.shutdowntimeout=5000
async.gracefulshutdown=true
async.queuesize=100
async.threads=5
async.priority=10
compression=false
maxmessagelength=10240
timeout=10000
```

### 1.0.6 `add tag:app_name`

* fetch from "app.name" first
* otherwise from  "spring.applicaiton.name"

### 1.0.7 `add log custom level`

* pom dependency add scope provided
* add log4j2 custom level
* add custom console color
* add sentry tag:pg_level 
* sentry.pg_level = log4j2.level
* log4j2.level => sentry.level (cast to sentry's supported log type)

```
EMER to ERROR     <CustomLevel name="EMER" intLevel="150" />     
BEAT to WARNING   <CustomLevel name="BEAT" intLevel="230" />     
BIZ to WARNING    <CustomLevel name="BIZ" intLevel="270" />     
DIAG to INFO      <CustomLevel name="DIAG" intLevel="350" />     
NOTICE to DEBUG   <CustomLevel name="NOTICE" intLevel="450" />     
VERBOSE to DEBUG  <CustomLevel name="VERBOSE" intLevel="550" />     
```

### 1.0.8 `add ConnectionFactory`

* ConnectionFactory used to provide dbdatasource to log4j2
* Notice config in ConnectionFactory cannot get from SpringF，because of log4j loaded before SpringF

