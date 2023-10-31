# XNAT Transporter Plugin
 

The XNAT Transporter supports the creation of Data Snapshots and the transport of those snapshots to a remote host/client by way of the SCP protocol.

Transporter functionality is implemented across two components: the [XNAT Plugin](https://github.com/kelseym/transporter-plugin) and the [Transporter App](https://github.com/kelseym/transporter-app
).

![Transporter](https://drive.google.com/uc?id=1jQ01d_IpH4SPsQsTrAaDmZbfAF5J6PMi)

Snapshot creation is currently supported via [python script](https://github.com/kelseym/transporter-plugin/tree/main/snapshot-container) and the Docker container (`xnat/data-snapshot:0.1`).

## Alpha Version Plugin 
* This plugin is built on XNAT version 1.8.9.1
* This plugin is in Alpha release and is subject to breaking changes without notice.
* Building the plugin relies on Maven Central as well as a Maven repository hosted by the XNAT team


## Build and install the plugin
1. Run `./gradlew clean jar` in the transporter-plugin folder. 
2. Copy build/libs/xnat-transporter-plugin-0.1.0.jar to the XNAT plugins folder
3. Stop and start tomcat


## Transporter Usage

`scp -P 2222 -rp -O  -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null username@host_url:snapshot_lable /destination/folder`
