package io.vertx.blueprint.todolist.constants;

/**
 * 路由常量
 */
public final class Constants {
    private Constants(){}

    /**
     * API ROUTER
     */
    public static final String API_GET="/todos/:todoId";
    public static final String API_LIST_ALL="/todos";
    public static final String API_CREATE="/todos";
    public static final String API_UPDATE="/todos/:todoId";
    public static final String API_DELETE="/todos/:todoId";
    public static final String API_DELETE_ALL="/todos";

}
