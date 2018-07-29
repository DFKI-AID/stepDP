package de.dfki.tocalog.kb;

import java.util.function.Function;

/**
 */
public class Result<T> {
    private T value;
    private Exception error;

    private Result() {
    }

    public static <R> Result<R> of(R r) {
        Result result = new Result();
        result.value = r;
        return result;
    }

    public static <R> Result<R> failed(Exception exception) {
        Result result = new Result();
        result.error = exception;
        return result;
    }

    public boolean isPresent() {
        return value != null;
    }

    public <R> Result<R> ifPresent(Function<T, R> consumer) {
        if (!isPresent()) {
            return failed(new Exception("no result available"));
        }
        try {
            R result = consumer.apply(value);
            return Result.of(result);
        } catch (Exception ex) {
            return failed(ex);
        }
    }
}
