package com.ecmkit.service.convert.impl;

public class TransformOption {
	private String fileName;
	private int resolution;
	private long modifyDate;
	private int startPage;
	private int endPage;
	private String originalFile;
	private int targetPage;
	private int totalPages;
	private String targetFolder;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public int getResolution() {
		return resolution;
	}
	public void setResolution(int resolution) {
		this.resolution = resolution;
	}
	public long getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(long modifyDate) {
		this.modifyDate = modifyDate;
	}
	public int getStartPage() {
		return startPage;
	}
	public void setStartPage(int startPage) {
		this.startPage = startPage;
	}
	public int getEndPage() {
		return endPage;
	}
	public void setEndPage(int endPage) {
		this.endPage = endPage;
	}
	public String getOriginalFile() {
		return originalFile;
	}
	public void setOriginalFile(String originalFile) {
		this.originalFile = originalFile;
	}
	public int getTargetPage() {
		return targetPage;
	}
	public String getTargetFolder() {
		return targetFolder;
	}
	public void setTargetFolder(String targetFolder) {
		this.targetFolder = targetFolder;
	}
	public void setTargetPage(int targetPage) {
		this.targetPage = targetPage;
	}
	public int getTotalPages() {
		return totalPages;
	}
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

}
