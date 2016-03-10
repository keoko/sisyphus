# sisyphus

FIXME: Central configuration system.

## Installation

lein run

## Usage

lein run


### TODO
- schema coercion
- merge data from multiple files
- merge data from multiple schemas
- sort file strategy (by name?)
- configuration hierarchy
- configuration variants
- store data as YAML and convert it to EDN
- return data in YAML
- store data in git repository, each branch one environment
- backoffice to manage configuration
- logging
- server caching strategy
- rename endpoint
- security
- versioning
- HEAD

# hierarchical keys
prd/oms/default.edn
prd/oms/HH/default.edn
prd/oms/HH/jobs.edn
prd/oms/HH/ICC/EU/UK

### DONE
- different configurations by environment

### NICE TO HAVE
- show historical data
- yada: missing some documentation

### DOUBTS
- how to autoreload clojure system after a change?
- how to sync config and schema?
- how to store the general config? replicated in all branches? how to keep them sync?
- how flexible are hierarchical keys?


## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.