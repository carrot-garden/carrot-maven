/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */

def buildLog = new File( "$basedir", "build.log" )

def logLines = buildLog.readLines()

def failureLines = logLines.findAll { it.contains("BUILD FAILURE") }

return failureLines.isEmpty() 

