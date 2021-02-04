package com.zarbosoft.merman;

import com.zarbosoft.merman.helper.GeneralTestWizard;
import com.zarbosoft.merman.helper.PrimitiveSyntax;
import com.zarbosoft.merman.helper.PrimitiveTestWizard;
import com.zarbosoft.merman.helper.TreeBuilder;
import org.junit.Test;

public class TestPrimitiveBreaking {

  @Test
  public void testHardLines() {
    new PrimitiveTestWizard("amp dog\npear").check("amp dog", "pear");
  }

  @Test
  public void testNoBreakUnbreak() {
    new PrimitiveTestWizard("amp dog")
        .resize(3000)
        .check("amp dog")
        .resize(2000000)
        .check("amp dog");
  }

  @Test
  public void testBreakOne() {
    new PrimitiveTestWizard("amp\npear digitize").resize(100).check("amp", "pear ", "digitize");
  }

  @Test
  public void testBreakTwo() {
    new PrimitiveTestWizard("amp dog laserasticatellage\npear volume")
        .resize(200)
        .check("amp dog ", "laserasticatellage", "pear volume");
  }

  @Test
  public void testRebreakOne() {
    new PrimitiveTestWizard("over three houses rotisserie volume")
        .resize(200)
        .check("over three houses ", "rotisserie volume")
        .resize(120)
        .check("over three ", "houses ", "rotisserie ", "volume");
  }

  @Test
  public void testRebreakTwo() {
    new PrimitiveTestWizard("over three houses timing\n rotisserie volume")
        .resize(200)
        .check("over three houses ", "timing", " rotisserie volume")
        .resize(160)
        .check("over three ", "houses timing", " rotisserie ", "volume");
  }

  @Test
  public void testUnbreakOne() {
    new PrimitiveTestWizard("over three houses rotisserie volume")
        .resize(200)
        .check("over three houses ", "rotisserie volume")
        .resize(300)
        .check("over three houses rotisserie ", "volume");
  }

  @Test
  public void testUnbreakOneFully() {
    new PrimitiveTestWizard("over three houses rotisserie volume")
        .resize(200)
        .check("over three houses ", "rotisserie volume")
        .resize(1000)
        .check("over three houses rotisserie volume");
  }

  @Test
  public void testUnbreakableOne() {
    new PrimitiveTestWizard("123456789").resize(40).check("1234", "5678", "9");
  }

  @Test
  public void testUnbreakableMid() {
    new PrimitiveTestWizard("123456789").resize(43).check("1234", "5678", "9");
  }

  @Test
  public void testUnbreakableRebreak() {
    new PrimitiveTestWizard("123456789")
        .resize(60)
        .check("123456", "789")
        .resize(40)
        .check("1234", "5678", "9");
  }

  @Test
  public void testUnbreakableUnbreak() {
    new PrimitiveTestWizard("123456789")
        .resize(40)
        .check("1234", "5678", "9")
        .resize(50)
        .check("12345", "6789");
  }

  @Test
  public void testUnbreakableUnbreakFull() {
    new PrimitiveTestWizard("123456789")
        .resize(40)
        .check("1234", "5678", "9")
        .resize(10000)
        .check("123456789");
  }

  @Test
  public void testMultipleAtoms() {
    new GeneralTestWizard(
            PrimitiveSyntax.syntax,
             new TreeBuilder(PrimitiveSyntax.primitive).add("value", "oret").build(),
            new TreeBuilder(PrimitiveSyntax.primitive).add("value", "nyibhye").build())
        .checkTextBrick(0, 1, "oret")
        .checkTextBrick(0, 3, "nyibhye")
        .displayWidth(50)
        .checkTextBrick(0, 1, "oret")
        .checkTextBrick(1, 1, "nyibh")
        .checkTextBrick(2, 0, "ye");
  }

  @Test
  public void testMultipleAtomsPriorities() {
    new GeneralTestWizard(
            PrimitiveSyntax.syntax,
             new TreeBuilder(PrimitiveSyntax.array)
                .addArray(
                    "value",
                    new TreeBuilder(PrimitiveSyntax.low).add("value", "oret").build(),
                    new TreeBuilder(PrimitiveSyntax.high).add("value", "nyibhye").build())
                .build())
        .checkTextBrick(0, 1, "oret")
        .checkTextBrick(0, 2, "nyibhye")
        .displayWidth(90)
        .checkTextBrick(0, 1, "oret")
        .checkTextBrick(0, 2, "nyibh")
        .checkTextBrick(1, 0, "ye");
  }

  @Test
  public void testFiniteBreaking() {
    new GeneralTestWizard(
            PrimitiveSyntax.syntax,
             new TreeBuilder(PrimitiveSyntax.quoted).add("value", "123456").build())
        .displayWidth(50)
        .checkCourseCount(2);
  }

  @Test
  public void testFiniteBreakLimit() {
    new GeneralTestWizard(
            PrimitiveSyntax.syntax,
             new TreeBuilder(PrimitiveSyntax.primitive).add("value", "1234").build())
        .displayWidth(30)
        .checkCourseCount(1);
  }
}
