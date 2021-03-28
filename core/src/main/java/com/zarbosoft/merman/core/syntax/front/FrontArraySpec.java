package com.zarbosoft.merman.core.syntax.front;

public class FrontArraySpec extends FrontArraySpecBase {
  private final String back;

  public static class Config {
    public final String back;
    public final FrontArraySpecBase.Config base;

    public Config(String back, FrontArraySpecBase.Config base) {
      this.back = back;
      this.base = base;
    }
  }

  public FrontArraySpec(Config config) {
    super(config.base);
    this.back = config.back;
  }

  @Override
  public String field() {
    return back;
  }
}
