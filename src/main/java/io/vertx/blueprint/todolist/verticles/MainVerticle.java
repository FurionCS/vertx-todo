package io.vertx.blueprint.todolist.verticles;

import io.vertx.blueprint.todolist.constants.Constants;
import io.vertx.blueprint.todolist.constants.RedisKey;
import io.vertx.blueprint.todolist.entity.Todo;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author chengsheng
 * @date 2017.12.07
 */
public class MainVerticle extends AbstractVerticle {

    private static final String HTTP_HOST="0.0.0.0";
    private static final String REDIS_HOST="106.14.166.180";
    private static final int HTTP_PORT=8889;
    private static final int REDIS_PORT=6379;

    private RedisClient redis;

    @Override
    public void start(Future<Void> future) throws Exception {
        //初始化数据库连接
        initData();

        Router router=Router.router(vertx);
        //CORS SUPPORT
        corsSet(router);

        router.post(Constants.API_CREATE).handler(this::handleAddTodo);
        router.delete(Constants.API_DELETE).handler(this::handlerDeleteOne);
        router.delete(Constants.API_DELETE_ALL).handler(this::handlerDeleteAll);
        router.get(Constants.API_GET).handler(this::handlerGetOne);
        router.get(Constants.API_LIST_ALL).handler(this::handlerListTodo);
        router.patch(Constants.API_UPDATE).handler(this::handlerUpdateTodo);

        vertx.createHttpServer().requestHandler(router::accept
            ).listen(HTTP_PORT,HTTP_HOST,result->{
                if (result.succeeded()){
                    future.complete();
                }else{
                    future.fail(result.cause());
                }
        });
    }

    /**
     * 更新待办事项
     * @param routingContext
     */
    private void handlerUpdateTodo(RoutingContext routingContext) {
    }

    /**
     * 获得待办事项列表
     * @param routingContext
     */
    private void handlerListTodo(RoutingContext routingContext) {
    }

    /**
     * 获得待办事项
     * @param routingContext
     */
    private void handlerGetOne(RoutingContext routingContext) {
        String todoId=routingContext.request().getParam("todoId");
        if(Objects.isNull(todoId)){
            sendError(400,routingContext.response());
        }else{
            redis.hget(RedisKey.REDIS_TODO_KEY,todoId,x->{
                if(x.succeeded()){
                    String result=x.result();
                    System.out.println(result);
                    if(Objects.isNull(result)){
                        sendError(404,routingContext.response());
                    }else{
                        routingContext.response()
                            .setStatusCode(200)
                            .putHeader("content-type","application/json")
                            .end(result);
                    }
                }else{
                    sendError(503,routingContext.response());
                }
            });
        }
    }



    /**
     * 删除所有待办事项
     * @param routingContext
     */
    private void handlerDeleteAll(RoutingContext routingContext) {
    }

    /**
     * 删除一个待办事项
     * @param routingContext
     */
    private void handlerDeleteOne(RoutingContext routingContext) {
        String todoId=routingContext.request().getParam("todoId");
        if(Objects.isNull(todoId)){
            sendError(400,routingContext.response());
            return;
        }else{
            redis.hdel(RedisKey.REDIS_TODO_KEY,todoId,r->{
                if(r.succeeded()){
                    routingContext.response().end("删除成功");
                }else{
                    sendError(404,routingContext.response());
                }
            });
        }

    }

    /**
     * 新增待办事项
     * @param routingContext
     */
    private void handleAddTodo(RoutingContext routingContext) {
           Todo todo= wrapObject(new Todo(routingContext.getBodyAsJson()),routingContext);
           redis.hset(RedisKey.REDIS_TODO_KEY,String.valueOf(todo.getId()),todo.toJson().toString(),r->{
               if(r.succeeded()) {
                   routingContext.response()
                       .setStatusCode(200)
                       .putHeader("content-type","application/json")
                       .end(Json.encodePrettily(todo));
               }else {
                   sendError(500,routingContext.response());
               }
           });
    }

    /**
     * 发送错误
     * @param code
     * @param response
     */
    private void sendError(int code, HttpServerResponse response) {
        response.setStatusCode(code).end("请求出错");
    }

    /**
     * 跨域设置
     * @param router
     */
    private void corsSet(Router router) {
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);

        router.route().handler(CorsHandler.create("*")
            .allowedHeaders(allowHeaders)
            .allowedMethods(allowMethods));
        router.route().handler(BodyHandler.create());
    }

    /**
     * 初始化数据库
     */
    private void initData() {
        /**
         * 创建Redis配置
         */
        RedisOptions config=new RedisOptions()
            .setHost(config().getString("redis.host",REDIS_HOST))
            .setPort(config().getInteger("redis.port",REDIS_PORT));
        /**
         * 创建redis连接对象
         */
        this.redis=RedisClient.create(vertx,config);
    }

    /**
     * Wrap the Todo entity with appropriate id and url
     *
     * @param todo    a todo entity
     * @param context RoutingContext
     * @return the wrapped todo entity
     */
    private Todo wrapObject(Todo todo, RoutingContext context) {
        int id = todo.getId();
        if (id > Todo.getIncId()) {
            Todo.setIncIdWith(id);
        } else if (id == 0) {
            todo.setIncId();
        }
        todo.setUrl(context.request().absoluteURI() + "/" + todo.getId());
        return todo;
    }

}
