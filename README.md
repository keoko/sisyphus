# sisyphus

Central configuration system.

## Installation

lein run

## Usage

curl http://localhost:3000/<environment>/<key>/<subkey>/...

curl http://localhost:3000/dev/A/B


### TODO
- move out logic from endpoint
- move keywords :stg, :dev, :prd, etc into directories
- store data in git repository, each branch one environment
- check file-seq, it's getting subdirs also
- sort file strategy (by name?)
- key definition
- return data in YAML
- versioning
- logging
- security (authentication OAuth?)
- HEAD HTTP header
- backoffice to manage configuration
- UI to simplify
- do not overwrite environment copies
- view clearly config inheritance

# hierarchical keys

prd/oms/default.edn
prd/oms/payments.edn
prd/oms/HH/default.edn
prd/oms/HH/jobs.edn
prd/oms/HH/EU/UK/default.edn


### Emacs
- bind mac copy, paste keys
- block selection
- magit
- clj-refactor

### Features
- different configuration by environment (development, staging, production).
- hierarchical keys (e.g. A, A/B, A/B/C).
- optional config validation.


### NICE TO HAVE
- metrics
- schema coercion
- stored files in EDN or YAML
- show historical data
- yada: missing some documentation
- unit/integration/acceptance testing
- server caching strategy


### DOUBTS
- how to autocomplete in emacs (autocomplete)
- how to jump to clj functions (imenu)
- how to debug web app - logging?
- how to autoreload clojure system after a change?
- how to sync config and schema?
- how to store the general config? replicated in all branches? how to keep them sync?
- how flexible are hierarchical keys?
- does it make sense to expose API to fine grained keys?
- how to change configuration? git clients? special UI?
- can we build UI from schema definition?


## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.