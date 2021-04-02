package com.zarbosoft.merman.core.editor.serialization;

import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.document.values.Field;
import com.zarbosoft.merman.core.document.values.FieldArray;
import com.zarbosoft.merman.core.document.values.FieldAtom;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

import java.util.Map;

public interface Serializer {
  /**
   * Does initialSet on a tree that's just been created/initialized.  This can't be done during ser
   * @param atom
   * @return
   */
  static Atom initialSet(ROPair<Atom, ROMap<String, ROPair<Field, Object>>> atom) {
    Atom out = atom.first;
    TSMap<String, Field> fields = new TSMap<>();
    for (Map.Entry<String, ROPair<Field, Object>> field : atom.second) {
      if (field.getValue().first instanceof FieldArray) {
        TSList<Atom> fieldData = new TSList<>();
        for (ROPair<Atom, ROMap<String, ROPair<Field, Object>>> element :
            ((ROList<ROPair<Atom, ROMap<String, ROPair<Field, Object>>>>) field.getValue().second)) {
          fieldData.add(initialSet(element));
        }
        ((FieldArray) field.getValue().first).initialSet(fieldData);
        fields.put(field.getKey(), field.getValue().first);
      } else if (field.getValue().first instanceof FieldAtom) {
        ((FieldAtom) field.getValue().first)
            .initialSet(initialSet((ROPair<Atom, ROMap<String, ROPair<Field, Object>>>) field.getValue().second));
        fields.put(field.getKey(), field.getValue().first);
      } else {
        fields.put(field.getKey(), field.getValue().first);
      }
    }
    out.initialSet(fields);
    return out;
  }

  /**
   * byte[] in java, string in js
   *
   * @param atom
   * @return
   */
  Object write(Atom atom);

  /**
   * byte[] in java, string in js
   *
   * @param atom
   * @return
   */
  Object write(ROList<Atom> atom);

  /**
   * Per clipboard, data is bytes or string depending on execution environment (js vs java)
   *
   * @param syntax
   * @param type
   * @param data
   * @return
   */
  ROList<Atom> loadFromClipboard(Syntax syntax, String type, Object data);
}
