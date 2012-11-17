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

WORK=$(dirname $0 )

srm -r -l -v $WORK

echo "##################################################"

exit 0
