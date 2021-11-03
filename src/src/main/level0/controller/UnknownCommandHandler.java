package src.main.level0.controller;

import org.springframework.stereotype.Component;

@Component
public class UnknownCommandHandler implements Handler {

  @Override
  public boolean accept(String input) {
    return true;
  }

  @Override
  public String handle(String input) {
    return "{\"status\":\"ERROR\",\"message\":\"unknown command\"}";
  }
}
