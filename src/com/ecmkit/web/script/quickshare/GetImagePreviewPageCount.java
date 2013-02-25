package com.ecmkit.web.script.quickshare;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.quickshare.InvalidSharedIdException;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Scriptable;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.ecmkit.service.preview.PreviewService;

public class GetImagePreviewPageCount  extends AbstractWebScript {

	private QuickShareService quickShareService;
	private NodeService nodeService;
	private ThumbnailService thumbnailService;
	private static final Log logger = LogFactory.getLog(GetImagePreviewPageCount.class);
	private ServiceRegistry serviceRegistry;
	private PreviewService previewService;
	protected ContentService contentService;
	
	@Override
	public void execute(WebScriptRequest req, final WebScriptResponse res)
			throws IOException {
		
        
        // create map of template vars (params)
        final Map<String, String> params = req.getServiceMatch().getTemplateVars();
        final String sharedId = params.get("shared_id");
        if (sharedId == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "A valid sharedId must be specified !");
        }
        
        try
        {
            Pair<String, NodeRef> pair = quickShareService.getTenantNodeRefFromSharedId(sharedId);
            final String tenantDomain = pair.getFirst();
            final NodeRef nodeRef = pair.getSecond();
            
            final String thumbnailName = params.get("thumbnailname");

            TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
            {
                public Void doWork() throws Exception
                {
                    if (! nodeService.getAspects(nodeRef).contains(QuickShareModel.ASPECT_QSHARE))
                    {
                        throw new InvalidNodeRefException(nodeRef);
                    }
                    

                    String pageConuntResult = getThumbnailRef(thumbnailName, nodeRef, "force");
                    res.getWriter().write(pageConuntResult);
                    return null;
                }
            }, tenantDomain);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("QuickShare - retrieved content: "+sharedId+" ["+nodeRef+"]");
            }
            
            
            
    		
        }
        catch (InvalidSharedIdException ex)
        {
            logger.error("Unable to find: "+sharedId);
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find: "+sharedId);
        }
        catch (InvalidNodeRefException inre)
        {
            logger.error("Unable to find: "+sharedId+" ["+inre.getNodeRef()+"]");
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find: "+sharedId);
        }
        
        
		
	}
	
	private String getThumbnailRef(String thumbnailName, NodeRef nodeRef, String generatePattern) {
		NodeRef thumbnailNodeRef = thumbnailService.getThumbnailByName(nodeRef, ContentModel.PROP_CONTENT, thumbnailName);
        if (thumbnailNodeRef == null)
        {
            // Get the queue/force create setting
            boolean qc = false;
            boolean fc = false;
            String c = generatePattern;
            if (c != null)
            {
               if (c.equals("queue"))
               {
                  qc = true;
               }
               else if (c.equals("force"))
               {
                  fc = true;
               }
            }
            
            // Get the place holder flag
            
            Scriptable scope = new BaseScopableProcessorExtension().getScope(); // note: required for ValueConverter (collection)
            ScriptNode node = new ScriptNode(nodeRef, serviceRegistry, scope);
            
            // Queue the creation of the thumbnail if appropriate
            if (fc)
            {
                ScriptNode thumbnailNode = node.createThumbnail(thumbnailName, false);
                if (thumbnailNode != null)
                {
                    thumbnailNodeRef = thumbnailNode.getNodeRef();
                }
            }
            else
            {
               if (qc)
               {
                   node.createThumbnail(thumbnailName, true);
               }
            }
            
        }
        
//        return thumbnailNodeRef;
//        ContentReader reader = contentService.getReader(thumbnailNodeRef, ContentModel.PROP_CONTENT);
//        String url = reader.getContentUrl();
//		 String previewName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
		 String previewName = previewService.getStoreId(thumbnailNodeRef);
        if(!previewService.isOriginalFileAvailable(previewName, thumbnailNodeRef)) {
			previewService.makeOriginalFile(thumbnailNodeRef);
		}
		long modifiedTime = previewService.getModifyTime(thumbnailNodeRef);
		int pageNumber =  previewService.getTotalPageNumber(previewName, modifiedTime);
		
		JSONObject json = new JSONObject();
		try {
			json.put("totalPageNumber", Integer.toString(pageNumber));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}


	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public ThumbnailService getThumbnailService() {
		return thumbnailService;
	}

	public void setThumbnailService(ThumbnailService thumbnailService) {
		this.thumbnailService = thumbnailService;
	}

	public PreviewService getPreviewService() {
		return previewService;
	}

	public void setPreviewService(PreviewService previewService) {
		this.previewService = previewService;
	}

	public ContentService getContentService() {
		return contentService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public QuickShareService getQuickShareService() {
		return quickShareService;
	}

	public void setQuickShareService(QuickShareService quickShareService) {
		this.quickShareService = quickShareService;
	}

}
