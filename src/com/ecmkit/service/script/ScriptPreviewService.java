package com.ecmkit.service.script;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

import com.ecmkit.service.preview.PreviewService;

public class ScriptPreviewService extends BaseScopableProcessorExtension {
	/** Service Registry */
	private ServiceRegistry serviceRegistry;
	private PreviewService previewService;
	
	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	public PreviewService getPreviewService() {
		return previewService;
	}
	public void setPreviewService(PreviewService previewService) {
		this.previewService = previewService;
	}
	
	public int getPageNumber(String nodeId, NodeRef thumbnail) {
//		int lastSpIndex = contentUrl.lastIndexOf("/");
//		int extIndex = contentUrl.lastIndexOf(".bin");
//		String previewName = contentUrl.substring(lastSpIndex + 1, extIndex); 
		if(!previewService.isOriginalFileAvailable(nodeId, thumbnail)) {
			previewService.makeOriginalFile(thumbnail);
		}
		long modifiedTime = previewService.getModifyTime(thumbnail);
		return previewService.getTotalPageNumber(nodeId, modifiedTime);
	}
	
//	public String getIndentifyByUrl(String url) {
//		return previewService.getIndentifyByURL(url);
//	}
}
