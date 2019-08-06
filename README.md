# Blockchain based logging system

Chaincode for logging system prototype built on Hyperledger Fabric The project uses Hyperledger Fabric framework and Spring Boot Java Framework to create RESTful Webservice containing APIs that exposes the functions of the blockchain based logging system. The project aim is evaluated through tests performed on the blockchain network to demonstrate scalability of the system in handling request and the integrity and immutability of log data provided by the system.

### 7.2.1 RESTful API Design Document

The table below shows the design of the APIs that will be exposed in the client application.

| Method | Path | Action | Result |
| --- | --- | --- | --- |
| POST | /api/logs | Create | Creates a new log entry |
| GET | /api/logs/{application} | Read | Gets the current log entry for the application |
| GET | /api/logs/history/{application} | Read | Gets the log history for the application |

The table below shows the design of APIs that will be exposed only for testing purposes in order to evaluate the project aims and objectives.

| Method | Path | Action | Result |
| --- | --- | --- | --- |
| DELETE | /api/logs/{application} | Delete | Deletes log entry from state database |
| DELETE | /api/peers/{number} | Delete | Deletes number of Peer node from the network |
