# sisyphus

Central configuration system.

## Installation

lein run

## Usage

curl http://localhost:3000/<profile>/<variant>/<sub-variant>/...

curl http://localhost:3000/app1-prd/tenant1/brand2


## Features
- different configuration by environment (development, staging, production).
- hierarchical keys (e.g. A, A/B, A/B/C).
- optional config validation.
- configuration data stored in git repositories.
- supported format: YAML.


## TODO
- documentation


## CURRENT ISSUES
- handle errors on updating repos
- if the branch does not exist, no exception is thrown
- git merging strategies fail when repo is already created :(


## Git issues
Add user SSH keys, do not confuse with SSH deply keys!!!!
Use SSH protocol, not HTTP.
HTTP needs credentials

GitLab: Deploy keys are not allowed to push code.
fatal: Could not read from remote repository.

Please make sure you have the correct access rights
and the repository exists.

### permission denied
Permission denied (publickey).
fatal: Could not read from remote repository.



## NICE TO HAVE
### FEATURES
- clean up naming: branch, version, repo, environment
- security (authentication OAuth?) check CMS TW
- git repo security (credentials)
- configurable repositories (do we store them in git?) every X minutes update the conf file
- on re-loading app, remove directories and start clean
- timbre logger added as a component. add log wrapper? look at pepa project
- use an atom for each profile, not a big one.
- key definition in Schema
- interesting hybrid model (push with gitconsul and pull REST)
- metrics
- swagger
- schema coercion
- show historical data
- support different formats (json, edn, etc.). 
- unit/integration/acceptance testing
- client UI to manage and store data
- versioning: use git tags, or git hash numbers to retrieve specific versions. Not so sure, whether it's so useful, as you would usually be interested on the latest changes. If it's rarely used, it could be implemented just cloning a repo, just for that case. It would take more time to load, but it's a tradeoff.

### CLOJURE
- wrap cors
- use jail component to evaluate clojure schema files like clojail.
- clean logger data in a component?
- use protocol to abstract data store
- yada not used, missing documentation
- Datomic as data store.
- cljx for validation in server and client with boot
- how to autoreload clojure system after a change?

### Emacs
- block selection
- magit
- how to jump to clj functions (imenu)


## DOUBTS
- super big atom? performance issues?
- different apps + variants + local env's -> memory consumption?
- does it make sense to expose API to fine grained keys? (e.g. jobs conf, topic2, topic1?
- communication between endpoint and data-store through channel? weird.
- 1 channel per repo?
- how to communicate changes to atoms? via fs-watch? or via channel message?
- how do I push changes to remote repo with clj-jgit?
- how to validate from local env? using cljx?
- 1 repo per environment vs all environments in 1 repo?
- 1 atom per repo? vs 1 big repo?
- how to debug web app - logging?
- how to sync config and schema?
- how to store the general config? replicated in all branches? how to keep them sync?
- how flexible are hierarchical keys model?
- how to change configuration? git clients? special UI?
- can we build UI from schema definition?


## Links
- http://akazlou.com/posts/2015-12-12-small-experiment-git-ds.html
- https://www.thoughtworks.com/insights/blog/incremental-approach-content-management-using-git
- https://www.thoughtworks.com/insights/blog/implementing-content-management-and-publication-using-git
- http://hacienda.io/
- https://github.com/rapid7/conqueso
- http://12factor.net/config
- http://blog.doismellburning.co.uk/twelve-factor-config-misunderstandings-and-advice/
- http://sigops.org/sosp/sosp15/current/2015-Monterey/printable/008-tang.pdf
- http://lifeinvistaprint.com/techblog/configuration-management-git-consul/
- http://akazlou.com/posts/2015-12-12-small-experiment-git-ds.html


## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.