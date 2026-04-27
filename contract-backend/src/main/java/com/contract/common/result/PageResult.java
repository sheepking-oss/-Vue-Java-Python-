package com.contract.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> records;
    private Long total;
    private Long size;
    private Long current;
    private Long pages;

    public static <T> PageResult<T> of(List<T> records, Long total, Long size, Long current) {
        long pages = total > 0 && size > 0 ? (total + size - 1) / size : 0;
        return new PageResult<>(records, total, size, current, pages);
    }
}
