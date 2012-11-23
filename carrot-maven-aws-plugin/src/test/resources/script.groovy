
println "hello groovy"

assert project.properties["prop-key"] == "prop-value-1"

println "prop-key = " + project.properties["prop-key"]

project.properties["prop-key"] = "prop-value-2"

assert project.properties["prop-key"] == "prop-value-2"

println "prop-key = " + project.properties["prop-key"]

def result = "result"

println "result = $result"

return result
 
