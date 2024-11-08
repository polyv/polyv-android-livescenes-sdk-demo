package com.easefun.polyv.livecommon.module.modules.venue.enums;
import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.utils.PLVAppUtils;

import kotlin.Pair;

public enum PLVVenueStatusEnum {
    unStart() {
        @Override
        public Pair<Integer, Integer> getColor() {
            return new Pair<Integer, Integer>(0xFFABAFC0, 0xFF73778C);
        }

        @Override
        public String getStatusDesc() {
            return PLVAppUtils.getString(R.string.plv_multi_venue_status_no_start);
        }
    },

    end() {
        @Override
        public Pair<Integer, Integer> getColor() {
            return new Pair<Integer, Integer>(0xFFABAFC0, 0xFF73778C);
        }

        @Override
        public String getStatusDesc() {
            return PLVAppUtils.getString(R.string.plv_multi_venue_status_no_live);
        }
    },

    playback() {
        @Override
        public Pair<Integer, Integer> getColor() {
            return new Pair<Integer, Integer>(0xFF57B7FF, 0xFF2987FF);
        }

        @Override
        public String getStatusDesc() {
            return PLVAppUtils.getString(R.string.plv_multi_venue_status_replaying);
        }
    },

    waiting() {
        @Override
        public Pair<Integer, Integer> getColor() {
            return new Pair<Integer, Integer>(0xFF57B7FF, 0xFF2987FF);
        }

        @Override
        public String getStatusDesc() {
            return PLVAppUtils.getString(R.string.plv_multi_venue_status_waitting);
        }
    },

    live() {
        @Override
        public Pair<Integer, Integer> getColor() {
            return new Pair<Integer, Integer>(0xFFF06E6E, 0xFFE63A3A);
        }

        @Override
        public String getStatusDesc() {
            return PLVAppUtils.getString(R.string.plv_multi_venue_status_live);
        }
    },

    DEFAULT_STATUS() {
        @Override
        public Pair<Integer, Integer> getColor() {
            return new Pair<Integer, Integer>(0xFFABAFC0, 0xFF73778C);
        }

        @Override
        public String getStatusDesc() {
            return PLVAppUtils.getString(R.string.plv_multi_venue_status_no_live);
        }
    };

    public Pair<Integer, Integer> getColor() {
        return null;
    }
    public String getStatusDesc() {
        return null;
    }
}
