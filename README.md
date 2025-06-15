SonarQube AI Summarize Plugin
==========

Main idea
--------

Developing a plugin for an existing platform to analyze code. 
It is supposed to add functionality that will allow to additionally analyze the code with the help of AI

<h2>Features</h2>

- Analyze PR on ALM platform using pre-selected AI providers, with an established template.
- Report in the form of comments on the results of AI summarize
  ![1748004073744](https://github.com/user-attachments/assets/23f32985-4b8f-4268-8db0-a5691cc2b207)


<h2>Requirements</h2>

1. SonarQube Server ( tested on v24.12 )
2. At least Developer Edition, or community branch plugin
3. Configured project-level connectivity to the ALM platform ( currently there is only support for Github and Bitbucket Cloud )
4. The token (in the case of Bitbucket) or the application (in the case of Github) must have the appropriate accesses. Basic - PR read&write

<h2>Deploy & Configuration </h2>

To install the plugin you can take either a compiled binary from the assets of the corresponding releases, or build from source.
To build a plugin from source, execute this command from the project root directory:

`./build.sh`

The plugin jar file is generated in the project's `target/` directory.

<h4>"Cold" Deploy</h4>

The standard way to install the plugin for regular users is to copy the jar artifact, from the `target/` directory to the `extensions/plugins/` directory of your SonarQube Server installation, then start the server.

It is also mandatory to specify an agent for correct registration of the plugin:
`<sonar_installation_dir>/conf/sonar.properties` :
```
sonar.web.javaAdditionalOpts=-javaagent:./extensions/plugins/sonar-ai-summarize-1.0.0.jar
sonar.ce.javaAdditionalOpts=-javaagent:./extensions/plugins/sonar-ai-summarize-1.0.0.jar
```

<h4>Installation example ( Docker-based )</h4>

````
version: "3.8"
services:
  sonarqube:
    image: sonarqube:10.7-community
    ports:
      - "9000:9000"
      - "9092:9092"
    depends_on:
      - db
    environment:
      SONAR_JDBC_URL: jdbc:postgresql://db:5432/sonar
      SONAR_JDBC_USERNAME: sonar
      SONAR_JDBC_PASSWORD: sonar
      SONAR_WEB_JAVAADDITIONALOPTS: "-javaagent:./extensions/plugins/sonar-ai-summarize-1.0.0.jar"
      SONAR_CE_JAVAADDITIONALOPTS: "-javaagent:./extensions/plugins/sonar-ai-summarize-1.0.0.jar"
    volumes:
      - sonarqube_conf:/opt/sonarqube/conf
      - sonarqube_data:/opt/sonarqube/data
      - sonarqube_logs:/opt/sonarqube/logs
      - sonarqube_extensions:/opt/sonarqube/extensions
      - sonarqube_bundled-plugins:/opt/sonarqube/lib/bundled-plugins
      - ./sonar-ai-summarize-1.0.0.jar:/opt/sonarqube/extensions/plugins/sonar-ai-summarize-1.0.0.jar
    networks:
      sonar_network:
  db:
    image: postgres:12
    ports:
      - "5432:5432"
    command: postgres -c 'max_connections=300'
    volumes:
      - postgresql:/var/lib/postgresql
      - postgresql_data:/var/lib/postgresql/data
    environment:
      POSTGRES_DB: sonar
      POSTGRES_USER: sonar
      POSTGRES_PASSWORD: sonar
    networks:
      sonar_network:
    restart: unless-stopped
volumes:
  sonarqube_conf:
  sonarqube_data:
  sonarqube_logs:
  sonarqube_extensions:
  sonarqube_bundled-plugins:
  postgresql:
  postgresql_data:
networks:
  sonar_network:

````

### Configuration 
A screenshot of the settings example.
![Screenshot_20250529_173055](https://github.com/user-attachments/assets/71945ed0-5835-45d5-bbdb-5f38f2bc2510)

The settings themselves can be found in

`Administration -> General Settings -> AI Summarize` (at global level)

`Project Settings -> General Settings -> AI Summarize` (at the project level).

Among the settings
<ul>
    <li>Enable/Disable ( project level )</li>
    <li>Select AI provider ( project level )</li>
    <li>Specify model ( project level )</li>
    <li>Specify API token ( project level )</li>
    <li>Prompt template ( optionally ). Only data customization is available before and after the main prompt ( source file + patch ).</li>
    <li>File Limit option - number of files in PR that will be analyzed ( for limitations ) </li>
    <li>File Max Lines - maximum number of lines in the content or patchdiff file, if the actual value exceeds the value set in the config - the file will be omitted ( source code, if and diff above this value - it will also be omitted )</li>
    
</ul>


### Why SQ and the plugin

---------

SQ is a comprehensive platform, not just a standalone tool.
Officially SQ are developing a plugin for AI CodeFix Suggestions, this suggests a similar degree of integration, but the upside is that it includes analysis.
It's much more convenient to manage multiple tools (static analysis, vulnerability analysis, AI code review - etc.) from one place, and configure providers there, rather than having and customizing a number of different tools

### TODO List

- Availability for all known ALM platforms ( currently available only on github and bitbucket cloud )
- Static tests ( not available at the moment )
- Expanding the list of AI providers ( currently OpenRouter and OpenAI are available )
- General refactoring of the code-styles ( alpha version )
