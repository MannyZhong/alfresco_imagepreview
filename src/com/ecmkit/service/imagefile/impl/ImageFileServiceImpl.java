package com.ecmkit.service.imagefile.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.ecmkit.service.convert.impl.TransformOption;
import com.ecmkit.service.imagefile.ImageFileService;

public class ImageFileServiceImpl implements ImageFileService {
	
	private String rootPath;
	final static String PATH_SEPATOR = "/";
	final static String IMAGE_EXT = ".png";


	@Override
	public boolean isImageAvailable(TransformOption option) {
		return isImageAvailable(-1, option);
	}
	
	public boolean isImageAvailable(int targetPageNumber, TransformOption option) {
		int targetPage = -1;
		if(targetPageNumber == -1) {
			targetPage = option.getTargetPage();
		} else {
			targetPage = targetPageNumber;
		}
		
		String imageFolderPath = getImageFolderPath(option);
		String imagePath = imageFolderPath + PATH_SEPATOR + targetPage + IMAGE_EXT;
		File targetImage = new File(imagePath);
		return targetImage.exists();
	}

	@Override
	public File getImageFile(TransformOption option) {
		String folderPath = getImageFolderPath(option);
		File imageFile = new File(folderPath + PATH_SEPATOR + option.getTargetPage() + IMAGE_EXT);

		return imageFile;
	}
	

	@Override
	public int getFilePages(String filePath) {
		int pageNumber = 0;
		try {
			PDDocument doc = PDDocument.load(new File(filePath));
			pageNumber = doc.getNumberOfPages();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pageNumber;
	}

	@Override
	public boolean copySourceFile(String filePath, TransformOption option) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String getImageFolderPath(TransformOption option) {
		StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append(getRootPath());
		pathBuilder.append(getPathByDate(option.getModifyDate()) + PATH_SEPATOR);
		pathBuilder.append(option.getFileName() + PATH_SEPATOR);
		pathBuilder.append(option.getResolution());
//		pathBuilder.append(option.getPageNumber() + IMAGE_EXT);
		return pathBuilder.toString();
	}
	
	public String getImageRootPath(TransformOption option) {
		return getImageRootPath(option.getFileName(), option.getModifyDate());
	}
	
	public String getImageRootPath(String fileName, long modifiedDate) {
		StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append(getRootPath());
		pathBuilder.append(getPathByDate(modifiedDate) + PATH_SEPATOR);
		pathBuilder.append(fileName);
		return pathBuilder.toString();
	}
	
	public String getPathByDate(long dateTile) {
		Date date=new Date(dateTile);
		Calendar cal=Calendar.getInstance();
		cal.setTime(date); 
		
		StringBuilder builder = new StringBuilder();
		builder.append(cal.get(Calendar.YEAR) + PATH_SEPATOR);
		builder.append(cal.get(Calendar.MONTH) + 1 + PATH_SEPATOR);
		builder.append(cal.get(Calendar.DAY_OF_MONTH));
		
		return builder.toString();
	}
	
	public void filterExistedPages(TransformOption option) {
		int startPage = option.getStartPage();
		int endPage = option.getEndPage();
		List<Integer> filteredPageNumbers = new ArrayList<Integer>();
		for(int pageIndex = startPage; pageIndex <= endPage; pageIndex++) {
			boolean isExisted = isImageAvailable(pageIndex, option);
			if(!isExisted) {
				filteredPageNumbers.add(pageIndex);
			} 
		}
		
		if(filteredPageNumbers.size() > 1) {
			option.setStartPage(filteredPageNumbers.get(0));
			option.setEndPage(filteredPageNumbers.get(filteredPageNumbers.size() - 1));
		} else if(filteredPageNumbers.size() == 1) {
			option.setStartPage(filteredPageNumbers.get(0));
			option.setEndPage(filteredPageNumbers.get(0));
		} else {
			option.setStartPage(-1);
			option.setEndPage(-1);
		}
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		if(rootPath.substring(rootPath.length() - 1) != PATH_SEPATOR) {
			rootPath += PATH_SEPATOR;
		}
		this.rootPath = rootPath;
	}
	
	public File makeMotherFile(String folder, String file) {
		File parentFolder = null;
		if(folder != null) {
			parentFolder = new File(folder);
			parentFolder.mkdirs();
		}
		
		File targetFile = null;
		if(file != null && parentFolder != null) {
			targetFile = new File(folder + PATH_SEPATOR + file);
			try {
				targetFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(targetFile != null) {
			return targetFile;
		} else {
			return null;
		}
	}
	
	public File makePath(String path) {
		File parentFolder = null;
		if(path != null) {
			parentFolder = new File(path);
			parentFolder.mkdirs();
		}
		return parentFolder;
	}
	
	public boolean isOrginalFileAvailable(String path){
		File targetImage = new File(path);
		return targetImage.exists();
	}

}
