package com.ecmkit.service.imagefile;

import java.io.File;

import com.ecmkit.service.convert.impl.TransformOption;

public interface ImageFileService {
	public boolean isImageAvailable(TransformOption option);
	public File getImageFile(TransformOption option);
	public int getFilePages(String filePath);
	public boolean copySourceFile(String filePath, TransformOption option);
	public String getImageFolderPath(TransformOption option);
	public void filterExistedPages(TransformOption option);
	public File makeMotherFile(String folder, String file);
	public String getImageRootPath(TransformOption option);
	public String getImageRootPath(String fileName, long modifiedDate);
	public File makePath(String path);
	public boolean isOrginalFileAvailable(String path);
}
