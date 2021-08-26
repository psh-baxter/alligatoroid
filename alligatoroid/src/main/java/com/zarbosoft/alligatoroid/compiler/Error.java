package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.luxem.read.path.LuxemPath;
import com.zarbosoft.luxem.write.Writer;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSSet;

public class Error implements Serializable {
  public static final String DESCRIPTION_KEY = "description";
  private final String type;
  private final ROMap<String, Object> data;

  public Error(String type, TSMap<String, Object> data) {
    this.type = type;
    this.data = data;
  }

  public static Error deserializeNotArray(LuxemPath path) {
    return new Error(
        "deserialize_not_array",
        new TSMap<String, Object>()
            .put("path", path.toString())
            .put(DESCRIPTION_KEY, "a luxem array is not allowed at this location in the source"));
  }

  public static Error deserializeNotRecord(LuxemPath path) {
    return new Error(
        "deserialize_not_record",
        new TSMap<String, Object>()
            .put("path", path.toString())
            .put(DESCRIPTION_KEY, "a luxem record is not allowed at this location in the source"));
  }

  public static Error deserializeNotPrimitive(LuxemPath path) {
    return new Error(
        "deserialize_not_primitive",
        new TSMap<String, Object>()
            .put("path", path.toString())
            .put(
                DESCRIPTION_KEY,
                "a luxem primitive is not allowed at this location in the source"));
  }

  public static Error deserializeNotTyped(LuxemPath path) {
    return new Error(
        "deserialize_not_typed",
        new TSMap<String, Object>()
            .put("path", path.toString())
            .put(
                DESCRIPTION_KEY,
                "a typed luxem value is not allowed at this location in the source"));
  }

  public static Error deserializeUnknownType(
      LuxemPath path, String type, TSList<String> knownTypes) {
    return new Error(
        "deserialize_unknown_type",
        new TSMap<String, Object>()
            .put("path", path.toString())
            .put("got", type)
            .put("expected", knownTypes)
            .put(DESCRIPTION_KEY, "this is not a known language node type"));
  }

  public static Error deserializeUnknownField(
      LuxemPath path, String type, String field, ROList<String> fields) {
    return new Error(
        "deserialize_unknown_field",
        new TSMap<String, Object>()
            .put("path", path.toString())
            .put("got", field)
            .put("expected", fields)
            .put("type", type)
            .put(DESCRIPTION_KEY, "this type does not have a field with this name"));
  }

  public static Error deserializeMissingField(LuxemPath path, String type, String field) {
    return new Error(
        "deserialize_missing_field",
        new TSMap<String, Object>()
            .put("path", path.toString())
            .put("field", field)
            .put("type", type)
            .put(DESCRIPTION_KEY, "a value was not provided for this field in the source"));
  }

  public static Error deserializeUnknownLanguageVersion(LuxemPath path, String version) {
    return new Error(
        "deserialize_unknown_language_version",
        new TSMap<String, Object>()
            .put("path", path.toString())
            .put("version", version)
            .put(DESCRIPTION_KEY, "this language version is not supported"));
  }

  public static Error deserializeMissingVersion() {
    return new Error(
        "deserialize_missing_version",
        new TSMap<String, Object>().put(DESCRIPTION_KEY, "the source version is missing"));
  }

  public static Error deserializeNotInteger(LuxemPath path, String value) {
    return new Error(
        "deserialize_not_integer",
        new TSMap<String, Object>()
            .put("path", path.toString())
            .put("got", value)
            .put(DESCRIPTION_KEY, "expected an integer but got a value that is not an integer"));
  }

  public static Error incompatibleTargetValues(
      Location location, String expectedTarget, String gotTarget) {
    return new Error(
        "incompatible_target_values",
        new TSMap<String, Object>()
            .put("location", location)
            .put("got", gotTarget)
            .put("expected", expectedTarget)
            .put(DESCRIPTION_KEY, "this block contains values for incompatible targets"));
  }

  public static Error noField(Location location, Value field) {
    return new Error(
        "no_field",
        new TSMap<String, Object>()
            .put("location", location)
            .put("field", field)
            .put(DESCRIPTION_KEY, "the field accessed does not exist"));
  }

  public static TSMap<String, Object> convertThrowable(Throwable e) {
    TSList<Object> stack = new TSList<>();
    for (StackTraceElement element : e.getStackTrace()) {
      stack.add(
          new TSMap<String, Object>()
              .put("class", element.getClassName())
              .put("method", element.getMethodName())
              .put("line", element.getLineNumber()));
    }
    TSMap<String, Object> out =
        new TSMap<String, Object>().put("exception", e.toString()).put("stacktrace", stack);
    if (e.getCause() != null) out.put("cause", convertThrowable(e.getCause()));
    return out;
  }

  public static Error unexpected(Throwable e) {
    return new Error(
        "unexpected",
        convertThrowable(e)
            .put(DESCRIPTION_KEY, "an unexpected error occurred while processing module"));
  }

  public static Error callNotSupported(Location location) {
    return new Error(
        "call_not_supported",
        new TSMap<String, Object>()
            .put("location", location)
            .put(DESCRIPTION_KEY, "this value cannot be called"));
  }

  public static Error accessNotSupported(Location location) {
    return new Error(
        "access_not_supported",
        new TSMap<String, Object>()
            .put("location", location)
            .put(DESCRIPTION_KEY, "the base value doesn't have fields that can be accessed"));
  }

  public static Error bindNotSupported(Location location) {
    return new Error(
        "bind_not_supported",
        new TSMap<String, Object>()
            .put("location", location)
            .put(DESCRIPTION_KEY, "the base value cannot be bound to a variable"));
  }

  public static Error valueNotWhole(Location location, Value value) {
    return new Error(
        "value_not_known_at_phase_1",
        new TSMap<String, Object>()
            .put("location", location)
            .put("value", value.getClass().getCanonicalName())
            .put(
                DESCRIPTION_KEY, "this value needs to be known completely in phase 1 to use here"));
  }

  public static Error methodsNotDefined(TSSet<String> incompleteMethods) {
    return new Error(
        "methods_not_defined",
        new TSMap<String, Object>()
            .put("methods", TSList.fromSet(incompleteMethods))
            .put(DESCRIPTION_KEY, "these methods were declared but never defined"));
  }

  public static Error notRecordPair(Location location, String gotType) {
    return new Error(
        "record_element_not_record_pair",
        new TSMap<String, Object>()
            .put("location", location)
            .put("got", gotType)
            .put("expected", "record pair")
            .put("description", "this element in a record literal is not a record pair"));
  }

  public static Error lowerTooDeep(Location location) {
    return new Error(
        "lower_too_deep",
        new TSMap<String, Object>()
            .put("location", location)
            .put("got", "no matching containing stage element")
            .put("expected", "at least one more containing element is a stage")
            .put(
                "description",
                "This lower element isn't in a matching stage element. If multiple stage elements are nested, the number of corresponding nested lower elements can't exceed the number of stage elements."));
  }

  @Override
  public void serialize(Writer writer) {
    writer.type(type);
    Serializable.serialize(writer, data);
  }
}
