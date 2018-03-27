package controllers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.lang.StringUtils;

import utils.ErrorInfo;
import utils.FileType;
import utils.FileUtil;
import utils.JSONUtils;
import utils.Security;
import business.newr.User;
import constants.Constants;

public class FileUpload extends BaseController{

	public static void upload(File attachment) {
		ErrorInfo error = new ErrorInfo();
		FileType type = FileUtil.uploadFile(attachment,error);
		type.filePath = Constants.HTTP_PATH + type.filePath;
		
		JSONObject json = new JSONObject();
		
		if(error.code < 0) {
			json.put("error", error.msg);
			renderJSON(json);
		}
		
		json.put("type", type);
		
		renderJSON(type);
		
		
	}
	
	/**
	 * 上传文件
	 * @param file
	 * @param type(1 图片、2 文本、3 视频、4 音频、5 表格)
	 */
	public static void uploadFile(File file, int type) {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> fileInfo = FileUtil.uploadFile(file, type, error);
		
		if (error.code < 0) {
			renderText(JSONObject.fromObject(error).toString());
		}
		
		fileInfo.put("fileName", (Constants.HTTP_PATH + fileInfo.get("fileName")));

		renderText(JSONObject.fromObject(fileInfo).toString());
	}
	
	/**
	 * 上传图片(用于编辑器)
	 * @param imgFile
	 */
	public static void uploadImage2(File imgFile) {
		ErrorInfo error = new ErrorInfo();
		FileType type = FileUtil.uploadFile(imgFile, error);
		if (error.code < 0) {
			JSONObject json = new JSONObject();
			json.put("error", error);
			
			renderText(json.toString());
		}
		
		String filename = Constants.HTTP_PATH + type.filePath.replaceAll("\\\\", "/");
		
		JSONObject json = new JSONObject();
		 json.put("error", 0);
		 json.put("url",filename);
		
		
		renderText(json.toString());
	}
	
	/**
	 * 上传图片
	 * @param imgFile
	 */
	public static void uploadImage(File imgFile) {
		ErrorInfo error = new ErrorInfo();
		FileType type = FileUtil.uploadFile(imgFile, error);
		if (error.code < 0) {
			JSONObject json = new JSONObject();
			json.put("error", error);
			
			renderText(json.toString());
		}
		
		String filename = Constants.HTTP_PATH + type.filePath.replaceAll("\\\\", "/");
		
		JSONObject json = new JSONObject();
		json.put("filename", filename);
		json.put("error", error);
		
		renderText(json.toString());
	}
	
	/**
	 * 上传图片
	 * @param imgFile
	 */
	public static void uploadImageReturnType(File imgFile) {
		ErrorInfo error = new ErrorInfo();
		FileType type = FileUtil.uploadFile(imgFile, error);
		if (error.code < 0) {
			JSONObject json = new JSONObject();
			json.put("error", error);
			
			renderText(json.toString());
		}
		
		type.filePath = Constants.HTTP_PATH + type.filePath.replaceAll("\\\\", "/");
		type.size = type.size;
		type.resolution = type.resolution;
		
		JsonConfig config = new JsonConfig();  
		config.setExcludes(new String[]{"file"}); 
		JSONArray array = JSONArray.fromObject(type, config);
		
		renderText(array.toString());
	}
	
	/**
	 * app上传文件
	 * imgFile 上传文件
	 * type 上传类型
	 * @throws IOException 
	 */
	public static String uploadPhoto(File imgFile, int type) throws IOException {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object>  jsonMap = new HashMap<String, Object>();
		
		Map<String, Object> fileInfo = FileUtil.uploadFile(imgFile, type, error);
		
		if (error.code < 0) {
			JSONObject json = new JSONObject();
			json.put("error", error);
			
			renderText(json.toString());
		}
		
		String fileName = null;
		
		//处理图片路径（即去掉图片名称的后缀名，提高上传安全）
		if(type == 1){
			String fileExt = (String) fileInfo.get("fileName");
			fileName = fileExt.substring(0, fileExt.lastIndexOf("."));
			
		}else{
			fileName = (String) fileInfo.get("fileName");
			
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "上传图片成功");
		jsonMap.put("filename", fileName);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	
}
