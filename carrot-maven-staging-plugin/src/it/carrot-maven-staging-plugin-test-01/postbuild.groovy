
def buildLog = new File( "$basedir", "build.log" )

def logLines = buildLog.readLines()

def failureLines = logLines.findAll { it.contains("BUILD FAILURE") }

return failureLines.isEmpty() 

