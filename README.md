# sisyphus

Central configuration system.

## Installation

lein run

## Usage

curl http://localhost:3000/<environment>/<key>/<subkey>/...

curl http://localhost:3000/dev/A/B


## Features
- different configuration by environment (development, staging, production).
- hierarchical keys (e.g. A, A/B, A/B/C).
- optional config validation.
- configuration data stored in git repositories.

## hierarchical keys

prd/oms/default.edn
prd/oms/payments.edn
prd/oms/HH/default.edn
prd/oms/HH/jobs.edn
prd/oms/HH/EU/UK/default.edn


## TODO
- return data in YAML
- store data in YAML, useful for the pull-mode
- filter extension files not available
- sort file strategy (by name?)
- concurrency issues when updating git repo - http://akazlou.com/posts/2015-12-12-small-experiment-git-ds.html
- server caching strategy: use atom, agent to store in-memory data structure
- security (authentication OAuth?)
- backoffice to manage configuration
- configurable repositories

## CURRENT ISSUES
- if the branch does not exist, no exception is thrown


## NICE TO HAVE
### FEATURES
- timbre logger added as a component. add log wrapper?
- key definition in Schema
- git repo security
- interesting hybrid model (push with gitconsul and pull REST)
- metrics
- schema coercion
- show historical data
- unit/integration/acceptance testing
- client UI to manage and store data
- caching strategy (HTTP HEAD, etc)
- versioning: use git tags, or git hash numbers to retrieve specific versions. Not so sure, it's so useful, as you would usually be interested on the latest changes. If it's rarely used, it could be implemented just cloning a repo, just for that case. It would take more time to load, but it's a tradeoff.
- use jail component to evaluate clojure schema files like clojail.

### CLOJURE
- use protocol to abstract data store
- yada not used, missing documentation
- Datomic as data store.
- cljx for validation in server and client

### Emacs
- bind mac copy, paste keys
- block selection
- magit
- clj-refactor
- how to autocomplete in emacs (autocomplete)
- how to jump to clj functions (imenu)


## DOUBTS
- how do I push changes to remote repo with clj-jgit?
- how to validate from local env? using cljx?
- 1 repo per environment vs all environments in 1 repo?
- how to pull files in git repo wihout concurrency issues
- how to debug web app - logging?
- how to autoreload clojure system after a change?
- how to sync config and schema?
- how to store the general config? replicated in all branches? how to keep them sync?
- how flexible are hierarchical keys?
- does it make sense to expose API to fine grained keys?
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


## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.