## CarrotGarden SCR

CarrotGarden SCR provides 
OSGI Service-Component descriptor generator
according to a proposal described in 
[RFC 0172 Declarative Services Annotations]
(http://www.osgi.org/download/osgi-early-draft-2011-09.pdf).

CarrotGarden SCR allows for interactive SCR component descriptor updates
in eclipse which will be compatible with your non-interactive jenkins maven builds.

Extremely fast. Incremental. Single SCR xml descriptor per component. 
Watch your SCR descriptors built before your eyes in eclipse maven console.

CarrotGarden SCR is comprised of: annotations, maven plugin, eclipse connector.

### Annotations

[carrot-osgi-anno-scr]
(https://github.com/carrot-garden/carrot-osgi/tree/master/carrot-osgi-anno-scr)
provides osgi annotations and the annotation processor.


### Maven Plugin

[carrot-maven-scr-plugin]
(https://github.com/carrot-garden/carrot-maven/tree/master/carrot-maven-scr-plugin)
incorporates annotation processor in maven build.

learn more on
[maven info site]
(http://carrot-garden.github.com/carrot-maven/site/carrot-maven-scr-plugin/)
and get 
[latest version of the plugin]
( http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22carrot-maven-scr-plugin%22)
from maven central.

### Eclipse Connector

[com.carrotgarden.m2e.scr]
(https://github.com/carrot-garden/carrot-eclipse/tree/master/com.carrotgarden.m2e/com.carrotgarden.m2e.scr)
is an m2e eclipse connector for the annotation processor and the maven plugin. 
 
use this 
[repository update site]
(http://carrot-garden.github.com/carrot-eclipse/repository/com.carrotgarden.m2e.scr-LATEST/)
to add connector to an eclpse profile.
