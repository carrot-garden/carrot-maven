
File buildLog = new File((String) basedir, "build.log")

return buildLog.readLines().contains("     [echo] Mojo Executor ran successfully.");

