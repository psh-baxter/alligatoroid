package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.display.Display;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.Document;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.serialization.Serializer;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.back.BackAtomSpec;
import com.zarbosoft.merman.core.syntax.back.BackSpecData;
import com.zarbosoft.merman.core.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.style.ObboxStyle;
import com.zarbosoft.merman.core.syntax.style.Padding;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.syntax.symbol.Symbol;
import com.zarbosoft.merman.core.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldAtom;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldAtomBase;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldAtomFromArray;
import com.zarbosoft.merman.editorcore.banner.Banner;
import com.zarbosoft.merman.editorcore.displayderived.BeddingContainer;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.merman.editorcore.history.changes.ChangeAtom;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROOrderedSetRef;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Map;
import java.util.function.Function;

public class Editor {
  public final Context context;
  public final History history;
  public final ObboxStyle choiceCursorStyle;
  public final Padding choicePreviewPadding;
  public final Style choiceDescriptionStyle;
  public final Symbol gapPlaceholderSymbol;
  public final double detailSpan;
  public final Padding bannerPad;
  public final ObboxStyle detailStyle;
  public final Padding detailPad;
  public final double choiceRowStride;
  public final Padding choiceRowPadding;
  public final double choiceColumnSpace;
  public Banner banner;
  public BeddingContainer details;

  public Editor(
      final Syntax syntax,
      final Document doc,
      final Display display,
      Environment environment,
      final History history,
      Serializer serializer,
      Function<Editor, EditorCursorFactory> cursorFactory,
      Config config) {
    context =
        new EditorContext(
            config.context,
            syntax,
            doc,
            display,
            environment,
            serializer,
            cursorFactory.apply(this),
            this);
    this.history = history;
    this.choiceCursorStyle =
        config.choiceCursorStyle == null
            ? new ObboxStyle(new ObboxStyle.Config())
            : config.choiceCursorStyle;
    this.choiceDescriptionStyle =
        config.choiceDescriptionStyle == null
            ? new Style(new Style.Config())
            : config.choiceDescriptionStyle;
    this.choicePreviewPadding = config.choicePreviewPadding;

    choiceRowPadding = config.choiceRowPadding;
    choiceRowStride = config.choiceRowStride;
    choiceColumnSpace = config.choiceColumnSpace;
    this.gapPlaceholderSymbol =
        config.gapPlaceholderSymbol == null
            ? new SymbolTextSpec(new SymbolTextSpec.Config("▢"))
            : config.gapPlaceholderSymbol;
    this.banner =
        new Banner(
            this.context,
            config.bannerStyle == null ? new Style(new Style.Config()) : config.bannerStyle);
    this.bannerPad = config.bannerPad;
    this.details = new BeddingContainer(this.context, false);
    this.detailPad = config.detailPad;
    this.detailStyle =
        config.detailsStyle == null ? new ObboxStyle(new ObboxStyle.Config()) : config.detailsStyle;
    this.detailSpan = config.detailSpan;
  }

  public static Editor get(Context context) {
    return ((EditorContext) context).editor;
  }

  public static void atomSet(
      Context context, History.Recorder recorder, VisualFieldAtomBase base, Atom value) {
    base.dispatch(
        new VisualFieldAtomBase.VisualNestedDispatcher() {
          @Override
          public void handle(VisualFieldAtomFromArray visual) {
            recorder.apply(
                context,
                new ChangeArray(((VisualFieldAtomFromArray) base).value, 0, 1, TSList.of(value)));
          }

          @Override
          public void handle(VisualFieldAtom visual) {
            recorder.apply(context, new ChangeAtom(((VisualFieldAtom) base).value, value));
          }
        });
  }

  public static Atom createEmptyGap(AtomType gapType) {
    Atom out = new Atom(gapType);
    TSMap<String, Field> fields = new TSMap<>();
    for (Map.Entry<String, BackSpecData> field : gapType.fields) {
      fields.put(field.getKey(), createEndEmptyField(field.getValue()));
    }
    out.initialSet(fields);
    return out;
  }

  public static Atom createEmptyAtom(Syntax syntax, AtomType atomType) {
    return createEmptyAtom(syntax, atomType, 0);
  }

  public static Atom createEmptyAtom(Syntax syntax, AtomType atomType, int depth) {
    Atom out = new Atom(atomType);
    TSMap<String, Field> fields = new TSMap<>();
    for (Map.Entry<String, BackSpecData> field : atomType.fields) {
      fields.put(field.getKey(), createEmptyField(syntax, field.getValue(), depth));
    }
    out.initialSet(fields);
    return out;
  }

