package net.sf.jrtps.message.parameter;

public enum TypeKind {
    NO_TYPE(0),
    BOOLEAN_TYPE(1),
    BYTE_TYPE(2),
    INT_16_TYPE(3),
    UINT_16_TYPE(4),
    INT_32_TYPE(5),
    UINT_32_TYPE(6),
    INT_64_TYPE(7),
    UINT_64_TYPE(8),
    FLOAT_32_TYPE(9),
    FLOAT_64_TYPE(10),
    FLOAT_128_TYPE(11),
    CHAR_8_TYPE(12),
    CHAR_32_TYPE(13),
    ENUMERATION_TYPE(14),
    BITSET_TYPE(15),
    ALIAS_TYPE(16),
    ARRAY_TYPE(17),
    SEQUENCE_TYPE(18),
    STRING_TYPE(19),
    MAP_TYPE(20),
    UNION_TYPE(21),
    STRUCTURE_TYPE(22),
    ANNOTATION_TYPE(23);

    private short kind;

    TypeKind(int kind) {
        this.kind = (short) kind;
    }
    
    public short getKind() {
        return kind;
    }
}
