package com.hjc.backend.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;

    private Long total;

    private Long pageNum;

    private Long pageSize;

    private Long pages;

    public static <T> PageResult<T> of(List<T> records, Long total, Long pageNum, Long pageSize) {
        long pages = pageSize == null || pageSize <= 0 ? 0L : (total + pageSize - 1) / pageSize;
        return new PageResult<>(records, total, pageNum, pageSize, pages);
    }

    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages()
        );
    }
}
