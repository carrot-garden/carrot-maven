package a;
/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */


public interface HealthReport {

	/**
	 * Health report URL.
	 * <p>
	 * Used in load balancer cluster configuration.
	 */
	String PATH = "/health-report";

}
