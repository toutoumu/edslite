package com.sovworks.eds.android.filemanager.activities.zip;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <pre>
 * 输入:
 *  /a/b/c/d/e
 *  /a/b/e/f/g
 *  /a/b/h
 *  /a/i/j
 *  /a/i/k
 *
 * 输出:
 *          a
 *        /   \
 *       b     i
 *      /|\   / \
 *     c e h j   k
 *     | |
 *     d f
 *     | |
 *     e g
 * </pre>
 */
public class TreeData<T> {
    // 节点数据
    private T data;
    private String name;
    // 父节点
    private TreeData<T> parent;
    // NB: LinkedHashSet preserves insertion order
    private Set<TreeData<T>> children = new LinkedHashSet<>();

    public TreeData(String name) {
        this.name = name;
    }

    /**
     * 添加子节点并返回该节点, 如果已经存在那么返回存在的节点
     *
     * @param name 子节点名称
     * @return 子节点
     */
    public TreeData<T> addAndReturnChild(String name) {
        // 在子节点中查找, 如果找到直接返回该节点
        for (TreeData<T> child : children) {
            if (child.name.equals(name)) {
                return child;
            }
        }
        // 如果子节点中不存在,那么添加进去
        return addChild(new TreeData<>(name));
    }

    /**
     * 添加子节点
     *
     * @param child 子节点
     * @return 返回添加的节点
     */
    TreeData<T> addChild(TreeData<T> child) {
        child.parent = this;
        children.add(child);
        return child;
    }

    public Set<TreeData<T>> getChildren() {
        return children;
    }

    public void setChildren(Set<TreeData<T>> children) {
        this.children = children;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeData<T> getParent() {
        return parent;
    }

    public void setParent(@Nullable TreeData<T> parent) {
        this.parent = parent;
    }

    public class TreeTest {
        public void test(String[] args) {
            TreeData<String> forest = new TreeData<>("root");

            for (String tree : Arrays.asList("a/b/c/d/e", "a/b/e/f/g", "a/b/h", "a/i/j", "a/i/k")) {
                TreeData<String> temp = forest;
                for (String data : tree.split("/")) {
                    temp = temp.addAndReturnChild(data);
                }
                temp.setData(tree);
            }
            System.out.println(forest);
        }
    }
}




