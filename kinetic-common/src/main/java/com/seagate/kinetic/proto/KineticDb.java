/**
 * Copyright 2013-2015 Seagate Technology LLC.
 *
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at
 * https://mozilla.org/MP:/2.0/.
 * 
 * This program is distributed in the hope that it will be useful,
 * but is provided AS-IS, WITHOUT ANY WARRANTY; including without 
 * the implied warranty of MERCHANTABILITY, NON-INFRINGEMENT or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the Mozilla Public 
 * License for more details.
 *
 * See www.openkinetic.org for more project information
 */

package com.seagate.kinetic.proto;

public final class KineticDb {
  private KineticDb() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface VersionedOrBuilder
      extends com.google.protobuf.MessageOrBuilder {

    // optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;
    /**
     * <code>optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;</code>
     *
     * <pre>
     *metadata
     * </pre>
     */
    boolean hasMetadata();
    /**
     * <code>optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;</code>
     *
     * <pre>
     *metadata
     * </pre>
     */
    com.seagate.kinetic.proto.KineticDb.Versioned.Metadata getMetadata();
    /**
     * <code>optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;</code>
     *
     * <pre>
     *metadata
     * </pre>
     */
    com.seagate.kinetic.proto.KineticDb.Versioned.MetadataOrBuilder getMetadataOrBuilder();

    // optional bytes value = 2;
    /**
     * <code>optional bytes value = 2;</code>
     *
     * <pre>
     *entry value/data
     * </pre>
     */
    boolean hasValue();
    /**
     * <code>optional bytes value = 2;</code>
     *
     * <pre>
     *entry value/data
     * </pre>
     */
    com.google.protobuf.ByteString getValue();
  }
  /**
   * Protobuf type {@code com.seagate.kinetic.proto.Versioned}
   *
   * <pre>
   **
   * persisted entry value message format.
   * &lt;p&gt;
   * db persisted entry (KVValue)
   * </pre>
   */
  public static final class Versioned extends
      com.google.protobuf.GeneratedMessage
      implements VersionedOrBuilder {
    // Use Versioned.newBuilder() to construct.
    private Versioned(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private Versioned(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final Versioned defaultInstance;
    public static Versioned getDefaultInstance() {
      return defaultInstance;
    }

    public Versioned getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private Versioned(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.Builder subBuilder = null;
              if (((bitField0_ & 0x00000001) == 0x00000001)) {
                subBuilder = metadata_.toBuilder();
              }
              metadata_ = input.readMessage(com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.PARSER, extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(metadata_);
                metadata_ = subBuilder.buildPartial();
              }
              bitField0_ |= 0x00000001;
              break;
            }
            case 18: {
              bitField0_ |= 0x00000002;
              value_ = input.readBytes();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.seagate.kinetic.proto.KineticDb.internal_static_com_seagate_kinetic_proto_Versioned_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.seagate.kinetic.proto.KineticDb.internal_static_com_seagate_kinetic_proto_Versioned_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.seagate.kinetic.proto.KineticDb.Versioned.class, com.seagate.kinetic.proto.KineticDb.Versioned.Builder.class);
    }

    public static com.google.protobuf.Parser<Versioned> PARSER =
        new com.google.protobuf.AbstractParser<Versioned>() {
      public Versioned parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new Versioned(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<Versioned> getParserForType() {
      return PARSER;
    }

    public interface MetadataOrBuilder
        extends com.google.protobuf.MessageOrBuilder {

      // optional bytes key = 1;
      /**
       * <code>optional bytes key = 1;</code>
       *
       * <pre>
       *entry key
       * </pre>
       */
      boolean hasKey();
      /**
       * <code>optional bytes key = 1;</code>
       *
       * <pre>
       *entry key
       * </pre>
       */
      com.google.protobuf.ByteString getKey();

      // optional bytes dbVersion = 2;
      /**
       * <code>optional bytes dbVersion = 2;</code>
       *
       * <pre>
       *entry version in store
       * </pre>
       */
      boolean hasDbVersion();
      /**
       * <code>optional bytes dbVersion = 2;</code>
       *
       * <pre>
       *entry version in store
       * </pre>
       */
      com.google.protobuf.ByteString getDbVersion();

      // optional bytes tag = 3;
      /**
       * <code>optional bytes tag = 3;</code>
       *
       * <pre>
       * this is the integrity value of the data. This may or may not be in the clear, depending on the algorithm
       * used.
       * </pre>
       */
      boolean hasTag();
      /**
       * <code>optional bytes tag = 3;</code>
       *
       * <pre>
       * this is the integrity value of the data. This may or may not be in the clear, depending on the algorithm
       * used.
       * </pre>
       */
      com.google.protobuf.ByteString getTag();

      // optional .com.seagate.kinetic.proto.Command.Algorithm algorithm = 4;
      /**
       * <code>optional .com.seagate.kinetic.proto.Command.Algorithm algorithm = 4;</code>
       *
       * <pre>
       * The following is for the protection of the data. If the data is protected with a hash or CRC, then
       * the algorithm will be negative. If the data protection algorithm is not a standard unkeyed algorithm
       * then  a positive number is used and the drive has no idea what the key is. See the discussion of
       * encrypted key/value store.(See security document).
       * </pre>
       */
      boolean hasAlgorithm();
      /**
       * <code>optional .com.seagate.kinetic.proto.Command.Algorithm algorithm = 4;</code>
       *
       * <pre>
       * The following is for the protection of the data. If the data is protected with a hash or CRC, then
       * the algorithm will be negative. If the data protection algorithm is not a standard unkeyed algorithm
       * then  a positive number is used and the drive has no idea what the key is. See the discussion of
       * encrypted key/value store.(See security document).
       * </pre>
       */
      com.seagate.kinetic.proto.Kinetic.Command.Algorithm getAlgorithm();
    }
    /**
     * Protobuf type {@code com.seagate.kinetic.proto.Versioned.Metadata}
     *
     * <pre>
     *key/value entry op metadata
     * </pre>
     */
    public static final class Metadata extends
        com.google.protobuf.GeneratedMessage
        implements MetadataOrBuilder {
      // Use Metadata.newBuilder() to construct.
      private Metadata(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
        super(builder);
        this.unknownFields = builder.getUnknownFields();
      }
      private Metadata(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

      private static final Metadata defaultInstance;
      public static Metadata getDefaultInstance() {
        return defaultInstance;
      }

      public Metadata getDefaultInstanceForType() {
        return defaultInstance;
      }

      private final com.google.protobuf.UnknownFieldSet unknownFields;
      @java.lang.Override
      public final com.google.protobuf.UnknownFieldSet
          getUnknownFields() {
        return this.unknownFields;
      }
      private Metadata(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        initFields();
        int mutable_bitField0_ = 0;
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
            com.google.protobuf.UnknownFieldSet.newBuilder();
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              default: {
                if (!parseUnknownField(input, unknownFields,
                                       extensionRegistry, tag)) {
                  done = true;
                }
                break;
              }
              case 10: {
                bitField0_ |= 0x00000001;
                key_ = input.readBytes();
                break;
              }
              case 18: {
                bitField0_ |= 0x00000002;
                dbVersion_ = input.readBytes();
                break;
              }
              case 26: {
                bitField0_ |= 0x00000004;
                tag_ = input.readBytes();
                break;
              }
              case 32: {
                int rawValue = input.readEnum();
                com.seagate.kinetic.proto.Kinetic.Command.Algorithm value = com.seagate.kinetic.proto.Kinetic.Command.Algorithm.valueOf(rawValue);
                if (value == null) {
                  unknownFields.mergeVarintField(4, rawValue);
                } else {
                  bitField0_ |= 0x00000008;
                  algorithm_ = value;
                }
                break;
              }
            }
          }
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.setUnfinishedMessage(this);
        } catch (java.io.IOException e) {
          throw new com.google.protobuf.InvalidProtocolBufferException(
              e.getMessage()).setUnfinishedMessage(this);
        } finally {
          this.unknownFields = unknownFields.build();
          makeExtensionsImmutable();
        }
      }
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.seagate.kinetic.proto.KineticDb.internal_static_com_seagate_kinetic_proto_Versioned_Metadata_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.seagate.kinetic.proto.KineticDb.internal_static_com_seagate_kinetic_proto_Versioned_Metadata_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.class, com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.Builder.class);
      }

      public static com.google.protobuf.Parser<Metadata> PARSER =
          new com.google.protobuf.AbstractParser<Metadata>() {
        public Metadata parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new Metadata(input, extensionRegistry);
        }
      };

