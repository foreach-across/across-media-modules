package com.foreach.imageserver.core.web.controllers;


import com.foreach.imageserver.core.web.displayables.JsonResponse;

public class BaseImageAPIController {

    protected JsonResponse<Void> error(String message) {
        JsonResponse<Void> result = new JsonResponse<>();
        result.setSuccess(false);
        result.setErrorMessage(message);
        return result;
    }

    protected <T> JsonResponse<T> success(T result) {
        JsonResponse<T> jsonResponse = success();
        jsonResponse.setResult(result);
        return jsonResponse;
    }


    protected <T> JsonResponse<T> success() {
        JsonResponse<T> jsonResponse = new JsonResponse<>();
        jsonResponse.setSuccess(true);
        return jsonResponse;
    }
}
