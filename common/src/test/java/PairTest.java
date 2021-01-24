import com.zarbosoft.rendaw.common.Pair;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class PairTest {
  @Test
  public void testHashEqual() {
    final Pair p1 = new Pair<>(1, 2);
    assertThat(p1.hashCode(), equalTo(p1.hashCode()));
  }

  @Test
  public void testHashFirstDiffers() {
    final Pair p1 = new Pair<>(1, 2);
    final Pair p2 = new Pair<>(2, 2);
    assertThat(p1.hashCode(), not(equalTo(p2.hashCode())));
  }

  @Test
  public void testHashSecondDiffers() {
    final Pair p1 = new Pair<>(1, 2);
    final Pair p2 = new Pair<>(1, 3);
    assertThat(p1.hashCode(), not(equalTo(p2.hashCode())));
  }
}
