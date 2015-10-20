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

public final class KineticIo {
  private KineticIo() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface ExtendedMessageOrBuilder
      extends com.google.protobuf.MessageOrBuilder {

    // optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;
    /**
     * <code>optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;</code>
     *
     * <pre>
     *drive interface message
     * </pre>
     */
    boolean hasInterfaceMessage();
    /**
     * <code>optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;</code>
     *
     * <pre>
     *drive interface message
     * </pre>
     */
    com.seagate.kinetic.proto.Kinetic.Message getInterfaceMessage();
    /**
     * <code>optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;</code>
     *
     * <pre>
     *drive interface message
     * </pre>
     */
    com.seagate.kinetic.proto.Kinetic.MessageOrBuilder getInterfaceMessageOrBuilder();

    // optional bytes value = 2;
    /**
     * <code>optional bytes value = 2;</code>
     *
     * <pre>
     *optional value
     * </pre>
     */
    boolean hasValue();
    /**
     * <code>optional bytes value = 2;</code>
     *
     * <pre>
     *optional value
     * </pre>
     */
    com.google.protobuf.ByteString getValue();
  }
  /**
   * Protobuf type {@code com.seagate.kinetic.proto.ExtendedMessage}
   *
   * <pre>
   **
   * kinetic extended message.
   *
   * This is currently used by extended transport and for evaluation only.
   * </pre>
   */
  public static final class ExtendedMessage extends
      com.google.protobuf.GeneratedMessage
      implements ExtendedMessageOrBuilder {
    // Use ExtendedMessage.newBuilder() to construct.
    private ExtendedMessage(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private ExtendedMessage(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final ExtendedMessage defaultInstance;
    public static ExtendedMessage getDefaultInstance() {
      return defaultInstance;
    }

    public ExtendedMessage getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private ExtendedMessage(
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
              com.seagate.kinetic.proto.Kinetic.Message.Builder subBuilder = null;
              if (((bitField0_ & 0x00000001) == 0x00000001)) {
                subBuilder = interfaceMessage_.toBuilder();
              }
              interfaceMessage_ = input.readMessage(com.seagate.kinetic.proto.Kinetic.Message.PARSER, extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(interfaceMessage_);
                interfaceMessage_ = subBuilder.buildPartial();
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
      return com.seagate.kinetic.proto.KineticIo.internal_static_com_seagate_kinetic_proto_ExtendedMessage_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.seagate.kinetic.proto.KineticIo.internal_static_com_seagate_kinetic_proto_ExtendedMessage_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.seagate.kinetic.proto.KineticIo.ExtendedMessage.class, com.seagate.kinetic.proto.KineticIo.ExtendedMessage.Builder.class);
    }

    public static com.google.protobuf.Parser<ExtendedMessage> PARSER =
        new com.google.protobuf.AbstractParser<ExtendedMessage>() {
      public ExtendedMessage parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new ExtendedMessage(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<ExtendedMessage> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    // optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;
    public static final int INTERFACEMESSAGE_FIELD_NUMBER = 1;
    private com.seagate.kinetic.proto.Kinetic.Message interfaceMessage_;
    /**
     * <code>optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;</code>
     *
     * <pre>
     *drive interface message
     * </pre>
     */
    public boolean hasInterfaceMessage() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;</code>
     *
     * <pre>
     *drive interface message
     * </pre>
     */
    public com.seagate.kinetic.proto.Kinetic.Message getInterfaceMessage() {
      return interfaceMessage_;
    }
    /**
     * <code>optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;</code>
     *
     * <pre>
     *drive interface message
     * </pre>
     */
    public com.seagate.kinetic.proto.Kinetic.MessageOrBuilder getInterfaceMessageOrBuilder() {
      return interfaceMessage_;
    }

    // optional bytes value = 2;
    public static final int VALUE_FIELD_NUMBER = 2;
    private com.google.protobuf.ByteString value_;
    /**
     * <code>optional bytes value = 2;</code>
     *
     * <pre>
     *optional value
     * </pre>
     */
    public boolean hasValue() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional bytes value = 2;</code>
     *
     * <pre>
     *optional value
     * </pre>
     */
    public com.google.protobuf.ByteString getValue() {
      return value_;
    }

    private void initFields() {
      interfaceMessage_ = com.seagate.kinetic.proto.Kinetic.Message.getDefaultInstance();
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
        output.writeMessage(1, interfaceMessage_);
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
          .computeMessageSize(1, interfaceMessage_);
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

    public static com.seagate.kinetic.proto.KineticIo.ExtendedMessage parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.seagate.kinetic.proto.KineticIo.ExtendedMessage parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.seagate.kinetic.proto.KineticIo.ExtendedMessage parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.seagate.kinetic.proto.KineticIo.ExtendedMessage parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.seagate.kinetic.proto.KineticIo.ExtendedMessage parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static com.seagate.kinetic.proto.KineticIo.ExtendedMessage parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static com.seagate.kinetic.proto.KineticIo.ExtendedMessage parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static com.seagate.kinetic.proto.KineticIo.ExtendedMessage parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static com.seagate.kinetic.proto.KineticIo.ExtendedMessage parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static com.seagate.kinetic.proto.KineticIo.ExtendedMessage parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.seagate.kinetic.proto.KineticIo.ExtendedMessage prototype) {
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
     * Protobuf type {@code com.seagate.kinetic.proto.ExtendedMessage}
     *
     * <pre>
     **
     * kinetic extended message.
     *
     * This is currently used by extended transport and for evaluation only.
     * </pre>
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements com.seagate.kinetic.proto.KineticIo.ExtendedMessageOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.seagate.kinetic.proto.KineticIo.internal_static_com_seagate_kinetic_proto_ExtendedMessage_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.seagate.kinetic.proto.KineticIo.internal_static_com_seagate_kinetic_proto_ExtendedMessage_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.seagate.kinetic.proto.KineticIo.ExtendedMessage.class, com.seagate.kinetic.proto.KineticIo.ExtendedMessage.Builder.class);
      }

      // Construct using com.seagate.kinetic.proto.KineticIo.ExtendedMessage.newBuilder()
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
          getInterfaceMessageFieldBuilder();
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        if (interfaceMessageBuilder_ == null) {
          interfaceMessage_ = com.seagate.kinetic.proto.Kinetic.Message.getDefaultInstance();
        } else {
          interfaceMessageBuilder_.clear();
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
        return com.seagate.kinetic.proto.KineticIo.internal_static_com_seagate_kinetic_proto_ExtendedMessage_descriptor;
      }

      public com.seagate.kinetic.proto.KineticIo.ExtendedMessage getDefaultInstanceForType() {
        return com.seagate.kinetic.proto.KineticIo.ExtendedMessage.getDefaultInstance();
      }

      public com.seagate.kinetic.proto.KineticIo.ExtendedMessage build() {
        com.seagate.kinetic.proto.KineticIo.ExtendedMessage result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public com.seagate.kinetic.proto.KineticIo.ExtendedMessage buildPartial() {
        com.seagate.kinetic.proto.KineticIo.ExtendedMessage result = new com.seagate.kinetic.proto.KineticIo.ExtendedMessage(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        if (interfaceMessageBuilder_ == null) {
          result.interfaceMessage_ = interfaceMessage_;
        } else {
          result.interfaceMessage_ = interfaceMessageBuilder_.build();
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
        if (other instanceof com.seagate.kinetic.proto.KineticIo.ExtendedMessage) {
          return mergeFrom((com.seagate.kinetic.proto.KineticIo.ExtendedMessage)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.seagate.kinetic.proto.KineticIo.ExtendedMessage other) {
        if (other == com.seagate.kinetic.proto.KineticIo.ExtendedMessage.getDefaultInstance()) return this;
        if (other.hasInterfaceMessage()) {
          mergeInterfaceMessage(other.getInterfaceMessage());
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
        com.seagate.kinetic.proto.KineticIo.ExtendedMessage parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.seagate.kinetic.proto.KineticIo.ExtendedMessage) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      // optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;
      private com.seagate.kinetic.proto.Kinetic.Message interfaceMessage_ = com.seagate.kinetic.proto.Kinetic.Message.getDefaultInstance();
      private com.google.protobuf.SingleFieldBuilder<
          com.seagate.kinetic.proto.Kinetic.Message, com.seagate.kinetic.proto.Kinetic.Message.Builder, com.seagate.kinetic.proto.Kinetic.MessageOrBuilder> interfaceMessageBuilder_;
      /**
       * <code>optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;</code>
       *
       * <pre>
       *drive interface message
       * </pre>
       */
      public boolean hasInterfaceMessage() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;</code>
       *
       * <pre>
       *drive interface message
       * </pre>
       */
      public com.seagate.kinetic.proto.Kinetic.Message getInterfaceMessage() {
        if (interfaceMessageBuilder_ == null) {
          return interfaceMessage_;
        } else {
          return interfaceMessageBuilder_.getMessage();
        }
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;</code>
       *
       * <pre>
       *drive interface message
       * </pre>
       */
      public Builder setInterfaceMessage(com.seagate.kinetic.proto.Kinetic.Message value) {
        if (interfaceMessageBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          interfaceMessage_ = value;
          onChanged();
        } else {
          interfaceMessageBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;</code>
       *
       * <pre>
       *drive interface message
       * </pre>
       */
      public Builder setInterfaceMessage(
          com.seagate.kinetic.proto.Kinetic.Message.Builder builderForValue) {
        if (interfaceMessageBuilder_ == null) {
          interfaceMessage_ = builderForValue.build();
          onChanged();
        } else {
          interfaceMessageBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;</code>
       *
       * <pre>
       *drive interface message
       * </pre>
       */
      public Builder mergeInterfaceMessage(com.seagate.kinetic.proto.Kinetic.Message value) {
        if (interfaceMessageBuilder_ == null) {
          if (((bitField0_ & 0x00000001) == 0x00000001) &&
              interfaceMessage_ != com.seagate.kinetic.proto.Kinetic.Message.getDefaultInstance()) {
            interfaceMessage_ =
              com.seagate.kinetic.proto.Kinetic.Message.newBuilder(interfaceMessage_).mergeFrom(value).buildPartial();
          } else {
            interfaceMessage_ = value;
          }
          onChanged();
        } else {
          interfaceMessageBuilder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;</code>
       *
       * <pre>
       *drive interface message
       * </pre>
       */
      public Builder clearInterfaceMessage() {
        if (interfaceMessageBuilder_ == null) {
          interfaceMessage_ = com.seagate.kinetic.proto.Kinetic.Message.getDefaultInstance();
          onChanged();
        } else {
          interfaceMessageBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;</code>
       *
       * <pre>
       *drive interface message
       * </pre>
       */
      public com.seagate.kinetic.proto.Kinetic.Message.Builder getInterfaceMessageBuilder() {
        bitField0_ |= 0x00000001;
        onChanged();
        return getInterfaceMessageFieldBuilder().getBuilder();
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;</code>
       *
       * <pre>
       *drive interface message
       * </pre>
       */
      public com.seagate.kinetic.proto.Kinetic.MessageOrBuilder getInterfaceMessageOrBuilder() {
        if (interfaceMessageBuilder_ != null) {
          return interfaceMessageBuilder_.getMessageOrBuilder();
        } else {
          return interfaceMessage_;
        }
      }
      /**
       * <code>optional .com.seagate.kinetic.proto.Message interfaceMessage = 1;</code>
       *
       * <pre>
       *drive interface message
       * </pre>
       */
      private com.google.protobuf.SingleFieldBuilder<
          com.seagate.kinetic.proto.Kinetic.Message, com.seagate.kinetic.proto.Kinetic.Message.Builder, com.seagate.kinetic.proto.Kinetic.MessageOrBuilder> 
          getInterfaceMessageFieldBuilder() {
        if (interfaceMessageBuilder_ == null) {
          interfaceMessageBuilder_ = new com.google.protobuf.SingleFieldBuilder<
              com.seagate.kinetic.proto.Kinetic.Message, com.seagate.kinetic.proto.Kinetic.Message.Builder, com.seagate.kinetic.proto.Kinetic.MessageOrBuilder>(
                  interfaceMessage_,
                  getParentForChildren(),
                  isClean());
          interfaceMessage_ = null;
        }
        return interfaceMessageBuilder_;
      }

      // optional bytes value = 2;
      private com.google.protobuf.ByteString value_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>optional bytes value = 2;</code>
       *
       * <pre>
       *optional value
       * </pre>
       */
      public boolean hasValue() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional bytes value = 2;</code>
       *
       * <pre>
       *optional value
       * </pre>
       */
      public com.google.protobuf.ByteString getValue() {
        return value_;
      }
      /**
       * <code>optional bytes value = 2;</code>
       *
       * <pre>
       *optional value
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
       *optional value
       * </pre>
       */
      public Builder clearValue() {
        bitField0_ = (bitField0_ & ~0x00000002);
        value_ = getDefaultInstance().getValue();
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:com.seagate.kinetic.proto.ExtendedMessage)
    }

    static {
      defaultInstance = new ExtendedMessage(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:com.seagate.kinetic.proto.ExtendedMessage)
  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_seagate_kinetic_proto_ExtendedMessage_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_seagate_kinetic_proto_ExtendedMessage_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\017kineticIo.proto\022\031com.seagate.kinetic.p" +
      "roto\032\rkinetic.proto\"^\n\017ExtendedMessage\022<" +
      "\n\020interfaceMessage\030\001 \001(\0132\".com.seagate.k" +
      "inetic.proto.Message\022\r\n\005value\030\002 \001(\014B\013B\tK" +
      "ineticIo"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_com_seagate_kinetic_proto_ExtendedMessage_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_com_seagate_kinetic_proto_ExtendedMessage_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_com_seagate_kinetic_proto_ExtendedMessage_descriptor,
              new java.lang.String[] { "InterfaceMessage", "Value", });
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
