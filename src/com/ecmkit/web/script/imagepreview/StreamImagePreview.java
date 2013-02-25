package com.ecmkit.web.script.imagepreview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Date;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.web.scripts.content.StreamContent;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.FileCopyUtils;

import com.ecmkit.service.preview.PreviewService;

public class StreamImagePreview extends StreamContent {
	private PreviewService previewService;
	private static final Log logger = LogFactory.getLog(StreamImagePreview.class);
	
	
	protected void streamContentImpl(WebScriptRequest req,
			WebScriptResponse res, ContentReader reader, boolean attach,
			Date modified, String eTag, String attachFileName,
			Map<String, Object> model) throws IOException {
		setAttachment(res, attach, attachFileName);

		// establish mimetype
//		String mimetype = reader.getMimetype();
//		String extensionPath = req.getExtensionPath();
//		if (mimetype == null || mimetype.length() == 0) {
//			mimetype = MimetypeMap.MIMETYPE_BINARY;
//			int extIndex = extensionPath.lastIndexOf('.');
//			if (extIndex != -1) {
//				String ext = extensionPath.substring(extIndex + 1);
//				mimetype = mimetypeService.getMimetype(ext);
//			}
//		}

		//get preview image begin
//		File pdfTmpfile = TempFileProvider.createTempFile("streamContent-", ".pdf");
		
		String pageNumber = req.getParameter("page");
//		String url = req.getURL(); ///alfresco/s/api/node/workspace/SpacesStore/18ab4cb8-c3db-418e-a2a1-048922a60d60/content/imagepreview/pdfpreview?c=force&lastModified=pdfpreview:1357824021855&page=1&alf_ticket=TICKET_b84905ff9e8ac422b2f240657b34457eee10ee30
//		String previewName = url.substring(url.indexOf("SpacesStore/") + 12, url.indexOf("/content"));
		String previewName = previewService.getStoreId(reader.getContentUrl());
		String resolutionString = req.getParameter("resolution");
		int resolution = 82;
		if(resolutionString != null) {
			resolution = Integer.parseInt(resolutionString);
		}
//		String contentUrl = reader.getContentUrl();
//		int lastSpIndex = contentUrl.lastIndexOf("/");
//		int extIndex = contentUrl.lastIndexOf(".bin");
//		String previewName = contentUrl.substring(lastSpIndex + 1, extIndex); 
		
		boolean isImageAvailable = previewService.isImageAvailable(previewName, reader.getLastModified(), Integer.parseInt(pageNumber), resolution);
		File previewImage = null;
		if(isImageAvailable) {
			previewImage = previewService.getPreview(previewName, reader.getLastModified(), Integer.parseInt(pageNumber), resolution);
		} else {
			String folderPath = previewService.getImageRootFolder(previewName, reader.getLastModified());
			File pdfTmpfile = new File(folderPath + "/" + previewName + ".pdf");
			if(!pdfTmpfile.exists()) {
				previewService.makeMotherFile(folderPath, previewName + ".pdf");
				OutputStream os = new FileOutputStream(pdfTmpfile);
				reader.getContent(os);
			}
			previewImage = previewService.createPreview(previewName, reader.getLastModified(), Integer.parseInt(pageNumber), resolution);
		}
		
		
		
		
		
//		File previewImage = getPreviewImage(pdfTmpfile.getAbsolutePath(), pageNumber);
		
   String mimetype = MimetypeMap.MIMETYPE_IMAGE_PNG;
//		int extIndex = previewImagePath.lastIndexOf('.');
//		if (extIndex != -1) {
//			mimetype = mimetypeService.getMimetype(previewImagePath
//					.substring(extIndex + 1));
//		}
        
        // setup file reader and stream
   if (logger.isDebugEnabled())
   logger.debug("Ready to output image " + previewImage.getName() + ", the image existed " + previewImage.exists());
        FileContentReader imageReader = new FileContentReader(previewImage);
        imageReader.setMimetype(mimetype);
        imageReader.setEncoding("UTF-8");
        
        Date lastModifiedDate = modified == null ? new Date(previewImage.lastModified()) : modified;
		
		// stream the contents of the file, but using the modifiedDate of the
		// original resource.
//		streamContent(req, res, previewImage, modified.getTime(), attach, attachFileName, model);

		//get preview image end
		
		
		// set mimetype for the content and the character encoding + length for
		// the stream
		res.setContentType(mimetype);
		res.setContentEncoding(imageReader.getEncoding());
		res.setHeader("Content-Length", Long.toString(imageReader.getSize()));
//
//		// set caching
		setResponseCache(res, lastModifiedDate, eTag, model);

		// get the content and stream directly to the response output stream
		// assuming the repository is capable of streaming in chunks, this
		// should allow large files
		// to be streamed directly to the browser response stream.
		try {
			imageReader.getContent(res.getOutputStream());
		} catch (SocketException e1) {
			// the client cut the connection - our mission was accomplished
			// apart from a little error message

		} catch (ContentIOException e2) {

		}
	}
	
//	private File getPreviewImage(final String pdfPath, String pageNumber) {
//		File tmpImagePath = TempFileProvider.getTempDir();
//		String targetFilePath = tmpImagePath.getAbsolutePath() + "/test";
//		String[] imageArgs = {"-imageType", "png", "-outputPrefix", targetFilePath, "-startPage", pageNumber, "-endPage", pageNumber, pdfPath};
//		previewService.createPreview(imageArgs);
//		return new File(targetFilePath + pageNumber + ".png");
//	}

	public void setPreviewService(PreviewService previewService) {
		this.previewService = previewService;
	}
}


