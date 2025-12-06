package com.google.flatbuffers;

import java.util.List;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface Allocator<ReturnType> {

  int allocate(FlatBufferBuilder builder);

  class UnionAllocator<T> implements Allocator<T> {

    private byte type;
    private Allocator allocator;

    public static UnionAllocator of(byte type, Allocator allocator) {
      return new UnionAllocator(type, allocator);
    }

    public static UnionAllocator ofString(byte type, String str) {
      return new UnionAllocator(type, builder -> builder.createString(str));
    }

    private UnionAllocator(byte type, Allocator allocator) {
      this.type = type;
      this.allocator = allocator;
    }

    @Override
    public int allocate(FlatBufferBuilder builder) {
      return this.allocator.allocate(builder);
    }

    public byte getType() {
      return type;
    }
  }

  @FunctionalInterface
  interface CreateTableVector {
    int create(FlatBufferBuilder builder, int[] arrays);
  }


  @FunctionalInterface
  interface StartStructVector {
    void start(FlatBufferBuilder builder, int pos);
  }

  static <X extends Table> Allocator<List<X>> ofTableVector(CreateTableVector creator, Allocator<X>... values) {
    return builder -> {
      int[] innerOffsets = new int[values.length];
      for (int i = 0; i < values.length; i++) {
        innerOffsets[i] = values[i].allocate(builder);
      }
      return creator.create(builder, innerOffsets);
    };
  }

  static <X extends Struct> Allocator<X[]> ofStructVector(
    StartStructVector start
    , Allocator<X>... values) {
    return (builder) -> {
      start.start(builder, values.length);
      for (int i = 0; i < values.length; i++) {
        values[i].allocate(builder);
      }
      return builder.endVector();
    };
  }

  static <X extends Table> Allocator<List<X>> ofStringVector(CreateTableVector creator, String... values) {
    return builder -> {
      int[] innerOffsets = new int[values.length];
      for (int i = 0; i < values.length; i++) {
        innerOffsets[i] = builder.createString(values[i]);
      }
      return creator.create(builder, innerOffsets);
    };
  }
}
