# Escalate

Customisable online digital media application using React and Springboot.

Escalate is a single page web application that renders videos hosted on Youtube as per filtering criteria matching video properties. This application could be used host any kind of online personalized digital media channel comprising of desired media mix.

[This video](https://www.youtube.com/watch?v=L1pKYqutHGQ) describes features of the app while going through a use case of content pertaining to personal improvement. 

See Escalate in action [here](https://escalate.opulencesix.com).

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. Documentation section below has further pointers to developmer and deployment documentation.

### Prerequisites
Runtime:
* JRE 1.8
* mongodb 3.x

Development:
* npm

### Build

Clone the repository and run one of the following to build with minification of html/css/js (deployEnv=prod), or not.

* Run _npm install_ from the o6 folder, to install node modules. 
* _gradlew build_: To build from command line.
* _gradlew cleanEclipse eclipse_: To create eclipse projects to be imported in its workspace, for development purposes
* _npm run watch_: To track changes to javascript files and rebundle client side code while app is running from IDE debugger.

### Debug, Run
* Configure mongodb parameters in app.config to match with DB
* Run Main application class from within eclipse if debugging
* Else, run from command line: _java -jar o6-0.0.1-SNAPSHOT.war  --spring.config.location=<path>/application.properties_
  
## Documentation

* Deployment, [see here](github.com/opulencesix/escalate/wiki/Deployment).
* Development, [see here](github.com/opulencesix/escalate/wiki/Development).

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE) file for details

