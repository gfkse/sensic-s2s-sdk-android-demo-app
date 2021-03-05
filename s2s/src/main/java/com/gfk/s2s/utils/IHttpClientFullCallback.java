package com.gfk.s2s.utils;

public interface IHttpClientFullCallback<T> {
    void onCompletion(T data);
    void onFinished();
}
