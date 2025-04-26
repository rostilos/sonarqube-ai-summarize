SonarQube AI Summarize Plugin
==========

Draft concept for a plug-in for SonarQube Server

Main idea
--------

Developing a plugin for an existing platform to analyze code. 
It is supposed to add functionality that will allow to additionally analyze the code with the help of AI

### Concept Points

- Analyzing only PRs ( MRs )
- Synchronize analysis data through AI into separate reports for both SQ and alm platforms ( github, bitbucket, gitlab )

### Why SQ and the plugin

---------

SQ is a comprehensive platform, not just a standalone tool. 
Officially SQ are developing a plugin for AI CodeFix Suggestions, this suggests a similar degree of integration, but the upside is that it includes analysis.
It's much more convenient to manage multiple tools (static analysis, vulnerability analysis, AI code review - etc.) from one place, and configure providers there, rather than having and customizing a number of different tools

### Item to be realized

- Post-hook on scanner, selecting only PR analyses
- Create functionality to define diffs within PR ( most likely through separate adapters and API requests ). SQ is not supposed to directly manipulate data in the database via plugins ( for maintainability purposes ), and in the context of this data obviously won't be there. Adapters for GitHub, GitLab, Bitbucket
- Parsing of received data from API, creation of corresponding DTOs
- Determining what should go into the prompt. Preliminary - source file + diff.
- Functionality of interaction with various LLMs, adapters for the most famous sites
- Construction of prompt data based on previously constructed DTOs