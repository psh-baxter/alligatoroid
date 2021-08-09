package com.zarbosoft.merman.editorcore;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.display.Display;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.Document;
import com.zarbosoft.merman.core.document.fields.Field;
import com.zarbosoft.merman.core.document.fields.FieldArray;
import com.zarbosoft.merman.core.document.fields.FieldAtom;
import com.zarbosoft.merman.core.document.fields.FieldId;
import com.zarbosoft.merman.core.document.fields.FieldPrimitive;
import com.zarbosoft.merman.core.serialization.Serializer;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.back.BackAtomSpec;
import com.zarbosoft.merman.core.syntax.back.BackIdSpec;
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
import com.zarbosoft.merman.editorcore.history.FileIds;
import com.zarbosoft.merman.editorcore.history.History;
import com.zarbosoft.merman.editorcore.history.changes.ChangeArray;
import com.zarbosoft.merman.editorcore.history.changes.ChangeAtom;
import com.zarbosoft.merman.editorcore.history.changes.ChangeId;
import com.zarbosoft.rendaw.common.Assertion;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROOrderedSetRef;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.ROSetRef;
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
  public final ROSetRef<String> suffixOnPatternMismatch;
  public final FileIds fileIds;
  public Banner banner;
  public BeddingContainer details;

  public Editor(
      final Syntax syntax,
      final FileIds fileIds,
      final Document doc,
      final Display display,
      Environment environment,
      final History history,
      Serializer serializer,
      Function<Editor, EditorCursorFactory> cursorFactory,
      Config config) {
    this.fileIds = fileIds;
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
    suffixOnPatternMismatch = config.suffixOnPatternMismatch;
    this.bannerPad = config.bannerPad;
    this.details = new BeddingContainer(this.context, false);
    this.detailPad = config.detailPad;
    this.detailStyle =
        config.detailsStyle == null ? new ObboxStyle(new ObboxStyle.Config()) : config.detailsStyle;
    this.detailSpan = config.detailSpan;
    history.record(
        this,
        null,
        recorder -> {
          makeIdsUnique(this, recorder, doc.root);
        });
    history.clear();
  }

  public static Editor get(Context context) {
    return ((EditorContext) context).editor;
  }

  public static void replaceInParent(
      Editor editor, History.Recorder recorder, Atom child, Atom replacement) {
    child.fieldParentRef.dispatch(
        new Field.ParentDispatcher() {
          @Override
          public void handle(FieldArray.Parent parent) {
            arrayChange(editor, recorder, parent.field, parent.index, 1, new TSList<>(replacement));
          }

          @Override
          public void handle(FieldAtom.Parent parent) {
            atomSet(editor, recorder, parent.field, replacement);
          }
        });
  }

  public static void atomSet(
      Editor editor, History.Recorder recorder, FieldAtom field, Atom value) {
    if (field.data != null) removeIds(editor, field.data);
    makeIdsUnique(editor, recorder, value);
    recorder.apply(editor, new ChangeAtom(field, value));
  }

  private static void removeIds(Editor editor, Atom atom) {
    for (Map.Entry<String, Field> entry : atom.namedFields) {
      Field field = entry.getValue();
      if (field instanceof FieldId) {
        editor.fileIds.remove(((FieldId) field).id);
      } else if (field instanceof FieldAtom) {
        removeIds(editor, ((FieldAtom) field).data);
      } else if (field instanceof FieldArray) {
        for (Atom child : ((FieldArray) field).data) {
          removeIds(editor, child);
        }
      } else if (field instanceof FieldPrimitive) {
        // nop
      } else throw new Assertion();
    }
  }

  public static void arrayChange(
      Editor editor,
      History.Recorder recorder,
      FieldArray array,
      int index,
      int remove,
      ROList<Atom> add) {
    for (Atom atom : array.data) {
      removeIds(editor, atom);
    }
    for (Atom atom : add) {
      makeIdsUnique(editor, recorder, atom);
    }
    recorder.apply(editor, new ChangeArray(array, index, remove, add));
  }

  private static void makeIdsUniqueInner(Editor editor, History.Recorder recorder, Field field) {
    if (field instanceof FieldId) {
      Integer uniqueId = editor.fileIds.take(((FieldId) field).id);
      if (uniqueId != null) recorder.apply(editor, new ChangeId((FieldId) field, uniqueId));
    } else if (field instanceof FieldAtom) {
      makeIdsUnique(editor, recorder, ((FieldAtom) field).data);
    } else if (field instanceof FieldArray) {
      for (Atom child : ((FieldArray) field).data) {
        makeIdsUnique(editor, recorder, child);
      }
    } else if (field instanceof FieldPrimitive) {
      // nop
    } else throw new Assertion();
  }

  public static void makeIdsUnique(Editor editor, History.Recorder recorder, Atom atom) {
    for (Field field : atom.unnamedFields) {
      makeIdsUniqueInner(editor, recorder, field);
    }
    for (Map.Entry<String, Field> entry : atom.namedFields) {
      Field field = entry.getValue();
      makeIdsUniqueInner(editor, recorder, field);
    }
  }

  public static void visualAtomSet(
      Editor editor, History.Recorder recorder, VisualFieldAtomBase base, Atom value) {
    base.dispatch(
        new VisualFieldAtomBase.VisualNestedDispatcher() {
          @Override
          public void handle(VisualFieldAtomFromArray visual) {
            arrayChange(
                editor, recorder, ((VisualFieldAtomFromArray) base).value, 0, 1, TSList.of(value));
          }

          @Override
          public void handle(VisualFieldAtom visual) {
            atomSet(editor, recorder, ((VisualFieldAtom) base).value, value);
          }
        });
  }

  public static Atom createEmptyGap(FileIds fileIds, AtomType gapType) {
    Atom out = new Atom(gapType);
    TSList<Field> unnamedFields = new TSList<>();
    for (BackSpecData field : gapType.unnamedFields) {
      unnamedFields.add(createEndEmptyField(fileIds, field));
    }
    TSMap<String, Field> namedFields = new TSMap<>();
    for (Map.Entry<String, BackSpecData> field : gapType.namedFields) {
      namedFields.put(field.getKey(), createEndEmptyField(fileIds, field.getValue()));
    }
    out.initialSet(unnamedFields, namedFields);
    return out;
  }

  public static Atom createEmptyAtom(Syntax syntax, FileIds fileIds, AtomType atomType) {
    return createEmptyAtom(syntax, fileIds, atomType, 0);
  }

  public static Atom createEmptyAtom(Syntax syntax, FileIds fileIds, AtomType atomType, int depth) {
    Atom out = new Atom(atomType);
    TSList<Field> unnamedFields = new TSList<>();
    for (BackSpecData field : atomType.unnamedFields) {
      unnamedFields.add(createEndEmptyField(fileIds, field));
    }
    TSMap<String, Field> namedFields = new TSMap<>();
    for (Map.Entry<String, BackSpecData> field : atomType.namedFields) {
      namedFields.put(field.getKey(), createEmptyField(syntax, fileIds, field.getValue(), depth));
    }
    out.initialSet(unnamedFields, namedFields);
    return out;
  }

  /**
   * Non-recursing field types only
   *
   * @param backSpecData
   * @return
   */
  public static Field createEndEmptyField(FileIds fileIds, BackSpecData backSpecData) {
    if (backSpecData instanceof BaseBackArraySpec) {
      return new FieldArray((BaseBackArraySpec) backSpecData);
    } else if (backSpecData instanceof BaseBackPrimitiveSpec) {
      return new FieldPrimitive((BaseBackPrimitiveSpec) backSpecData, "");
    } else if (backSpecData instanceof BackIdSpec) {
      return new FieldId((BackIdSpec) backSpecData, fileIds.take(null));
    } else throw new Assertion();
  }

  public static Field createEmptyField(
      Syntax syntax, FileIds fileIds, BackSpecData backSpecData, int depth) {
    if (backSpecData instanceof BackAtomSpec) {
      FieldAtom field = new FieldAtom((BaseBackAtomSpec) backSpecData);
      ROOrderedSetRef<AtomType> candidates = syntax.splayedTypes.get(field.back().type);
      if (depth < 10 && candidates.size() == 1) {
        field.initialSet(createEmptyAtom(syntax, fileIds, candidates.iterator().next(), depth + 1));
      } else {
        field.initialSet(createEmptyGap(fileIds, syntax.gap));
      }
      return field;
    } else return createEndEmptyField(fileIds, backSpecData);
  }

  public Field createEmptyField(BackSpecData backSpecData) {
    return Editor.createEmptyField(context.syntax, fileIds, backSpecData, 0);
  }

  public Atom createEmptyGap(AtomType gapType) {
    return createEmptyGap(fileIds, gapType);
  }

  public Atom arrayInsertNewDefault(History.Recorder recorder, FieldArray value, int index) {
    final ROOrderedSetRef<AtomType> childTypes =
        this.context.syntax.splayedTypes.get(value.back().elementAtomType());
    final Atom element;
    if (childTypes.size() == 1)
      element = createEmptyAtom(this.context.syntax, fileIds, childTypes.iterator().next());
    else element = createEmptyGap(fileIds, this.context.syntax.gap);
    recorder.apply(this, new ChangeArray(value, index, 0, TSList.of(element)));
    return element;
  }

  public void destroy() {
    context.wall.clear(context);
  }

  public void redo() {
    history.redo(this);
  }

  public void undo() {
    history.undo(this);
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
    public ROSetRef<String> suffixOnPatternMismatch = ROSet.empty;

    public Config(Context.InitialConfig context) {
      this.context = context;
    }

    public Config suffixOnPatternMismatch(ROSetRef<String> set) {
      this.suffixOnPatternMismatch = set;
      return this;
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
