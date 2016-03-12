# sisyphus

FIXME: Central configuration system.

## Installation

lein run

## Usage

lein run


### TODO
- check file-seq, it's getting subdirs also
- sort file strategy (by name?)
- key definition
- move keywords :stg, :dev, :prd, etc into directories
- move out logic from endpoint
- rename endpoint to config
- return data in YAML
- store data in git repository, each branch one environment
- versioning
- logging
- security (authentication OAuth?)
- HEAD HTTP header
- backoffice to manage configuration
- UI to simplify
- do not overwrite environment copies
- change 
- view clearly config inheritance

# hierarchical keys

prd/oms/default.edn
prd/oms/payments.edn
prd/oms/HH/default.edn
prd/oms/HH/jobs.edn
prd/oms/HH/EU/UK/default.edn

### DONE
- different configurations by environment

### Emacs
- bind mac copy, paste keys
- block selection
- magit

### NICE TO HAVE
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