package com.ecmkit.service.preview.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;


import com.ecmkit.service.convert.ImageConvertService;
import com.ecmkit.service.convert.impl.TransformOption;
import com.ecmkit.service.imagefile.ImageFileService;
import com.ecmkit.service.preview.PreviewService;

public class PreviewServiceImpl implements PreviewService {
	private String imagePath = "/home/time_master/temp/images/";
	private String pdfPath = imagePath + "C0133A-Internet.pdf";	
	private String[] imageArgs = {"-imageType", "png", "-outputPrefix", imagePath + "yy", "-startPage", "10", "-endPage", "10", pdfPath};
	

	private ImageFileService imageService;
	private ImageConvertService imageConvertService;
	protected ContentService contentService;
	protected NodeService nodeService;
	
	@Override
	public File createPreview(String fileName, long lastModify, int pageNumber, int resolution) {
		TransformOption option = makeThansformOption(fileName, lastModify, pageNumber, resolution);
		imageConvertService.CalculateStartAndEndPage(option);
		imageService.filterExistedPages(option);
		boolean isScussed = imageConvertService.convert(option);
		File targetImageFile = null;
		if(isScussed) {
			targetImageFile = imageService.getImageFile(option);
			
		}
		return targetImageFile;
	}
	
	public File getPreview(String fileName, long lastModify, int pageNumber, int resolution) {
		TransformOption option = makeThansformOption(fileName, lastModify, pageNumber, resolution);
		File targetImageFile = imageService.getImageFile(option);
		return targetImageFile;
	}
	
	private TransformOption makeThansformOption(String fileName, long lastModify, int pageNumber, int resolution) {
		TransformOption option = new TransformOption();
		option.setFileName(fileName);
		option.setModifyDate(lastModify);
		option.setTargetPage(pageNumber);
		option.setResolution(resolution);
		
		String imageRootFolderPath = getImageRootFolder(fileName, lastModify) + "/" + fileName + ".pdf";
		int totalPageNumber = getTotalPageNumber(imageRootFolderPath);
		option.setTotalPages(totalPageNumber);
		
		option.setOriginalFile(imageRootFolderPath);
		
		String imageFolderPath = imageService.getImageFolderPath(option);
		option.setTargetFolder(imageFolderPath);
		
		return option;
	}

	@Override
	public TransformOption makeTransformOption() {
		// TODO Auto-generated method stub
		return null;
	}

	public ImageFileService getImageService() {
		return imageService;
	}

	public void setImageService(ImageFileService imageService) {
		this.imageService = imageService;
	}

	@Override
	public boolean isImageAvailable(String fileName, long modifyDate,
			int pageNumber, int resolution) {
		TransformOption option = new TransformOption();
		option.setFileName(fileName);
		option.setModifyDate(modifyDate);
		option.setTargetPage(pageNumber);
		option.setResolution(resolution);
		return imageService.isImageAvailable(option);
	}
	
	public String getImageRootFolder(String fileName, long lastModify) {
		TransformOption option = new TransformOption();
		option.setFileName(fileName);
		option.setModifyDate(lastModify);
		return imageService.getImageRootPath(option);
	}

	public ImageConvertService getImageConvertService() {
		return imageConvertService;
	}

	public void setImageConvertService(ImageConvertService imageConvertService) {
		this.imageConvertService = imageConvertService;
	}
	
	public File makeMotherFile(String folder, String file) {
		return imageService.makeMotherFile(folder, file);
	}
	
	public int getTotalPageNumber(String path){
		return imageService.getFilePages(path);
	}
	
	public int getTotalPageNumber(String fileName, long modifiedDate) {
		String fileFolderPath = imageService.getImageRootPath(fileName, modifiedDate);
		String imageRootFolderPath = fileFolderPath + "/" + fileName + ".pdf";
		return imageService.getFilePages(imageRootFolderPath);
	}
	
	public ContentService getContentService() {
		return contentService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

//	public String getIndentifyByURL(String url) {
//		String identify = null;
//		if(url != null) {
//			identify = url.substring(url.indexOf("SpacesStore/") + 12, url.indexOf("/content/imagepreview"));
//		}
//		return identify;
//	}
	
	public boolean isOriginalFileAvailable(String originalFileName, NodeRef thumbnail) {
		File pdfTmpfile = getOriginalFilePath(originalFileName, thumbnail);
		return pdfTmpfile.exists();
	}
	
	private File getOriginalFilePath(String originalFileName, NodeRef thumbnail) {
		Date modified = (Date)nodeService.getProperty(thumbnail, ContentModel.PROP_MODIFIED);
//		ContentReader reader = contentService.getReader(thumbnail, ContentModel.PROP_CONTENT);
		String folderPath = getImageRootFolder(originalFileName, modified.getTime());
		File pdfTmpfile = new File(folderPath + "/" + originalFileName + ".pdf");
		return pdfTmpfile;
	}
	
	public boolean makeOriginalFile(NodeRef thumbnail) {
//		Date modified = (Date)nodeService.getProperty(thumbnail, ContentModel.PROP_MODIFIED);
//		String url = reader.getContentUrl();
		String fileName = getStoreId(thumbnail);
//		String folderPath = getImageRootFolder(originalFileName, reader.getLastModified());
		File pdfTmpfile = getOriginalFilePath(fileName, thumbnail);
		if(!pdfTmpfile.exists()) {
			makeMotherFile(pdfTmpfile.getParent(), pdfTmpfile.getName());

			try {
				OutputStream os = new FileOutputStream(pdfTmpfile);
				ContentReader reader = contentService.getReader(thumbnail, ContentModel.PROP_CONTENT);
				reader.getContent(os);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return true;
	}
	
	public String getStoreId(NodeRef thumbnail) {
		ContentReader reader = contentService.getReader(thumbnail,
				ContentModel.PROP_CONTENT);
		String url = reader.getContentUrl();
		String previewName = getStoreId(url);
		return previewName;
	}
	
	public String getStoreId(String storeUrl) {
//		ContentReader reader = contentService.getReader(storeUrl,
//				ContentModel.PROP_CONTENT);
//		String url = reader.getContentUrl();
		String previewName = storeUrl.substring(storeUrl.lastIndexOf("/") + 1,
				storeUrl.lastIndexOf("."));
		return previewName;
	}
	
	public long getModifyTime(NodeRef thumbnail) {
		Date modified = (Date)nodeService.getProperty(thumbnail, ContentModel.PROP_MODIFIED);
		return modified.getTime();
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
}
