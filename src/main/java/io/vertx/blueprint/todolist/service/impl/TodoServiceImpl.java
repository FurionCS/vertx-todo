package io.vertx.blueprint.todolist.service.impl;

import io.vertx.blueprint.todolist.constants.RedisKey;
import io.vertx.blueprint.todolist.entity.Todo;
import io.vertx.blueprint.todolist.service.TodoService;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TodoServiceImpl implements TodoService{

    private final Vertx vertx;
    private final RedisOptions config;
    private final RedisClient redis;

    public TodoServiceImpl(RedisOptions config){
        this(Vertx.vertx(),config);
    }

    public TodoServiceImpl(Vertx vertx,RedisOptions config){
        this.vertx=vertx;
        this.config=config;
        this.redis=RedisClient.create(vertx,config);
    }



    @Override
    public Future<Boolean> insert(Todo todo) {
        Future<Boolean> result=Future.future();
        redis.hset(RedisKey.REDIS_TODO_KEY,String.valueOf(todo.getId()),todo.toJson().toString(),r->{
            if(r.succeeded()){
                result.complete(true);
            }else{
                result.complete(false);
            }
        });
        return result;
    }

    @Override
    public Future<List<Todo>> getAll() {
        Future<List<Todo>> result=Future.future();
        redis.hvals(RedisKey.REDIS_TODO_KEY,r->{
            if(r.succeeded()){
                result.complete(r.result().stream().map(res->new Todo((String) res) ).collect(Collectors.toList()));
            }else{
                result.fail(r.cause());
            }
        });
        return result;
    }

    @Override
    public Future<Optional<Todo>> getCertain(String todoId) {
        Future<Optional<Todo>> result=Future.future();
        redis.hget(RedisKey.REDIS_TODO_KEY,todoId,r->{
            if(r.succeeded()) {
                result.complete(Optional.ofNullable(
                    r.result()==null?null:new Todo(r.result())
                ));
            }else{
                result.fail(r.cause());
            }
        });
        return result;
    }

    @Override
    public Future<Todo> update(String todoId, Todo newTodo) {
        return this.getCertain(todoId).compose(old->{
            if(old.isPresent()){
                Todo fnTodo=old.get().merge(newTodo);
                return this.insert(fnTodo).map(r->r?fnTodo:null);
            }else{
                return Future.succeededFuture();
            }
        });
    }



    @Override
    public Future<Boolean> delete(String todoId) {
        Future<Boolean> result=Future.future();
        redis.hdel(RedisKey.REDIS_TODO_KEY,todoId,r->{
            if(result.succeeded()){
                result.complete(true);
            }else{
                result.complete(false);
            }
        });
        return result;
    }

    @Override
    public Future<Boolean> deleteAll() {
        Future<Boolean> result=Future.future();
        redis.del(RedisKey.REDIS_TODO_KEY,r->{
            if(r.succeeded()){
                result.complete(true);
            }else{
                result.complete(false);
            }
        });
        return result;
    }
}
