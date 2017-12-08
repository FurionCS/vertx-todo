package io.vertx.blueprint.todolist.service;

import io.vertx.blueprint.todolist.entity.Todo;
import io.vertx.core.Future;

import java.util.List;
import java.util.Optional;

/**
 * 待办事项接口
 * @author chengsheng
 * @date 2017.12.08
 */
public interface TodoService {


    /**
     * 插入
     * @param todo
     * @return
     */
    Future<Boolean> insert(Todo todo);

    /**
     * 获得所有列表
     * @return
     */
    Future<List<Todo>> getAll();

    /**
     * 获得一个待办事项
     * @param todoId
     * @return
     */
    Future<Optional<Todo>> getCertain(String todoId);

    /**
     * 更新
     * @param todoId
     * @param newTodo
     * @return
     */
    Future<Todo> update(String todoId,Todo newTodo);

    /**
     * 删除一条待办事项
     * @param todoId
     * @return
     */
    Future<Boolean> delete(String todoId);

    /**
     * 删除所有待办事项
     * @return
     */
    Future<Boolean> deleteAll();

}
