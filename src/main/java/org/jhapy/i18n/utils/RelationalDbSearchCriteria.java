package org.jhapy.i18n.utils;

import lombok.Data;

@Data
public class RelationalDbSearchCriteria {
  private String key;
  private Object value;
  private RelationalDbSearchOperation operation;

  public RelationalDbSearchCriteria() {}

  public RelationalDbSearchCriteria(
      String key, Object value, RelationalDbSearchOperation operation) {
    this.key = key;
    this.value = value;
    this.operation = operation;
  }
}
