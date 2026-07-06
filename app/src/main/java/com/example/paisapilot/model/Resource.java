package com.example.paisapilot.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Generic Resource wrapper to represent loading states and results.
 *
 * @param <T> type of the payload when the operation succeeds
 */
public class Resource<T> {

    public enum Status {
        LOADING,
        SUCCESS,
        ERROR
    }

    private final Status status;
    @Nullable
    private final T data;
    @Nullable
    private final String message;

    private Resource(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    /**
     * Create a loading resource. Data is typically null while loading.
     */
    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null);
    }

    /**
     * Create a success resource that carries data.
     */
    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    /**
     * Create an error resource with a human-readable message.
     */
    public static <T> Resource<T> error(@NonNull String message) {
        return new Resource<>(Status.ERROR, null, message);
    }

    public Status getStatus() {
        return status;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public String getMessage() {
        return message;
    }
}
