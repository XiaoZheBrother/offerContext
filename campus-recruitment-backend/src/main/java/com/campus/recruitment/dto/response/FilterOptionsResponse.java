package com.campus.recruitment.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class FilterOptionsResponse {
    private List<FilterItem> classTypes;
    private List<FilterItem> campusTypes;
    private List<FilterItem> cities;

    @Data
    public static class FilterItem {
        private Integer id;
        private String name;
        private Boolean isTop;  // For cities, whether it's a top city

        public FilterItem() {}
        public FilterItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }
        public FilterItem(Integer id, String name, Boolean isTop) {
            this.id = id;
            this.name = name;
            this.isTop = isTop;
        }
    }
}
