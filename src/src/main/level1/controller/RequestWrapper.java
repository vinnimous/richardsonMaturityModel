package src.main.level1.controller;

public class RequestWrapper<T> {

  private String command;
  private T data;

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }
}
