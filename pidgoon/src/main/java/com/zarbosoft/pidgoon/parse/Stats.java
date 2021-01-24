package com.zarbosoft.pidgoon.parse;

public class Stats {
  public int totalLeaves = 0;
  public int maxLeaves = 0;
  public int steps = 0;

  public Stats(final Stats stats) {
    this.totalLeaves = stats.totalLeaves;
    this.maxLeaves = stats.maxLeaves;
    this.steps = stats.steps;
  }

  public Stats() {}
}
