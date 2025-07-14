package org.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * ID列表包装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdsWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID列表
     */
    private List<Integer> ids;
} 