/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */

println "hello groovy"

assert project.properties["prop-key"] == "prop-value-1"

println "prop-key = " + project.properties["prop-key"]

project.properties["prop-key"] = "prop-value-2"

assert project.properties["prop-key"] == "prop-value-2"

println "prop-key = " + project.properties["prop-key"]

def result = "result"

println "result = $result"

return result
 
