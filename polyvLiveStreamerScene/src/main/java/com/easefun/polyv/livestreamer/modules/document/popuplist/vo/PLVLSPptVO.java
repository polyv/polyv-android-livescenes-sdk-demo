package com.easefun.polyv.livestreamer.modules.document.popuplist.vo;

import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVPptUploadStatus;

/**
 * PPT视图数据对象
 *
 * @author suhongtao
 */
public class PLVLSPptVO implements Cloneable {

    private String image;
    private String name;
    private String suffix;
    private Integer id;
    @PLVPptUploadStatus.Range
    private Integer uploadStatus;
    private Integer uploadProgress;
    private String fileId;

    public PLVLSPptVO(String image, Integer id) {
        this.image = image;
        this.id = id;
    }

    public PLVLSPptVO(String image, String name, String suffix, Integer id) {
        this.image = image;
        this.name = name;
        this.suffix = suffix;
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(Integer uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public Integer getUploadProgress() {
        return uploadProgress;
    }

    public void setUploadProgress(Integer uploadProgress) {
        this.uploadProgress = uploadProgress;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public PLVLSPptVO clone() {
        try {
            return (PLVLSPptVO) super.clone();
        } catch (CloneNotSupportedException e) {
            PLVLSPptVO pptVO = new PLVLSPptVO(image, name, suffix, id);
            pptVO.setUploadStatus(uploadStatus);
            pptVO.setUploadProgress(uploadProgress);
            pptVO.setFileId(fileId);
            return pptVO;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PLVLSPptVO pptVO = (PLVLSPptVO) o;

        if (image != null ? !image.equals(pptVO.image) : pptVO.image != null) return false;
        if (name != null ? !name.equals(pptVO.name) : pptVO.name != null) return false;
        if (suffix != null ? !suffix.equals(pptVO.suffix) : pptVO.suffix != null) return false;
        if (id != null ? !id.equals(pptVO.id) : pptVO.id != null) return false;
        if (uploadStatus != null ? !uploadStatus.equals(pptVO.uploadStatus) : pptVO.uploadStatus != null)
            return false;
        if (uploadProgress != null ? !uploadProgress.equals(pptVO.uploadProgress) : pptVO.uploadProgress != null)
            return false;
        return fileId != null ? fileId.equals(pptVO.fileId) : pptVO.fileId == null;
    }

    @Override
    public int hashCode() {
        int result = image != null ? image.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (suffix != null ? suffix.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (uploadStatus != null ? uploadStatus.hashCode() : 0);
        result = 31 * result + (uploadProgress != null ? uploadProgress.hashCode() : 0);
        result = 31 * result + (fileId != null ? fileId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PLVLSPptVO{" +
                "image='" + image + '\'' +
                ", name='" + name + '\'' +
                ", suffix='" + suffix + '\'' +
                ", id=" + id +
                ", uploadStatus=" + uploadStatus +
                ", uploadProgress=" + uploadProgress +
                ", fileId='" + fileId + '\'' +
                '}';
    }
}
