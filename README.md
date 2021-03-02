# FFARS and MUSE to Tanzania Health Supply Chain Mediator


[![Java CI Badge](https://github.com/SoftmedTanzania/thscp-mediator-ffars-muse/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/SoftmedTanzania/thscp-mediator-ffars-muse/actions?query=workflow%3A%22Java+CI+with+Maven%22)
[![Coverage Status](https://coveralls.io/repos/github/SoftmedTanzania/thscp-mediator-ffars-muse/badge.svg?branch=development)](https://coveralls.io/github/SoftmedTanzania/thscp-mediator-ffars-muse?branch=development)

An [OpenHIM](http://openhim.org/) mediator for handling system integration between FFARS and MUSE and Tanzania Health Supply Chain Mediator.

## 1. Dev Requirements

1. Java 1.8
2. IntelliJ or Visual Studio Code
3. Maven 3.6.3

## 2. Mediator Configuration

This mediator is designed to work with FFARS and MUSE that send JSON Payloads while communicating with the THSCP via the HIM.

### 3 Configuration Parameters

The configuration parameters specific to the mediator and destination system can be found at

`src/main/resources/mediator.properties`

```
    # Mediator Properties
    mediator.name=THSCP-Mediator-FFARS-and-MUSE
    mediator.host=localhost
    mediator.port=3021
    mediator.timeout=60000
    mediator.heartbeats=true
    
    core.host=localhost
    core.api.port=8080
    core.api.user=openhim-username
    core.api.password=openhim-password
    
    destination.host=destination-system-address
    destination.api.port=destination-system-address-port
    destination.api.path=/destination-system-path
    destination.scheme=destination-system-scheme
```

The configuration parameters specific to the mediator and the mediator's metadata can be found at

`src/main/resources/mediator-registration-info.json`

```
    {
      "urn": "urn:uuid:ee883220-7b2d-11eb-851e-610d06e9c9de",
      "version": "0.1.0",
      "name": "THSCP-Mediator-FFARS and MUSE",
      "description": "A mediator for handling system integrations between FFARS and MUSE with THSCP",
      "endpoints": [
        {
          "name": "THSCP-Mediator-FFARS and MUSE Route",
          "host": "localhost",
          "port": "3021",
          "path": "/thscp",
          "type": "http"
        }
      ],
      "defaultChannelConfig": [
        {
          "name": "FFARS and MUSE to THSCP Channel",
          "urlPattern": "^/thscp$",
          "type": "http",
          "allow": ["thscp-mediator-ffarsandmuse-role"],
          "routes": [
            {
              "name": "THSCP-Mediator-FFARS and MUSE Route",
              "host": "localhost",
              "port": "3021",
              "path": "/thscp",
              "type": "http",
              "primary": "true"
            }
          ]
        }
      ],
      "configDefs": [
        {
          "param": "destinationConnectionProperties",
          "displayName": "Destination Connection Properties",
          "description": "Configuration to set the hostname, port and path for the destination server",
          "type": "struct",
          "template": [
            {
              "param": "destinationHost",
              "displayName": "Destination Host Name",
              "description": "IP address/hostname of the destination server. e.g 192.168.1.1",
              "type": "string"
            },
            {
              "param": "destinationPort",
              "displayName": "Destination Port Number",
              "description": "The port number of the destination server. e.g 8080",
              "type": "number"
            },
            {
              "param": "destinationPath",
              "displayName": "Destination Path",
              "description": "The destination path for receiving data from the HIM. eg /hdr",
              "type": "string"
            },
            {
              "param": "destinationScheme",
              "displayName": "Destination Scheme",
              "description": "Whether the destination is using HTTP or HTTPS requests.",
              "type": "option",
              "values": [
                "http",
                "https"
              ]
            },
            {
              "param": "destinationUsername",
              "displayName": "Destination Username",
              "description": "The destination username for receiving data from the HIM.",
              "type": "string"
            },
            {
              "param": "destinationPassword",
              "displayName": "Destination Password",
              "description": "The destination password for receiving data from the HIM.",
              "type": "password"
            }
          ]
        }
      ]
    }
```

## 4. Deployment

To build and run the mediator after performing the above configurations, run the following

```
  mvn clean package -DskipTests=true -e source:jar javadoc:jar
  java -jar target/thscp-mediator-ffars-muse-<version>-jar-with-dependencies.jar
```
