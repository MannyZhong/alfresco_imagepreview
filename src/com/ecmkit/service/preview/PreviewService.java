package com.ecmkit.service.preview;

import java.io.File;

import org.alfresco.service.cmr.repository.NodeRef;

import com.ecmkit.service.convert.impl.TransformOption;

public interface PreviewService {
	public File createPreview(String fileName, long lastModify, int pageNumber, int resolution);
	public File getPreview(String fileName, long lastModify, int pageNumber, int resolution);
	public String getImageRootFolder(String fileName, long lastModify);
	public TransformOption makeTransformOption();
	public boolean isImageAvailable(String fileName, long modifyDate, int pageNumber, int resolution);
	public File makeMotherFile(String folder, String file);
	public int getTotalPageNumber(String path);
	public int getTotalPageNumber(String filename, long modifiedDate);
//	public String getIndentifyByURL(String url);
	public boolean isOriginalFileAvailable(String originalFileName, NodeRef thumbnail);
	public boolean makeOriginalFile(NodeRef thumbnail);
	public long getModifyTime(NodeRef thumbnail);
	public String getStoreId(NodeRef thumbnail);
	public String getStoreId(String storeUrl);
}
