package com.biokack.bacteriofag;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class FileWindow extends Window {

	public FileWindow(String fileString) {
		super("File Window");
		setPosition(20, 150);
		setWidth(50, Unit.PERCENTAGE);
		setHeight(60, Unit.PERCENTAGE);
		setModal(true);
		init(fileString);
	}

	private void init(String fileString) {
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeFull();
		windowContent.setMargin(true);
		windowContent.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		setContent(windowContent);

		TextArea textArea = new TextArea();
		textArea.addStyleName(ValoTheme.TEXTAREA_ALIGN_CENTER);
		textArea.setSizeFull();
		textArea.setValue(fileString);
		textArea.setReadOnly(true);
		textArea.setRows(50);

		Button closeButton = new Button("Close");
		closeButton.addClickListener(event -> {
			close();
		});

		windowContent.addComponent(textArea);
		windowContent.setExpandRatio(textArea, 0.9F);

		windowContent.addComponent(closeButton);
	}

}

//
