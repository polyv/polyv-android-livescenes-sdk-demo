package com.easefun.polyv.livedemo.hiclass.fragments.teacher.vo;

import com.google.gson.Gson;
import com.plv.livescenes.hiclass.vo.PLVHCTeacherLoginResultVO;

/**
 * @author suhongtao
 */
public class PLVHCLoginCompanyVO {

    private String companyId;
    private String companyName;

    public PLVHCLoginCompanyVO() {
    }

    public static PLVHCLoginCompanyVO fromLoginResult(PLVHCTeacherLoginResultVO.CompanyVO companyVO) {
        PLVHCLoginCompanyVO vo = new PLVHCLoginCompanyVO();
        vo.setCompanyName(companyVO.getCompany());
        vo.setCompanyId(companyVO.getUserId());
        return vo;
    }

    public static PLVHCLoginCompanyVO fromJson(String json) {
        return new Gson().fromJson(json, PLVHCLoginCompanyVO.class);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String toString() {
        return "PLVHCLoginCompanyVO{" +
                "companyId='" + companyId + '\'' +
                ", companyName='" + companyName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PLVHCLoginCompanyVO)) return false;

        PLVHCLoginCompanyVO that = (PLVHCLoginCompanyVO) o;

        if (companyId != null ? !companyId.equals(that.companyId) : that.companyId != null)
            return false;
        return companyName != null ? companyName.equals(that.companyName) : that.companyName == null;
    }

    @Override
    public int hashCode() {
        int result = companyId != null ? companyId.hashCode() : 0;
        result = 31 * result + (companyName != null ? companyName.hashCode() : 0);
        return result;
    }
}
