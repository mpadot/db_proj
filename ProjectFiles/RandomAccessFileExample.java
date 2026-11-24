/*
The RandomAccessFile class in Java enables reading from and writing to specific locations within a file, offering capabilities beyond sequential file access. It treats the file as a byte array, allowing movement to any position for data manipulation.

Usage
Opening a File:
RandomAccessFile(String name, String mode): Opens a file in the specified mode. Modes include "r" (read-only), "rw" (read-write)
The file is created if it does not exist when using "rw" mode.

Moving the File Pointer:
seek(long pos): Sets the file pointer offset, measured from the beginning of the file, to where the next read or write operation will occur. 
getFilePointer(): Returns the current position of the file pointer.

Reading Data:
read(): Reads a byte of data.
read(byte[] b): Reads up to b.length bytes of data into an array of bytes.
read(byte[] b, int off, int len): Reads up to len bytes of data into an array of bytes, starting at offset off.
Specific data type reading methods: readInt(), readLong(), readFloat(), readDouble(), readBoolean(), readChar(), readUTF().

Writing Data:
write(int b): Writes a byte to the file.
write(byte[] b): Writes b.length bytes from the specified byte array to the file.
write(byte[] b, int off, int len): Writes len bytes from the specified byte array starting at offset off.
Specific data type writing methods: writeInt(int v), writeLong(long v), writeFloat(float v), writeDouble(double v), writeBoolean(boolean v), writeChar(int v), writeUTF(String str).

Closing the File:
close(): Closes the file and releases any system resources associated with it. It's recommended to use try-with-resources for automatic resource management.

Example
*/

import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileExample {
    public static void main(String[] args) {
        try (RandomAccessFile file = new RandomAccessFile("data.txt", "rw")) {
            // Write data
            file.writeInt(100);
            file.writeDouble(3.14);
            file.writeUTF("Hello");

            // Move to the beginning of the file
            file.seek(0);

            // Read data
            int intValue = file.readInt();
            double doubleValue = file.readDouble();
            String stringValue = file.readUTF();

            System.out.println("Integer: " + intValue);
            System.out.println("Double: " + doubleValue);
            System.out.println("String: " + stringValue);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
