package src.main.level0.controller;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractHandler implements Handler {

  private static final Log LOG = LogFactory.getLog(AbstractHandler.class);

  protected ObjectMapper mapper = new ObjectMapper();

  protected abstract String command();

  @Override
  public boolean accept(String input) {
    try {
      final String command = mapper.readTree(input).at("/command").asText("NOP");
      return command().equals(command);
    } catch (IOException ex) {
      LOG.warn(ex.getMessage(), ex);
      return false;
    }
  }
}
