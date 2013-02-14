/**
 * Copyright (C) 2010-201
2 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.ecc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.MojoFailureException;

import com.amazonaws.services.ec2.model.Image;

/**
 * List existing AMI images, using filter and regex.
 * <p>
 * See <a href=
 * "http://docs.aws.amazon.com/AWSEC2/latest/APIReference/ApiReference-query-DescribeImages.html"
 * >filter definitions</a>
 * 
 * @goal elastic-compute-image-list
 * 
 * @phase prepare-package
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class ElastiCompImageList extends ElastiComp {

	/**
	 * AWS ElasticCompute existing image search filter expression; also can
	 * loaded via {@link #imageFilterProperty}
	 * <p>
	 * Filter is encoded the following syntax:
	 * 
	 * <pre>
	 * key-1=value-A,value-B;key-2=value-C,value-D;
	 * </pre>
	 * 
	 * @required
	 * @parameter default-value="is-public=false"
	 */
	private String imageFilter;

	/**
	 * Name of project.property which, if set dynamically, will be used instead
	 * of static plug-in property {@link #imageFilter}. Not set by default.
	 * 
	 * @parameter
	 */
	private String imageFilterProperty;

	/**
	 * Image search regex expression; also can loaded via
	 * {@link #imageRegexProperty}
	 * <p>
	 * Regex is used as follows:
	 * <p>
	 * 1) find all images matching {@link #imageFilter}
	 * <p>
	 * 2) match regex against {@link Image#toString()}
	 * 
	 * @required
	 * @parameter default-value=".*"
	 */
	private String imageRegex;

	/**
	 * Name of project.property which, if set dynamically, will be used instead
	 * of static plug-in property {@link #imageRegex}. Not set by default.
	 * 
	 * @parameter
	 */
	private String imageRegexProperty;

	/**
	 * Image filter entries separator.
	 * 
	 * @required
	 * @parameter default-value=";"
	 */
	private String imageFilterSplitEntry;

	/**
	 * Image filter keys form values separator.
	 * 
	 * @required
	 * @parameter default-value="="
	 */

	private String imageFilterSplitKey;
	/**
	 * Image filter multiple values separator.
	 * 
	 * @required
	 * @parameter default-value=","
	 */
	private String imageFilterSplitValue;

	/**
	 * Name of project.property which will contain list of {@link Image}
	 * instance after execution of this maven goal, which can be used by groovy
	 * script as follows:
	 * 
	 * <pre>
	 * def imageList = project.properties["amazonImageList"]
	 * println "name  = " + imageList[0].name
	 * println "state = " + imageList[0].state
	 * </pre>
	 * 
	 * @required
	 * @parameter default-value="amazonImageList"
	 */
	private String imageListResultProperty;

	/**
	 * Name of project.property which will contain Map[String,List[Image]] (maps
	 * from amazon region into found image list) for found {@link Image}s after
	 * execution of this maven goal, which can be used by groovy script as
	 * follows:
	 * 
	 * <pre>
	 * def imageMap = project.properties["amazonImageMap"]
	 * println "$imageMap.size"
	 * </pre>
	 * 
	 * @required
	 * @parameter default-value="amazonImageMap"
	 */
	private String imageMapResultProperty;

	/**
	 * Collect total list from multiple plug-in executions.
	 * 
	 * @required
	 * @parameter default-value="false"
	 */
	private boolean imageListIsCumulative;

	protected String imageFilter() throws Exception {
		return projectValue(imageFilter, imageFilterProperty);
	}

	protected String imageRegex() throws Exception {
		return projectValue(imageRegex, imageRegexProperty);
	}

	protected List<Image> ensureImageList() {

		final Properties props = project().getProperties();

		@SuppressWarnings("unchecked")
		List<Image> imageList = (List<Image>) props
				.get(imageListResultProperty);

		if (imageList == null) {
			imageList = new ArrayList<Image>();
			props.put(imageListResultProperty, imageList);
		}

		return imageList;
	}

	protected Map<String, List<Image>> ensureImageMap() {

		final Properties props = project().getProperties();

		@SuppressWarnings("unchecked")
		Map<String, List<Image>> imageMap = (Map<String, List<Image>>) props
				.get(imageMapResultProperty);

		if (imageMap == null) {
			imageMap = new HashMap<String, List<Image>>();
			props.put(imageMapResultProperty, imageMap);
		}

		return imageMap;
	}

	@Override
	public void execute() throws MojoFailureException {

		try {

			final String signature = imageFilter() + " / " + imageRegex();

			getLog().info("image list region : " + amazonRegion());
			getLog().info("image list init : " + signature);

			final CarrotElasticCompute compute = newElasticCompute();

			/** obtain result */
			final List<Image> imageList = compute.imageList( //
					imageFilter(), //
					imageRegex(), //
					imageFilterSplitEntry, //
					imageFilterSplitKey, //
					imageFilterSplitValue //
					);

			for (final Image image : imageList) {
				getLog().info("image list item : " + image);
			}

			/** publish list result */

			if (imageListIsCumulative) {
				getLog().info("image list mode : collecting total list");
			} else {
				ensureImageList().clear();
			}

			ensureImageList().addAll(imageList);

			/** publish map result */

			ensureImageMap().put(amazonRegion(), imageList);

			getLog().info("image list done : " + signature);

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}
}
