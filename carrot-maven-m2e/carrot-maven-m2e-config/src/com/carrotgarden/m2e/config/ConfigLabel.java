package com.carrotgarden.m2e.config;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ConfigLabel extends ContributionItem {

	private Label label;

	private String tooltip = "";
	private ImageDescriptor imageDescriptor = null; // HudsonPlugin.getImageDescriptor("resources/icons/hudson.gif");
	private String text;

	private static Map<Image, ImageDescriptor> imageDescriptionMap = new HashMap<Image, ImageDescriptor>();

	public ConfigLabel(final String id) {
		super(id);
	}

	@Override
	public void fill(final Composite composite) {

		final Composite parent = new Composite(composite, SWT.NONE);
		final GridLayout layout = new GridLayout(3, false);

		layout.marginWidth = 0;
		layout.marginHeight = 0;

		parent.setLayout(layout);

		// separator
		final Label sep = new Label(parent, SWT.SEPARATOR);
		sep.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		// project count label
		label = new Label(parent, SWT.LEFT);
		label.setText("done");
		label.setVisible(false);
		label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));

		setTooltip(tooltip);

		if (text != null) {
			setText(text);
		}

	}

	public void setText(final String text) {

		this.text = text;

		if (!label.isDisposed()) {

			label.setText(text);

			if (!label.isVisible()) {
				label.setVisible(true);
			}

		}

	}

	public void setImage(final Image image) {

		if (!imageDescriptionMap.containsKey(image)) {
			// if (image == null)
			// imageDescriptionMap.put(image, null);
			// else
			// imageDescriptionMap
			// .put(image, new ImageImageDescription(image));
		}

		final ImageDescriptor imageDescriptor = imageDescriptionMap.get(image);

		this.imageDescriptor = imageDescriptor;

	}

	public void setTooltip(final String tooltip) {

		this.tooltip = tooltip;

		if (!label.isDisposed()) {
			label.setToolTipText(tooltip);
		}

	}

}
