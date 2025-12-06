package com.google.flatbuffers;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public interface TableBuilder<T extends Table> extends Allocator<T> {
    class FieldBuilder {
        public final BiConsumer<FlatBufferBuilder, Integer> mapping;
        public final Allocator allocate;

        int idx = -1;

        public FieldBuilder(BiConsumer<FlatBufferBuilder, Integer> putIn, Allocator regis) {
            mapping = putIn;
            allocate = regis;
        }

        public void allocate(FlatBufferBuilder ctx) {
            if (Objects.nonNull(allocate)) {
                this.idx = this.allocate.allocate(ctx);
            }
        }

        public void fulfilled(FlatBufferBuilder ctx) {
            if (Objects.nonNull(this.mapping)) {
                this.mapping.accept(ctx, this.idx);
            }

        }
    }


    default ByteBuffer toBuffer() {
        return toBuffer(null);
    }

    default ByteBuffer toBuffer(FlatBufferBuilder builder) {
        if (Objects.isNull(builder)) {
            builder = new FlatBufferBuilder();
        }
        builder.finish(allocate(builder));
        return builder.dataBuffer();
    }

    T asObject(T obj);


    default T asObject() {
        return asObject(null);
    }


    abstract class AbstractTableBuilder<T extends Table, SELF extends AbstractTableBuilder<T, SELF>> implements TableBuilder<T> {
        private final Map<String, FieldBuilder> fieldOpts = new HashMap<>();

        protected SELF field(String key,
                             Allocator allocate,
                             BiConsumer<FlatBufferBuilder, Integer> mapping
        ) {
            this.fieldOpts.put(key, new FieldBuilder(mapping, allocate));
            return (SELF) this;
        }

        protected void allocates(FlatBufferBuilder builder) {
            for (Map.Entry<String, FieldBuilder> entry : fieldOpts.entrySet()) {
                entry.getValue().allocate(builder);
            }
        }

        protected void compose(FlatBufferBuilder builder) {
            for (Map.Entry<String, FieldBuilder> entry : fieldOpts.entrySet()) {
                entry.getValue().fulfilled(builder);
            }
        }
    }
}