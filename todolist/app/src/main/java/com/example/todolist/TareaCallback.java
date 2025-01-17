package com.example.todolist;

public interface TareaCallback {
    void onStart();
    void onSuccess();
    void onError(String errorMsg);
}

