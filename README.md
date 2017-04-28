[![Build Status](https://travis-ci.org/SecuritySquad/webifier-platform.svg?branch=master)](https://travis-ci.org/SecuritySquad/webifier-platform)

# webifier-platform
webifier-platform is a webapp for webifier-tester, which tests certain websites for malicious code.

## Installation

To install and run webifier-platform your system needs the following requirements:

### Requirements

- Java 8
- Docker
- Bower
- git
- libprocname.so

If you have all the requirements installed you need to create the folder persistent/ and run/ in the parent directory of webifier-platform. Move the libprocname.so in the persistent/ folder. The executable .jar files, start scripts and log files are moved in the run/ folder. Now you can simply execute the [install.sh](install.sh) script, which installs all the other needed webifier-components.

### Execution

To run webifier-platform you have run the script start-platform.sh, which will be created in the run/ folder.

### Configuration

...
