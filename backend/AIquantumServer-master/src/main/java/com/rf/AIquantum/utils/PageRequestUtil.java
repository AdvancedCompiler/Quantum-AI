package com.rf.AIquantum.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * @Description:分页工具
 * @Author: zsf
 * @Date: 2022/7/5
 */
public class PageRequestUtil {

    public static PageRequest of(int page, int size) {
        if (page <=0){page=0;}else {page = page-1;}
        return PageRequest.of(page, size, Sort.unsorted());
    }
}
