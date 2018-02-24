package test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mail.api.serial.DataStructure;
import mail.api.serial.SerializationHandler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class JSONSerializationHandlerImpl implements SerializationHandler {

    private final Gson gson = new Gson();

    @Override
    public DataStructure read(InputStream inputStream) {
        InputStreamReader reader = new InputStreamReader(inputStream);
        JsonObject json = gson.fromJson(reader, JsonObject.class);
        return new JSONDataStructure(json);
    }

    private static final class JSONDataStructure implements DataStructure {

        private static final JSONDataStructure MISSING = new JSONDataStructure(null);

        private final JsonObject object;

        private JSONDataStructure(JsonObject object) {
            this.object = object;
        }

        @Override
        public boolean isPresent() {
            return this != MISSING;
        }

        @Override
        public void ifPresent(Consumer<DataStructure> consumer) {
            if (this == MISSING) return;

            consumer.accept(this);
        }

        @Override
        public DataStructure getChild(String name) {
            if (this == MISSING) return MISSING;
            if (!object.has(name)) return MISSING;

            JsonElement element = object.get(name);
            return element.isJsonObject() ? new JSONDataStructure(element.getAsJsonObject()) : MISSING;
        }

        @Override
        public List<DataStructure> getChildren(String name) {
            if (this == MISSING) return Collections.emptyList();
            if (!object.has(name)) return Collections.emptyList();

            JsonElement element = object.get(name);
            if (!element.isJsonArray()) return Collections.emptyList();

            List<DataStructure> list = new ArrayList<>();
            for (JsonElement child : element.getAsJsonArray()) {
                if (child.isJsonObject()) {
                    list.add(new JSONDataStructure(child.getAsJsonObject()));
                }
            }
            return list;
        }

        @Override
        public DataStructureStream streamChildren(String name) {
            return null;
        }

        @Override
        public DataElement get(String name) {
            if (this == MISSING) return JSONDataElement.MISSING;
            if (!object.has(name)) return JSONDataElement.MISSING;

            JsonElement element = object.get(name);
            if (!element.isJsonPrimitive()) return JSONDataElement.MISSING;
            return new JSONDataElement(element);
        }

        @Override
        public List<DataElement> getAll(String name) {
            if (this == MISSING) return Collections.emptyList();
            if (!object.has(name)) return Collections.emptyList();

            JsonElement element = object.get(name);
            if (!element.isJsonArray()) return Collections.emptyList();

            List<DataElement> list = new ArrayList<>();
            for (JsonElement child : element.getAsJsonArray()) {
                if (child.isJsonPrimitive()) {
                    list.add(new JSONDataElement(child));
                }
            }
            return list;
        }

        @Override
        public <T> T get(String name, Class<T> type) throws NullPointerException {
            return get(name).as(type);
        }

        @Override
        public <T> T orElse(String name, Class<T> type, T other) {
            return get(name).orElse(type, other);
        }

        @Override
        public <T> T orElseGet(String name, Class<T> type, Supplier<T> supplier) {
            return get(name).orElseGet(type, supplier);
        }

        @Override
        public <T, E extends Throwable> T orElseThrow(String name, Class<T> type, Supplier<E> supplier) throws E {
            return get(name).orElseThrow(type, supplier);
        }

        @Override
        public <T> List<T> getAll(String name, Class<T> type) {
            return null;
        }

    }

    private static final class JSONDataElement implements DataStructure.DataElement {

        private static final JSONDataElement MISSING = new JSONDataElement(null);

        private final JsonElement element;

        private JSONDataElement(JsonElement element) {
            this.element = element;
        }

        @Override
        public boolean isPresent() {
            return this != MISSING;
        }

        @Override
        public void ifPresent(Consumer<DataStructure.DataElement> consumer) {
            if (this == MISSING) return;
            consumer.accept(this);
        }

        @Override
        public <T> Optional<T> map(Function<DataStructure.DataElement, T> mapper) {
            if (this == MISSING) return Optional.empty();
            return Optional.ofNullable(mapper.apply(this));
        }

        @Override
        public <T> Optional<T> flatMap(Function<DataStructure.DataElement, Optional<T>> mapper) {
            if (this == MISSING) return Optional.empty();
            return mapper.apply(this);
        }

        @Override
        public <T> Optional<T> map(Class<T> type) {
            if (this == MISSING) return Optional.empty();
            return Optional.ofNullable(orElse(type, null));
        }

        @Override
        public <T> T as(Class<T> type) throws NullPointerException {
            T obj = orElse(type, null);
            if (obj == null) throw new NullPointerException();
            return obj;
        }

        @Override
        public <T> T orElse(Class<T> type, T other) {
            if (this == MISSING) return other;
            if (type == int.class || type == Integer.class) return (T) (Integer) element.getAsInt();
            if (type == short.class || type == Short.class) return (T) (Short) element.getAsShort();
            if (type == long.class || type == Long.class) return (T) (Long) element.getAsLong();
            if (type == byte.class || type == Byte.class) return (T) (Byte) element.getAsByte();
            if (type == char.class || type == Character.class) return (T) (Character) element.getAsCharacter();
            if (type == float.class || type == Float.class) return (T) (Float) element.getAsFloat();
            if (type == double.class || type == Double.class) return (T) (Double) element.getAsDouble();
            if (type == boolean.class || type == Boolean.class) return (T) (Boolean) element.getAsBoolean();
            if (type == String.class) return (T) element.getAsString();
            return other;
        }

        @Override
        public <T> T orElseGet(Class<T> type, Supplier<T> supplier) {
            T obj = orElse(type, null);
            if (obj == null) return supplier.get();
            return obj;
        }

        @Override
        public <T, E extends Throwable> T orElseThrow(Class<T> type, Supplier<E> supplier) throws E {
            T obj = orElse(type, null);
            if (obj == null) throw supplier.get();
            return obj;
        }
    }

}
