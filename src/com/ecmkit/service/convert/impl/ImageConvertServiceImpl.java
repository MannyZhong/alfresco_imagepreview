package com.ecmkit.service.convert.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ecmkit.service.convert.ImageConvertService;
import com.ecmkit.service.imagefile.ImageFileService;

public class ImageConvertServiceImpl implements ImageConvertService {
	private static final Log logger = LogFactory.getLog(ImageConvertServiceImpl.class);
	private static final int NTHREDS = 10;
	private static final String PAGE_SEPATOR = "-"; 
	private static final String EMPTY_VALUE = "empty_value";
	private ConcurrentMap<String, List<String>> convertEntry = new ConcurrentHashMap<String, List<String>>();
	ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
	private ImageFileService imageFileService;
	private int convertScale;
	private String convertTool;

	@Override
	public boolean convert(TransformOption option) {
		//check whether the convert request is already contained in the predecessor.
		String entryKey = imageFileService.getImageFolderPath(option);
		String newPages = option.getStartPage() + PAGE_SEPATOR + option.getEndPage();
//		List<String> checkResult = checkOverlap(entryKey, newPages);
		List<String> checkResult = putConvertEntry(entryKey, Integer.toString(option.getTargetPage()), newPages);
		newPages = checkResult.get(0);
		boolean isSuccessed = false;
		if(newPages != EMPTY_VALUE) {
			//new convert request is validate, we should create a runable to the request queue, 
			//and add an entry into entry map. 

			if (logger.isDebugEnabled()) {
				logger.debug("Make new runnable task, the target page is: " + option.getTargetPage());
			}
			ConvertRunnable runnable = new ConvertRunnable(entryKey, newPages, option, convertTool);
			
			try {
				Future<List<String>> submit = executor.submit(runnable);
				submit.get();
				isSuccessed = true;
				if (logger.isDebugEnabled()) {
					logger.debug("Target page " + option.getTargetPage() + " runnable task finished.");
				}
				
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				removeConvertEntry(entryKey, newPages);
			}
			
		}  
		
			
//		if(checkResult.size() > 1) {
		if (logger.isDebugEnabled()) {
			logger.debug("Waiting for existed task finish, the target page for this task is " + option.getTargetPage() + ".");
		}
//			checkResult.remove(0);
		if (logger.isDebugEnabled()) {
			StringBuilder builder = new StringBuilder();
			builder.append("Wait for existed entry ");
			for(String entry : checkResult) {
				builder.append(entry);
				builder.append(",");
			}
			builder.append(" to finish");
			logger.debug(builder.toString());
		}
		
		int count = 1;
		
		while(!(isSuccessed = checkEntryFinished(entryKey, checkResult)) && ++count <= 100) {
			LockSupport.parkNanos(100000000);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Final Check the target page " + option.getTargetPage() + " count number: " + count);
		}
//		}
		
//		else {
//			//new convert request is already contained in the predecessor, so we only need to add
//			// a dummy runable to the request to wait for the predecessor complete.
//			putConvertEntry(entryKey, newPages);
//		}
		
		
//		int count = 1;
//		boolean isSuccessed = false;
//		if (logger.isDebugEnabled())
//		logger.debug("Check the target file " + imageFile.getName() + " existed: " + imageFile.exists());
//		while(!(isSuccessed = imageFile.exists()) && ++count <= 100) {
//			LockSupport.parkNanos(100000000);
//		}
		if (logger.isDebugEnabled()) {
			File imageFile = imageFileService.getImageFile(option);
			logger.debug("Final Check the target file " + imageFile.getName() + " existed: " + imageFile.exists());
//			logger.debug("Final Check the target file " + imageFile.getName() + " count number: " + count);
			logger.debug("Final Check the target file " + imageFile.getName() + " size: " + imageFile.length());
		}
		
		return isSuccessed;
	}
	
	private boolean checkEntryFinished(String entryKey, List<String> existedPages) {
		boolean isCompleted = false;
		if(!convertEntry.containsKey(entryKey)) {
			isCompleted = true;
		} else {
			isCompleted = true;
			List<String> processingPages = convertEntry.get(entryKey);
			for(String page : existedPages) {
				if(processingPages.contains(page)) {
					isCompleted = false;
					break;
				}
			}
			
			
		}
		return isCompleted;
	}
	
	
	private List<String> checkOverlap(String entryKey, String targetPage, String newPages) {
		List<String> checkResult = new ArrayList<String>();
		List<String> overlapedPages = new ArrayList<String>();
		if(convertEntry.containsKey(entryKey)) {
			List<String> queuedPages = convertEntry.get(entryKey);
			for(String pageEntry : queuedPages) {
				List<String> filterResult = getNonOverlapPages(targetPage, newPages, pageEntry);
				if(filterResult.size() > 1) {
					overlapedPages.add(filterResult.get(1));
				}
				newPages = filterResult.get(0);
				if(newPages == EMPTY_VALUE) {
					break;
				}
			}
		}
		checkResult.add(newPages);
		//overlapedPages are pages that has overlaps with the target page, 
		//not all the existed queued pages.
		checkResult.addAll(overlapedPages);
		return checkResult;
	}
	
