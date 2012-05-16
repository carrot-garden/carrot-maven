#!/bin/bash
#
# Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#


echo "##################################################"

echo "project : ${project.artifactId}"

echo "##################################################"

pwd

echo "##################################################"

ls -las

echo "##################################################"

add-apt-repository --yes ppa:webupd8team/java
apt-get --yes update
apt-get --yes install mc tar wget zip unzip secure-delete
apt-get --yes install oracle-jdk7-installer
apt-get --yes upgrade
apt-get --yes dist-upgrade

echo "##################################################"
												
java -version 2>&1

echo "##################################################"

ADMIN_GROUP="ubuntu"
ADMIN_USER="ubuntu"

KARAF_HOME="/var/karaf"
KARAF_GROUP="karaf"
KARAF_USER="karaf"

addgroup --system $KARAF_GROUP
adduser --system --ingroup $KARAF_GROUP --home $KARAF_HOME $KARAF_USER
adduser $ADMIN_USER $KARAF_GROUP

chown --changes --recursive $ADMIN_GROUP:$KARAF_USER $KARAF_HOME
chmod --changes --recursive o-rwx,g+rw,ugo-s $KARAF_HOME
find $KARAF_HOME -type d -exec chmod --changes g+s {} \;

echo "##################################################"

WORK=$(dirname $0 )

srm -r -l -v $WORK

echo "##################################################"

sleep 3s

exit 0