  /**
   * Non-recursing field types only
   *
   * @param backSpecData
   * @return
   */
  public static Field createEndEmptyField(BackSpecData backSpecData) {
    if (backSpecData instanceof BaseBackArraySpec) {
      return new FieldArray((BaseBackArraySpec) backSpecData);
    } else if (backSpecData instanceof BaseBackPrimitiveSpec) {
      return new FieldPrimitive((BaseBackPrimitiveSpec) backSpecData, "");
    } else throw new Assertion();
  }

  public static Field createEmptyField(Syntax syntax, BackSpecData backSpecData, int depth) {
    if (backSpecData instanceof BackAtomSpec) {
      FieldAtom field = new FieldAtom((BaseBackAtomSpec) backSpecData);
      ROOrderedSetRef<AtomType> candidates = syntax.splayedTypes.get(field.back().type);
      if (depth < 10 && candidates.size() == 1) {
        field.initialSet(createEmptyAtom(syntax, candidates.iterator().next(), depth + 1));
      } else {
        field.initialSet(createEmptyGap(syntax.gap));
      }
      return field;
    } else return createEndEmptyField(backSpecData);
  }

  public Atom arrayInsertNewDefault(History.Recorder recorder, FieldArray value, int index) {
    final ROOrderedSetRef<AtomType> childTypes =
        this.context.syntax.splayedTypes.get(value.back().elementAtomType());
    final Atom element;
    if (childTypes.size() == 1)
      element = createEmptyAtom(this.context.syntax, childTypes.iterator().next());
    else element = createEmptyGap(this.context.syntax.gap);
    recorder.apply(this.context, new ChangeArray(value, index, 0, TSList.of(element)));
    return element;
  }

  public void destroy() {
    context.wall.clear(context);
  }

  public void redo(Context context) {
    history.redo(context);
  }

  public void undo(Context context) {
    history.undo(context);
  }

  public static class EditorContext extends Context {
    public final Editor editor;

    public EditorContext(
        InitialConfig config,
        Syntax syntax,
        Document document,
        Display display,
        Environment env,
        Serializer serializer,
        com.zarbosoft.merman.core.CursorFactory cursorFactory,
        Editor editor) {
      super(config, syntax, document, display, env, serializer, cursorFactory);
      this.editor = editor;
    }
  }

  public static class Config {
    public final Context.InitialConfig context;
    public double choiceRowStride;
    public Padding choiceRowPadding = Padding.empty;
    public double choiceColumnSpace;
    public Style choiceDescriptionStyle;
    public Padding choicePreviewPadding = Padding.empty;
    public Symbol gapPlaceholderSymbol;
    public ObboxStyle choiceCursorStyle;
    public Style bannerStyle;
    public ObboxStyle detailsStyle;
    public double detailSpan = 300;
    public Padding bannerPad = Padding.empty;
    public Padding detailPad = Padding.empty;

    public Config(Context.InitialConfig context) {
      this.context = context;
    }

    public Config bannerStyle(Style style) {
      this.bannerStyle = style;
      return this;
    }

    public Config bannerPad(Padding padding) {
      this.bannerPad = padding;
      return this;
    }

    public Config choiceDescriptionStyle(Style style) {
      this.choiceDescriptionStyle = style;
      return this;
    }

    public Config choicePreviewPadding(Padding style) {
      this.choicePreviewPadding = style;
      return this;
    }

    public Config choiceRowStride(double span) {
      this.choiceRowStride = span;
      return this;
    }

    public Config choiceRowPadding(Padding padding) {
      this.choiceRowPadding = padding;
      return this;
    }

    public Config choiceColumnSpace(double span) {
      this.choiceColumnSpace = span;
      return this;
    }

    public Config detailsBoxStyle(ObboxStyle style) {
      this.detailsStyle = style;
      return this;
    }

    public Config detailsPad(Padding padding) {
      this.detailPad = padding;
      return this;
    }

    public Config detailsMaxTransverseSpan(double value) {
      this.detailSpan = value;
      return this;
    }

    public Config gapPlaceholderSymbol(Symbol symbol) {
      this.gapPlaceholderSymbol = symbol;
      return this;
    }

    public Config choiceCursorStyle(ObboxStyle style) {
      this.choiceCursorStyle = style;
      return this;
    }
  }
}
