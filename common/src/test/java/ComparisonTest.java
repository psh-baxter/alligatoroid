import com.zarbosoft.rendaw.common.ChainComparator;
import com.zarbosoft.rendaw.common.Common;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ComparisonTest {
  @Test
  public void ordered() {
    assertThat(Common.isOrdered(0, 1), equalTo(true));
  }

  @Test
  public void notOrdered() {
    assertThat(Common.isOrdered(1, 0), equalTo(false));
  }

  @Test
  public void chainLesser() {
    assertThat(
        Common.isOrdered(new ChainComparator<Integer>().lesserFirst(v -> v).build(), 0, 1),
        equalTo(true));
  }

  @Test
  public void chainNotLesser() {
    assertThat(
        Common.isOrdered(new ChainComparator<Integer>().lesserFirst(v -> v).build(), 1, 0),
        equalTo(false));
  }

  @Test
  public void chainGreater() {
    assertThat(
        Common.isOrdered(new ChainComparator<Integer>().greaterFirst(v -> v).build(), 1, 0),
        equalTo(true));
  }

  @Test
  public void chainNotGreater() {
    assertThat(
        Common.isOrdered(new ChainComparator<Integer>().greaterFirst(v -> v).build(), 0, 1),
        equalTo(false));
  }

  @Test
  public void chainTrueFirst() {
    assertThat(
        Common.isOrdered(new ChainComparator<Boolean>().trueFirst(v -> v).build(), true, false),
        equalTo(true));
  }

  @Test
  public void chainNotTrueFirst() {
    assertThat(
        Common.isOrdered(new ChainComparator<Boolean>().trueFirst(v -> v).build(), false, true),
        equalTo(false));
  }

  @Test
  public void chainNotFalseFirst() {
    assertThat(
        Common.isOrdered(new ChainComparator<Boolean>().falseFirst(v -> v).build(), true, false),
        equalTo(false));
  }

  @Test
  public void chainFalseFirst() {
    assertThat(
        Common.isOrdered(new ChainComparator<Boolean>().falseFirst(v -> v).build(), false, true),
        equalTo(true));
  }
}
