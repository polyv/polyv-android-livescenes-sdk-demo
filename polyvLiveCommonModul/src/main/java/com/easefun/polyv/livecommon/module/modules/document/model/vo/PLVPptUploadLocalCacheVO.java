package com.easefun.polyv.livecommon.module.modules.document.model.vo;

import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVPptUploadStatus;
import com.easefun.polyv.livescenes.upload.PLVSDocumentUploadConstant;
import com.plv.foundationsdk.utils.PLVGsonUtil;

/**
 * @author suhongtao
 */
public class PLVPptUploadLocalCacheVO {

    private String fileId;
    private String filePath;
    private String fileName;
    @PLVSDocumentUploadConstant.PPTConvertType.PPTConvertTypeAnno
    private String convertType;
    @PLVPptUploadStatus.Range
    private Integer status;

    public PLVPptUploadLocalCacheVO() {
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getConvertType() {
        return convertType;
    }

    public void setConvertType(String convertType) {
        this.convertType = convertType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "PLVPptUploadLocalCacheVO{" +
                "fileId='" + fileId + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", convertType='" + convertType + '\'' +
                ", status=" + status +
                '}';
    }

    public static class Serializer {

        public static String toJson(PLVPptUploadLocalCacheVO vo) {
            return PLVGsonUtil.toJson(vo);
        }

        public static PLVPptUploadLocalCacheVO fromJson(String json) {
            return PLVGsonUtil.fromJson(PLVPptUploadLocalCacheVO.class, json);
        }

    }
}