      @java.lang.Override
      public com.google.protobuf.Parser<Metadata> getParserForType() {
        return PARSER;
      }

      private int bitField0_;
      // optional bytes key = 1;
      public static final int KEY_FIELD_NUMBER = 1;
      private com.google.protobuf.ByteString key_;
      /**
       * <code>optional bytes key = 1;</code>
       *
       * <pre>
       *entry key
       * </pre>
       */
      public boolean hasKey() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional bytes key = 1;</code>
       *
       * <pre>
       *entry key
       * </pre>
       */
      public com.google.protobuf.ByteString getKey() {
        return key_;
      }

      // optional bytes dbVersion = 2;
      public static final int DBVERSION_FIELD_NUMBER = 2;
      private com.google.protobuf.ByteString dbVersion_;
      /**
       * <code>optional bytes dbVersion = 2;</code>
       *
       * <pre>
       *entry version in store
       * </pre>
       */
      public boolean hasDbVersion() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional bytes dbVersion = 2;</code>
       *
       * <pre>
       *entry version in store
       * </pre>
       */
      public com.google.protobuf.ByteString getDbVersion() {
        return dbVersion_;
      }

      // optional bytes tag = 3;
      public static final int TAG_FIELD_NUMBER = 3;
      private com.google.protobuf.ByteString tag_;
      /**
       * <code>optional bytes tag = 3;</code>
       *
       * <pre>
       * this is the integrity value of the data. This may or may not be in the clear, depending on the algorithm
       * used.
       * </pre>
       */
      public boolean hasTag() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      /**
       * <code>optional bytes tag = 3;</code>
       *
       * <pre>
       * this is the integrity value of the data. This may or may not be in the clear, depending on the algorithm
       * used.
       * </pre>
       */
      public com.google.protobuf.ByteString getTag() {
        return tag_;
      }