	private List<String> getNonOverlapPages(String targetPage, String newPages, String queuedPages) {
		String nonOverlapPages = null;
		List<String> result = new ArrayList<String>();
		
		List<Integer> queuePageList = convertToList(queuedPages);
		//if the existed queue task already contains the target page,
		//we don't create new task, in order to make the convention as less as possible
		if(queuePageList.contains(Integer.parseInt(targetPage))) {
			logger.debug("The target page " + targetPage + " is contained in the existed queue page: " + queuePageList);
			result.add(EMPTY_VALUE);
			result.add(queuedPages);
			return result;
		} else {
			logger.debug("The target page " + targetPage + " is not contained in the existed queue page: " + queuePageList);
		}
		
		//the existed queue doesn't contains the target page, 
		//we need to filter the new task to avoid the duplicated convention.
		List<Integer> newPageList = convertToList(newPages);
		int newPageNum = newPageList.size();
		
		newPageList.removeAll(queuePageList);
		
		int remaindPageNum = newPageList.size();
		if(remaindPageNum > 0) {
			if(remaindPageNum > 1) {
				nonOverlapPages = newPageList.get(0) + PAGE_SEPATOR + newPageList.get(remaindPageNum - 1);
			} else {
				nonOverlapPages = newPageList.get(0) + PAGE_SEPATOR + newPageList.get(0);
			}
			
		}
		if(nonOverlapPages == null) {
			result.add(EMPTY_VALUE);
		} else {
			result.add(nonOverlapPages);
		}
		
		
		if(remaindPageNum < newPageNum) {
			logger.debug("The target page " + targetPage + 
					" with new queue " + newPages + 
					" overlaps with the existed queue " + queuedPages + 
					", and the remained pages is " + nonOverlapPages);
			result.add(queuedPages);
		} else {
			logger.debug("The target page " + targetPage + 
					" with new queue " + newPages + 
					" doesn't overlaps with the existed queue " + queuedPages);
		}
		return result;
	}
	
	private List<Integer> convertToList(String pages) {
		List<Integer> pageList = new ArrayList<Integer>(); 
		if(pages != null) {
			String[] startAndEnd = pages.split(PAGE_SEPATOR);
			int start = Integer.parseInt(startAndEnd[0]);
			int end = Integer.parseInt(startAndEnd[1]);
			if(start < end) {
				for(int pageIndex = start; pageIndex <= end; pageIndex++) {				
						pageList.add(pageIndex);		
				}
			} else {
				pageList.add(start);
			}
		}
		return pageList;
	}
	
	
	private static List<String> Subtract(List<String> coll1, List<String> coll2) {
		List<String> result = new ArrayList<String>(coll2);
		result.removeAll(coll1);
		return result;
	}
	
	private List<String> putConvertEntry(String entryKey, String targetPage, String newPages) {
		List<String> checkResult = null;
		//in multiple threads environment, we use C.A.S solution to avoid add lock, 
		//in order to improve the concurrent performance.
		boolean isSuccessed = false;
		do{
			if(convertEntry.containsKey(entryKey)) {
				
				List<String> queuedPages = convertEntry.get(entryKey);
				checkResult = checkOverlap(entryKey, targetPage, newPages);
				String filteredPages = checkResult.get(0);
				
				if(filteredPages == EMPTY_VALUE) {
					return checkResult;
				}
				
				List<String> newPageValues = new ArrayList<String>(queuedPages);
				newPageValues.add(filteredPages);
	//			queuedPages.add(newPages);
				isSuccessed = convertEntry.replace(entryKey, queuedPages, newPageValues);
	//					.put(entryKey, queuedPages);
			} else {
				List<String> newPageValues = new ArrayList<String>();
				newPageValues.add(newPages);
				checkResult = newPageValues;
				newPageValues = convertEntry.putIfAbsent(entryKey, newPageValues);
	//			put(entryKey, newPageValues);
				if(newPageValues == null) {
					isSuccessed = true;
				}
			}
		} while(!isSuccessed);
		return checkResult;
	}
	
	private boolean removeConvertEntry(String entryKey, String newPages) {
//		if(convertEntry.containsKey(entryKey)) {
		//in multiple threads environment, we use C.A.S solution to avoid add lock, 
		//in order to improve the concurrent performance.
		boolean isSuccessed = false;
		do{
			List<String> queuedPages = convertEntry.get(entryKey);
			List<String> clonedPages = new ArrayList<String>(queuedPages);
			clonedPages.remove(newPages);
			if(clonedPages.size() == 0) {
				isSuccessed = convertEntry.remove(entryKey, queuedPages);
			} else {
				isSuccessed = convertEntry.replace(entryKey, queuedPages, clonedPages);
			}
		} while(!isSuccessed);
//		}
		return true;
	}
	
	@Override
	public void CalculateStartAndEndPage(TransformOption option) {
		int pageNumber = option.getTotalPages();
		int targetPage = option.getTargetPage();
		
		int startPage = targetPage;
		int endPage = targetPage;
		if(targetPage >= 1 && targetPage <= pageNumber) {
			startPage = targetPage - convertScale;
			if(startPage < 1) {
				startPage = 1;
			}
			
			endPage = targetPage + convertScale;
			if(endPage > pageNumber) {
				endPage = pageNumber;
			}
		} else {
			
		}
		
		option.setStartPage(startPage);
		option.setEndPage(endPage);
	}

	public ImageFileService getImageFileService() {
		return imageFileService;
	}

	public void setImageFileService(ImageFileService imageFileService) {
		this.imageFileService = imageFileService;
	}

	public int getConvertScale() {
		return convertScale;
	}

	public void setConvertScale(int convertScale) {
		this.convertScale = convertScale;
	}

	public String getConvertTool() {
		return convertTool;
	}

	public void setConvertTool(String convertTool) {
		this.convertTool = convertTool;
	}
	

}
