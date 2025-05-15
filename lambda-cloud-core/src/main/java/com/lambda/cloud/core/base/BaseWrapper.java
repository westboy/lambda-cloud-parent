package com.lambda.cloud.core.base;

import cn.hutool.core.lang.tree.TreeNode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * BaseWrapper
 *
 * @author Jin
 */
public interface BaseWrapper<E, V> {

    /**
     * entityVO
     *
     * @param entity E
     */
    V entityVO(E entity);

    /**
     * treeVO
     *
     * @param list List<TreeNode<V>>
     * @return List<TreeNode<V>>
     */
    default List<TreeNode<V>> treeVO(List<TreeNode<V>> list) {
        return null;
    }

    /**
     * listVO
     *
     * @param list List<E>
     * @return List<E>
     */
    default List<V> listVO(List<E> list) {
        return list.stream().map(this::entityVO).collect(Collectors.toList());
    }

    /**
     * pageVO
     *
     * @param pages IPage<E>
     * @return IPage<V>
     */
    default IPage<V> pageVO(IPage<E> pages) {
        List<V> records = this.listVO(pages.getRecords());
        IPage<V> pageVo;
        pageVo = new Page<>(pages.getCurrent(), pages.getSize(), pages.getTotal());
        pageVo.setRecords(records);
        return pageVo;
    }

}
