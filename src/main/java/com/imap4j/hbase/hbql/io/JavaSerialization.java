package com.imap4j.hbase.hbql.io;

import com.imap4j.hbase.hbql.HPersistException;
import com.imap4j.hbase.hbql.schema.FieldType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 31, 2009
 * Time: 2:18:29 PM
 */
public class JavaSerialization extends Serialization {

    @Override
    public Object getScalarFromBytes(final FieldType fieldType, final byte[] b) throws IOException, HPersistException {

        final ByteArrayInputStream bais = new ByteArrayInputStream(b);
        final ObjectInputStream ois = new ObjectInputStream(bais);

        try {
            switch (fieldType) {

                case BooleanType:
                    return ois.readBoolean();

                case ByteType:
                    return ois.readByte();

                case CharType:
                    return ois.readByte();

                case ShortType:
                    return ois.readShort();

                case IntegerType:
                    return ois.readInt();

                case LongType:
                    return ois.readLong();

                case FloatType:
                    return ois.readFloat();

                case DoubleType:
                    return ois.readDouble();

                case StringType:
                    return ois.readUTF();

                case DateType:
                case ObjectType:
                    return ois.readObject();
            }
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new HPersistException("Error in getScalarfromBytes()");
        }
        finally {
            ois.close();
        }

        throw new HPersistException("Error in getScalarfromBytes()");
    }

    @Override
    public byte[] getScalarAsBytes(final FieldType fieldType, final Object obj) throws IOException, HPersistException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        switch (fieldType) {

            case BooleanType:
                oos.writeBoolean((Boolean)obj);
                break;

            case ByteType:
                oos.writeByte((Byte)obj);
                break;

            case CharType:
                oos.writeByte((Character)obj);
                break;

            case ShortType:
                oos.writeShort((Short)obj);
                break;

            case IntegerType:
                oos.writeInt((Integer)obj);
                break;

            case LongType:
                oos.writeLong((Long)obj);
                break;

            case FloatType:
                oos.writeFloat((Float)obj);
                break;

            case DoubleType:
                oos.writeDouble((Double)obj);
                break;

            case StringType:
                oos.writeUTF((String)obj);
                break;

            case DateType:
            case ObjectType:
                oos.writeObject(obj);
                break;
        }

        oos.flush();
        return baos.toByteArray();
    }

    @Override
    public Object getArrayFromBytes(final FieldType fieldType, final Class clazz, final byte[] b) throws IOException, HPersistException {

        if (fieldType == FieldType.CharType) {
            final String s = new String(b);
            return s.toCharArray();
        }

        final ByteArrayInputStream bais = new ByteArrayInputStream(b);
        final ObjectInputStream ois = new ObjectInputStream(bais);

        try {
            // Read length
            final int length = ois.readInt();
            final Object array = Array.newInstance(clazz, length);

            switch (fieldType) {

                case BooleanType: {
                    for (int i = 0; i < length; i++)
                        Array.set(array, i, ois.readBoolean());
                    return array;
                }

                case ByteType: {
                    for (int i = 0; i < length; i++)
                        Array.set(array, i, ois.readByte());
                    return array;
                }

                case CharType: {
                    // See above
                    return null;
                }

                case ShortType: {
                    for (int i = 0; i < length; i++)
                        Array.set(array, i, ois.readShort());
                    return array;
                }

                case IntegerType: {
                    for (int i = 0; i < length; i++)
                        Array.set(array, i, ois.readInt());
                    return array;
                }

                case LongType: {
                    for (int i = 0; i < length; i++)
                        Array.set(array, i, ois.readLong());
                    return array;
                }

                case FloatType: {
                    for (int i = 0; i < length; i++)
                        Array.set(array, i, ois.readFloat());
                    return array;
                }

                case DoubleType: {
                    for (int i = 0; i < length; i++)
                        Array.set(array, i, ois.readDouble());
                    return array;
                }

                case StringType: {
                    for (int i = 0; i < length; i++)
                        Array.set(array, i, ois.readUTF());
                    return array;
                }

                case DateType:
                case ObjectType: {
                    for (int i = 0; i < length; i++)
                        Array.set(array, i, ois.readObject());
                    return array;
                }

                default:
                    throw new HPersistException("Error in getScalarfromBytes() - " + fieldType);
            }
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new HPersistException("Error in getScalarfromBytes()");
        }
        finally {
            ois.close();
        }

    }

    @Override
    public byte[] getArrayasBytes(final FieldType fieldType, final Object obj) throws IOException, HPersistException {

        if (fieldType == FieldType.CharType) {
            final String s = new String((char[])obj);
            return s.getBytes();
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        switch (fieldType) {

            case BooleanType: {
                oos.writeInt(((boolean[])obj).length);
                for (final boolean val : (boolean[])obj)
                    oos.writeBoolean(val);
                break;
            }

            case ByteType: {
                oos.writeInt(((byte[])obj).length);
                for (final byte val : (byte[])obj)
                    oos.write(val);
                break;
            }

            case CharType: {
                // See above
                break;
            }

            case ShortType: {
                oos.writeInt(((short[])obj).length);
                for (final short val : (short[])obj)
                    oos.writeShort(val);
                break;
            }

            case IntegerType: {
                oos.writeInt(((int[])obj).length);
                for (final int val : (int[])obj)
                    oos.writeInt(val);
                break;
            }

            case LongType: {
                oos.writeInt(((long[])obj).length);
                for (final long val : (long[])obj)
                    oos.writeLong(val);
                break;
            }

            case FloatType: {
                oos.writeInt(((float[])obj).length);
                for (final float val : (float[])obj)
                    oos.writeFloat(val);
                break;
            }

            case DoubleType: {
                oos.writeInt(((double[])obj).length);
                for (final double val : (double[])obj)
                    oos.writeDouble(val);
                break;
            }

            case StringType: {
                oos.writeInt(((String[])obj).length);
                for (final String val : (String[])obj)
                    oos.writeUTF(val);
                break;
            }

            case DateType:
            case ObjectType: {
                oos.writeInt(((Object[])obj).length);
                for (final Object val : (Object[])obj)
                    oos.writeObject(val);
                break;
            }
        }
        oos.flush();
        return baos.toByteArray();
    }

}