      // optional .com.seagate.kinetic.proto.Command.Algorithm algorithm = 4;
      public static final int ALGORITHM_FIELD_NUMBER = 4;
      private com.seagate.kinetic.proto.Kinetic.Command.Algorithm algorithm_;
      /**
       * <code>optional .com.seagate.kinetic.proto.Command.Algorithm algorithm = 4;</code>
       *
       * <pre>
       * The following is for the protection of the data. If the data is protected with a hash or CRC, then
       * the algorithm will be negative. If the data protection algorithm is not a standard unkeyed algorithm
       * then  a positive number is used and the drive has no idea what the key is. See the discussion of
       * encrypted key/value store.(See security document).
       * </pre>
       */
      public boolean hasAlgorithm() {
        return ((bitField0_ & 0x00000008) == 0x00000008);
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Command.Algorithm algorithm = 4;</code>
       *
       * <pre>
       * The following is for the protection of the data. If the data is protected with a hash or CRC, then
       * the algorithm will be negative. If the data protection algorithm is not a standard unkeyed algorithm
       * then  a positive number is used and the drive has no idea what the key is. See the discussion of
       * encrypted key/value store.(See security document).
       * </pre>
       */
      public com.seagate.kinetic.proto.Kinetic.Command.Algorithm getAlgorithm() {
        return algorithm_;
      }

      private void initFields() {
        key_ = com.google.protobuf.ByteString.EMPTY;
        dbVersion_ = com.google.protobuf.ByteString.EMPTY;
        tag_ = com.google.protobuf.ByteString.EMPTY;
        algorithm_ = com.seagate.kinetic.proto.Kinetic.Command.Algorithm.INVALID_ALGORITHM;
      }
      private byte memoizedIsInitialized = -1;
      public final boolean isInitialized() {
        byte isInitialized = memoizedIsInitialized;
        if (isInitialized != -1) return isInitialized == 1;

        memoizedIsInitialized = 1;
        return true;
      }

      public void writeTo(com.google.protobuf.CodedOutputStream output)
                          throws java.io.IOException {
        getSerializedSize();
        if (((bitField0_ & 0x00000001) == 0x00000001)) {
          output.writeBytes(1, key_);
        }
        if (((bitField0_ & 0x00000002) == 0x00000002)) {
          output.writeBytes(2, dbVersion_);
        }
        if (((bitField0_ & 0x00000004) == 0x00000004)) {
          output.writeBytes(3, tag_);
        }
        if (((bitField0_ & 0x00000008) == 0x00000008)) {
          output.writeEnum(4, algorithm_.getNumber());
        }
        getUnknownFields().writeTo(output);
      }

      private int memoizedSerializedSize = -1;
      public int getSerializedSize() {
        int size = memoizedSerializedSize;
        if (size != -1) return size;

        size = 0;
        if (((bitField0_ & 0x00000001) == 0x00000001)) {
          size += com.google.protobuf.CodedOutputStream
            .computeBytesSize(1, key_);
        }
        if (((bitField0_ & 0x00000002) == 0x00000002)) {
          size += com.google.protobuf.CodedOutputStream
            .computeBytesSize(2, dbVersion_);
        }
        if (((bitField0_ & 0x00000004) == 0x00000004)) {
          size += com.google.protobuf.CodedOutputStream
            .computeBytesSize(3, tag_);
        }
        if (((bitField0_ & 0x00000008) == 0x00000008)) {
          size += com.google.protobuf.CodedOutputStream
            .computeEnumSize(4, algorithm_.getNumber());
        }
        size += getUnknownFields().getSerializedSize();
        memoizedSerializedSize = size;
        return size;
      }

      private static final long serialVersionUID = 0L;
      @java.lang.Override
      protected java.lang.Object writeReplace()
          throws java.io.ObjectStreamException {
        return super.writeReplace();
      }

      public static com.seagate.kinetic.proto.KineticDb.Versioned.Metadata parseFrom(
          com.google.protobuf.ByteString data)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
      }
      public static com.seagate.kinetic.proto.KineticDb.Versioned.Metadata parseFrom(
          com.google.protobuf.ByteString data,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
      }
      public static com.seagate.kinetic.proto.KineticDb.Versioned.Metadata parseFrom(byte[] data)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
      }
      public static com.seagate.kinetic.proto.KineticDb.Versioned.Metadata parseFrom(
          byte[] data,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
      }
      public static com.seagate.kinetic.proto.KineticDb.Versioned.Metadata parseFrom(java.io.InputStream input)
          throws java.io.IOException {
        return PARSER.parseFrom(input);
      }
      public static com.seagate.kinetic.proto.KineticDb.Versioned.Metadata parseFrom(
          java.io.InputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        return PARSER.parseFrom(input, extensionRegistry);
      }
      public static com.seagate.kinetic.proto.KineticDb.Versioned.Metadata parseDelimitedFrom(java.io.InputStream input)
          throws java.io.IOException {
        return PARSER.parseDelimitedFrom(input);
      }
      public static com.seagate.kinetic.proto.KineticDb.Versioned.Metadata parseDelimitedFrom(
          java.io.InputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        return PARSER.parseDelimitedFrom(input, extensionRegistry);
      }
      public static com.seagate.kinetic.proto.KineticDb.Versioned.Metadata parseFrom(
          com.google.protobuf.CodedInputStream input)
          throws java.io.IOException {
        return PARSER.parseFrom(input);
      }
      public static com.seagate.kinetic.proto.KineticDb.Versioned.Metadata parseFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        return PARSER.parseFrom(input, extensionRegistry);
      }

      public static Builder newBuilder() { return Builder.create(); }
      public Builder newBuilderForType() { return newBuilder(); }
      public static Builder newBuilder(com.seagate.kinetic.proto.KineticDb.Versioned.Metadata prototype) {
        return newBuilder().mergeFrom(prototype);
      }
      public Builder toBuilder() { return newBuilder(this); }

      @java.lang.Override
      protected Builder newBuilderForType(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        Builder builder = new Builder(parent);
        return builder;
      }
      /**
       * Protobuf type {@code com.seagate.kinetic.proto.Versioned.Metadata}
       *
       * <pre>
       *key/value entry op metadata
       * </pre>
       */
      public static final class Builder extends
          com.google.protobuf.GeneratedMessage.Builder<Builder>
         implements com.seagate.kinetic.proto.KineticDb.Versioned.MetadataOrBuilder {
        public static final com.google.protobuf.Descriptors.Descriptor
            getDescriptor() {
          return com.seagate.kinetic.proto.KineticDb.internal_static_com_seagate_kinetic_proto_Versioned_Metadata_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
            internalGetFieldAccessorTable() {
          return com.seagate.kinetic.proto.KineticDb.internal_static_com_seagate_kinetic_proto_Versioned_Metadata_fieldAccessorTable
              .ensureFieldAccessorsInitialized(
                  com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.class, com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.Builder.class);
        }

        // Construct using com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.newBuilder()
        private Builder() {
          maybeForceBuilderInitialization();
        }

        private Builder(
            com.google.protobuf.GeneratedMessage.BuilderParent parent) {
          super(parent);
          maybeForceBuilderInitialization();
        }
        private void maybeForceBuilderInitialization() {
          if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
          }
        }
        private static Builder create() {
          return new Builder();
        }

        public Builder clear() {
          super.clear();
          key_ = com.google.protobuf.ByteString.EMPTY;
          bitField0_ = (bitField0_ & ~0x00000001);
          dbVersion_ = com.google.protobuf.ByteString.EMPTY;
          bitField0_ = (bitField0_ & ~0x00000002);
          tag_ = com.google.protobuf.ByteString.EMPTY;
          bitField0_ = (bitField0_ & ~0x00000004);
          algorithm_ = com.seagate.kinetic.proto.Kinetic.Command.Algorithm.INVALID_ALGORITHM;
          bitField0_ = (bitField0_ & ~0x00000008);
          return this;
        }

        public Builder clone() {
          return create().mergeFrom(buildPartial());
        }

        public com.google.protobuf.Descriptors.Descriptor
            getDescriptorForType() {
          return com.seagate.kinetic.proto.KineticDb.internal_static_com_seagate_kinetic_proto_Versioned_Metadata_descriptor;
        }

        public com.seagate.kinetic.proto.KineticDb.Versioned.Metadata getDefaultInstanceForType() {
          return com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.getDefaultInstance();
        }

        public com.seagate.kinetic.proto.KineticDb.Versioned.Metadata build() {
          com.seagate.kinetic.proto.KineticDb.Versioned.Metadata result = buildPartial();
          if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
          }
          return result;
        }

        public com.seagate.kinetic.proto.KineticDb.Versioned.Metadata buildPartial() {
          com.seagate.kinetic.proto.KineticDb.Versioned.Metadata result = new com.seagate.kinetic.proto.KineticDb.Versioned.Metadata(this);
          int from_bitField0_ = bitField0_;
          int to_bitField0_ = 0;
          if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
            to_bitField0_ |= 0x00000001;
          }
          result.key_ = key_;
          if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
            to_bitField0_ |= 0x00000002;
          }
          result.dbVersion_ = dbVersion_;
          if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
            to_bitField0_ |= 0x00000004;
          }
          result.tag_ = tag_;
          if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
            to_bitField0_ |= 0x00000008;
          }
          result.algorithm_ = algorithm_;
          result.bitField0_ = to_bitField0_;
          onBuilt();
          return result;
        }

        public Builder mergeFrom(com.google.protobuf.Message other) {
          if (other instanceof com.seagate.kinetic.proto.KineticDb.Versioned.Metadata) {
            return mergeFrom((com.seagate.kinetic.proto.KineticDb.Versioned.Metadata)other);
          } else {
            super.mergeFrom(other);
            return this;
          }
        }

        public Builder mergeFrom(com.seagate.kinetic.proto.KineticDb.Versioned.Metadata other) {
          if (other == com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.getDefaultInstance()) return this;
          if (other.hasKey()) {
            setKey(other.getKey());
          }
          if (other.hasDbVersion()) {
            setDbVersion(other.getDbVersion());
          }
          if (other.hasTag()) {
            setTag(other.getTag());
          }
          if (other.hasAlgorithm()) {
            setAlgorithm(other.getAlgorithm());
          }
          this.mergeUnknownFields(other.getUnknownFields());
          return this;
        }

        public final boolean isInitialized() {
          return true;
        }

        public Builder mergeFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
          com.seagate.kinetic.proto.KineticDb.Versioned.Metadata parsedMessage = null;
          try {
            parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
          } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            parsedMessage = (com.seagate.kinetic.proto.KineticDb.Versioned.Metadata) e.getUnfinishedMessage();
            throw e;
          } finally {
            if (parsedMessage != null) {
              mergeFrom(parsedMessage);
            }
          }
          return this;
        }
        private int bitField0_;

        // optional bytes key = 1;
        private com.google.protobuf.ByteString key_ = com.google.protobuf.ByteString.EMPTY;
        /**
         * <code>optional bytes key = 1;</code>
         *
         * <pre>
         *entry key
         * </pre>
         */
        public boolean hasKey() {
          return ((bitField0_ & 0x00000001) == 0x00000001);
        }
        /**
         * <code>optional bytes key = 1;</code>
         *
         * <pre>
         *entry key
         * </pre>
         */
        public com.google.protobuf.ByteString getKey() {
          return key_;
        }
        /**
         * <code>optional bytes key = 1;</code>
         *
         * <pre>
         *entry key
         * </pre>
         */
        public Builder setKey(com.google.protobuf.ByteString value) {
          if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
          key_ = value;
          onChanged();
          return this;
        }
        /**
         * <code>optional bytes key = 1;</code>
         *
         * <pre>
         *entry key
         * </pre>
         */
        public Builder clearKey() {
          bitField0_ = (bitField0_ & ~0x00000001);
          key_ = getDefaultInstance().getKey();
          onChanged();
          return this;
        }

        // optional bytes dbVersion = 2;
        private com.google.protobuf.ByteString dbVersion_ = com.google.protobuf.ByteString.EMPTY;
        /**
         * <code>optional bytes dbVersion = 2;</code>
         *
         * <pre>
         *entry version in store
         * </pre>
         */
        public boolean hasDbVersion() {
          return ((bitField0_ & 0x00000002) == 0x00000002);
        }
        /**
         * <code>optional bytes dbVersion = 2;</code>
         *
         * <pre>
         *entry version in store
         * </pre>
         */
        public com.google.protobuf.ByteString getDbVersion() {
          return dbVersion_;
        }
        /**
         * <code>optional bytes dbVersion = 2;</code>
         *
         * <pre>
         *entry version in store
         * </pre>
         */
        public Builder setDbVersion(com.google.protobuf.ByteString value) {
          if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
          dbVersion_ = value;
          onChanged();
          return this;
        }
        /**
         * <code>optional bytes dbVersion = 2;</code>
         *
         * <pre>
         *entry version in store
         * </pre>
         */
        public Builder clearDbVersion() {
          bitField0_ = (bitField0_ & ~0x00000002);
          dbVersion_ = getDefaultInstance().getDbVersion();
          onChanged();
          return this;
        }

        // optional bytes tag = 3;
        private com.google.protobuf.ByteString tag_ = com.google.protobuf.ByteString.EMPTY;
        /**
         * <code>optional bytes tag = 3;</code>
         *
         * <pre>
         * this is the integrity value of the data. This may or may not be in the clear, depending on the algorithm
         * used.
         * </pre>
         */
        public boolean hasTag() {
          return ((bitField0_ & 0x00000004) == 0x00000004);
        }
        /**
         * <code>optional bytes tag = 3;</code>
         *
         * <pre>
         * this is the integrity value of the data. This may or may not be in the clear, depending on the algorithm
         * used.
         * </pre>
         */
        public com.google.protobuf.ByteString getTag() {
          return tag_;
        }
        /**
         * <code>optional bytes tag = 3;</code>
         *
         * <pre>
         * this is the integrity value of the data. This may or may not be in the clear, depending on the algorithm
         * used.
         * </pre>
         */
        public Builder setTag(com.google.protobuf.ByteString value) {
          if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000004;
          tag_ = value;
          onChanged();
          return this;
        }
        /**
         * <code>optional bytes tag = 3;</code>
         *
         * <pre>
         * this is the integrity value of the data. This may or may not be in the clear, depending on the algorithm
         * used.
         * </pre>
         */
        public Builder clearTag() {
          bitField0_ = (bitField0_ & ~0x00000004);
          tag_ = getDefaultInstance().getTag();
          onChanged();
          return this;
        }

        // optional .com.seagate.kinetic.proto.Command.Algorithm algorithm = 4;
        private com.seagate.kinetic.proto.Kinetic.Command.Algorithm algorithm_ = com.seagate.kinetic.proto.Kinetic.Command.Algorithm.INVALID_ALGORITHM;
        /**
         * <code>optional .com.seagate.kinetic.proto.Command.Algorithm algorithm = 4;</code>
         *
         * <pre>
         * The following is for the protection of the data. If the data is protected with a hash or CRC, then
         * the algorithm will be negative. If the data protection algorithm is not a standard unkeyed algorithm
         * then  a positive number is used and the drive has no idea what the key is. See the discussion of
         * encrypted key/value store.(See security document).
         * </pre>
         */
        public boolean hasAlgorithm() {
          return ((bitField0_ & 0x00000008) == 0x00000008);
        }
        /**
         * <code>optional .com.seagate.kinetic.proto.Command.Algorithm algorithm = 4;</code>
         *
         * <pre>
         * The following is for the protection of the data. If the data is protected with a hash or CRC, then
         * the algorithm will be negative. If the data protection algorithm is not a standard unkeyed algorithm
         * then  a positive number is used and the drive has no idea what the key is. See the discussion of
         * encrypted key/value store.(See security document).
         * </pre>
         */
        public com.seagate.kinetic.proto.Kinetic.Command.Algorithm getAlgorithm() {
          return algorithm_;
        }
        /**
         * <code>optional .com.seagate.kinetic.proto.Command.Algorithm algorithm = 4;</code>
         *
         * <pre>
         * The following is for the protection of the data. If the data is protected with a hash or CRC, then
         * the algorithm will be negative. If the data protection algorithm is not a standard unkeyed algorithm
         * then  a positive number is used and the drive has no idea what the key is. See the discussion of
         * encrypted key/value store.(See security document).
         * </pre>
         */
        public Builder setAlgorithm(com.seagate.kinetic.proto.Kinetic.Command.Algorithm value) {
          if (value == null) {
            throw new NullPointerException();
          }
          bitField0_ |= 0x00000008;
          algorithm_ = value;
          onChanged();
          return this;
        }
        /**
         * <code>optional .com.seagate.kinetic.proto.Command.Algorithm algorithm = 4;</code>
         *
         * <pre>
         * The following is for the protection of the data. If the data is protected with a hash or CRC, then
         * the algorithm will be negative. If the data protection algorithm is not a standard unkeyed algorithm
         * then  a positive number is used and the drive has no idea what the key is. See the discussion of
         * encrypted key/value store.(See security document).
         * </pre>
         */
        public Builder clearAlgorithm() {
          bitField0_ = (bitField0_ & ~0x00000008);
          algorithm_ = com.seagate.kinetic.proto.Kinetic.Command.Algorithm.INVALID_ALGORITHM;
          onChanged();
          return this;
        }

        // @@protoc_insertion_point(builder_scope:com.seagate.kinetic.proto.Versioned.Metadata)
      }

      static {
        defaultInstance = new Metadata(true);
        defaultInstance.initFields();
      }

      // @@protoc_insertion_point(class_scope:com.seagate.kinetic.proto.Versioned.Metadata)
    }

    private int bitField0_;
    // optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;
    public static final int METADATA_FIELD_NUMBER = 1;
    private com.seagate.kinetic.proto.KineticDb.Versioned.Metadata metadata_;
    /**
     * <code>optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;</code>
     *
     * <pre>
     *metadata
     * </pre>
     */
    public boolean hasMetadata() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;</code>
     *
     * <pre>
     *metadata
     * </pre>
     */
    public com.seagate.kinetic.proto.KineticDb.Versioned.Metadata getMetadata() {
      return metadata_;
    }
    /**
     * <code>optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;</code>
     *
     * <pre>
     *metadata
     * </pre>
     */
    public com.seagate.kinetic.proto.KineticDb.Versioned.MetadataOrBuilder getMetadataOrBuilder() {
      return metadata_;
    }

    // optional bytes value = 2;
    public static final int VALUE_FIELD_NUMBER = 2;
    private com.google.protobuf.ByteString value_;
    /**
     * <code>optional bytes value = 2;</code>
     *
     * <pre>
     *entry value/data
     * </pre>
     */
    public boolean hasValue() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional bytes value = 2;</code>
     *
     * <pre>
     *entry value/data
     * </pre>
     */
    public com.google.protobuf.ByteString getValue() {
      return value_;
    }

    private void initFields() {
      metadata_ = com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.getDefaultInstance();
      value_ = com.google.protobuf.ByteString.EMPTY;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeMessage(1, metadata_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, value_);
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, metadata_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, value_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static com.seagate.kinetic.proto.KineticDb.Versioned parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.seagate.kinetic.proto.KineticDb.Versioned parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.seagate.kinetic.proto.KineticDb.Versioned parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.seagate.kinetic.proto.KineticDb.Versioned parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.seagate.kinetic.proto.KineticDb.Versioned parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static com.seagate.kinetic.proto.KineticDb.Versioned parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static com.seagate.kinetic.proto.KineticDb.Versioned parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static com.seagate.kinetic.proto.KineticDb.Versioned parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static com.seagate.kinetic.proto.KineticDb.Versioned parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static com.seagate.kinetic.proto.KineticDb.Versioned parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.seagate.kinetic.proto.KineticDb.Versioned prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code com.seagate.kinetic.proto.Versioned}
     *
     * <pre>
     **
     * persisted entry value message format.
     * &lt;p&gt;
     * db persisted entry (KVValue)
     * </pre>
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements com.seagate.kinetic.proto.KineticDb.VersionedOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.seagate.kinetic.proto.KineticDb.internal_static_com_seagate_kinetic_proto_Versioned_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.seagate.kinetic.proto.KineticDb.internal_static_com_seagate_kinetic_proto_Versioned_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.seagate.kinetic.proto.KineticDb.Versioned.class, com.seagate.kinetic.proto.KineticDb.Versioned.Builder.class);
      }

      // Construct using com.seagate.kinetic.proto.KineticDb.Versioned.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
          getMetadataFieldBuilder();
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        if (metadataBuilder_ == null) {
          metadata_ = com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.getDefaultInstance();
        } else {
          metadataBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000001);
        value_ = com.google.protobuf.ByteString.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.seagate.kinetic.proto.KineticDb.internal_static_com_seagate_kinetic_proto_Versioned_descriptor;
      }

      public com.seagate.kinetic.proto.KineticDb.Versioned getDefaultInstanceForType() {
        return com.seagate.kinetic.proto.KineticDb.Versioned.getDefaultInstance();
      }

      public com.seagate.kinetic.proto.KineticDb.Versioned build() {
        com.seagate.kinetic.proto.KineticDb.Versioned result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public com.seagate.kinetic.proto.KineticDb.Versioned buildPartial() {
        com.seagate.kinetic.proto.KineticDb.Versioned result = new com.seagate.kinetic.proto.KineticDb.Versioned(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        if (metadataBuilder_ == null) {
          result.metadata_ = metadata_;
        } else {
          result.metadata_ = metadataBuilder_.build();
        }
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.value_ = value_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.seagate.kinetic.proto.KineticDb.Versioned) {
          return mergeFrom((com.seagate.kinetic.proto.KineticDb.Versioned)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.seagate.kinetic.proto.KineticDb.Versioned other) {
        if (other == com.seagate.kinetic.proto.KineticDb.Versioned.getDefaultInstance()) return this;
        if (other.hasMetadata()) {
          mergeMetadata(other.getMetadata());
        }
        if (other.hasValue()) {
          setValue(other.getValue());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.seagate.kinetic.proto.KineticDb.Versioned parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.seagate.kinetic.proto.KineticDb.Versioned) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      // optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;
      private com.seagate.kinetic.proto.KineticDb.Versioned.Metadata metadata_ = com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.getDefaultInstance();
      private com.google.protobuf.SingleFieldBuilder<
          com.seagate.kinetic.proto.KineticDb.Versioned.Metadata, com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.Builder, com.seagate.kinetic.proto.KineticDb.Versioned.MetadataOrBuilder> metadataBuilder_;
      /**
       * <code>optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;</code>
       *
       * <pre>
       *metadata
       * </pre>
       */
      public boolean hasMetadata() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;</code>
       *
       * <pre>
       *metadata
       * </pre>
       */
      public com.seagate.kinetic.proto.KineticDb.Versioned.Metadata getMetadata() {
        if (metadataBuilder_ == null) {
          return metadata_;
        } else {
          return metadataBuilder_.getMessage();
        }
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;</code>
       *
       * <pre>
       *metadata
       * </pre>
       */
      public Builder setMetadata(com.seagate.kinetic.proto.KineticDb.Versioned.Metadata value) {
        if (metadataBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          metadata_ = value;
          onChanged();
        } else {
          metadataBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;</code>
       *
       * <pre>
       *metadata
       * </pre>
       */
      public Builder setMetadata(
          com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.Builder builderForValue) {
        if (metadataBuilder_ == null) {
          metadata_ = builderForValue.build();
          onChanged();
        } else {
          metadataBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;</code>
       *
       * <pre>
       *metadata
       * </pre>
       */
      public Builder mergeMetadata(com.seagate.kinetic.proto.KineticDb.Versioned.Metadata value) {
        if (metadataBuilder_ == null) {
          if (((bitField0_ & 0x00000001) == 0x00000001) &&
              metadata_ != com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.getDefaultInstance()) {
            metadata_ =
              com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.newBuilder(metadata_).mergeFrom(value).buildPartial();
          } else {
            metadata_ = value;
          }
          onChanged();
        } else {
          metadataBuilder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;</code>
       *
       * <pre>
       *metadata
       * </pre>
       */
      public Builder clearMetadata() {
        if (metadataBuilder_ == null) {
          metadata_ = com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.getDefaultInstance();
          onChanged();
        } else {
          metadataBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;</code>
       *
       * <pre>
       *metadata
       * </pre>
       */
      public com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.Builder getMetadataBuilder() {
        bitField0_ |= 0x00000001;
        onChanged();
        return getMetadataFieldBuilder().getBuilder();
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;</code>
       *
       * <pre>
       *metadata
       * </pre>
       */
      public com.seagate.kinetic.proto.KineticDb.Versioned.MetadataOrBuilder getMetadataOrBuilder() {
        if (metadataBuilder_ != null) {
          return metadataBuilder_.getMessageOrBuilder();
        } else {
          return metadata_;
        }
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Versioned.Metadata metadata = 1;</code>
       *
       * <pre>
       *metadata
       * </pre>
       */
      private com.google.protobuf.SingleFieldBuilder<
          com.seagate.kinetic.proto.KineticDb.Versioned.Metadata, com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.Builder, com.seagate.kinetic.proto.KineticDb.Versioned.MetadataOrBuilder> 
          getMetadataFieldBuilder() {
        if (metadataBuilder_ == null) {
          metadataBuilder_ = new com.google.protobuf.SingleFieldBuilder<
              com.seagate.kinetic.proto.KineticDb.Versioned.Metadata, com.seagate.kinetic.proto.KineticDb.Versioned.Metadata.Builder, com.seagate.kinetic.proto.KineticDb.Versioned.MetadataOrBuilder>(
                  metadata_,
                  getParentForChildren(),
                  isClean());
          metadata_ = null;
        }
        return metadataBuilder_;
      }

      // optional bytes value = 2;
      private com.google.protobuf.ByteString value_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>optional bytes value = 2;</code>
       *
       * <pre>
       *entry value/data
       * </pre>
       */
      public boolean hasValue() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional bytes value = 2;</code>
       *
       * <pre>
       *entry value/data
       * </pre>
       */
      public com.google.protobuf.ByteString getValue() {
        return value_;
      }
      /**
       * <code>optional bytes value = 2;</code>
       *
       * <pre>
       *entry value/data
       * </pre>
       */
      public Builder setValue(com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        value_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional bytes value = 2;</code>
       *
       * <pre>
       *entry value/data
       * </pre>
       */
      public Builder clearValue() {
        bitField0_ = (bitField0_ & ~0x00000002);
        value_ = getDefaultInstance().getValue();
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:com.seagate.kinetic.proto.Versioned)
    }

    static {
      defaultInstance = new Versioned(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:com.seagate.kinetic.proto.Versioned)
  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_seagate_kinetic_proto_Versioned_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_seagate_kinetic_proto_Versioned_fieldAccessorTable;
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_seagate_kinetic_proto_Versioned_Metadata_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_seagate_kinetic_proto_Versioned_Metadata_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\017kineticDb.proto\022\031com.seagate.kinetic.p" +
      "roto\032\rkinetic.proto\"\325\001\n\tVersioned\022?\n\010met" +
      "adata\030\001 \001(\0132-.com.seagate.kinetic.proto." +
      "Versioned.Metadata\022\r\n\005value\030\002 \001(\014\032x\n\010Met" +
      "adata\022\013\n\003key\030\001 \001(\014\022\021\n\tdbVersion\030\002 \001(\014\022\013\n" +
      "\003tag\030\003 \001(\014\022?\n\talgorithm\030\004 \001(\0162,.com.seag" +
      "ate.kinetic.proto.Command.AlgorithmB\013B\tK" +
      "ineticDb"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_com_seagate_kinetic_proto_Versioned_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_com_seagate_kinetic_proto_Versioned_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_com_seagate_kinetic_proto_Versioned_descriptor,
              new java.lang.String[] { "Metadata", "Value", });
          internal_static_com_seagate_kinetic_proto_Versioned_Metadata_descriptor =
            internal_static_com_seagate_kinetic_proto_Versioned_descriptor.getNestedTypes().get(0);
          internal_static_com_seagate_kinetic_proto_Versioned_Metadata_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_com_seagate_kinetic_proto_Versioned_Metadata_descriptor,
              new java.lang.String[] { "Key", "DbVersion", "Tag", "Algorithm", });
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.seagate.kinetic.proto.Kinetic.getDescriptor(),
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}
