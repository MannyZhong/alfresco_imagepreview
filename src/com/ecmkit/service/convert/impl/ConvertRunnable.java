package com.ecmkit.service.convert.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import org.alfresco.util.TempFileProvider;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;

public class ConvertRunnable implements Callable<List<String>> {

	private static final Log logger = LogFactory.getLog(ConvertRunnable.class);
	private String entryKey;
	private String newPages;
	private TransformOption option;
	private String cmd = null;
	private String args = "-depth 8 -quality 75 -density ";
	
	public ConvertRunnable(String entryKey, String newPages, TransformOption option, String convertTool) {
		this.entryKey = entryKey;
		this.newPages = newPages;
		this.option = option;
		cmd = convertTool;
	}

	@Override
	public List<String> call() throws Exception {
		String[] startAndEnd = newPages.split("-");
		int start = Integer.parseInt(startAndEnd[0]);
		int end = Integer.parseInt(startAndEnd[1]);
		int resolution = option.getResolution();
		String originalFile = option.getOriginalFile();
		String outputPrefix = option.getTargetFolder(); 
		File targetFolder = new File(outputPrefix);
		if(!targetFolder.exists()) {
			targetFolder.mkdirs();
		}
		
		Runtime runtime = Runtime.getRuntime();
   Process process = null;
   String runtimeArg =  args + resolution + " -scene " + start;
//   List<String> pages = new ArrayList<String>();
   StringBuilder builder = new StringBuilder();
   builder.append("[");
   for(int pageIndex = start; pageIndex <= end; pageIndex++) {
	   builder.append(Integer.toString(pageIndex - 1));
	   if(pageIndex != end) {
		   builder.append(",");
	   } else {
		   builder.append("]");
	   }
   }
   
   runtimeArg = runtimeArg + " " + originalFile + builder.toString() + " " + outputPrefix + "/%d.png";
   String[] commandToExecute = new String[]{cmd, runtimeArg};
		try {
			// execute the command with full property replacement
			long beforeExe = System.currentTimeMillis();
			process = runtime.exec(cmd + " " + runtimeArg);
			process.waitFor();
			long afterExe = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				long usedTime = afterExe - beforeExe;
				logger.debug("Convert Entry " + entryKey + " for page " + newPages + " use time: " + usedTime + "ms");
			}
		} catch (IOException e) {
			// The process could not be executed here, so just drop out with an
			// appropriate error state
			String execOut = "";
			String execErr = e.getMessage();
			// int exitValue = defaultFailureExitValue;
			// ExecutionResult result = new ExecutionResult(null,
			// commandToExecute, errCodes, exitValue, execOut, execErr);
			if (logger.isDebugEnabled()) {
				logger.debug(execErr);
			}
		}

//		String[] imageArgs = {"-imageType", "png", "-outputPrefix", outputPrefix + "/", "-startPage", startAndEnd[0], "-endPage", startAndEnd[1], originalFile};
//		PDFToImage.main(imageArgs);
		
		List<String> convertEntry = new ArrayList<String>();
		convertEntry.add(entryKey);
		convertEntry.add(newPages);
		return convertEntry;
	}

	

}
