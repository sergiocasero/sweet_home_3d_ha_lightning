package com.sergiocasero;

import com.eteks.sweethome3d.j3d.AbstractPhotoRenderer.Quality;

public class HomeAssistantLightningOptions {
	private final String path;
	private final int imageWidth;
	private final int imageHeight;
	private final Quality quality;
	
	public HomeAssistantLightningOptions(String path, int imageWidth, int imageHeight, Quality quality) {
		super();
		this.path = path;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.quality = quality;
	}

	public String getPath() {
		return path;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public Quality getQuality() {
		return quality;
	}

	@Override
	public String toString() {
		return "HomeAssistantLightningOptions [path=" + path + ", imageWidth=" + imageWidth + ", imageHeight="
				+ imageHeight + ", quality=" + quality + "]";
	}
	
	
	
}
