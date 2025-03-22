package com.lamuda.cloud.core.base;

import cn.hutool.core.lang.tree.TreeNode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * EntityWrapper
 *
 * @author Jin
 */
public interface BaseWrapper<E, V> {

    /**
     * entityVO
     *
     * @param entity
     */
    V entityVO(E entity);

    /**
     * treeVO
     *
     * @param list
     * @return
     */
    default List<TreeNode<V>> treeVO(List<TreeNode<V>> list) {
        return null;
    }

    /**
     * listVO
     *
     * @param list
     * @return
     */
    default List<V> listVO(List<E> list) {
        return list.stream().map(this::entityVO).collect(Collectors.toList());
    }

    /**
     * pageVO
     *
     * @param pages
     * @return
     */
    default IPage<V> pageVO(IPage<E> pages) {
        List<V> records = this.listVO(pages.getRecords());
        IPage<V> pageVo;
        pageVo = new Page<>(pages.getCurrent(), pages.getSize(), pages.getTotal());
        pageVo.setRecords(records);
        return pageVo;
    }

}
