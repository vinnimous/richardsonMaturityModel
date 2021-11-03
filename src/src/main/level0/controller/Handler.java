package src.main.level0.controller;

public interface Handler {

  boolean accept(final String input);

  String handle(final String input);
}
