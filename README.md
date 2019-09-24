# Baton Messenger - Kotlin

Corda Version 4.0 B2B Messenger

### Baton Messenger Network Setup


1) Install the Baton Messenger CorDapp locally via Git:

```bash

git clone https://github.com/dappsinc/baton-messenger

```

2) Deploy the Nodes


```bash

cd baton-messenger
gradle clean build
gradlew.bat deployNodes (Windows) OR ./gradlew deployNodes (Linux)

```

3) Run the Nodes

```bash

cd build 
cd nodes
runnodes.bat (Windows) OR ./runnodes (Linux)

```
4) Run the Spring Boot Server

```bash

cd ..
cd ..
gradlew.bat runTemplateServer (Windows) OR .gradlew runTemplateServer

```
The Baton Messenger Network Interface will be running at `localhost:10050` in your browser

To change the name of your `organisation` or any other parameters, edit the `node.conf` file and repeat the above steps.

### Joining the Network

Add the following to the `node.conf` file:

`compatibilityZoneUrl="http://dsoa.network:8080"`

This is the current network map and doorman server URL for the DSOA Testnet

1) Remove Existing Network Parameters and Certificates

```bash

cd build
cd nodes
cd Dapps
rm -rf persistence.mv.db nodeInfo-* network-parameters certificates additional-node-infos

```

2) Download the Network Truststore

```bash

curl -o /var/tmp/network-truststore.jks http://dsoa.network:8080//network-map/truststore

```

3) Initial Node Registration

```bash

java -jar corda.jar --initial-registration --network-root-truststore /var/tmp/network-truststore.jks --network-root-truststore-password trustpass

```
4) Start the Node

```bash

java -jar corda.jar

```


#### Docker Setup

Coming Soon.


#### Node Configuration

Configuration 

- Corda version: Corda 4.0
- Vault SQL Database: PostgreSQL
- Cloud Service Provider: GCP
- JVM or Kubernetes


So far, we get:

- Baton Messenger Spring Boot Webserver
- 8 Corda Nodes 

### Baton Messenger State

Message States are transferred between stakeholders on the network.

#### Messages

The first state to be deployed on the network is the `Message`. Version 0.1 of the `Message` State has the following structure:

```jsx

// *********
// * Message State *
// *********

     data class Message(val id: UniqueIdentifier,
                       val body: String,
                       val fromUserId: String,
                       val to: Party,
                       val from: Party,
                       val toUserId: String,
                       val sentReceipt: Boolean?,
                       val deliveredReceipt: Boolean?,
                       val fromMe: Boolean?,
                       val time: String?,
                       val messageNumber: String,
                       override val participants: List<AbstractParty> = listOf(to, from)) : ContractState


```
