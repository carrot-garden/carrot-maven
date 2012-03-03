### info

this is a maven plugin test project that 
shows automated ami image creation:

* create ec2 instance via cloud formation template

* upload and execute bash installation script on this instance

* create new ami image from the instance

* terminate all resources, keep the image

### demo

to try it for yourself, you need:

* clone in eclipse the
[carrot-maven]
(https://carrot-garden@github.com/carrot-garden/carrot-maven.git)
project

* select   
carrot-maven > carrot-maven-aws-plugin -> carrot-maven-aws-plugin-test-image-build   
[and run]
(http://www.sonatype.com/books/m2eclipse-book/reference/creating-sect-importing-projects.html)   
Eclipse -> Import -> Existing Maven Projects 

* study
[mojo]
(http://carrot-garden.github.com/carrot-maven/site/carrot-maven-aws-plugin/plugin-info.html)
and
[pom.xml]
(https://github.com/carrot-garden/carrot-maven/blob/master/carrot-maven-aws-plugin/carrot-maven-aws-plugin-test-image-build/pom.xml) 

* replace your
[install.sh]
(https://github.com/carrot-garden/carrot-maven/blob/master/carrot-maven-aws-plugin/carrot-maven-aws-plugin-test-image-build/src/main/script/install.sh)
and 
[formation.template]
(https://github.com/carrot-garden/carrot-maven/blob/master/carrot-maven-aws-plugin/carrot-maven-aws-plugin-test-image-build/src/main/template/formation.template)

* replace your 
[amazon credentials]
(http://www.sonatype.com/books/mvnref-book/reference/appendix-settings-sect-details.html)
servers/server/id

* run the 
[ant build]
(https://github.com/carrot-garden/carrot-maven/blob/master/carrot-maven-aws-plugin/carrot-maven-aws-plugin-test-image-build/build/maven-package.ant) 
from eclipse