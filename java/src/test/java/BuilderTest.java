import static com.google.common.truth.Truth.assertThat;
import static com.google.flatbuffers.Constants.*;

import com.google.common.io.ByteStreams;
import com.google.flatbuffers.Allocator;
import com.google.flatbuffers.ArrayReadWriteBuf;
import com.google.flatbuffers.ByteBufferUtil;
import com.google.flatbuffers.ByteVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.FlexBuffers;
import com.google.flatbuffers.FlexBuffers.FlexBufferException;
import com.google.flatbuffers.FlexBuffers.KeyVector;
import com.google.flatbuffers.FlexBuffers.Reference;
import com.google.flatbuffers.FlexBuffers.Vector;
import com.google.flatbuffers.FlexBuffersBuilder;
import com.google.flatbuffers.StringVector;
import com.google.flatbuffers.UnionVector;
import com.google.flatbuffers.test.java_builder.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import optional_scalars.OptionalByte;
import optional_scalars.ScalarStuff;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@RunWith(JUnit4.class)
public class BuilderTest {

  @org.junit.Test
  public void builder_with_package_prefix() {
    String uuid = UUID.randomUUID().toString();

    Allocator<HelloRequest>[] tables = new Allocator[]{
      HelloRequest.newV1Builder()
        .name(uuid),
      HelloRequest.newV1Builder()
        .name("uuid")
    };

    Allocator<Vec3>[] vec3Builders = new Allocator[]{
      Vec3.createVec3(1, 2, 3, new float[]{1, 2, 3}),
      Vec3.createVec3(1, 2, 3, new float[]{1, 2, 3}),
      Vec3.createVec3(1, 2, 3, new float[]{1, 2, 3})
    };

    HelloResponse.V1Builder builder = HelloResponse.newV1Builder()
      .singleTable(tables[0])
      .arrayTable(tables)
      .singleStruct(vec3Builders[0])
      .arrayStruct(vec3Builders)
      .singleInt(114514)
      .arrayInt(1, 2, 3, 4)
      .singleString("a")
      .arrayString("a", "b")
      .singleUnion(Test_Union.V1Builder.name("acx"))
      .arrayUnion(Test_Union.V1Builder.name("name"), Test_Union.V1Builder.name1("name1"));

    HelloResponse response = builder.asObject();

    ByteBuffer buffer = builder.toBuffer();

    HelloResponse fromBuffer = HelloResponse.getRootAsHelloResponse(buffer);

    assertThat(response.singleTable().name()).isEqualTo(uuid);
    assertThat(response.singleTable().name()).isEqualTo(uuid);
    assertThat(fromBuffer.singleTable().name()).isEqualTo(uuid);
    assertThat(response.arrayTableLength()).isEqualTo(tables.length);
    assertThat(fromBuffer.arrayTableLength()).isEqualTo(tables.length);
    assertThat(response.arrayTable(0).name()).isEqualTo(uuid);
    assertThat(fromBuffer.arrayTable(0).name()).isEqualTo(uuid);
    assertThat(response.singleInt()).isEqualTo(114514);
    assertThat(fromBuffer.singleInt()).isEqualTo(114514);
    assertThat(response.arrayStructLength()).isEqualTo(3);
    assertThat(fromBuffer.arrayStructLength()).isEqualTo(3);

//      assertThat(response.vIntS(3)).isEqualTo(4);
//      assertThat(fromBuffer.vIntS(3)).isEqualTo(4);

    assertThat(response.arrayIntLength()).isEqualTo(4);
    assertThat(fromBuffer.arrayIntLength()).isEqualTo(4);

    assertThat(response.singleUnionType()).isEqualTo(Test_Union.name);
    assertThat(fromBuffer.singleUnionType()).isEqualTo(Test_Union.name);

    assertThat(response.arrayUnionLength()).isEqualTo(2);
    assertThat(fromBuffer.arrayUnionLength()).isEqualTo(2);

  }


  @org.junit.Test
  public void builder_without_package_prefix() {
    String uuid = UUID.randomUUID().toString();

    Allocator<test.java_builder.HelloRequest>[] tables = new Allocator[]{
      test.java_builder.HelloRequest.newV1Builder()
        .name(uuid),
      test.java_builder.HelloRequest.newV1Builder()
        .name("uuid")
    };

    Allocator<test.java_builder.Vec3>[] vec3Builders = new Allocator[]{
      test.java_builder.Vec3.createVec3(1, 2, 3, new float[]{1, 2, 3}),
      test.java_builder.Vec3.createVec3(1, 2, 3, new float[]{1, 2, 3}),
      test.java_builder.Vec3.createVec3(1, 2, 3, new float[]{1, 2, 3})
    };

    test.java_builder.HelloResponse.V1Builder builder = test.java_builder.HelloResponse.newV1Builder()
      .singleTable(tables[0])
      .arrayTable(tables)
      .singleStruct(vec3Builders[0])
      .arrayStruct(vec3Builders)
      .singleInt(114514)
      .arrayInt(1, 2, 3, 4)
      .singleString("a")
      .arrayString("a", "b");

    test.java_builder.HelloResponse response = builder.asObject();

    ByteBuffer buffer = builder.toBuffer();

    test.java_builder.HelloResponse fromBuffer = test.java_builder.HelloResponse.getRootAsHelloResponse(buffer);

    assertThat(response.singleTable().name()).isEqualTo(uuid);
    assertThat(response.singleTable().name()).isEqualTo(uuid);
    assertThat(fromBuffer.singleTable().name()).isEqualTo(uuid);
    assertThat(response.arrayTableLength()).isEqualTo(tables.length);
    assertThat(fromBuffer.arrayTableLength()).isEqualTo(tables.length);
    assertThat(response.arrayTable(0).name()).isEqualTo(uuid);
    assertThat(fromBuffer.arrayTable(0).name()).isEqualTo(uuid);
    assertThat(response.singleInt()).isEqualTo(114514);
    assertThat(fromBuffer.singleInt()).isEqualTo(114514);
    assertThat(response.arrayStructLength()).isEqualTo(3);
    assertThat(fromBuffer.arrayStructLength()).isEqualTo(3);

//      assertThat(response.vIntS(3)).isEqualTo(4);
//      assertThat(fromBuffer.vIntS(3)).isEqualTo(4);

    assertThat(response.arrayIntLength()).isEqualTo(4);
    assertThat(fromBuffer.arrayIntLength()).isEqualTo(4);

  }
}
