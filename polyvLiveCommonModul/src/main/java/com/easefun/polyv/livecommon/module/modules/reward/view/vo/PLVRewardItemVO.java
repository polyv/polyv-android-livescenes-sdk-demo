package com.easefun.polyv.livecommon.module.modules.reward.view.vo;

import com.plv.livescenes.model.pointreward.PLVRewardSettingVO;

/**
 * @author Hoshiiro
 */
public class PLVRewardItemVO {

    private int goodId;
    private String name;
    private String img;
    private String price;
    private String unit;
    private Integer sequence;
    private String enabled;

    public PLVRewardItemVO(PLVRewardSettingVO.GiftDonateDTO.GiftDetailDTO giftDetailDTO, String priceUnit) {
        this.goodId = giftDetailDTO.getGoodId();
        this.name = giftDetailDTO.getName();
        this.img = giftDetailDTO.getImg();
        this.price = giftDetailDTO.getPrice();
        this.sequence = giftDetailDTO.getSequence();
        this.enabled = giftDetailDTO.getEnabled();
        this.unit = priceUnit;
    }

    public int getGoodId() {
        return goodId;
    }

    public PLVRewardItemVO setGoodId(int goodId) {
        this.goodId = goodId;
        return this;
    }

    public String getName() {
        return name;
    }

    public PLVRewardItemVO setName(String name) {
        this.name = name;
        return this;
    }

    public String getImg() {
        return img;
    }

    public PLVRewardItemVO setImg(String img) {
        this.img = img;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public PLVRewardItemVO setPrice(String price) {
        this.price = price;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public PLVRewardItemVO setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    public Integer getSequence() {
        return sequence;
    }

    public PLVRewardItemVO setSequence(Integer sequence) {
        this.sequence = sequence;
        return this;
    }

    public String getEnabled() {
        return enabled;
    }

    public PLVRewardItemVO setEnabled(String enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public String toString() {
        return "PLVRewardItemVO{" +
                "name='" + name + '\'' +
                ", img='" + img + '\'' +
                ", price='" + price + '\'' +
                ", unit='" + unit + '\'' +
                ", sequence=" + sequence +
                ", enabled='" + enabled + '\'' +
                '}';
    }
}
