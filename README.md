# XNAT Transporter Plugin
 

The XNAT Transporter supports the creation of Data Snapshots and the transport of those snapshots to a remote host/client by way of the SCP protocol.

Transporter functionality is implemented across two components: the [XNAT Plugin](https://github.com/kelseym/transporter-plugin) and the [Transporter App](https://github.com/kelseym/transporter-app
).

![Transporter](https://drive.google.com/uc?id=1jQ01d_IpH4SPsQsTrAaDmZbfAF5J6PMi)

Snapshot creation is currently supported via [python script](https://github.com/kelseym/transporter-plugin/tree/main/snapshot-container) and the Docker container (`xnat/data-snapshot:0.1`).

## Beta Version Plugin 
* This plugin is built on XNAT version 1.8.9.x
* This plugin is in Beta release and is subject to breaking changes without notice.
* Building the plugin relies on Maven Central as well as a Maven repository hosted by the XNAT team


### Build and install the plugin
1. Run `./gradlew clean jar` in the transporter-plugin folder. To build without running tests, run `./gradlew clean jar -x test` 
2. Copy build/libs/xnat-transporter-plugin-x.x.x.jar to the XNAT plugins folder
3. Stop and start tomcat


### Plugin Configuration

#### Notes for Docker based XNAT deployment:
These notes deal with similar concepts covered in xnat-docker-compose [Path Translation](https://github.com/NrgXnat/xnat-docker-compose#path-translation). Knowledge of that topic will be helpful in understanding the configuration options here. 

***Docker Compose Configuration***

The Transporter plugin builds snapshot images by creating symlinks to folders and files in the XNAT archive. The target path of this link needs to be resolvable by the remote Transporter application and by code running within XNAT under Tomcat. Because a containerized XNAT/Tomcat sees only directories mounted to the container during startup, we must take steps to expose the host view of our snapshot build directory to XNAT using an identical path within the contianer.

For example, on a host system the XNAT_HOME directory is mapped from `/data/xnat` within the container, to `/Users/UserHome/Projects/XNAT/xnat-docker-compose/xnat-data` on the host. To expose this host path (at the same path) within the XNAT container, we need to add a bind-mound directive to our docker-compose.yml file.
```
    volumes:
      - ./xnat/webapps:/usr/local/tomcat/webapps
      - ./xnat/plugins:${XNAT_HOME}/plugins
      - ./xnat-data/home/logs:${XNAT_HOME}/logs
      - ./xnat-data/archive:${XNAT_ROOT}/archive
      - ./xnat-data/build:${XNAT_ROOT}/build
      - ./xnat-data/cache:${XNAT_ROOT}/cache
      - /var/run/docker.sock:/var/run/docker.sock
      - /Users/UserHome/Projects/XNAT/xnat-docker-compose/xnat-data/build:/Users/UserHome/Projects/XNAT/xnat-docker-compose/xnat-data/build
```      

The last line in this  block allows the Transporter plugin code to create host-accessible simlinked files and folders with a target under `/Users/UserHome/Projects/XNAT/xnat-docker-compose/xnat-data/build`.

***Plugin Path Mapping***

The second component to supporting different views of the file system, e.g. docker-compose based XNAT deployment, is to tell the Transporter Plugin about this path mapping. Via REST API or Swagger, configure the `
/transporter/path-mapping` endpoint to:
```
{
  "xnatRootPath": "/data/xnat",
  "serverRootPath": "/Users/UserHome/Projects/XNAT/xnat-docker-compose/xnat-data"
}
```
where `xnatRootPath` is the path to XNAT_HOME as seen by XNAT and `serverRootPath` is this same path as seen by the host system.


## Transporter Usage

`scp -P 2222 -rp -O username@host_url:snapshot_label /destination/folder`
