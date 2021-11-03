package src.main.level1.controller;

public class ResponseWrapper<T> {

  private String status;
  private String message;
  private T data;

  public static <T> ResponseWrapper<T> error(String message) {
    return new ResponseWrapper<>("ERROR", message, null);
  }

  public static <T> ResponseWrapper success(T data) {
    return new ResponseWrapper<>("SUCCESS", null, data);
  }

  public ResponseWrapper(String status, String message, T data) {
    this.status = status;
    this.message = message;
    this.data = data;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }
}
